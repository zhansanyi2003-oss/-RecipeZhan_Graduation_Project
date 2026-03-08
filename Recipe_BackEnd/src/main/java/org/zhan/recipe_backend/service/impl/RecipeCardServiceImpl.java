package org.zhan.recipe_backend.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.zhan.recipe_backend.document.RecipeDoc;
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.entity.*;
import org.zhan.recipe_backend.mapper.RecipeMapper;
import org.zhan.recipe_backend.repository.*;
import org.zhan.recipe_backend.service.RecipeCardService;
import org.zhan.recipe_backend.utils.AuthUtils;


import java.util.List;
import java.util.stream.Collectors;


@Service
public class RecipeCardServiceImpl implements RecipeCardService {

    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private FlavourRepository flavourRepository;
    @Autowired
    private CuisineRepository cuisineRepository;

    @Autowired
    private UserSavedRepository userSavedRepository;

    @Autowired
    private RecipeEsRepository recipeEsRepository;

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public Slice<RecipeCardDto> getRecipeCards(RecipeCardDto cardParam,int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Slice<RecipeDoc> esSlice = recipeEsRepository.findAll(pageable);

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
        Long currentUserId=AuthUtils.getCurrentUserIdOrNull();
        List<RecipeCardDto> cardDtoList = searchHits.getSearchHits().stream()
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

        // ==========================================
        // 4. 打包成 Slice 返回前端
        // ==========================================
        // 判断是否还有下一页：如果查出来的数量 == 你要求的一页的数量，通常说明可能还有下一页
        boolean hasNext = cardDtoList.size() == pageable.getPageSize();
        return new SliceImpl<>(cardDtoList, pageable, hasNext);





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

}
