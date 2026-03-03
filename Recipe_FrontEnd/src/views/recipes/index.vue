<script setup>
import { ref, onMounted } from 'vue'
import RecipeCard from '../../component/recipeCard.vue'
import { getRecipeCardApi } from '../../api/recipeCard'
import { getAllCuisinesApi } from '../../api/recipeCard'
import { getAllFlavoursApi } from '../../api/recipeCard'

import { useRouter } from 'vue-router'
// 引入图标
import {
  ArrowRight,
  Search,
  Notebook,
  Timer,
  Sugar,
  KnifeFork,
  Dish,
} from '@element-plus/icons-vue'

// 1. 模拟食谱数据
const recipeCards = ref([])

// 2. 搜索表单对象 (完美的数据结构)
const searchRecipe = ref({
  title: '',
  ingredientTags: [],
  cookingTimeMin: '',
  flavours: [],
  courses: [],
  cuisines: [],
})

// 3. 食材动态标签的处理逻辑
const ingredientInputValue = ref('')
const addIngredientTag = () => {
  const val = ingredientInputValue.value.trim()
  if (val && !searchRecipe.value.ingredientTags.includes(val)) {
    searchRecipe.value.ingredientTags.push(val)
  }
  ingredientInputValue.value = '' // 清空输入框
}
const removeIngredientTag = (tag) => {
  searchRecipe.value.ingredientTags = searchRecipe.value.ingredientTags.filter((t) => t !== tag)
}

// 4. 4宫格下拉框配置 (必须用 ref 包裹，因为有动态数据)
const filterConfig = ref([
  {
    prop: 'cookingTimeMin',
    label: 'Cooking Time',
    icon: 'Timer',
    multiple: false, // 👈 时间只能单选
    options: [
      { label: 'Less than 15 mins', value: '15' },
      { label: 'Less than 30 mins', value: '30' },
      { label: 'Less than an hour', value: '60' },
      { label: 'More than an hour', value: '60+' },
    ],
  },
  {
    prop: 'flavours',
    label: 'Flavour',
    icon: 'Sugar',
    multiple: true, // 👈 可多选
    options: [], // 👈 留空，等待后端数据
  },
  {
    prop: 'courses',
    label: 'Course',
    icon: 'KnifeFork',
    multiple: true, // 👈 可多选
    options: [
      { label: 'Breakfast', value: 'Breakfast' },
      { label: 'Lunch', value: 'Lunch' },
      { label: 'Dinner', value: 'Dinner' },
      { label: 'Dessert', value: 'Dessert' },
      { label: 'Snack', value: 'Snack' },
    ],
  },
  {
    prop: 'cuisines',
    label: 'Cuisine',
    icon: 'Dish',
    multiple: true, // 👈 可多选
    options: [], // 👈 留空，等待后端数据
  },
])

// 5. 重置所有搜索条件
const handleReset = () => {
  searchRecipe.value = {
    title: '',
    ingredientTags: [],
    cookingTimeMin: '',
    flavours: [],
    courses: [],
    cuisines: [],
  }
}

// 6. 点击搜索按钮
const hasNext = ref(false)
const handleSearch = () => {
  currentPage.value = 1
  fetchRecipes(true)
}

const fetchRecipes = async (isNewSearch) => {
  try {
    const result = await getRecipeCardApi(searchRecipe.value, currentPage.value, pageSize.value)

    if (result.code) {
      const newData = result.data.content

      // 判断是覆盖还是追加
      if (isNewSearch) {
        recipeCards.value = newData
      } else {
        recipeCards.value.push(...newData) // ✨ 展开语法，把新卡片追加到老卡片后面
      }

      // ✨ 核心魔法：Spring Boot 返回的 Slice 里面有一个 last 属性。
      // 如果 last 为 true，说明是最后一页了。所以 hasNext 就是 !last。
      hasNext.value = !result.data.last
    }
  } catch (error) {
    console.error('请求炸了：', error)
  }
}

const loadMoreRecipes = () => {
  currentPage.value++ // 页码 +1
  fetchRecipes(false)
}

// 7. 排序和分页逻辑
const sortBy = ref('default')
const currentPage = ref(1)
const pageSize = ref(12)
const flavours = ref([])
const cuisines = ref([])

