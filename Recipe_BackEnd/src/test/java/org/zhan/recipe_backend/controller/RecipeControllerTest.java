package org.zhan.recipe_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.zhan.recipe_backend.exception.ConflictException;
import org.zhan.recipe_backend.exception.ResourceNotFoundException;
import org.zhan.recipe_backend.service.RatingService;
import org.zhan.recipe_backend.service.RecipeCardService;
import org.zhan.recipe_backend.service.RecipeService;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecipeControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    private RecipeService recipeService;
    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        RecipeController controller = new RecipeController();
        recipeService = mock(RecipeService.class);
        ratingService = mock(RatingService.class);
        ReflectionTestUtils.setField(controller, "recipeService", recipeService);
        ReflectionTestUtils.setField(controller, "recipeCardService", mock(RecipeCardService.class));
        ReflectionTestUtils.setField(controller, "ratingService", ratingService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addRecipe_acceptsRequestDtoAndIgnoresUnknownServerManagedFields() throws Exception {
        mockMvc.perform(post("/api/recipes/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Secure Recipe",
                                "coverImage", "/images/cover.png",
                                "description", "Test",
                                "cookingTimeMin", 10,
                                "difficulty", "EASY",
                                "averageRating", 5.0,
                                "ingredients", List.of(Map.of("name", "Salt")),
                                "steps", List.of(Map.of("content", "Mix everything"))
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(recipeService).addRecipe(any());
    }

    @Test
    void addRecipe_rejectsUnknownFixedCourseValue() throws Exception {
        mockMvc.perform(post("/api/recipes/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Invalid Course Recipe",
                                "coverImage", "/images/cover.png",
                                "cookingTimeMin", 10,
                                "difficulty", "EASY",
                                "courses", List.of("MIDNIGHT_MEAL")
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void addRecipe_rejectsUnknownFixedDietTypeValue() throws Exception {
        mockMvc.perform(post("/api/recipes/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Invalid Diet Recipe",
                                "coverImage", "/images/cover.png",
                                "cookingTimeMin", 10,
                                "difficulty", "EASY",
                                "dietTypes", List.of("Only Moon Food")
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void submitRating_returnsNotFoundWhenRecipeDoesNotExist() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(7L, null, List.of())
        );
        doThrow(new ResourceNotFoundException("Recipe not found"))
                .when(ratingService).submitRating(anyLong(), anyLong(), anyDouble());

        mockMvc.perform(post("/api/recipes/rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "recipeId", 999,
                                "score", 4.5
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Recipe not found"));
    }

    @Test
    void deleteRating_returnsConflictWhenUserHasNoRating() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(7L, null, List.of())
        );
        doThrow(new ConflictException("You have not rated this recipe yet."))
                .when(ratingService).deleteRating(99L, 7L);

        mockMvc.perform(delete("/api/recipes/rate").param("id", "99"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("You have not rated this recipe yet."));
    }

    @Test
    void deleteRecipe_delegatesToRecipeService() throws Exception {
        mockMvc.perform(delete("/api/recipes/55"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(recipeService).deleteRecipe(55L);
    }
}
