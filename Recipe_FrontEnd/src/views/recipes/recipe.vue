<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Timer, Location, KnifeFork } from '@element-plus/icons-vue'
import { getRecipeApi } from '../../api/recipe'
import { submitRatingApi } from '../../api/recipe'

const route = useRoute()
const router = useRouter()

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

const recipe = ref(null)

const getRecipe = async (id) => {
  const result = await getRecipeApi(id)
  if (result.code) {
    recipe.value = result.data
  }
}

// 🌟 新增：处理用户打分的交互逻辑
const handleRateChange = async (newScore) => {
  const token = localStorage.getItem('loginUser')

  if (!token) {
    // 没登录：温柔地提示用户
    ElMessage.warning('Please log in first then you can rate the recipe.')

    // 🚨 极其关键的 UX 细节：强制熄灭星星！
    // 因为 v-model 是双向绑定的，用户刚才点下去的那一瞬间，星星其实已经亮了。
    // 我们必须手动把它拨回 0，否则视觉上看着像打分成功了一样。
    recipe.value.userScore = 0

    // 带着“记忆”跳转到登录页（登录成功后还能自动跳回这道菜）
    router.push({
      path: '/login',
      query: { redirect: route.fullPath }, // 把当前 URL 作为参数传给登录页
    })

    return // 彻底拦截，绝对不让代码往下执行去发后端请求！
  }

  if (!newScore || !recipe.value) return

  try {
    const res = await submitRatingApi({
      recipeId: recipe.value.id,
      score: newScore,
    })

    // 2. 判断后端是否处理成功 (假设 code 1 是成功，以你后端的实际设定为准)
    if (res.code) {
      ElMessage.success('Thank you for your rating!')

      // 3. 🌟 核心：无感刷新页面数据！
      // 后端返回了新数据，我们直接把页面的变量替换掉
      recipe.value.averageRating = res.data.newAverageRating
      recipe.value.ratingCount = res.data.newRatingCount
    } else {
      ElMessage.error(res.msg || 'Failed to submit rating.')
    }
  } catch (error) {
    ElMessage.error('Network error, please try again.')
  }
}

const getDifficultyColor = (difficulty) => {
  if (!difficulty) return '#999'
  const d = difficulty.toUpperCase()
  if (d === 'HARD') return '#F56C6C'
  if (d === 'MEDIUM') return '#E6A23C'
  if (d === 'EASY') return '#4ea685' // 🌟 把简单难度改成你的品牌绿，更和谐
  return '#409EFF'
}

