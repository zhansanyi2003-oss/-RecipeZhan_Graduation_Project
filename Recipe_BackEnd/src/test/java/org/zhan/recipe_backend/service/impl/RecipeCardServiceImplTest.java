package org.zhan.recipe_backend.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.zhan.recipe_backend.document.RecipeDoc;
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.dto.UserPreferenceDto;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.mapper.RecipeMapper;
import org.zhan.recipe_backend.repository.UserSavedRepository;
import org.zhan.recipe_backend.service.UserService;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RecipeCardServiceImplTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getTasteRecommendations_usesSingleSearchAndFunctionScoreSignals() {
        RecipeCardServiceImpl service = new RecipeCardServiceImpl();
        ElasticsearchOperations elasticsearchOperations = mock(ElasticsearchOperations.class);
        UserService userService = mock(UserService.class);
        UserSavedRepository userSavedRepository = mock(UserSavedRepository.class);
        @SuppressWarnings("unchecked")
        SearchHits<RecipeDoc> searchHits = mock(SearchHits.class);

        UserPreferenceDto preferences = new UserPreferenceDto();
        preferences.setCuisines(Collections.singletonList("Korean"));
        preferences.setFlavours(Collections.singletonList("Spicy"));
        preferences.setDietary(Collections.singletonList("Vegetarian"));

        when(userService.getUserPreferences()).thenReturn(preferences);
        when(userSavedRepository.findRecipeIdsByUserId(1L)).thenReturn(Set.of());
        when(searchHits.getSearchHits()).thenReturn(Collections.emptyList());
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(RecipeDoc.class))).thenReturn(searchHits);

        ReflectionTestUtils.setField(service, "elasticsearchOperations", elasticsearchOperations);
        ReflectionTestUtils.setField(service, "userService", userService);
        ReflectionTestUtils.setField(service, "userSavedRepository", userSavedRepository);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList())
        );

        service.getTasteRecommendations(0, 12);

        ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);
        verify(elasticsearchOperations, times(1)).search(queryCaptor.capture(), eq(RecipeDoc.class));

        Query query = queryCaptor.getValue().getQuery();
        assertTrue(query.isFunctionScore(), "taste recommendation should use function_score");

        var functionScoreQuery = query.functionScore();
        assertEquals(2, functionScoreQuery.functions().size(), "cuisine and flavour should contribute weighted boosts");

        Query baseQuery = functionScoreQuery.query();
        assertTrue(baseQuery.isBool(), "function_score should wrap a bool base query");

        var boolQuery = baseQuery.bool();
        assertEquals(1, boolQuery.filter().size(), "dietary remains a hard filter");
        assertEquals(0, boolQuery.should().size(), "soft preference signals should not become hard bool clauses");
        assertNull(boolQuery.minimumShouldMatch(), "taste preferences should not become required matches");
    }

    @Test
    void getPersonalizedRecommendations_keepsRightNowFocusedOnTimeInsteadOfTasteAndReturnsSlice() {
        RecipeCardServiceImpl service = new RecipeCardServiceImpl();
        ElasticsearchOperations elasticsearchOperations = mock(ElasticsearchOperations.class);
        UserService userService = mock(UserService.class);
        UserSavedRepository userSavedRepository = mock(UserSavedRepository.class);
        @SuppressWarnings("unchecked")
        SearchHits<RecipeDoc> searchHits = mock(SearchHits.class);

        UserPreferenceDto preferences = new UserPreferenceDto();
        preferences.setCuisines(Collections.singletonList("Korean"));
        preferences.setFlavours(Collections.singletonList("Spicy"));
        preferences.setIngredients(Collections.singletonList("Chicken"));
        preferences.setDietary(Collections.singletonList("Vegetarian"));
        preferences.setTimeAvailability("30");
        preferences.setSkillLevel("Beginner");

        when(userService.getUserPreferences()).thenReturn(preferences);
        when(userSavedRepository.findRecipeIdsByUserId(1L)).thenReturn(Set.of());
        when(searchHits.getSearchHits()).thenReturn(Collections.emptyList());
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(RecipeDoc.class))).thenReturn(searchHits);

        ReflectionTestUtils.setField(service, "elasticsearchOperations", elasticsearchOperations);
        ReflectionTestUtils.setField(service, "userService", userService);
        ReflectionTestUtils.setField(service, "userSavedRepository", userSavedRepository);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList())
        );

        var result = service.getPersonalizedRecommendations(9, 0, 12);

        ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);
        verify(elasticsearchOperations, times(1)).search(queryCaptor.capture(), eq(RecipeDoc.class));

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty(), "empty search hits should map to an empty slice");

        Query query = queryCaptor.getValue().getQuery();
        assertTrue(query.isFunctionScore(), "right-now recommendation should still use function_score");

        String serializedQuery = query.toString();
        assertTrue(serializedQuery.contains("courses"), "right-now recommendation should score by meal moment");
        assertFalse(serializedQuery.contains("cuisines"), "right-now recommendation should not reuse cuisine scoring");
        assertFalse(serializedQuery.contains("flavours"), "right-now recommendation should not reuse flavour scoring");
        assertFalse(serializedQuery.contains("ingredients"), "right-now recommendation should not reuse ingredient scoring");
    }

    @Test
    void getBehaviorRecommendations_usesLatestSavedRecipeSignalsAndReturnsSlice() {
        RecipeCardServiceImpl service = new RecipeCardServiceImpl();
        ElasticsearchOperations elasticsearchOperations = mock(ElasticsearchOperations.class);
        UserService userService = mock(UserService.class);
        UserSavedRepository userSavedRepository = mock(UserSavedRepository.class);
        RecipeMapper recipeMapper = mock(RecipeMapper.class);
        @SuppressWarnings("unchecked")
        SearchHits<RecipeDoc> searchHits = mock(SearchHits.class);
        @SuppressWarnings("unchecked")
        org.springframework.data.domain.Slice<Recipe> savedSlice = mock(org.springframework.data.domain.Slice.class);
        Recipe savedRecipe = new Recipe();
        RecipeCardDto savedSeed = new RecipeCardDto();
        savedSeed.setId(99L);
        savedSeed.setTitle("Spaghetti");
        savedSeed.setCuisines(Collections.singletonList("Italian"));
        savedSeed.setFlavours(Collections.singletonList("Savory"));

        UserPreferenceDto preferences = new UserPreferenceDto();
        preferences.setDietary(Collections.singletonList("Vegetarian"));

        when(userService.getUserPreferences()).thenReturn(preferences);
        when(userSavedRepository.findSavedRecipesByUserId(eq(1L), any())).thenReturn(savedSlice);
        when(savedSlice.hasContent()).thenReturn(true);
        when(savedSlice.getContent()).thenReturn(Collections.singletonList(savedRecipe));
        when(recipeMapper.toCardDto(savedRecipe)).thenReturn(savedSeed);
        when(userSavedRepository.findRecipeIdsByUserId(1L)).thenReturn(Set.of());
        when(searchHits.getSearchHits()).thenReturn(Collections.emptyList());
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(RecipeDoc.class))).thenReturn(searchHits);

        ReflectionTestUtils.setField(service, "elasticsearchOperations", elasticsearchOperations);
        ReflectionTestUtils.setField(service, "userService", userService);
        ReflectionTestUtils.setField(service, "userSavedRepository", userSavedRepository);
        ReflectionTestUtils.setField(service, "recipeMapper", recipeMapper);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList())
        );

        var result = service.getBehaviorRecommendations(0, 12);

        ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);
        verify(elasticsearchOperations, times(1)).search(queryCaptor.capture(), eq(RecipeDoc.class));

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty(), "empty search hits should map to an empty slice");

        Query query = queryCaptor.getValue().getQuery();
        assertTrue(query.isFunctionScore(), "behavior recommendation should use function_score");

        String serializedQuery = query.toString();
        assertTrue(serializedQuery.contains("Italian"), "saved recipe cuisines should influence behavior ranking");
        assertTrue(serializedQuery.contains("Savory"), "saved recipe flavours should influence behavior ranking");
        assertTrue(serializedQuery.contains("Spaghetti"), "saved recipe title should influence behavior ranking");
        assertTrue(serializedQuery.contains("dietTypes"), "behavior recommendation should still respect hard preference filters");
    }

    @Test
    void getHeroRecommendations_usesDedicatedNonPersonalizedQuery() {
        RecipeCardServiceImpl service = new RecipeCardServiceImpl();
        ElasticsearchOperations elasticsearchOperations = mock(ElasticsearchOperations.class);
        UserService userService = mock(UserService.class);
        UserSavedRepository userSavedRepository = mock(UserSavedRepository.class);
        @SuppressWarnings("unchecked")
        SearchHits<RecipeDoc> searchHits = mock(SearchHits.class);

        when(searchHits.getSearchHits()).thenReturn(Collections.emptyList());
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(RecipeDoc.class))).thenReturn(searchHits);

        ReflectionTestUtils.setField(service, "elasticsearchOperations", elasticsearchOperations);
        ReflectionTestUtils.setField(service, "userService", userService);
        ReflectionTestUtils.setField(service, "userSavedRepository", userSavedRepository);

        service.getHeroRecommendations(9);

        ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);
        verify(elasticsearchOperations, times(1)).search(queryCaptor.capture(), eq(RecipeDoc.class));
        verifyNoInteractions(userService);

        Query query = queryCaptor.getValue().getQuery();
        assertTrue(query.isFunctionScore(), "hero recommendation should use its own function_score query");

        String serializedQuery = query.toString();
        assertTrue(serializedQuery.contains("courses"), "hero recommendation should react to the time of day");
        assertFalse(serializedQuery.contains("dietTypes"), "hero recommendation should not depend on user dietary preferences");
        assertFalse(serializedQuery.contains("cuisines"), "hero recommendation should not inherit personalized taste boosts");
    }
}
