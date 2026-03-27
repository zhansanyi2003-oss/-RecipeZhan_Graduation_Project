package org.zhan.recipe_backend.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.json.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHit;

import org.springframework.stereotype.Service;
import org.zhan.recipe_backend.document.RecipeDoc;
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.dto.UserPreferenceDto;
import org.zhan.recipe_backend.entity.*;
import org.zhan.recipe_backend.mapper.RecipeMapper;
import org.zhan.recipe_backend.repository.*;
import org.zhan.recipe_backend.service.RecipeCardService;
import org.zhan.recipe_backend.service.UserService;
import org.zhan.recipe_backend.utils.AuthUtils;
import co.elastic.clients.elasticsearch._types.FieldValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class RecipeCardServiceImpl implements RecipeCardService {


    @Autowired
    private FlavourRepository flavourRepository;
    @Autowired
    private CuisineRepository cuisineRepository;

    @Autowired
    private UserSavedRepository userSavedRepository;

    @Autowired
    private RecipeMapper recipeMapper;
    @Autowired
    private  IngredientRepository ingredientRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private UserService userService;
    @Autowired
    private RecipeRepository recipeRepository;

    public Slice<RecipeCardDto> getRecipeCards(RecipeCardDto cardParam, int page, int pageSize) {

        BoolQuery.Builder filterBool = new BoolQuery.Builder();

        // 1. Keyword search -> Must context (calculates relevance score)
        if (cardParam.getTitle() != null && !cardParam.getTitle().trim().isEmpty()) {
            filterBool.must(m -> m.match(t -> t.field("title").query(cardParam.getTitle().trim())));
        }

        if (cardParam.getFlavours() != null && !cardParam.getFlavours().isEmpty()) {
            filterBool.filter(f -> f.terms(t -> t.field("flavours").terms(v -> v.value(
                    cardParam.getFlavours().stream()
                            .map(FieldValue::of) // ✨ Look how clean this is now!
                            .collect(Collectors.toList())
            ))));
        }

        if (cardParam.getCuisines() != null && !cardParam.getCuisines().isEmpty()) {
            filterBool.filter(f -> f.terms(t -> t.field("cuisines").terms(v -> v.value(
                    cardParam.getCuisines().stream()
                            .map(FieldValue::of)
                            .collect(Collectors.toList())
            ))));
        }

        if (cardParam.getDietTypes() != null && !cardParam.getDietTypes().isEmpty()) {
            filterBool.filter(f -> f.terms(t -> t.field("dietTypes").terms(v -> v.value(
                    cardParam.getDietTypes().stream()
                            .map(FieldValue::of)
                            .collect(Collectors.toList())
            ))));
        }
        if (cardParam.getCourses() != null && !cardParam.getCourses().isEmpty()) {

            filterBool.filter(f -> f.terms(t -> t.field("courses").terms(v -> v.value(
                    cardParam.getCourses().stream()
                            .map(FieldValue::of)
                            .collect(Collectors.toList())
            ))));

        }

        // 3. Range queries -> Filter context
        if (cardParam.getMinTime() != null) {
            filterBool.filter(f -> f.range(r -> r.field("cookingTimeMin").gte(co.elastic.clients.json.JsonData.of(cardParam.getMinTime()))));
        }
        if (cardParam.getMaxTime() != null) {
            filterBool.filter(f -> f.range(r -> r.field("cookingTimeMin").lte(co.elastic.clients.json.JsonData.of(cardParam.getMaxTime()))));
        }

        // 4. Ingredients -> Must be Match query because it's a Text field, NOT Keyword!
        if (cardParam.getIngredientTags() != null && !cardParam.getIngredientTags().isEmpty()) {
            // If we want the recipe to contain AT LEAST ONE of the selected ingredients:
            BoolQuery.Builder ingredientsBool = new BoolQuery.Builder();
            for (String ingredient : cardParam.getIngredientTags()) {
                ingredientsBool.should(s -> s.match(m -> m.field("ingredients").query(ingredient)));
            }
            // Add it to the main filter context
            filterBool.filter(f -> f.bool(ingredientsBool.build()));
        }

        // ==========================================
        // Build and execute the NativeQuery
        // ==========================================
        Query q = Query.of(b -> b.bool(filterBool.build()));

        Pageable pageable = PageRequest.of(page , pageSize+1);

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q)
                .withPageable(pageable)
                // Optional: Add sorting by ID descending (newest first) if no keyword search is provided
                // .withSort(Sort.by(Sort.Direction.DESC, "id"))
                .build();

        SearchHits<RecipeDoc> searchHits = elasticsearchOperations.search(nativeQuery, RecipeDoc.class);

        // Map results to DTO
        List<RecipeCardDto> cardDtoList = DocToDto(searchHits);

        // Check for next page
        boolean hasNext = cardDtoList.size() > pageSize;
        List<RecipeCardDto> content = hasNext ? cardDtoList.subList(0, pageSize) : cardDtoList;
        Pageable resultPageable = PageRequest.of(page, pageSize);
        return new SliceImpl<>(content, resultPageable, hasNext);
    }

    private List<RecipeCardDto>  DocToDto( SearchHits<RecipeDoc> searchHits)
    {
        Long currentUserId=AuthUtils.getCurrentUserIdOrNull();
        Set<Long> userSavedRecipeIds = (currentUserId != null)
                ? userSavedRepository.findRecipeIdsByUserId(currentUserId)
                : Collections.emptySet();

        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent) // 剥开 ES 的外壳，拿到 RecipeDoc
                .map(doc -> {
                    // a. MapStruct 极速转换
                    RecipeCardDto dto = recipeMapper.docToCardDto(doc);
                    dto.setIsLiked(userSavedRecipeIds.contains(doc.getId()));
                    return dto;
                })
                .collect(Collectors.toList());


    }

    private List<RecipeCardDto> executeTopFiveQuery(Query query) {
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withMaxResults(5)
                .build();
        SearchHits<RecipeDoc> searchHits = elasticsearchOperations.search(nativeQuery, RecipeDoc.class);
        return DocToDto(searchHits);
    }

    private Slice<RecipeCardDto> executePagedQuery(Query query, int page, int pageSize) {
        Pageable fetchPageable = PageRequest.of(page, pageSize + 1);
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withPageable(fetchPageable)
                .build();

        SearchHits<RecipeDoc> searchHits = elasticsearchOperations.search(nativeQuery, RecipeDoc.class);
        List<RecipeCardDto> cardDtoList = DocToDto(searchHits);

        boolean hasNext = cardDtoList.size() > pageSize;
        List<RecipeCardDto> content = hasNext ? cardDtoList.subList(0, pageSize) : cardDtoList;
        Pageable resultPageable = PageRequest.of(page, pageSize);
        return new SliceImpl<>(content, resultPageable, hasNext);
    }

    private String resolveCourse(Integer hour) {
        int safeHour = hour == null ? 12 : hour;

        if (safeHour >= 5 && safeHour < 11) {
            return "BREAKFAST";
        }
        if (safeHour >= 11 && safeHour < 16) {
            return "LUNCH";
        }
        if (safeHour >= 16 && safeHour < 22) {
            return "DINNER";
        }
        return "SNACK";
    }

    private List<FunctionScore> buildQualityFunctions(String targetCourse, String randomSeed) {
        List<FunctionScore> scoreFunctions = new ArrayList<>();

        if (targetCourse != null && !targetCourse.isBlank()) {
            scoreFunctions.add(FunctionScore.of(fs -> fs
                    .filter(f -> f.term(t -> t.field("courses").value(targetCourse)))
                    .weight(2.4)
            ));
        }

        scoreFunctions.add(FunctionScore.of(fs -> fs
                .fieldValueFactor(fvf -> fvf
                        .field("averageRating")
                        .factor(1.2)
                        .missing(3.0)
                        .modifier(FieldValueFactorModifier.Log1p)
                )
        ));

        scoreFunctions.add(FunctionScore.of(fs -> fs
                .fieldValueFactor(fvf -> fvf
                        .field("ratingCount")
                        .factor(0.5)
                        .missing(0.0)
                        .modifier(FieldValueFactorModifier.Log2p)
                )
        ));

        scoreFunctions.add(FunctionScore.of(fs -> fs
                .gauss(g -> g
                        .field("createdAt")
                        .placement(p -> p
                                .origin(JsonData.of("now"))
                                .scale(JsonData.of("30d"))
                                .offset(JsonData.of("7d"))
                                .decay(0.5)
                        )
                )
        ));

        scoreFunctions.add(FunctionScore.of(fs -> fs
                .randomScore(rs -> rs.seed(randomSeed).field("_seq_no"))
                .weight(1.1)
        ));

        return scoreFunctions;
    }

    private Query buildFunctionScoreQuery(Query baseQuery, List<FunctionScore> scoreFunctions) {
        if (scoreFunctions.isEmpty()) {
            return baseQuery;
        }

        return Query.of(q -> q.functionScore(fsq -> fsq
                .query(baseQuery)
                .functions(scoreFunctions)
                .scoreMode(FunctionScoreMode.Sum)
                .boostMode(FunctionBoostMode.Sum)
        ));
    }

    private RecipeCardDto getLatestSavedSeed(Long userId) {
        Slice<Recipe> savedSlice = userSavedRepository.findSavedRecipesByUserId(userId, PageRequest.of(0, 1));
        if (!savedSlice.hasContent()) {
            return null;
        }

        Recipe seedRecipe = savedSlice.getContent().get(0);
        return recipeMapper.toCardDto(seedRecipe);
    }

    @Override
    public List<RecipeCardDto> getHeroRecommendations(Integer hour) {
        String currentCourse = resolveCourse(hour);
        String seed = "hero-" + java.time.LocalDate.now();

        Query finalQuery = Query.of(q -> q.functionScore(fsq -> fsq
                .query(m -> m.matchAll(ma -> ma))
                .functions(buildQualityFunctions(currentCourse, seed))
                .scoreMode(FunctionScoreMode.Sum)
                .boostMode(FunctionBoostMode.Sum)
        ));

        return executeTopFiveQuery(finalQuery);
    }

    @Override
    public Slice<RecipeCardDto> getPersonalizedRecommendations(Integer hour, int page, int pageSize) {
        Long currentUserId = AuthUtils.getCurrentUserIdOrNull();
        UserPreferenceDto prefs = currentUserId == null ? null : userService.getUserPreferences();

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        applyHardPreferenceFilters(boolBuilder, prefs);
        Query baseQuery = Query.of(q -> q.bool(boolBuilder.build()));

        String currentCourse = resolveCourse(hour);
        String seed = (currentUserId == null ? "guest" : currentUserId) + "-right-now-" + java.time.LocalDate.now();
        Query finalQuery = buildFunctionScoreQuery(baseQuery, buildQualityFunctions(currentCourse, seed));

        return executePagedQuery(finalQuery, page, pageSize);
    }

    @Override
    public Slice<RecipeCardDto> getBehaviorRecommendations(int page, int pageSize) {
        Long currentUserId = AuthUtils.getCurrentUserIdOrNull();
        if (currentUserId == null) {
            return new SliceImpl<>(Collections.emptyList(), PageRequest.of(page, pageSize), false);
        }

        RecipeCardDto savedSeed = getLatestSavedSeed(currentUserId);
        if (savedSeed == null) {
            return new SliceImpl<>(Collections.emptyList(), PageRequest.of(page, pageSize), false);
        }

        UserPreferenceDto prefs = userService.getUserPreferences();
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        applyHardPreferenceFilters(boolBuilder, prefs);
        if (savedSeed.getId() != null) {
            boolBuilder.mustNot(m -> m.term(t -> t.field("id").value(savedSeed.getId())));
        }
        Query baseQuery = Query.of(q -> q.bool(boolBuilder.build()));

        List<FunctionScore> scoreFunctions = new ArrayList<>();
        if (savedSeed.getCuisines() != null) {
            for (String cuisine : savedSeed.getCuisines()) {
                scoreFunctions.add(FunctionScore.of(fs -> fs
                        .filter(f -> f.term(t -> t.field("cuisines").value(cuisine)))
                        .weight(2.2)
                ));
            }
        }
        if (savedSeed.getFlavours() != null) {
            for (String flavour : savedSeed.getFlavours()) {
                scoreFunctions.add(FunctionScore.of(fs -> fs
                        .filter(f -> f.term(t -> t.field("flavours").value(flavour)))
                        .weight(1.8)
                ));
            }
        }
        if (savedSeed.getTitle() != null && !savedSeed.getTitle().isBlank()) {
            scoreFunctions.add(FunctionScore.of(fs -> fs
                    .filter(f -> f.match(m -> m.field("title").query(savedSeed.getTitle())))
                    .weight(1.4)
            ));
        }
        scoreFunctions.addAll(buildQualityFunctions(null, currentUserId + "-behavior-" + java.time.LocalDate.now()));

        Query finalQuery = buildFunctionScoreQuery(baseQuery, scoreFunctions);
        return executePagedQuery(finalQuery, page, pageSize);
    }

    @Override
    public Slice<RecipeCardDto> getTasteRecommendations(int page, int pageSize) {
        if (AuthUtils.getCurrentUserIdOrNull() == null) {
            return new SliceImpl<>(Collections.emptyList(), PageRequest.of(page, pageSize), false);
        }

        UserPreferenceDto prefs = userService.getUserPreferences();
        if (prefs == null || prefs.isEmpty()) {
            return new SliceImpl<>(Collections.emptyList(), PageRequest.of(page, pageSize), false);
        }

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        applyHardPreferenceFilters(boolBuilder, prefs);
        Query baseQuery = Query.of(q -> q.bool(boolBuilder.build()));
        List<FunctionScore> scoreFunctions = new ArrayList<>();

        if (!prefs.getCuisines().isEmpty()) {
            for (String cuisine : prefs.getCuisines()) {
                scoreFunctions.add(FunctionScore.of(fs -> fs
                        .filter(f -> f.term(t -> t.field("cuisines").value(cuisine)))
                        .weight(1.5)
                ));
            }
        }

        if (!prefs.getFlavours().isEmpty()) {
            for (String flavour : prefs.getFlavours()) {
                scoreFunctions.add(FunctionScore.of(fs -> fs
                        .filter(f -> f.term(t -> t.field("flavours").value(flavour)))
                        .weight(2.0)
                ));
            }
        }

        Query query = buildFunctionScoreQuery(baseQuery, scoreFunctions);
        return executePagedQuery(query, page, pageSize);
    }

    private void applyHardPreferenceFilters(BoolQuery.Builder boolBuilder, UserPreferenceDto prefs) {
        if (prefs == null) {
            return;
        }

        if (!prefs.getDietary().isEmpty()) {
            for (String diet : prefs.getDietary()) {
                boolBuilder.filter(f -> f.term(t -> t.field("dietTypes").value(diet)));
            }
        }

        if (!prefs.getAllergies().isEmpty()) {
            for (String allergy : prefs.getAllergies()) {
                boolBuilder.mustNot(m -> m.match(t -> t.field("ingredients").query(allergy)));
            }
        }

        if (prefs.getTimeAvailability() != null && !prefs.getTimeAvailability().equals("") && !prefs.getTimeAvailability().equals("999")) {
            int maxTime = Integer.parseInt(prefs.getTimeAvailability());
            boolBuilder.filter(f -> f.range(r -> r.field("cookingTimeMin").lte(JsonData.of(maxTime))));
        }
    }

    @Override
    public Slice<RecipeCardDto> getTrendingRecipes(int page , int pageSize) {



        List<FunctionScore> functions = new ArrayList<>();
        Pageable fetchPageable = PageRequest.of(page, pageSize + 1);
        // 评分高更靠前
        functions.add(FunctionScore.of(fs -> fs
                .fieldValueFactor(fvf -> fvf
                        .field("averageRating")
                        .factor(1.4)
                        .missing(3.0)
                )
        ));

        // 评分人数多更靠前（log防止大数碾压）
        functions.add(FunctionScore.of(fs -> fs
                .fieldValueFactor(fvf -> fvf
                        .field("ratingCount")
                        .factor(0.6)
                        .missing(0.0)
                        .modifier(FieldValueFactorModifier.Log1p)
                )
        ));

        // 新鲜度加成（越新分越高）
        functions.add(FunctionScore.of(fs -> fs
                .gauss(g -> g
                        .field("createdAt")
                        .placement(p -> p
                                .origin(JsonData.of("now"))
                                .scale(JsonData.of("30d"))
                                .offset(JsonData.of("3d"))
                                .decay(0.5)
                        )
                )
        ));

        Query finalQuery = Query.of(q -> q.functionScore(fsq -> fsq
                .query(m -> m.matchAll(ma -> ma))
                .functions(functions)
                .scoreMode(FunctionScoreMode.Sum)
                .boostMode(FunctionBoostMode.Sum)
        ));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(fetchPageable)
                .build();

        SearchHits<RecipeDoc> searchHits = elasticsearchOperations.search(nativeQuery, RecipeDoc.class);
        var cardDtoList = DocToDto(searchHits);

        boolean hasNext = cardDtoList.size() > pageSize;
        List<RecipeCardDto> content = hasNext ? cardDtoList.subList(0, pageSize) : cardDtoList;
        Pageable resultPageable = PageRequest.of(page, pageSize);
        return new SliceImpl<>(content, resultPageable, hasNext);


    }


    @Override
    public List<String> getFlavours() {
        List<Flavour> flavours = flavourRepository.findAll();
        return flavours.stream()
                .map(Flavour::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getCuisines() {
        List<Cuisine> cuisines = cuisineRepository.findAll();
        return cuisines.stream()
                .map(Cuisine::getName) // 只提取名字
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getIngredients() {
        List<Ingredient> ingredients = ingredientRepository.findAll();
        return ingredients.stream()
                .map(Ingredient::getName) // 只提取名字
                .collect(Collectors.toList());
    }

}
