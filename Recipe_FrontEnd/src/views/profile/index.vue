<script setup>
import { ref, computed, onMounted } from 'vue'
import { Edit, Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import RecipeCard from '../../component/recipeCard.vue'
import RecipeSliceList from '../../component/RecipeSliceList.vue'

import {
  getMyRecipeApi,
  getSavedRecipeApi,
  getUserInfoApi,
  deleteAvatarApi,
  getPreferenceApi,
  updatePreferenceApi,
} from '../../api/user'

import { getAllCuisinesApi, getAllFlavoursApi, getAllIngredientsApi } from '../../api/recipeCard'

const activeTab = ref('myRecipes')

const userInfo = ref({
  username: '',
  avatarUrl: '',
  bio: '',
  email: '',
  createdCount: null,
  savedCount: null,
})

const router = useRouter()
const myRecipeSliceRef = ref()
const savedRecipeSliceRef = ref()
const myRecipeCount = ref(0)
const myRecipeHasNext = ref(true)

const userInitial = computed(() => {
  return userInfo.value.username ? userInfo.value.username.charAt(0).toUpperCase() : ''
})

// ================= 🌟 新增/大改：全维度口味设置 (Food DNA) =================
// 这些字段将作为参数发送给你的后端 Elasticsearch 推荐引擎
const preferences = ref({
  dietary: [],
  allergies: [],
  skillLevel: '',
  timeAvailability: null,
  flavours: [],
  cuisines: [],
})
const cuisines = ref([])
const flavours = ref([])
const ingredients = ref([])
const getFlavours = async () => {
  const result = await getAllFlavoursApi()
  if (result.code) {
    flavours.value = result.data
  }
}
const getCuisines = async () => {
  const result = await getAllCuisinesApi()
  if (result.code) {
    cuisines.value = result.data
  }
}
const getIngredients = async () => {
  const result = await getAllIngredientsApi()
  if (result.code) {
    ingredients.value = result.data
  }
}
// 供用户选择的选项常量
const dietaryOptions = ['Vegetarian', 'Vegan', 'Gluten-Free', 'Dairy-Free', 'Keto', 'Halal']

// 模拟保存接口
const handleSavePreferences = async () => {
  try {
    const res = await updatePreferenceApi(preferences.value)
    if (res.code) {
      // 假设 1 是成功状态码
      ElMessage.success('Taste preferences saved! Your recommendations have been updated 🪄')
    }
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleResetPreferences = () => {
  // 直接把 preferences 的值覆盖回最干净的初始状态
  preferences.value = {
    dietary: [],
    allergies: [],
    skillLevel: '', // 恢复默认值
    timeAvailability: '', // 恢复默认值
    flavours: [],
    cuisines: [],
    ingredients: [],
  }
}
const fetchUserPreferences = async () => {
  try {
    const res = await getPreferenceApi()
    if (res.code && res.data) {
      preferences.value = { ...preferences.value, ...res.data }
    }
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleAvatarSuccess = (response) => {
  userInfo.value.avatarUrl = response.data
  ElMessage.success('Avatar uploaded successfully!')
}

const beforeAvatarUpload = (rawFile) => {
  const isImage = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png'
  const isLt2M = rawFile.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('Avatar picture must be JPG or PNG format!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('Avatar picture size cannot exceed 2MB!')
    return false
  }
  return true
}

const getUserInfo = async () => {
  const result = await getUserInfoApi()
  if (result.code) {
    userInfo.value = result.data
  }
}
const fetchMyRecipesPage = async (page, pageSize) => {
  return await getMyRecipeApi(page, pageSize)
}

const removeAvatar = async () => {
  userInfo.value.avatarUrl = ''
  await deleteAvatarApi()
}

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('loginUser')
  return token ? { Authorization: JSON.parse(token).Authorization } : {}
})

const fetchSavedRecipesPage = async (page, pageSize) => {
  return await getSavedRecipeApi(page, pageSize)
}

const handleLikeToggled = (recipeId, newStatus) => {
  if (!newStatus) {
    savedRecipeSliceRef.value?.removeItemById(recipeId)
  }
}

const handleTabChange = (tabName) => {
  if (tabName === 'myRecipes') {
    myRecipeHasNext.value = true
    myRecipeSliceRef.value?.refresh()
  } else if (tabName === 'savedRecipes') {
    savedRecipeSliceRef.value?.refresh()
  } else {
    fetchUserPreferences()
    getFlavours()
    getCuisines()
    getIngredients()
  }
}

const goToEditRecipe = (id) => {
  router.push(`/recipe/edit/${id}`)
}

const handleMyRecipesItemsChange = (items) => {
  myRecipeCount.value = items.length
}

const handleMyRecipesStateChange = (state) => {
  myRecipeHasNext.value = Boolean(state?.hasNext)
}

onMounted(() => {
  getUserInfo()
})
</script>

<template>
  <div class="profile-page">
    <div class="profile-header-card">
      <div class="header-left">
        <div class="avatar-wrapper">
          <el-upload
            class="avatar-uploader"
            action="http://localhost:8888/api/users/avatar"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            title="Click to upload a new avatar"
          >
            <el-avatar
              v-if="userInfo.avatarUrl"
              :size="100"
              :src="userInfo.avatarUrl"
              class="avatar"
            />
            <el-avatar v-else :size="100" class="avatar initial-avatar">
              {{ userInitial }}
            </el-avatar>
          </el-upload>

          <el-button
            v-if="userInfo.avatarUrl"
            type="danger"
            circle
            :icon="Delete"
            class="remove-avatar-btn"
            size="small"
            title="Remove Avatar"
            @click.stop="removeAvatar"
          />
        </div>

        <div class="user-info">
          <h1 class="username">{{ userInfo.username }}</h1>
          <p class="bio">{{ userInfo.bio }}</p>
          <div class="user-stats">
            <div class="stat-item">
              <strong>{{ userInfo.createdCount }}</strong> Recipes
            </div>
            <div class="stat-item">
              <strong>{{ userInfo.savedCount }}</strong> Saved
            </div>
          </div>
        </div>
      </div>
      <div class="header-right">
        <el-button round size="large" class="edit-btn">
          <el-icon><Edit /></el-icon>
          <span style="margin-left: 6px">Edit Profile</span>
        </el-button>
      </div>
    </div>

    <div class="profile-content">
      <el-tabs v-model="activeTab" class="modern-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="My Recipes" name="myRecipes">
          <RecipeSliceList
            ref="myRecipeSliceRef"
            :fetch-page="fetchMyRecipesPage"
            :enabled="activeTab === 'myRecipes'"
            :show-empty="false"
            :show-no-more-text="false"
            :xl="6"
            class="profile-recipe-grid"
            @items-change="handleMyRecipesItemsChange"
            @state-change="handleMyRecipesStateChange"
          >
            <template #item="{ recipe, onLikeToggled }">
              <div class="my-recipe-item">
                <RecipeCard :data="recipe" @like-toggled="onLikeToggled" />
                <el-button class="edit-recipe-btn" round @click="goToEditRecipe(recipe.id)">
                  Edit Recipe
                </el-button>
              </div>
            </template>
            <template #after>
              <el-col
                v-if="!myRecipeHasNext"
                :xs="24"
                :sm="12"
                :md="8"
                :lg="6"
                :xl="6"
                class="create-recipe-col"
                :class="{ 'only-create-col': myRecipeCount === 0 }"
              >
                <div class="create-recipe-item">
                  <div class="create-new-card" @click="router.push('/recipe/create')">
                    <el-icon :size="40" color="#4ea685"><Plus /></el-icon>
                    <span>Create New Recipe</span>
                  </div>
                  <el-button class="edit-recipe-btn create-card-spacer" round aria-hidden="true">
                    Edit Recipe
                  </el-button>
                </div>
              </el-col>
            </template>
          </RecipeSliceList>
        </el-tab-pane>

        <el-tab-pane label="Saved Recipes" name="savedRecipes">
          <RecipeSliceList
            ref="savedRecipeSliceRef"
            :fetch-page="fetchSavedRecipesPage"
            :enabled="activeTab === 'savedRecipes'"
            :xl="6"
            class="profile-recipe-grid"
            :load-more-text="'Load More Recipes'"
            :no-more-text="'—— No more saved recipes ——'"
            @like-toggled="handleLikeToggled"
          >
            <template #empty>
              <div class="empty-state-container">
                <el-empty description="You haven't saved any recipes yet 🥺" image-size="200">
                  <el-button type="primary" @click="router.push('/recipe')">
                    Go Explore Recipes
                  </el-button>
                </el-empty>
              </div>
            </template>
          </RecipeSliceList>
        </el-tab-pane>

        <el-tab-pane label="Taste Preferences" name="preferences">
          <div class="preferences-panel">
            <div class="pref-header">
              <h3 class="pref-title">Your Food DNA 🧬</h3>
              <p class="pref-desc">
                Tell us what you love (and what you avoid). Our recommendation engine will tailor
                the perfect recipes just for you.
              </p>
            </div>

            <el-form label-position="top" class="pref-form">
              <el-form-item label="Dietary Restrictions (Optional)">
                <el-checkbox-group v-model="preferences.dietary">
                  <el-checkbox-button v-for="diet in dietaryOptions" :key="diet" :value="diet">
                    {{ diet }}
                  </el-checkbox-button>
                </el-checkbox-group>
              </el-form-item>

              <el-form-item label="Allergies & Dislikes (We will hide these)">
                <el-select
                  v-model="preferences.allergies"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  placeholder="e.g. Peanut, Cilantro, Onion..."
                  size="large"
                  class="modern-select"
                >
                  <el-option v-for="item in ingredients" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>

              <el-form-item label="Cooking Skill Level">
                <el-radio-group v-model="preferences.skillLevel">
                  <el-radio-button value="Beginner">🔪 Beginner</el-radio-button>
                  <el-radio-button value="Intermediate">🍳 Intermediate</el-radio-button>
                  <el-radio-button value="Master">👨‍🍳 Master</el-radio-button>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="Time Availability (Max Cooking Time)">
                <el-radio-group v-model="preferences.timeAvailability">
                  <el-radio-button value="15">⏱️ < 15 mins</el-radio-button>
                  <el-radio-button value="30">⏱️ < 30 mins</el-radio-button>
                  <el-radio-button value="60">⏱️ < 60 mins</el-radio-button>
                  <el-radio-button value="999">🍲 No Limit</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="Favorite Flavours (口味偏好)">
                <el-select
                  v-model="preferences.flavours"
                  multiple
                  filterable
                  placeholder="Search or add flavours (e.g. Spicy, Sweet...)"
                  size="large"
                  class="modern-select"
                >
                  <el-option
                    v-for="flavor in flavours"
                    :key="flavor"
                    :label="flavor"
                    :value="flavor"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="Favorite Cuisines (喜爱菜系)">
                <el-select
                  v-model="preferences.cuisines"
                  multiple
                  filterable
                  placeholder="Select your favorite cuisines..."
                  size="large"
                  class="modern-select"
                >
                  <el-option
                    v-for="cuisine in cuisines"
                    :key="cuisine"
                    :label="cuisine"
                    :value="cuisine"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="Favorite Ingredients (喜爱菜系)">
                <el-select
                  v-model="preferences.ingredients"
                  multiple
                  filterable
                  placeholder="Select your favorite ingredients..."
                  size="large"
                  class="modern-select"
                >
                  <el-option v-for="ing in ingredients" :key="ing" :label="ing" :value="ing" />
                </el-select>
              </el-form-item>

              <div class="form-actions">
                <el-button size="large" class="reset-pref-btn" @click="handleResetPreferences">
                  Reset
                </el-button>
                <el-button
                  color="#4ea685"
                  size="large"
                  class="save-pref-btn"
                  @click="handleSavePreferences"
                >
                  Save My Food DNA
                </el-button>
              </div>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

/* ================= Header Card ================= */
.profile-header-card {
  background: white;
  border-radius: 20px;
  padding: 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f2f5;
  margin-bottom: 40px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 30px;
}

/* ================= Avatar & Remove Button Styles ================= */
.avatar-wrapper {
  position: relative;
  display: inline-block;
}

.avatar-uploader {
  cursor: pointer;
  border-radius: 50%;
  transition: opacity 0.3s ease;
  display: block;
}
.avatar-uploader:hover {
  opacity: 0.8;
  box-shadow: 0 0 10px rgba(78, 166, 133, 0.5);
}
.avatar {
  border: 4px solid #eef7f4;
  font-size: 40px;
}
.initial-avatar {
  background-color: #4ea685;
  color: white;
  font-weight: bold;
}

.remove-avatar-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s;
}
.remove-avatar-btn:hover {
  transform: scale(1.1);
}

/* ================= User Info ================= */
.user-info {
  display: flex;
  flex-direction: column;
}
.username {
  font-size: 32px;
  font-weight: 900;
  color: #2c3e50;
  margin: 0 0 8px 0;
}
.bio {
  font-size: 16px;
  color: #606266;
  margin: 0 0 16px 0;
}
.user-stats {
  display: flex;
  gap: 20px;
}
.stat-item {
  font-size: 15px;
  color: #909399;
}
.stat-item strong {
  color: #2c3e50;
  font-size: 18px;
}

.edit-btn {
  font-weight: bold;
  border-color: #dcdfe6;
}
.edit-btn:hover {
  color: #4ea685;
  border-color: #4ea685;
  background-color: #eef7f4;
}

/* ================= Tabs Styling ================= */
.profile-content {
  background: white;
  border-radius: 20px;
  padding: 20px 40px 40px 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.03);
}

:deep(.modern-tabs .el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #ebeef5;
}
:deep(.modern-tabs .el-tabs__active-bar) {
  background-color: #4ea685;
  height: 3px;
  border-radius: 3px;
}
:deep(.modern-tabs .el-tabs__item) {
  font-size: 18px;
  font-weight: bold;
  color: #909399;
  height: 60px;
  line-height: 60px;
}
:deep(.modern-tabs .el-tabs__item.is-active) {
  color: #2c3e50;
}
:deep(.modern-tabs .el-tabs__item:hover) {
  color: #4ea685;
}

/* ================= Tab 1 & 2: Recipes Grid ================= */
.profile-recipe-grid {
  margin-top: 20px;
}

:deep(.profile-recipe-grid .el-row > .el-col) {
  display: flex;
  margin-bottom: 24px;
}

:deep(.profile-recipe-grid .el-row > .el-col > *) {
  width: 100%;
}

.my-recipe-item {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

:deep(.my-recipe-item .modern-card) {
  flex: 1;
}

.edit-recipe-btn {
  border-color: #d5ebe1;
  color: #4ea685;
  font-weight: 600;
}

.edit-recipe-btn:hover {
  background-color: #eef7f4;
  border-color: #4ea685;
}

.create-recipe-item {
  width: 100%;
  display: flex;
  height: 100%;
  flex-direction: column;
  gap: 10px;
}

.create-recipe-col.only-create-col {
  margin-left: auto;
  margin-right: auto;
}

.create-new-card {
  flex: 1;
  width: 100%;
  height: 100%;
  border: 2px dashed #d5ebe1;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 15px;
  color: #4ea685;
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
  background-color: #fafdfb;
  transition: all 0.3s;
}

.create-new-card:hover {
  background-color: #eef7f4;
  border-color: #4ea685;
  transform: translateY(-4px);
}

.create-card-spacer {
  visibility: hidden;
  pointer-events: none;
}

.empty-state-container {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.pagination-footer {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 30px 0 10px 0;
  margin-top: 10px;
}

.load-more-btn {
  width: 200px;
  font-weight: bold;
  color: #4ea685;
  border-color: #d5ebe1;
}

.load-more-btn:hover {
  background-color: #eef7f4;
  border-color: #4ea685;
}

.no-more-text {
  font-size: 14px;
  color: #c0c4cc;
  margin: 0;
}

/* ================= 🌟 爆改的 Tab 3: Preferences ================= */
.preferences-panel {
  max-width: 700px;
  margin: 20px auto; /* 居中显示，像问卷一样优雅 */
  background: #fafdfb;
  padding: 40px;
  border-radius: 16px;
  border: 1px solid #eef7f4;
}
.pref-header {
  text-align: center;
  margin-bottom: 40px;
}
.pref-title {
  font-size: 26px;
  font-weight: 900;
  color: #2c3e50;
  margin-bottom: 10px;
}
.pref-desc {
  color: #606266;
  font-size: 15px;
  line-height: 1.6;
}

/* Element 表单深度覆盖 */
:deep(.pref-form .el-form-item__label) {
  font-size: 16px;
  font-weight: bold;
  color: #2c3e50;
  padding-bottom: 10px;
}
:deep(.pref-form .el-form-item) {
  margin-bottom: 30px;
}

/* ================= 🌟 高级定制：美化多选下拉框的 Tags ================= */

/* 让下拉框本身看起来更高级、更像一个搜索栏 */

/* 统一个性化按钮风格 */
/* ================= 统一个性化按钮风格 (薄荷绿) ================= */

/* 1. 默认状态下的样式（变成圆角药丸状，去掉难看的连体边框） */
:deep(.el-radio-button__inner),
:deep(.el-checkbox-button__inner) {
  border-radius: 8px !important;
  border: 1px solid #dcdfe6 !important;
  margin-right: 10px;
  margin-bottom: 10px;
  box-shadow: none !important;
  transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
}

/* 2. 🌟 激活/选中状态下的样式（变成主题薄荷绿！） */
:deep(.el-radio-button.is-active .el-radio-button__inner),
:deep(.el-checkbox-button.is-checked .el-checkbox-button__inner) {
  background-color: #eef7f4 !important; /* 浅绿背景 */
  border-color: #4ea685 !important; /* 深绿边框 */
  color: #4ea685 !important; /* 深绿文字 */
  font-weight: bold;
  box-shadow: 0 0 0 1px #4ea685 inset !important; /* 加深一点边框质感 */
}

/* 3. 修复 Element Plus 默认的聚焦阴影干扰 */
:deep(.el-radio-button__original:focus:not(:focus-visible) + .el-radio-button__inner) {
  box-shadow: none !important;
}

:deep(.modern-select .el-select__wrapper),
:deep(.modern-select .el-input__wrapper) {
  box-shadow: 0 0 0 1px #d5ebe1 inset !important;
  transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
}

/* 2. 鼠标悬浮（Hover）：变成标准薄荷绿 */
:deep(.modern-select .el-select__wrapper:hover),
:deep(.modern-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #4ea685 inset !important;
}

/* 3. 点击聚焦（Focus/Active）：变成加粗的薄荷绿，安全感拉满 */
:deep(.modern-select .el-select__wrapper.is-focused),
:deep(.modern-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #4ea685 inset !important;
  background-color: #fafdfb !important; /* 聚焦时给一点极其微弱的绿底色 */
}

.form-actions {
  margin-top: 50px;
  display: flex;
  justify-content: center;
  gap: 20px; /* 🌟 让两个按钮之间有一段舒适的距离 */
}

/* 🌟 Reset 按钮的高级感样式 */
.reset-pref-btn {
  border-radius: 12px;
  padding: 24px 40px;
  font-size: 16px;
  transition: transform 0.2s;
}
.reset-pref-btn:hover {
  transform: translateY(-2px);
  color: #4ea685;
  border-color: #4ea685;
  background-color: #eef7f4;
}

/* 原有的 Save 按钮样式保持不变，但如果你之前没加悬浮动画，可以确保有下面这句 */
.save-pref-btn {
  border-radius: 12px;
  padding: 24px 80px;
  font-size: 18px;
  box-shadow: 0 8px 20px rgba(78, 166, 133, 0.2);
  transition: transform 0.2s;
}
.save-pref-btn:hover {
  transform: translateY(-2px);
}

/* Responsive */
@media (max-width: 768px) {
  .profile-header-card {
    flex-direction: column;
    text-align: center;
    padding: 30px 20px;
  }
  .header-left {
    flex-direction: column;
    gap: 15px;
  }
  .user-stats {
    justify-content: center;
  }
  .header-right {
    margin-top: 25px;
  }
  .profile-content {
    padding: 15px;
  }
  .preferences-panel {
    padding: 20px;
  }
  .create-recipe-col.only-create-col {
    margin-left: 0;
    margin-right: 0;
  }
}
</style>