const getFlavours = async () => {
  const result = await getAllFlavoursApi()
  if (result.code) {
    flavours.value = result.data
    const flavourConfig = filterConfig.value.find((item) => item.prop === 'flavours')
    flavourConfig.options = flavours.value.map((i) => ({ label: i, value: i }))
  }
}
const getCuisines = async () => {
  const result = await getAllCuisinesApi()
  if (result.code) {
    cuisines.value = result.data
    const cuisineConfig = filterConfig.value.find((item) => item.prop === 'cuisines')
    cuisineConfig.options = cuisines.value.map((i) => ({ label: i, value: i }))
  }
}

// 8. 页面加载：拉取动态选项 & 初始化列表
onMounted(() => {
  fetchRecipes(true)
  getFlavours()
  getCuisines()
})
</script>

<template>
  <div class="recipe-list-page">
    <div class="container">
      <div class="search-panel">
        <div class="panel-header">
          <h2 class="panel-title">Searching Recipes</h2>
          <span class="clear-btn" @click="handleReset">
            Reset <el-icon><ArrowRight /></el-icon>
          </span>
        </div>
        <div class="keyword-search-box">
          <el-input
            v-model="searchRecipe.title"
            placeholder="Input the dish's name..."
            size="large"
            clearable
            class="custom-input"
          >
            <template #prefix
              ><el-icon><Search /></el-icon
            ></template>
          </el-input>
        </div>

        <div class="ingredient-tags-area">
          <span class="tags-label">
            <el-button color="#4ea685" style="color: white" :icon="Search"
              >Ingredients:</el-button
            ></span
          >

          <el-tag
            v-for="tag in searchRecipe.ingredientTags"
            :key="tag"
            closable
            effect="dark"
            color="#4ea685"
            class="ingredient-tag"
            @close="removeIngredientTag(tag)"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-model="ingredientInputValue"
            class="tag-input"
            size="small"
            placeholder="+ Add (Press Enter)"
            @keyup.enter="addIngredientTag"
            @blur="addIngredientTag"
          />
        </div>
        <div class="flex-filter-grid">
          <div v-for="item in filterConfig" :key="item.prop" class="flex-filter-item">
            <div class="filter-card">
              <div class="card-top">
                <el-icon class="card-icon" :size="32">
                  <component :is="item.icon" />
                </el-icon>
                <span class="card-label">{{ item.label }}</span>
              </div>

              <div class="card-bottom">
                <el-select
                  v-model="searchRecipe[item.prop]"
                  placeholder="All"
                  class="custom-select"
                  :multiple="item.multiple"
                  collapse-tags
                  collapse-tags-tooltip
                >
                  <el-option
                    v-for="opt in item.options"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
              </div>
            </div>
          </div>
        </div>

        <div class="search-btn-wrapper">
          <button class="search-btn" @click="handleSearch">
            <el-icon class="btn-icon"><Notebook /></el-icon>
            Searching
          </button>
        </div>
      </div>
    </div>
    <div class="recipes-section">
      <div class="list-header">
        <div class="left-spacer"></div>
        <h2 class="list-title">Recipes ({{ recipeCards.length }})</h2>
        <div class="sort-control">
          <el-select v-model="sortBy" placeholder="Sort By" class="sort-select">
            <el-option label="Default" value="default" />
            <el-option label="Newest" value="time_new" />
            <el-option label="Most Liked" value="likes_desc" />
            <el-option label="Fastest" value="time_asc" />
          </el-select>
        </div>
      </div>

      <el-row :gutter="24">
        <el-col
          v-for="item in recipeCards"
          :key="item.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          :xl="4"
        >
          <RecipeCard :data="item" />
        </el-col>
      </el-row>
    </div>
    <div class="load-more-container">
      <el-button
        v-if="hasNext"
        type="primary"
        size="large"
        round
        @click="loadMoreRecipes"
        class="load-more-btn"
      >
        Load More Recipes ...
      </el-button>

      <el-divider v-else-if="recipeCards.length > 0" class="no-more-tips">
        Oops, you have reached the end! 🍳
      </el-divider>
    </div>
  </div>
</template>

<style scoped>
/* 全局页面 */
.recipe-list-page {
  width: 100%;
}
.container {
  height: auto;
  margin-bottom: 40px;
}

