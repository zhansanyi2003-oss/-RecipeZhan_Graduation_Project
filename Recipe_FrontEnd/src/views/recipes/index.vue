<script setup>
import { ref, onMounted, watch } from 'vue'
import RecipeSliceList from '../../component/RecipeSliceList.vue'
import { getRecipeCardApi } from '../../api/recipeCard'
import { getAllCuisinesApi } from '../../api/recipeCard'
import { getAllFlavoursApi } from '../../api/recipeCard'

import { useRoute } from 'vue-router'
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

const route = useRoute()
const SEARCH_STATE_KEY = 'recipe_search_state_v1'
const defaultSearchState = () => ({
  title: '',
  ingredientTags: [],
  cookingTimeMin: '',
  flavours: [],
  courses: [],
  cuisines: [],
})

// 1. 食谱列表由 Slice 组件管理，页面只维护显示数量
const displayedRecipeCount = ref(0)
const sliceReloadKey = ref(0)
const sliceReady = ref(false)

// 2. 搜索表单对象 (完美的数据结构)
const searchRecipe = ref(defaultSearchState())

const normalizeStringArray = (value) => {
  if (Array.isArray(value)) return value
  if (typeof value === 'string' && value.trim()) return value.split(',').map((v) => v.trim()).filter(Boolean)
  return []
}

const saveSearchState = () => {
  sessionStorage.setItem(SEARCH_STATE_KEY, JSON.stringify(searchRecipe.value))
}

const loadSearchState = () => {
  const raw = sessionStorage.getItem(SEARCH_STATE_KEY)
  if (raw) {
    try {
      const parsed = JSON.parse(raw)
      searchRecipe.value = {
        ...defaultSearchState(),
        ...parsed,
        ingredientTags: normalizeStringArray(parsed.ingredientTags),
        flavours: normalizeStringArray(parsed.flavours),
        courses: normalizeStringArray(parsed.courses),
        cuisines: normalizeStringArray(parsed.cuisines),
      }
      return
    } catch (error) {
      sessionStorage.removeItem(SEARCH_STATE_KEY)
    }
  }

  if (typeof route.query.keyword === 'string' && route.query.keyword.trim()) {
    searchRecipe.value.title = route.query.keyword.trim()
  }
}

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
      { label: 'Breakfast', value: 'BREAKFAST' },
      { label: 'Lunch', value: 'LUNCH' },
      { label: 'Dinner', value: 'DINNER' },
      { label: 'Dessert', value: 'DESSERT' },
      { label: 'Snack', value: 'SNACK' },
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
  searchRecipe.value = defaultSearchState()
  ingredientInputValue.value = ''
  saveSearchState()
  sliceReloadKey.value += 1
}

// 6. 点击搜索按钮
const handleSearch = () => {
  saveSearchState()
  sliceReloadKey.value += 1
}

const fetchRecipePage = async (page, pageSize) => {
  return await getRecipeCardApi(searchRecipe.value, page, pageSize)
}

const handleSliceItemsChange = (items) => {
  displayedRecipeCount.value = items.length
}

// 7. 排序和分页逻辑
const sortBy = ref('default')
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
  loadSearchState()
  getFlavours()
  getCuisines()
  sliceReady.value = true
})

watch(
  searchRecipe,
  () => {
    saveSearchState()
  },
  { deep: true },
)
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
        <h2 class="list-title">Recipes ({{ displayedRecipeCount }})</h2>
        <div class="sort-control">
          <el-select v-model="sortBy" placeholder="Sort By" class="sort-select">
            <el-option label="Default" value="default" />
            <el-option label="Newest" value="time_new" />
            <el-option label="Most Liked" value="likes_desc" />
            <el-option label="Fastest" value="time_asc" />
          </el-select>
        </div>
      </div>
      <RecipeSliceList
        :fetch-page="fetchRecipePage"
        :reload-key="sliceReloadKey"
        :enabled="sliceReady"
        @items-change="handleSliceItemsChange"
      />
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
</style>




