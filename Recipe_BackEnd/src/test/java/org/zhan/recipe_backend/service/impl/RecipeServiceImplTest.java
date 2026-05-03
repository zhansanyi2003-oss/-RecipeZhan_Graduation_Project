package org.zhan.recipe_backend.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.zhan.recipe_backend.common.CourseEnum;
import org.zhan.recipe_backend.common.DietTypeEnum;
import org.zhan.recipe_backend.dto.RecipeRequestDto;
import org.zhan.recipe_backend.entity.Course;
import org.zhan.recipe_backend.entity.Cuisine;
import org.zhan.recipe_backend.entity.DietType;
import org.zhan.recipe_backend.entity.Flavour;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.entity.User;
import org.zhan.recipe_backend.exception.ForbiddenOperationException;
import org.zhan.recipe_backend.mapper.RecipeMapper;
import org.zhan.recipe_backend.repository.*;
import org.zhan.recipe_backend.service.RecipeSearchSyncService;
import org.zhan.recipe_backend.service.RecipeTaxonomyService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class RecipeServiceImplTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addRecipe_usesRecipeTaxonomyServiceForFlavourAndCuisineResolution() {
        RecipeServiceImpl service = new RecipeServiceImpl();
        RecipeRepository recipeRepository = mock(RecipeRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        RecipeMapper recipeMapper = mock(RecipeMapper.class);
        RecipeTaxonomyService recipeTaxonomyService = mock(RecipeTaxonomyService.class);
        RecipeSearchSyncService recipeSearchSyncService = mock(RecipeSearchSyncService.class);
        RecipeRelationAssembler recipeRelationAssembler = new RecipeRelationAssembler();

        RecipeRequestDto dto = new RecipeRequestDto();
        dto.setTitle("Test Recipe");
        dto.setCoverImage("/cover.jpg");
        dto.setDescription("Test");
        dto.setFlavours(List.of("  sPiCy  "));
        dto.setCuisines(List.of("  middle   eastern "));

        when(userRepository.getReferenceById(anyLong())).thenReturn(User.builder().id(1L).build());
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(recipeTaxonomyService.getOrCreateFlavour("  sPiCy  ")).thenReturn(Flavour.builder().name("Spicy").build());
        when(recipeTaxonomyService.getOrCreateCuisine("  middle   eastern "))
                .thenReturn(Cuisine.builder().name("Middle Eastern").build());
        when(recipeMapper.toRecipe(dto)).thenReturn(Recipe.builder()
                .id(10L)
                .title("Test Recipe")
                .coverImage("/cover.jpg")
                .description("Test")
                .build());

        ReflectionTestUtils.setField(service, "recipeRepository", recipeRepository);
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
        ReflectionTestUtils.setField(service, "recipeMapper", recipeMapper);
        ReflectionTestUtils.setField(service, "recipeSearchSyncService", recipeSearchSyncService);
        ReflectionTestUtils.setField(service, "recipeRelationAssembler", recipeRelationAssembler);
        ReflectionTestUtils.setField(recipeRelationAssembler, "recipeTaxonomyService", recipeTaxonomyService);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of())
        );

        service.addRecipe(dto);

        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository, times(1)).save(recipeCaptor.capture());
        verify(recipeTaxonomyService, times(1)).getOrCreateFlavour("  sPiCy  ");
        verify(recipeTaxonomyService, times(1)).getOrCreateCuisine("  middle   eastern ");
        verify(recipeSearchSyncService, times(1)).enqueueUpsert(10L);

        Recipe savedRecipe = recipeCaptor.getValue();
        assertEquals("Spicy", savedRecipe.getRecipeFlavours().get(0).getFlavour().getName());
        assertEquals("Middle Eastern", savedRecipe.getRecipeCuisines().get(0).getCuisine().getName());
    }

    @Test
    void addRecipe_mapsOnlyRequestFieldsThroughRecipeMapper() {
        RecipeServiceImpl service = new RecipeServiceImpl();
        RecipeRepository recipeRepository = mock(RecipeRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        RecipeMapper recipeMapper = mock(RecipeMapper.class);
        RecipeTaxonomyService recipeTaxonomyService = mock(RecipeTaxonomyService.class);
        RecipeSearchSyncService recipeSearchSyncService = mock(RecipeSearchSyncService.class);
        RecipeRelationAssembler recipeRelationAssembler = new RecipeRelationAssembler();

        RecipeRequestDto dto = new RecipeRequestDto();
        dto.setTitle("Trusted Title");
        dto.setCoverImage("/cover.jpg");
        dto.setDescription("Test");
        dto.setCookingTimeMin(15);

        when(userRepository.getReferenceById(anyLong())).thenReturn(User.builder().id(1L).build());
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(recipeMapper.toRecipe(dto)).thenReturn(Recipe.builder()
                .id(11L)
                .title("Trusted Title")
                .coverImage("/cover.jpg")
                .description("Test")
                .cookingTimeMin(15)
                .build());

        ReflectionTestUtils.setField(service, "recipeRepository", recipeRepository);
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
        ReflectionTestUtils.setField(service, "recipeMapper", recipeMapper);
        ReflectionTestUtils.setField(service, "recipeSearchSyncService", recipeSearchSyncService);
        ReflectionTestUtils.setField(service, "recipeRelationAssembler", recipeRelationAssembler);
        ReflectionTestUtils.setField(recipeRelationAssembler, "recipeTaxonomyService", recipeTaxonomyService);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of())
        );

        service.addRecipe(dto);

        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).save(recipeCaptor.capture());

        Recipe savedRecipe = recipeCaptor.getValue();
        assertEquals("Trusted Title", savedRecipe.getTitle());
        assertEquals("/cover.jpg", savedRecipe.getCoverImage());
        assertEquals(15, savedRecipe.getCookingTimeMin());
        assertNull(savedRecipe.getAverageRating());
        assertNull(savedRecipe.getRatingCount());
        assertNull(savedRecipe.getVersion());
        verify(recipeSearchSyncService, times(1)).enqueueUpsert(11L);
    }

    @Test
    void addRecipe_resolvesFixedCourseAndDietTypesWithoutCreatingNewValues() {
        RecipeServiceImpl service = new RecipeServiceImpl();
        RecipeRepository recipeRepository = mock(RecipeRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        RecipeMapper recipeMapper = mock(RecipeMapper.class);
        RecipeTaxonomyService recipeTaxonomyService = mock(RecipeTaxonomyService.class);
        RecipeSearchSyncService recipeSearchSyncService = mock(RecipeSearchSyncService.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        DietTypeRepository dietTypeRepository = mock(DietTypeRepository.class);
        RecipeRelationAssembler recipeRelationAssembler = new RecipeRelationAssembler();

        RecipeRequestDto dto = new RecipeRequestDto();
        dto.setTitle("Fixed Tags Recipe");
        dto.setCoverImage("/cover.jpg");
        dto.setCourses(List.of(CourseEnum.BREAKFAST));
        dto.setDietTypes(List.of(DietTypeEnum.GLUTEN_FREE));

        when(userRepository.getReferenceById(anyLong())).thenReturn(User.builder().id(1L).build());
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(recipeMapper.toRecipe(dto)).thenReturn(Recipe.builder().id(12L).title("Fixed Tags Recipe").build());
        when(courseRepository.findByName("BREAKFAST"))
                .thenReturn(Optional.of(Course.builder().name("BREAKFAST").build()));
        when(dietTypeRepository.findByName("Gluten-Free"))
                .thenReturn(Optional.of(DietType.builder().name("Gluten-Free").build()));

        ReflectionTestUtils.setField(service, "recipeRepository", recipeRepository);
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
        ReflectionTestUtils.setField(service, "recipeMapper", recipeMapper);
        ReflectionTestUtils.setField(service, "recipeSearchSyncService", recipeSearchSyncService);
        ReflectionTestUtils.setField(service, "recipeRelationAssembler", recipeRelationAssembler);
        ReflectionTestUtils.setField(recipeRelationAssembler, "recipeTaxonomyService", recipeTaxonomyService);
        ReflectionTestUtils.setField(recipeRelationAssembler, "courseRepository", courseRepository);
        ReflectionTestUtils.setField(recipeRelationAssembler, "dietTypeRepository", dietTypeRepository);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of())
        );

        service.addRecipe(dto);

        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).save(recipeCaptor.capture());
        verify(courseRepository, never()).save(any(Course.class));
        verify(dietTypeRepository, never()).save(any(DietType.class));

        Recipe savedRecipe = recipeCaptor.getValue();
        assertEquals("BREAKFAST", savedRecipe.getRecipeCourses().get(0).getCourse().getName());
        assertEquals("Gluten-Free", savedRecipe.getRecipeDietTypes().get(0).getDietType().getName());
    }

    @Test
    void deleteRecipe_rejectsUserWhoIsNotOwnerOrAdmin() {
        RecipeServiceImpl service = new RecipeServiceImpl();
        RecipeRepository recipeRepository = mock(RecipeRepository.class);
        UserSavedRepository userSavedRepository = mock(UserSavedRepository.class);
        RatingRepository ratingRepository = mock(RatingRepository.class);
        RecipeSearchSyncService recipeSearchSyncService = mock(RecipeSearchSyncService.class);

        Recipe recipe = Recipe.builder()
                .id(20L)
                .author(User.builder().id(1L).build())
                .build();
        when(recipeRepository.findById(20L)).thenReturn(Optional.of(recipe));

        ReflectionTestUtils.setField(service, "recipeRepository", recipeRepository);
        ReflectionTestUtils.setField(service, "userSavedRepository", userSavedRepository);
        ReflectionTestUtils.setField(service, "ratingRepository", ratingRepository);
        ReflectionTestUtils.setField(service, "recipeSearchSyncService", recipeSearchSyncService);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(2L, null, List.of())
        );

        assertThrows(ForbiddenOperationException.class, () -> service.deleteRecipe(20L));

        verify(userSavedRepository, never()).deleteByRecipeId(anyLong());
        verify(ratingRepository, never()).deleteByRecipeId(anyLong());
        verify(recipeRepository, never()).delete(any(Recipe.class));
        verify(recipeSearchSyncService, never()).enqueueDelete(anyLong());
    }

    @Test
    void deleteRecipe_deletesOwnedRecipeAndEnqueuesSearchDelete() {
        RecipeServiceImpl service = new RecipeServiceImpl();
        RecipeRepository recipeRepository = mock(RecipeRepository.class);
        UserSavedRepository userSavedRepository = mock(UserSavedRepository.class);
        RatingRepository ratingRepository = mock(RatingRepository.class);
        RecipeSearchSyncService recipeSearchSyncService = mock(RecipeSearchSyncService.class);

        Recipe recipe = Recipe.builder()
                .id(21L)
                .author(User.builder().id(1L).build())
                .build();
        when(recipeRepository.findById(21L)).thenReturn(Optional.of(recipe));

        ReflectionTestUtils.setField(service, "recipeRepository", recipeRepository);
        ReflectionTestUtils.setField(service, "userSavedRepository", userSavedRepository);
        ReflectionTestUtils.setField(service, "ratingRepository", ratingRepository);
        ReflectionTestUtils.setField(service, "recipeSearchSyncService", recipeSearchSyncService);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of())
        );

        service.deleteRecipe(21L);

        verify(userSavedRepository).deleteByRecipeId(21L);
        verify(ratingRepository).deleteByRecipeId(21L);
        verify(recipeRepository).delete(recipe);
        verify(recipeSearchSyncService).enqueueDelete(21L);
    }
}
