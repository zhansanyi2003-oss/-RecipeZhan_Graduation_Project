package org.zhan.recipe_backend.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.zhan.recipe_backend.entity.Cuisine;
import org.zhan.recipe_backend.entity.Flavour;
import org.zhan.recipe_backend.repository.CuisineRepository;
import org.zhan.recipe_backend.repository.FlavourRepository;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RecipeTaxonomyServiceImplTest {

    @Test
    void getFlavours_returnsRedisValuesWithoutHittingRepository() {
        RecipeTaxonomyServiceImpl service = new RecipeTaxonomyServiceImpl();
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ListOperations<String, String> listOperations = mock(ListOperations.class);
        FlavourRepository flavourRepository = mock(FlavourRepository.class);

        when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range("recipe:taxonomy:flavours", 0, -1)).thenReturn(List.of("Spicy", "Sweet"));

        ReflectionTestUtils.setField(service, "stringRedisTemplate", stringRedisTemplate);
        ReflectionTestUtils.setField(service, "flavourRepository", flavourRepository);

        List<String> result = service.getFlavours();

        assertEquals(List.of("Spicy", "Sweet"), result);
        verify(flavourRepository, never()).findAll();
    }

    @Test
    void getCuisines_loadsFromRepositoryAndBackfillsRedisWhenCacheMissing() {
        RecipeTaxonomyServiceImpl service = new RecipeTaxonomyServiceImpl();
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ListOperations<String, String> listOperations = mock(ListOperations.class);
        CuisineRepository cuisineRepository = mock(CuisineRepository.class);

        when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range("recipe:taxonomy:cuisines", 0, -1)).thenReturn(Collections.emptyList());
        when(cuisineRepository.findAll()).thenReturn(List.of(
                Cuisine.builder().name("Italian").build(),
                Cuisine.builder().name("Mexican").build()
        ));

        ReflectionTestUtils.setField(service, "stringRedisTemplate", stringRedisTemplate);
        ReflectionTestUtils.setField(service, "cuisineRepository", cuisineRepository);

        List<String> result = service.getCuisines();

        assertEquals(List.of("Italian", "Mexican"), result);
        verify(cuisineRepository, times(1)).findAll();
        verify(stringRedisTemplate, times(1)).delete("recipe:taxonomy:cuisines");
        verify(listOperations, times(1)).rightPushAll(eq("recipe:taxonomy:cuisines"), anyCollection());
        verify(stringRedisTemplate, times(1)).expire("recipe:taxonomy:cuisines", Duration.ofHours(6));
    }

    @Test
    void getOrCreateFlavour_normalizesNameAndEvictsCacheForNewValue() {
        RecipeTaxonomyServiceImpl service = new RecipeTaxonomyServiceImpl();
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        FlavourRepository flavourRepository = mock(FlavourRepository.class);

        Flavour inserted = Flavour.builder().name("Spicy").build();
        when(flavourRepository.findByName("Spicy")).thenReturn(Optional.empty(), Optional.of(inserted));

        ReflectionTestUtils.setField(service, "stringRedisTemplate", stringRedisTemplate);
        ReflectionTestUtils.setField(service, "flavourRepository", flavourRepository);

        Flavour result = service.getOrCreateFlavour("  sPiCy  ");

        assertEquals("Spicy", result.getName());
        verify(flavourRepository, times(1)).insertIgnoreByName("Spicy");
        verify(stringRedisTemplate, times(1)).delete("recipe:taxonomy:flavours");
    }

    @Test
    void getOrCreateCuisine_reusesExistingNormalizedValueWithoutEvictingCache() {
        RecipeTaxonomyServiceImpl service = new RecipeTaxonomyServiceImpl();
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        CuisineRepository cuisineRepository = mock(CuisineRepository.class);

        Cuisine existing = Cuisine.builder().name("Middle Eastern").build();
        when(cuisineRepository.findByName("Middle Eastern")).thenReturn(Optional.of(existing));

        ReflectionTestUtils.setField(service, "stringRedisTemplate", stringRedisTemplate);
        ReflectionTestUtils.setField(service, "cuisineRepository", cuisineRepository);

        Cuisine result = service.getOrCreateCuisine(" middle   eastern ");

        assertEquals("Middle Eastern", result.getName());
        verify(cuisineRepository, never()).insertIgnoreByName(any());
        verify(stringRedisTemplate, never()).delete("recipe:taxonomy:cuisines");
    }

    @Test
    void getOrCreateFlavour_usesDatabaseUpsertForConcurrentCreate() {
        RecipeTaxonomyServiceImpl service = new RecipeTaxonomyServiceImpl();
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        FlavourRepository flavourRepository = mock(FlavourRepository.class);
        Flavour existing = Flavour.builder().name("Sour").build();

        when(flavourRepository.findByName("Sour")).thenReturn(Optional.empty(), Optional.of(existing));
        when(flavourRepository.insertIgnoreByName("Sour")).thenReturn(0);

        ReflectionTestUtils.setField(service, "stringRedisTemplate", stringRedisTemplate);
        ReflectionTestUtils.setField(service, "flavourRepository", flavourRepository);

        Flavour result = service.getOrCreateFlavour(" sour ");

        assertEquals(existing, result);
        verify(flavourRepository, times(1)).insertIgnoreByName("Sour");
        verify(flavourRepository, times(2)).findByName("Sour");
    }

    @Test
    void getOrCreateCuisine_usesDatabaseUpsertForConcurrentCreate() {
        RecipeTaxonomyServiceImpl service = new RecipeTaxonomyServiceImpl();
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        CuisineRepository cuisineRepository = mock(CuisineRepository.class);
        Cuisine existing = Cuisine.builder().name("Thai").build();

        when(cuisineRepository.findByName("Thai")).thenReturn(Optional.empty(), Optional.of(existing));
        when(cuisineRepository.insertIgnoreByName("Thai")).thenReturn(0);

        ReflectionTestUtils.setField(service, "stringRedisTemplate", stringRedisTemplate);
        ReflectionTestUtils.setField(service, "cuisineRepository", cuisineRepository);

        Cuisine result = service.getOrCreateCuisine(" thai ");

        assertEquals(existing, result);
        verify(cuisineRepository, times(1)).insertIgnoreByName("Thai");
        verify(cuisineRepository, times(2)).findByName("Thai");
    }
}