onMounted(() => {
  const recipeId = route.params.id
  getRecipe(recipeId)
})
</script>
<template>
  <div class="recipe-detail-page" v-loading="!recipe" v-if="recipe">
    <div class="nav-header">
      <el-button link @click="goBack" class="back-btn">
        <el-icon :size="20"><ArrowLeft /></el-icon>
        <span style="margin-left: 5px; font-size: 16px; font-weight: 600">Back to Recipes</span>
      </el-button>
    </div>

    <el-row :gutter="50" class="top-row">
      <el-col :xs="24" :md="10" :lg="9">
        <div class="image-wrapper">
          <el-image :src="recipe.coverImage" fit="cover" class="main-image" />
        </div>
      </el-col>

      <el-col :xs="24" :md="14" :lg="15">
        <div class="info-wrapper">
          <h1 class="title">{{ recipe.title }}</h1>

          <div class="rating-display">
            <el-rate v-model="recipe.averageRating" disabled allow-half text-color="#FF9900" />
            <span class="rating-text">{{
              recipe.averageRating ? recipe.averageRating.toFixed(1) : '0.0'
            }}</span>
            <span class="rating-count">({{ recipe.ratingCount || 0 }} reviews)</span>
          </div>

          <div class="description-box">
            <p class="description">
              {{
                recipe.description || 'This recipe is so delicious that the author left no words.'
              }}
            </p>
          </div>

          <div class="meta-info-container">
            <div class="meta-row primary-stats">
              <div class="stat-item" v-if="recipe.cookingTimeMin">
                <el-icon :size="22" color="#4ea685"><Timer /></el-icon>
                <span class="stat-text">{{ recipe.cookingTimeMin }} mins</span>
              </div>
              <el-divider direction="vertical" />
              <div
                class="stat-item"
                v-if="recipe.difficulty"
                :style="{ color: getDifficultyColor(recipe.difficulty) }"
              >
                <span class="stat-text">{{ recipe.difficulty }}</span>
              </div>
            </div>

            <div
              class="meta-row categories"
              v-if="
                (recipe.cuisines && recipe.cuisines.length) ||
                (recipe.courses && recipe.courses.length)
              "
            >
              <el-tag
                v-for="c in recipe.cuisines"
                :key="c"
                size="large"
                color="#eef7f4"
                class="meta-tag big-tag"
              >
                <el-icon :size="18"><Location /></el-icon> {{ c }}
              </el-tag>
              <el-tag
                v-for="c in recipe.courses"
                :key="c"
                size="large"
                color="#fff3e0"
                class="meta-tag big-tag"
                style="color: #e6a23c; border-color: #fde2e2"
              >
                <el-icon :size="18"><KnifeFork /></el-icon> {{ c }}
              </el-tag>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="50" class="bottom-row">
      <el-col :xs="24" :md="10" :lg="9">
        <div class="ingredients-panel">
          <h2 class="section-title">Ingredients</h2>
          <ul class="ingredient-list">
            <li v-for="(item, index) in recipe.ingredientsList" :key="index">
              <div class="ing-main">
                <span class="ing-name"><span class="dot"></span> {{ item.name }}</span>
                <span class="ing-amount">{{ item.amount }}</span>
              </div>
              <div class="ing-note" v-if="item.note">{{ item.note }}</div>
            </li>
          </ul>
        </div>
      </el-col>

      <el-col :xs="24" :md="14" :lg="15">
        <div class="steps-panel">
          <h2 class="section-title">Instructions</h2>
          <div class="step-item" v-for="(step, index) in recipe.steps" :key="index">
            <div class="step-number">{{ index + 1 }}</div>
            <div class="step-content">
              <div class="step-text">{{ step.content }}</div>
              <div class="step-images" v-if="step.imageUrls && step.imageUrls.length > 0">
                <el-image
                  v-for="(img, imgIndex) in step.imageUrls"
                  :key="imgIndex"
                  :src="img"
                  class="step-img"
                  fit="cover"
                  :preview-src-list="step.imageUrls"
                  :initial-index="imgIndex"
                  hide-on-click-modal
                  preview-teleported="true"
                  lazy
                />
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="full-width-rating-panel">
      <div class="rating-content">
        <h3>Tried this recipe?</h3>
        <p v-if="recipe.userScore > 0" class="echo-text">
          You previously rated this <strong>{{ recipe.userScore }}</strong> stars. Click to change.
        </p>
        <p v-else class="echo-text">We'd love to hear your thoughts! Tap a star to rate.</p>
        <el-rate
          v-model="recipe.userScore"
          :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          size="large"
          clearable
          @change="handleRateChange"
          class="massive-rate"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.recipe-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px 80px 20px;
}

.nav-header {
  margin-bottom: 30px;
}

.back-btn {
  color: #606266;
  transition: all 0.3s;
  padding: 0;
}
.back-btn:hover {
  color: #4ea685;
  transform: translateX(-4px);
}

/* ================= 全新智能行列布局 ================= */
.top-row {
  margin-bottom: 50px; /* 让第一行（照片/介绍）和第二行（食材/步骤）拉开宽敞的距离 */
  align-items: flex-start; /* 顶部对齐，让内容自然往下撑 */
}

.bottom-row {
  align-items: flex-start;
}

/* --- 照片区 (不再写死高度比例) --- */
.image-wrapper {
  width: 100%;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.08);
}
.main-image {
  width: 100%;
  height: auto;
  max-height: 480px; /* 限制最大高度，防止竖长图占满全屏，同时允许横图自然显示 */
  object-fit: cover;
  display: block;
}