/* 搜索面板 */
.search-panel {
  background-image: linear-gradient(135deg, #4ea685 0%, #57b894 100%);
  padding: 30px 40px;
  color: white;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.panel-title {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
}
.clear-btn {
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0.8;
}
.clear-btn:hover {
  opacity: 1;
  text-decoration: underline;
}

/* 菜名搜索框 */
.keyword-search-box {
  margin-bottom: 15px; /* 间距改小点，下面要放食材标签 */
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}
:deep(.custom-input .el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  box-shadow: none;
  padding-left: 15px;
}
:deep(.custom-input .el-input__inner) {
  color: #333;
  font-weight: bold;
}

/* ✨ 新增：食材动态标签区样式 */
.ingredient-tags-area {
  max-width: 600px;
  margin: 0 auto 35px auto; /* 居中，并和下方的4宫格拉开距离 */
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px; /* 标签和框之间的间距 */
}
.tags-label {
  font-size: 14px;
  font-weight: bold;
  color: #ffe0b2;
  margin-right: 8px;
}
.ingredient-tag {
  border: none;
  border-radius: 12px;
}

.tag-input {
  width: 140px;
}
:deep(.tag-input .el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  box-shadow: none;
  padding: 0 10px;
}
:deep(.tag-input .el-input__inner) {
  color: white;
  font-size: 12px;
}
:deep(.tag-input .el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.6);
}

/* ✨ 新增：4宫格 Flex 居中布局 */
.flex-filter-grid {
  display: flex;
  justify-content: center; /* 绝对居中 */
  flex-wrap: wrap;
  gap: 25px; /* 卡片之间的透气间距 */
  margin-bottom: 35px;
}

.flex-filter-item {
  flex: 0 0 180px; /* 宽度固定为 180px，4个正好很漂亮 */
}

/* 手机端自适应 */
@media (max-width: 768px) {
  .flex-filter-item {
    flex: 1 1 40%;
  }
}
@media (max-width: 480px) {
  .flex-filter-item {
    flex: 1 1 100%;
  }
}

.filter-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 6px;
  transition: transform 0.2s;
}
.filter-card:hover {
  transform: translateY(-3px);
}

.card-top {
  background-color: white;
  color: #4ea685;
  padding: 15px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-top-left-radius: 6px;
  border-top-right-radius: 6px;
}
.card-label {
  font-weight: bold;
  font-size: 16px;
}

.card-bottom {
  background-color: #eef7f4;
  border-bottom-left-radius: 6px;
  border-bottom-right-radius: 6px;
}

:deep(.custom-select .el-input__wrapper) {
  background-color: transparent !important;
  box-shadow: none !important;
  border-radius: 0;
  padding: 4px 10px;
}
:deep(.custom-select .el-input__inner) {
  text-align: center;
  color: #2a5948;
  font-weight: bold;
}

/* 搜索大按钮 */
.search-btn-wrapper {
  display: flex;
  justify-content: center;
}
.search-btn {
  background-color: white;
  color: #4ea685;
  border: none;
  border-radius: 50px;
  padding: 12px 60px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s;
}
.search-btn:hover {
  transform: scale(1.05);
}
.btn-icon {
  background-color: #4ea685;
  color: white;
  border-radius: 50%;
  padding: 4px;
  font-size: 16px;
}

/* 列表下半部分 */
.recipes-section {
  padding: 0 20px;
}
.list-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}
.left-spacer {
  flex: 1;
}
.list-title {
  flex: 1;
  text-align: center;
  color: #333;
  margin: 0;
  font-size: 24px;
}
.sort-control {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}
.sort-select {
  width: 130px;
}
:deep(.sort-select .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}
:deep(.sort-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #a8eb12 inset;
}

/* 分页 */
.load-more-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding-bottom: 50px;
}
.load-more-btn {
  width: 200px;
  font-weight: bold;
  background-color: #4ea685 !important;
  border: none;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}
.load-more-btn:hover {
  background-color: #57b894 !important; /* 悬浮时变成略浅一点的绿 */
  transform: scale(1.05);
}
.no-more-tips {
  color: #999;
  font-size: 14px;
}
</style>
