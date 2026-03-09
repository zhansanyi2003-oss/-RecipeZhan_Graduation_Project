<script setup>
import { ref, computed, onMounted } from 'vue'
import { Edit, Plus, Delete } from '@element-plus/icons-vue' // 引入 Delete 图标
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import RecipeCard from '../../component/recipeCard.vue'

import { getMyRecipeApi, getSavedRecipeApi, getUserInfoApi, deleteAvatarApi } from '../../api/user'
// Active Tab
const activeTab = ref('myRecipes')

// Mock user information
const userInfo = ref({
  username: '',
  avatarUrl: '', // 默认给个头像测试删除功能
  bio: '',
  email: '',
  createdCount: null,
  savedCount: null,
})

// Mock preferences
const preferences = ref({
  spiceLevel: 'Medium',
  dietary: ['Vegetarian', 'No Dairy'],
  favoriteCuisines: ['Italian', 'Chinese'],
})

const myRecipeList = ref([])
const router = useRouter()
// Get the first letter of the username
const userInitial = computed(() => {
  return userInfo.value.username.charAt(0).toUpperCase()
})

// Avatar Upload Handlers
const handleAvatarSuccess = (response) => {
  userInfo.value.avatarUrl = response.data
  ElMessage.success('Avatar uploaded successfully!')
}

const beforeAvatarUpload = (rawFile) => {
  const isImage = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png'
  const isLt2M = rawFile.size / 1024 / 1024 < 2 // Max 2MB

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
const getMyrecipe = async () => {
  const result = await getMyRecipeApi()
  if (result.code) {
    myRecipeList.value = result.data
  }
}
// ================= 新增：移除头像的方法 =================
const removeAvatar = async () => {
  // 1. 清空前端显示的头像 URL
  userInfo.value.avatarUrl = ''
  await deleteAvatarApi()
}

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('loginUser')
  return {
    Authorization: JSON.parse(token).Authorization,
  }
})

const savedRecipeList = ref([])
const savedPage = ref(1)
const savedPageSize = ref(12)
const hasMoreSaved = ref(true) // 是否还有下一页 (根据后端 Slice 的 last 属性判断)
const loadingSaved = ref(false) // 防止重复点击加载

// 🌟 2. 获取收藏食谱的方法
const fetchSavedRecipes = async () => {
  if (loadingSaved.value || !hasMoreSaved.value) return

  loadingSaved.value = true
  try {
    // 👇 导师模拟的假响应逻辑，真实对接时请删除这段模拟，用上面的 axios 👇
    const result = await getSavedRecipeApi(savedPage, savedPageSize)
    if (result.code) {
      const sliceData = result.data
      if (sliceData && sliceData.content) {
        savedRecipeList.value.push(...sliceData.content)
      }

      // 如果后端的 slice 表示这是最后一页 (last = true)，就把 hasMoreSaved 设为 false
      hasMoreSaved.value = !sliceData.last

      // 页码 +1，为下次点击 Load More 做准备
      savedPage.value++
    }
  } catch (error) {
  } finally {
    loadingSaved.value = false
  }
}
// 处理子组件传来的状态改变
const handleLikeToggled = (recipeId, newStatus) => {
  // 如果当前在 Saved 页面，并且用户点击了“取消收藏” (newStatus === false)
  if (!newStatus) {
    // 直接用 filter 把这个食谱从前端数组里“剔除”掉，页面会瞬间丝滑消失，不需要刷新！
    savedRecipeList.value = savedRecipeList.value.filter((recipe) => recipe.id !== recipeId)
  }
}
// 当用户切换 Tab 时触发
const handleTabChange = (tabName) => {
  if (tabName === 'myRecipes') {
    // 重新获取我创建的食谱
    getMyrecipe()
  } else if (tabName === 'savedRecipes') {
    // 🌟 重点：重置分页状态，重新获取第一页的收藏！
    savedRecipeList.value = [] // 先清空旧数据
    savedPage.value = 1 // 重置到第 1 页
    hasMoreSaved.value = true // 恢复可加载状态
    fetchSavedRecipes() // 重新调用你的获取收藏 API
  }
}

