package org.zhan.recipe_backend.service.impl;


import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhan.recipe_backend.document.RecipeDoc;
import org.zhan.recipe_backend.dto.*;
import org.zhan.recipe_backend.entity.*;
import org.zhan.recipe_backend.mapper.RecipeMapper;
import org.zhan.recipe_backend.repository.*;
import org.zhan.recipe_backend.service.RecipeService;
import org.zhan.recipe_backend.utils.AuthUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecipeServiceImpl implements RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private FlavourRepository flavourRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private  CuisineRepository cuisineRepository;

    @Autowired
    private  UserSavedRepository userSavedRepository;

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
   private  RecipeEsRepository recipeEsRepository;

    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private DietTypeRepository dietTypeRepository;
    @Override
    public RecipeDetailDto getRecipes(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("哎呀，找不到这个食谱！ID: " + id));
        RecipeDetailDto dto = recipeMapper.toDetailDto(recipe);
        Long currentUserId=AuthUtils.getCurrentUserIdOrNull();
        if ( currentUserId!= null) {
            boolean isLiked = userSavedRepository.existsByUserIdAndRecipeId(currentUserId, id);
            dto.setIsLiked(isLiked);
            Optional<Recipe_Rating> rating = ratingRepository.findByUserIdAndRecipeId(currentUserId, id);
            rating.ifPresent(r -> dto.setUserScore(r.getScore()));
        } else {
            dto.setIsLiked(false);
        }
        return dto;


    }

    @Transactional
    @Override
    public void addRecipe(RecipeDetailDto dto) {
        // ==========================================
        // 1. 基础属性与核心实体组装
        // ==========================================
        Recipe recipe = new Recipe();
        BeanUtils.copyProperties(dto, recipe);
        long currentUserId=AuthUtils.getCurrentUserIdOrNull();

        User author = userRepository.getReferenceById(currentUserId);
        recipe.setAuthor(author);


        recipe.setRecipeFlavours(buildFlavours(dto.getFlavours(), recipe));
        recipe.setRecipeCourses(buildCourses(dto.getCourses(), recipe));
        recipe.setRecipeCuisines(buildCuisines(dto.getCuisines(), recipe));
        recipe.setRecipeIngredients(buildIngredients(dto.getIngredients(), recipe));
        recipe.setRecipeDietTypes(buildDietType(dto.getDietTypes(),recipe));
        recipe.setSteps(buildSteps(dto.getSteps(), recipe));

        Recipe savedRecipe = recipeRepository.save(recipe);

        syncToElasticsearch(savedRecipe);
    }

    // ==========================================
    // ⬇️ 脏活累活全部藏在私有方法里 (代码复用，极度整洁) ⬇️
    // ==========================================

    private List<Recipe_Flavour> buildFlavours(List<String> names, Recipe recipe) {
        if (names == null) return new ArrayList<>();
        return names.stream().map(name -> {
            Flavour flavour = flavourRepository.findByName(name)
                    .orElseGet(() -> flavourRepository.save(Flavour.builder().name(name).build()));
            Recipe_Flavour relation = new Recipe_Flavour();
            relation.setRecipe(recipe);
            relation.setFlavour(flavour);
            return relation;
        }).collect(Collectors.toList());
    }

    private List<Recipe_Course> buildCourses(List<String> names, Recipe recipe) {
        if (names == null) return new ArrayList<>();
        return names.stream().map(name -> {
            Course course = courseRepository.findByName(name)
                    .orElseGet(() -> courseRepository.save(Course.builder().name(name).build()));
            Recipe_Course relation = new Recipe_Course();
            relation.setRecipe(recipe);
            relation.setCourse(course);
            return relation;
        }).collect(Collectors.toList());
    }

    private List<Recipe_Cuisine> buildCuisines(List<String> names, Recipe recipe) {
        if (names == null) return new ArrayList<>();
        return names.stream().map(name -> {
            Cuisine cuisine = cuisineRepository.findByName(name)
                    .orElseGet(() -> cuisineRepository.save(Cuisine.builder().name(name).build()));
            Recipe_Cuisine relation = new Recipe_Cuisine();
            relation.setRecipe(recipe);
            relation.setCuisine(cuisine);
            return relation;
        }).collect(Collectors.toList());
    }

    private List<Recipe_DietType> buildDietType(List<String> names, Recipe recipe) {
        if (names == null) return new ArrayList<>();
        return names.stream().map(name -> {
            DietType dietType = dietTypeRepository.findByName(name)
                    .orElseGet(() -> dietTypeRepository.save(DietType.builder().name(name).build()));
            Recipe_DietType relation = new Recipe_DietType();
            relation.setRecipe(recipe);
            relation.setDietType(dietType);
            return relation;
        }).collect(Collectors.toList());
    }

    private List<Recipe_Ingredient> buildIngredients(List<IngredientDto> dtos, Recipe recipe) {
        if (dtos == null) return new ArrayList<>();
        return dtos.stream().map(ingDto -> {
            Ingredient ingredient = ingredientRepository.findByName(ingDto.getName())
                    .orElseGet(() -> ingredientRepository.save(Ingredient.builder().name(ingDto.getName()).build()));
            Recipe_Ingredient relation = new Recipe_Ingredient();
            relation.setRecipe(recipe);
            relation.setIngredient(ingredient);
            relation.setAmount(ingDto.getAmount());
            relation.setNote(ingDto.getNote());
            return relation;
        }).collect(Collectors.toList());
    }

    private List<RecipeStep> buildSteps(List<RecipeStepDto> dtos, Recipe recipe) {
        if (dtos == null) return new ArrayList<>();
        List<RecipeStep> steps = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            RecipeStepDto stepDto = dtos.get(i);
            RecipeStep step = new RecipeStep();
            step.setRecipe(recipe);
            step.setStepNumber(i + 1);
            step.setContent(stepDto.getContent());
            step.setImageUrls(stepDto.getImageUrls());
            steps.add(step);
        }
        return steps;
    }


    public void syncToElasticsearch(Recipe recipe) {
        try {

            RecipeDoc esDoc = recipeMapper.toRecipeDoc(recipe);
            recipeEsRepository.save(esDoc);

        } catch (Exception e) {


        }
    }




}
