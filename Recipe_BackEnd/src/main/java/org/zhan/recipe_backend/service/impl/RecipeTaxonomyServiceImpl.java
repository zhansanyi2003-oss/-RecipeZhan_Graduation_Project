package org.zhan.recipe_backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhan.recipe_backend.entity.Cuisine;
import org.zhan.recipe_backend.entity.Flavour;
import org.zhan.recipe_backend.repository.CuisineRepository;
import org.zhan.recipe_backend.repository.FlavourRepository;
import org.zhan.recipe_backend.service.RecipeTaxonomyService;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class RecipeTaxonomyServiceImpl implements RecipeTaxonomyService {

    private static final String FLAVOURS_CACHE_KEY = "recipe:taxonomy:flavours";
    private static final String CUISINES_CACHE_KEY = "recipe:taxonomy:cuisines";
    private static final Duration TAXONOMY_CACHE_TTL = Duration.ofHours(6);

    @Autowired
    private FlavourRepository flavourRepository;

    @Autowired
    private CuisineRepository cuisineRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<String> getFlavours() {
        return getCachedTaxonomyValues(
                FLAVOURS_CACHE_KEY,
                () -> flavourRepository.findAll().stream()
                        .map(Flavour::getName)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public List<String> getCuisines() {
        return getCachedTaxonomyValues(
                CUISINES_CACHE_KEY,
                () -> cuisineRepository.findAll().stream()
                        .map(Cuisine::getName)
                        .collect(Collectors.toList())
        );
    }

    @Override
    @Transactional
    public Flavour getOrCreateFlavour(String rawName) {
        String normalized = normalizeTaxonomyName(rawName);
        if (normalized == null) {
            return null;
        }

        return flavourRepository.findByName(normalized)
                .orElseGet(() -> {
                    flavourRepository.insertIgnoreByName(normalized);
                    evictTaxonomyCache(FLAVOURS_CACHE_KEY);
                    return flavourRepository.findByName(normalized)
                            .orElseThrow(() -> new IllegalStateException("Failed to create flavour: " + normalized));
                });
    }

    @Override
    @Transactional
    public Cuisine getOrCreateCuisine(String rawName) {
        String normalized = normalizeTaxonomyName(rawName);
        if (normalized == null) {
            return null;
        }

        return cuisineRepository.findByName(normalized)
                .orElseGet(() -> {
                    cuisineRepository.insertIgnoreByName(normalized);
                    evictTaxonomyCache(CUISINES_CACHE_KEY);
                    return cuisineRepository.findByName(normalized)
                            .orElseThrow(() -> new IllegalStateException("Failed to create cuisine: " + normalized));
                });
    }

    private List<String> getCachedTaxonomyValues(String cacheKey, Supplier<List<String>> databaseLoader) {
        ListOperations<String, String> listOperations = stringRedisTemplate.opsForList();
        List<String> cachedValues = listOperations.range(cacheKey, 0, -1);
        if (cachedValues != null && !cachedValues.isEmpty()) {
            return cachedValues;
        }

        List<String> databaseValues = databaseLoader.get();
        if (databaseValues == null || databaseValues.isEmpty()) {
            return Collections.emptyList();
        }

        stringRedisTemplate.delete(cacheKey);
        listOperations.rightPushAll(cacheKey, databaseValues);
        stringRedisTemplate.expire(cacheKey, TAXONOMY_CACHE_TTL);
        return databaseValues;
    }

    private void evictTaxonomyCache(String cacheKey) {
        stringRedisTemplate.delete(cacheKey);
    }

    private String normalizeTaxonomyName(String rawName) {
        if (rawName == null) {
            return null;
        }

        String collapsed = rawName.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            return null;
        }

        return Arrays.stream(collapsed.toLowerCase().split(" "))
                .map(this::normalizeTaxonomyWord)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    private String normalizeTaxonomyWord(String word) {
        if (word == null || word.isEmpty()) {
            return null;
        }

        if (word.contains("-")) {
            return Arrays.stream(word.split("-"))
                    .filter(part -> !part.isEmpty())
                    .map(this::capitalizeWord)
                    .collect(Collectors.joining("-"));
        }

        return capitalizeWord(word);
    }

    private String capitalizeWord(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }
}
