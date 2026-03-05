package org.zhan.recipe_backend.service.impl;


import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhan.recipe_backend.dto.*;
import org.zhan.recipe_backend.entity.*;
import org.zhan.recipe_backend.repository.*;
import org.zhan.recipe_backend.service.RecipeService;
import org.zhan.recipe_backend.utils.AuthUtils;

import java.time.LocalDateTime;
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
    private RatingRepository ratingRepository;
    @Override
    public RecipeDetailDto getRecipes(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("哎呀，找不到这个食谱！ID: " + id));
        RecipeDetailDto dto = new RecipeDetailDto();
        BeanUtils.copyProperties(recipe, dto);
        UserDto authorDto = new UserDto();
        BeanUtils.copyProperties(recipe.getAuthor(), authorDto);
        dto.setAuthor(authorDto);
        dto.setFlavours(recipe.getRecipeFlavours().stream().map(rf -> rf.getFlavour().getName()).collect(Collectors.toList()));
        dto.setCourses(recipe.getRecipeCourses().stream().map(rc -> rc.getCourse().getName()).collect(Collectors.toList()));
        dto.setCuisines(recipe.getRecipeCuisines().stream().map(rc -> rc.getCuisine().getName()).collect(Collectors.toList()));
        if (recipe.getIngredientsList() != null) {
            List<IngredientDto> ingredientDtos = recipe.getIngredientsList().stream().map(ingredient -> {
                IngredientDto ingDto = new IngredientDto();
                ingDto.setName(ingredient.getIngredient().getName());
                ingDto.setAmount(ingredient.getAmount());// 假设你前端 amount 还是用 String 接收
                ingDto.setNote(ingredient.getNote());
                return ingDto;
            }).collect(Collectors.toList());
            dto.setIngredientsList(ingredientDtos);
        }
            List<RecipeStepDto> stepDtos = recipe.getSteps().stream().map(step -> {
                RecipeStepDto stepDto = new RecipeStepDto();
                // 根据你 RecipeStepDto 的实际字段来 set
                stepDto.setContent(step.getContent());
                stepDto.setImageUrls(step.getImageUrls());
                return stepDto;
            }).collect(Collectors.toList());
            dto.setSteps(stepDtos);

        Long userId = AuthUtils.getCurrentUserIdOrNull();
        if (userId != null) {
            Optional<RecipeRating> rating = ratingRepository.findByUserIdAndRecipeId(userId, id);
            rating.ifPresent(r -> dto.setUserScore(r.getScore()));
        }

        return dto;

    }

    @Transactional
    @Override
    public void addRecipe(RecipeDetailDto dto,  Long userId) {
        Recipe recipe = new Recipe();
        BeanUtils.copyProperties(dto, recipe);
        User author = userRepository.getReferenceById(userId);
        recipe.setAuthor(author); //
        recipe.setCreatedAt(LocalDateTime.now()); //
        recipe.setUpdatedAt(LocalDateTime.now());
            List<Recipe_Flavour> recipeFlavours = new ArrayList<>();
            for (String flavourName : dto.getFlavours()) {
                // 【核心逻辑】：去字典表找这个口味，找不到就当场新建一个！
                Flavour flavour = flavourRepository.findByName(flavourName)
                        .orElseGet(() -> flavourRepository.save(Flavour.builder().name(flavourName).build()));

                // 创建第 3 张表的关联实体
                Recipe_Flavour relation = new Recipe_Flavour();
                relation.setRecipe(recipe);
                relation.setFlavour(flavour);
                recipeFlavours.add(relation);
            }
            // 把关系塞给食谱
            recipe.setRecipeFlavours(recipeFlavours);


            List<Recipe_Course> recipeCourses = new ArrayList<>();
            for (String courseName : dto.getCourses()) {
                // 【核心逻辑】：去字典表找这个口味，找不到就当场新建一个！
                Course course = courseRepository.findByName(courseName)
                        .orElseGet(() -> courseRepository.save(Course.builder().name(courseName).build()));
                // 创建第 3 张表的关联实体
                Recipe_Course relation = new Recipe_Course();
                relation.setRecipe(recipe);
                relation.setCourse(course);
                recipeCourses.add(relation);
            }
            // 把关系塞给食谱
            recipe.setRecipeCourses(recipeCourses);

            List<Recipe_Cuisine> recipeCuisines = new ArrayList<>();
            for (String cuisineName : dto.getCuisines()) {
                // 【核心逻辑】：去字典表找这个口味，找不到就当场新建一个！
                Cuisine cuisine = cuisineRepository.findByName(cuisineName)
                        .orElseGet(() -> cuisineRepository.save(Cuisine.builder().name(cuisineName).build()));
                // 创建第 3 张表的关联实体
                Recipe_Cuisine relation = new Recipe_Cuisine();
                relation.setRecipe(recipe);
                relation.setCuisine(cuisine);
                recipeCuisines.add(relation);
            }
            // 把关系塞给食谱
            recipe.setRecipeCuisines(recipeCuisines);

            List<Recipe_Ingredient> recipeIngredients = new ArrayList<>();
            for (IngredientDto ingDto : dto.getIngredientsList()) {
                // 去字典表找这个食材名字，没有就新建
                Ingredient ingredient = ingredientRepository.findByName(ingDto.getName())
                        .orElseGet(() -> ingredientRepository.save(Ingredient.builder().name(ingDto.getName()).build()));

                // 创建带负载属性的第 3 张表关联实体
                Recipe_Ingredient relation = new Recipe_Ingredient();
                relation.setRecipe(recipe);
                relation.setIngredient(ingredient);
                relation.setAmount(ingDto.getAmount()); // 存入量
                relation.setNote(ingDto.getNote());     // 存备注
                recipeIngredients.add(relation);
            }
            recipe.setIngredientsList(recipeIngredients);

            List<RecipeStep> recipeSteps = new ArrayList<>();
            for (int i = 0; i < dto.getSteps().size(); i++) {
                RecipeStepDto stepDto = dto.getSteps().get(i);
                RecipeStep step = new RecipeStep();
                step.setRecipe(recipe);
                step.setStepNumber(i + 1); // 巧妙利用循环索引生成顺序
                step.setContent(stepDto.getContent());
                step.setImageUrls(stepDto.getImageUrls());
                recipeSteps.add(step);
            }
            recipe.setSteps(recipeSteps);



        recipeRepository.save(recipe);
    }




}
