package org.zhan.recipe_backend.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
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


import java.util.ArrayList;
import java.util.List;
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

    public Slice<RecipeCardDto> getRecipeCards(RecipeCardDto cardParam,int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size);


        Criteria criteria = new Criteria(); // 创建一个空的查询条件盒

        // A. 关键字模糊搜索 (搜标题)
        if (cardParam.getTitle() != null && !cardParam.getTitle() .trim().isEmpty()) {
            criteria.and(new Criteria("title").contains(cardParam.getTitle() ));
        }

        // B. 数组精确筛选：只要包含这些口味中的任意一个即可 (IN 查询)
        if (cardParam.getFlavours() != null && !cardParam.getFlavours().isEmpty()) {
            criteria.and(new Criteria("flavours").in(cardParam.getFlavours()));
        }

        if (cardParam.getCuisines() != null && !cardParam.getCuisines().isEmpty()) {
            criteria.and(new Criteria("cuisines").in(cardParam.getCuisines()));
        }
        if (cardParam.getDietTypes() != null && !cardParam.getDietTypes().isEmpty()) {
            criteria.and(new Criteria("dietTypes").in(cardParam.getDietTypes()));
        }
        if (cardParam.getCourses() != null && !cardParam.getCourses().isEmpty()) {
            criteria.and(new Criteria("courses").in(cardParam.getCourses()));
        }

        // C. 范围筛选：烹饪时间 (大于等于 min, 小于等于 max)
        if (cardParam.getMinTime() != null) {
            criteria.and(new Criteria("cookingTimeMin").greaterThanEqual(cardParam.getMinTime()));
        }
        if (cardParam.getMaxTime() != null) {
            criteria.and(new Criteria("cookingTimeMin").lessThanEqual(cardParam.getMaxTime()));
        }
        if(cardParam.getIngredientTags()!=null && !cardParam.getIngredientTags().isEmpty()) {
            criteria.and(new Criteria("ingredients").in(cardParam.getIngredientTags()));
        }

        // ==========================================
        // 2. 组装并发送给 Elasticsearch 执行
        // ==========================================
        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(pageable); // 把分页参数塞进去

        // 🌟 见证奇迹的时刻：执行动态查询！
        SearchHits<RecipeDoc> searchHits = elasticsearchOperations.search(query, RecipeDoc.class);

        // ==========================================
        // 3. 结果提取与“动态缝合” (结合 MapStruct 与 PG)
        // ==========================================
        // 注意：ES 返回的是 SearchHits，我们需要把里面的 content 提取出来
        List<RecipeCardDto> cardDtoList=DocToDto(searchHits);

        // ==========================================
        // 4. 打包成 Slice 返回前端
        // ==========================================
        // 判断是否还有下一页：如果查出来的数量 == 你要求的一页的数量，通常说明可能还有下一页
        boolean hasNext = cardDtoList.size() == pageable.getPageSize();
        return new SliceImpl<>(cardDtoList, pageable, hasNext);


    }

    private List<RecipeCardDto>  DocToDto( SearchHits<RecipeDoc> searchHits)
    {
        Long currentUserId=AuthUtils.getCurrentUserIdOrNull();
      return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent) // 剥开 ES 的外壳，拿到 RecipeDoc
                .map(doc -> {
                    // a. MapStruct 极速转换
                    RecipeCardDto dto = recipeMapper.docToCardDto(doc);
                    // b. 去 PostgreSQL 查私有状态
                    if (currentUserId != null) {
                        dto.setIsLiked(userSavedRepository.existsByUserIdAndRecipeId(currentUserId, doc.getId()));
                    } else {
                        dto.setIsLiked(false);
                    }
                    return dto;
                })
                .collect(Collectors.toList());


    }

    private  List<RecipeCardDto> getGuestRecommendations()
    {
        NativeQuery guestQuery = NativeQuery.builder()
                .withQuery(q -> q.matchAll(m -> m)) // 查所有食谱
                .withSort(Sort.by(Sort.Direction.DESC, "averageRating")) // 优先按评分降序
                .withSort(Sort.by(Sort.Direction.DESC, "ratingCount"))   // 评分一样，按评价人数降序
                .withMaxResults(5) // 🌟 大厂标准：只取 5 条！
                .build();

        SearchHits<RecipeDoc> hits = elasticsearchOperations.search(guestQuery, RecipeDoc.class);
        return DocToDto(hits);

    }

    public List<RecipeCardDto> getPersonalizedRecommendations(Integer hour) {
        Long currentUserId=AuthUtils.getCurrentUserIdOrNull();

        if (currentUserId == null) {

           return   getGuestRecommendations();
        }
        else {
            UserPreferenceDto prefs = userService.getUserPreferences();

            if (prefs.isEmpty()) {
               return getGuestRecommendations();
            }
            BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

            // 1. 饮食限制 (Dietary) -> 必须全部包含 (Must / Filter)
            if (!prefs.getDietary().isEmpty()) {
                for (String diet : prefs.getDietary()) {
                    boolBuilder.filter(f -> f.match(t -> t.field("dietTypes").query(diet)));
                }
            }

            // 2. 过敏源/厌恶 (Allergies) -> 绝对不能包含 (Must Not)
            if (!prefs.getAllergies().isEmpty()) {
                for (String allergy : prefs.getAllergies()) {
                    boolBuilder.mustNot(m -> m.match(t -> t.field("ingredients").query(allergy)));
                }
            }

            // 3. 烹饪时间限制 (Time) -> 小于等于用户设定的时间 (Filter Range)
            if (prefs.getTimeAvailability() != null && !prefs.getTimeAvailability().equals("999")) {
                int maxTime = Integer.parseInt(prefs.getTimeAvailability());
                boolBuilder.filter(f -> f.range(r -> r.field("cookingTimeMin").lte(co.elastic.clients.json.JsonData.of(maxTime))));
            }

            // 将基础过滤条件打包成一个 Query
            Query baseQuery = Query.of(q -> q.bool(boolBuilder.build()));


            // ==========================================
            // 🌟 第二步：施展魔法，构建 Function Score 加权
            // ==========================================
            List<FunctionScore> scoreFunctions = new ArrayList<>();

            // 1. 口味加分 (Flavours)：如果这道菜命中用户喜欢的口味，总分乘以 2 倍！
            if (!prefs.getFlavours().isEmpty()) {
                for (String flavour : prefs.getFlavours()) {
                    scoreFunctions.add(FunctionScore.of(fs -> fs
                            .filter(f -> f.term(t -> t.field("flavours").value(flavour)))
                            .weight(2.0) // 命中一个口味，权重 x 2
                    ));
                }
            }
            String targetCourse;

            if (hour >= 5 && hour < 11) {
                targetCourse = "BREAKFAST";   // 早上 5点-11点，推早餐
            } else if (hour >= 11 && hour < 16) {
                targetCourse = "LUNCH";       // 中午 11点-下午4点，推午餐
            } else if (hour >= 16 && hour < 22) {
                targetCourse = "DINNER";      // 下午 4点-晚上10点，推晚餐
            } else {
                targetCourse = "SNACK";       // 深夜报社时间，推夜宵/小吃！
            }
                final String currentCourse = targetCourse;
                scoreFunctions.add(FunctionScore.of(fs -> fs
                        .filter(f -> f.term(t -> t.field("courses").value(currentCourse)))
                        .weight(2.) // 时令菜肴，权重提升！
                ));


            // 2. 菜系加分 (Cuisines)：如果命中喜欢的菜系，总分乘以 1.5 倍！
            if (!prefs.getCuisines().isEmpty()) {
                for (String cuisine : prefs.getCuisines()) {
                    scoreFunctions.add(FunctionScore.of(fs -> fs
                            .filter(f -> f.term(t -> t.field("cuisines").value(cuisine)))
                            .weight(1.5) // 命中一个菜系，权重 x 1.5
                    ));
                }
            }
            if (!prefs.getIngredients().isEmpty()) {
                for (String ingredient : prefs.getIngredients()) {
                    scoreFunctions.add(FunctionScore.of(fs -> fs
                            // 注意：因为食材在 ES 里可能是个长文本（比如 "300g Chicken Breast"），
                            // 所以这里用 match 模糊匹配，而不是 term 精确匹配
                            .filter(f -> f.match(m -> m.field("ingredients").query(ingredient)))
                            .weight(1.8) // 命中一个喜欢的食材，权重直接 x 1.8 倍！
                    ));
                }
            }

            // 3. 厨艺匹配加分 (Skill Level)：给适合他厨艺的菜稍微提点分
            if (prefs.getSkillLevel() != null) {
                String difficulty = prefs.getSkillLevel().equals("Beginner") ? "EASY" :
                        (prefs.getSkillLevel().equals("Intermediate") ? "MEDIUM" : "HARD");
                scoreFunctions.add(FunctionScore.of(fs -> fs
                        .filter(f -> f.term(t -> t.field("difficulty").value(difficulty)))
                        .weight(1.2) // 难度匹配，权重 x 1.2
                ));
            }

            scoreFunctions.add(FunctionScore.of(fs -> fs

                    .randomScore(rs -> rs.seed(String.valueOf(System.currentTimeMillis())).field("_seq_no"))
                    .weight(1.2) // 给一个 1.2 的微小权重，不会完全颠覆推荐，但足以让列表产生“呼吸感”
            ));

            // 把底层的 baseQuery 和 加分逻辑 scoreFunctions 组合起来
            Query finalQuery = Query.of(q -> q.functionScore(fsq -> fsq
                    .query(baseQuery)
                    .functions(scoreFunctions)
                    .scoreMode(FunctionScoreMode.Multiply) // 多个加分项采用乘法叠加
                    .boostMode(FunctionBoostMode.Multiply) // 最终得分 = 基础得分 * 加权得分
            ));

            // 使用 NativeQuery 发送给 ES
            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(finalQuery)
                    .withMaxResults(5) // 每次推荐 20 道菜
                    .build();

            SearchHits<RecipeDoc> searchHits = elasticsearchOperations.search(nativeQuery, RecipeDoc.class);
            return DocToDto(searchHits);
        }


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