onMounted(() => {
  getMyrecipe()
  fetchSavedRecipes()
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
          <el-row :gutter="24" class="recipe-grid">
            <template v-if="myRecipeList.length > 0">
              <el-col
                v-for="recipe in myRecipeList"
                :key="recipe.id"
                :xs="24"
                :sm="12"
                :md="8"
                :lg="6"
              >
                <RecipeCard :data="recipe" @like-toggled="handleLikeToggled" />
              </el-col>
            </template>

            <el-col :xs="24" :sm="12" :md="8" :lg="6">
              <div class="create-new-card" @click="router.push('/recipe/create')">
                <el-icon :size="40" color="#4ea685"><Plus /></el-icon>
                <span>Create New Recipe</span>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="Saved Recipes" name="savedRecipes">
          <div class="saved-recipes-wrapper" v-loading="loadingSaved && savedPage === 1">
            <el-row :gutter="24" class="recipe-grid">
              <template v-if="savedRecipeList.length > 0">
                <el-col
                  v-for="recipe in savedRecipeList"
                  :key="recipe.id"
                  :xs="24"
                  :sm="12"
                  :md="8"
                  :lg="6"
                >
                  <RecipeCard :data="recipe" @like-toggled="handleLikeToggled" />
                </el-col>
              </template>

              <div v-else-if="!loadingSaved" class="empty-state-container">
                <el-empty description="You haven't saved any recipes yet 🥺" image-size="200">
                  <el-button type="primary" @click="router.push('/recipes')">
                    Go Explore Recipes
                  </el-button>
                </el-empty>
              </div>
            </el-row>

            <div v-if="savedRecipeList.length > 0" class="pagination-footer">
              <el-button
                v-if="hasMoreSaved"
                @click="fetchSavedRecipes"
                :loading="loadingSaved"
                round
                class="load-more-btn"
              >
                Load More Recipes
              </el-button>

              <p v-else class="no-more-text">—— No more saved recipes ——</p>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="Taste Preferences" name="preferences">
          <div class="preferences-panel">
            <h3 class="pref-title">Your Food DNA</h3>
            <p class="pref-desc">
              Tell us what you like, and we'll recommend the best recipes for you.
            </p>

            <div class="pref-section">
              <h4>Spice Level</h4>
              <el-radio-group v-model="preferences.spiceLevel" size="large">
                <el-radio-button label="None" />
                <el-radio-button label="Mild" />
                <el-radio-button label="Medium" />
                <el-radio-button label="Spicy🔥" />
              </el-radio-group>
            </div>

            <el-button type="primary" size="large" class="save-pref-btn">
              Save Preferences
            </el-button>
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
  position: relative; /* 为删除按钮提供绝对定位的参照物 */
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

/* 删除按钮样式 */
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

/* ================= Tab 1: Create Recipe ================= */
/* ================= Tab 1: Create Recipe (Flexbox 高度自动对齐) ================= */
.recipe-grid {
  margin-top: 20px;
  display: flex; /* 开启 Flex 布局 */
  flex-wrap: wrap; /* 允许自动换行 */
  align-items: stretch; /* 【核心！】让同一行内的所有元素拉伸到相同高度 */
}

/* 让 Element Plus 的列也变成 Flex 容器，这样里面的卡片才能撑满高度 */
.recipe-grid > .el-col {
  display: flex;
  margin-bottom: 24px; /* 把卡片的下边距移到外层列上，排版更规整 */
}

.create-new-card {
  flex: 1; /* 【核心！】让新建按钮卡片占满整个 el-col 的高度 */
  width: 100%;
  /* 删除了之前的 height: 100% 和 min-height: 280px，由外层 flex 自动控制 */
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
  /* margin-bottom 移除了，因为上面的 .el-col 已经加了 */
}

.create-new-card:hover {
  background-color: #eef7f4;
  border-color: #4ea685;
  transform: translateY(-4px);
}

/* ================= Tab 2: Saved ================= */
.empty-state-container {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

/* ================= 分页加载区域 ================= */
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

.empty-state-container {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

/* ================= Tab 3: Preferences ================= */
.preferences-panel {
  max-width: 600px;
  margin-top: 20px;
}
.pref-title {
  font-size: 24px;
  color: #2c3e50;
  margin-bottom: 8px;
}
.pref-desc {
  color: #909399;
  margin-bottom: 30px;
}
.pref-section {
  margin-bottom: 30px;
}
.pref-section h4 {
  font-size: 16px;
  color: #2c3e50;
  margin-bottom: 15px;
}
.save-pref-btn {
  background-color: #4ea685;
  border: none;
  border-radius: 8px;
  padding: 0 40px;
  font-weight: bold;
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
}
</style>