/* --- 介绍区 --- */
.info-wrapper {
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 42px;
  font-weight: 900;
  letter-spacing: -1px;
  margin: 0 0 12px 0;
  color: #1a1a1a;
  line-height: 1.2;
}

.rating-display {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}
.rating-text {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}
.rating-count {
  font-size: 15px;
  color: #909399;
}

.description-box {
  margin-bottom: 25px;
}
.description {
  font-size: 17px;
  color: #5c636a;
  line-height: 1.7;
}

.meta-info-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.primary-stats {
  background-color: #f8f9fa;
  border: 1px solid #ebeef5;
  padding: 12px 24px;
  border-radius: 12px;
  display: inline-flex;
  width: fit-content;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 16px;
}
.categories .big-tag {
  font-weight: 600;
  font-size: 15px;
  padding: 18px 16px;
  border: none;
}

/* ================= 底部：食材面板 ================= */
.ingredients-panel {
  background-color: #eef7f4;
  border: 1px solid #d5ebe1;
  padding: 35px 30px;
  border-radius: 20px;
  position: sticky; /* 🌟 因为使用了上下行的布局，这里的 sticky 会在左侧栏里完美吸顶！ */
  top: 90px;
}
.section-title {
  font-size: 24px;
  font-weight: 800;
  color: #2c3e50;
  margin-bottom: 24px;
  padding-bottom: 0;
  border: none;
}
.ingredient-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.ingredient-list li {
  padding: 16px 0;
  border-bottom: 1px solid rgba(78, 166, 133, 0.15);
}
.ingredient-list li:last-child {
  border-bottom: none;
}

/* 🌟 干脆的两端对齐，中间没有任何点 */
.ing-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  width: 100%;
  gap: 20px; /* 名字如果极长，强制给右侧数字留出空间 */
}
.ing-name {
  font-size: 16px;
  font-weight: 600;
  color: #2a5948;
  display: flex;
  align-items: flex-start;
}
.dot {
  width: 8px;
  height: 8px;
  background-color: #4ea685;
  border-radius: 50%;
  margin-right: 12px;
  margin-top: 6px;
  flex-shrink: 0;
}
.ing-amount {
  color: #4ea685;
  font-weight: 800;
  font-size: 16px;
  white-space: nowrap; /* 绝对不换行 */
  flex-shrink: 0; /* 绝对不压缩 */
  text-align: right;
}
.ing-note {
  color: #6b8e7f;
  font-size: 13px;
  margin-top: 8px;
  margin-left: 20px;
}

/* ================= 底部：步骤区 ================= */
.steps-panel {
  background-color: white;
}
.step-item {
  display: flex;
  gap: 24px;
  margin-bottom: 45px;
}
.step-number {
  flex: 0 0 40px;
  height: 40px;
  background-color: #4ea685;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 900;
  box-shadow: 0 4px 10px rgba(78, 166, 133, 0.3);
}
.step-text {
  font-size: 17px;
  line-height: 1.8;
  color: #444;
  margin-bottom: 15px;
}
.step-img {
  width: 220px;
  height: 160px;
  border-radius: 12px;
}

/* ================= 全宽打分板 ================= */
.full-width-rating-panel {
  margin-top: 60px;
  padding: 50px 20px;
  background: linear-gradient(145deg, #ffffff, #f4fbf8);
  border-top: 2px solid #eef7f4;
  border-radius: 20px;
  text-align: center;
}
.full-width-rating-panel h3 {
  font-size: 28px;
  color: #2c3e50;
  margin: 0 0 10px 0;
}
.echo-text {
  color: #606266;
  font-size: 16px;
  margin-bottom: 25px;
}
.echo-text strong {
  color: #ff9900;
  font-size: 20px;
}
:deep(.massive-rate .el-rate__icon) {
  font-size: 42px;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .top-row {
    margin-bottom: 30px;
  }
  .ingredients-panel {
    position: static;
    margin-bottom: 40px;
  }
}
</style>
