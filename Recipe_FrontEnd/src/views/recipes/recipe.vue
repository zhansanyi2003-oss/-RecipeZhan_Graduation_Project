<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getRecipeApi } from '../../api/recipe'
// import { getRecipeDetailApi } from '@/api/recipe' // 记得去写你的 API 请求函数！

const route = useRoute()
const router = useRouter()

const goBack = () => {
  if (window.history.length > 1) {
    router.back() // 有历史记录，就返回
  } else {
    router.push('/') // 没有历史记录（比如新标签页打开），就强制回首页
  }
}

const recipe = ref(null)

const getRecipe = async (id) => {
  const result = await getRecipeApi(id)
  if (result.code) {
    recipe.value = result.data
  }
}
const getDifficultyColor = (difficulty) => {
  if (!difficulty) return '#999'
  const d = difficulty.toUpperCase()
  if (d === 'HARD') return '#F56C6C' // 红色
  if (d === 'MEDIUM') return '#E6A23C' // 橙色
  if (d === 'EASY') return '#67C23A' // 绿色
  return '#409EFF' // 默认蓝色
}

onMounted(() => {
  // 从路由的 URL 里拿出那个数字 ID (比如 /recipe/5，拿到的就是 5)
  const recipeId = route.params.id
  getRecipe(recipeId)
})
</script>
<template>
  <div class="recipe-detail-page" v-loading="!recipe" v-if="recipe">
    <div class="nav-header">
      <el-button link @click="goBack" class="back-btn">
        <el-icon :size="20"><ArrowLeft /></el-icon>
        <span style="margin-left: 5px; font-size: 16px">Back to Recipes</span>
      </el-button>
    </div>
    <div class="top-section">
      <div class="image-wrapper">
        <el-image :src="recipe.coverImage" fit="cover" class="main-image" />
      </div>

      <div class="info-wrapper">
        <h1 class="title">{{ recipe.title }}</h1>

        <div class="description-box">
          <p class="description">
            {{ recipe.description || 'This recipe is so delicious that the author left no words.' }}
          </p>
        </div>

        <div class="meta-info-container">
          <div class="meta-row primary-stats">
            <div class="stat-item" v-if="recipe.cookingTimeMin">
              <el-icon :size="22" color="#666"><Timer /></el-icon>
              <span class="stat-text">{{ recipe.cookingTimeMin }} mins</span>
            </div>

            <el-divider direction="vertical" />

            <div
              class="stat-item"
              v-if="recipe.difficulty"
              :style="{ color: getDifficultyColor(recipe.difficulty) }"
            >
              <span class="stat-text">
                {{ recipe.difficulty }}
              </span>
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
              type="success"
              effect="plain"
              round
              class="meta-tag big-tag"
            >
              <el-icon :size="18"><Location /></el-icon> {{ c }}
            </el-tag>

            <el-tag
              v-for="c in recipe.courses"
              :key="c"
              size="large"
              type="warning"
              effect="plain"
              round
              class="meta-tag big-tag"
            >
              <el-icon :size="18"><KnifeFork /></el-icon> {{ c }}
            </el-tag>
          </div>

          <div class="meta-row flavours" v-if="recipe.flavours && recipe.flavours.length">
            <span class="flavour-label">Flavours:</span>
            <el-tag
              v-for="tag in recipe.flavours"
              :key="tag"
              type="info"
              effect="light"
              size="default"
              class="flavour-tag"
            >
              #{{ tag }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <div class="bottom-section">
      <el-row :gutter="40">
        <el-col :xs="24" :md="8">
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

        <el-col :xs="24" :md="16">
          <div class="steps-panel">
            <h2 class="section-title">Steps</h2>

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
    </div>
  </div>
</template>

<style scoped>
.recipe-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.nav-header {
  margin-bottom: 20px; /* 给下方留出呼吸空间 */
}

.back-btn {
  color: #666; /* 默认灰色 */
  transition: color 0.3s;
  padding: 0; /* 去掉按钮默认内边距，让它靠左对齐 */
}

.back-btn:hover {
  color: #5d007d; /* 悬停时变成你的主题紫 */
}

/* 顶部样式 */
.top-section {
  display: flex;
  gap: 40px;
  margin-bottom: 50px;
  height: 400px; /* 🌟 核心：给顶部定一个高度，方便 flex 布局撑开 */
}

.image-wrapper {
  flex: 0 0 500px; /* 图片宽一点更好看 */
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);
  height: 100%;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-start; /* 🌟 改动1：让内容顶端对齐，标题就会上去 */
  padding: 10px 0; /*稍微给点内边距*/
}

.title {
  font-size: 42px; /* 标题加大 */
  margin: 0 0 20px 0; /* 上边距为0，紧贴顶部 */
  color: #2c3e50;
  line-height: 1.2;
}

/* 🌟 改动2：描述区域占据剩余空间 */
.description-box {
  flex: 1; /* 占据中间所有空地 */
  display: flex;
  flex-direction: column;
  justify-content: flex-start; /* 描述文字从上开始排 */
}

.description {
  font-size: 18px; /* 字体加大，更易读 */
  color: #555;
  line-height: 1.8;
  max-width: 90%; /* 防止文字太宽 */
}

/* 底部元数据容器 */
.meta-info-container {
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-top: auto; /* 🌟 如果描述文字很少，这行代码会把标签强制推到最底部 */
}

.meta-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

/* 核心指标样式 */
.primary-stats {
  background-color: #f8f9fa;
  padding: 12px 20px;
  border-radius: 10px;
  display: inline-flex;
  width: fit-content;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 18px; /* 数字加大 */
  color: #555;
}

/* 🌟 改动3：大标签样式微调 */
.categories .big-tag {
  font-weight: 600;
  font-size: 16px; /* 文字加大 */
  padding: 20px 15px; /* 内边距加大，看起来更胖更点击友好 */
  display: inline-flex;
  align-items: center;
  gap: 6px; /* 图标和文字的间距 */
}

/* 这里的样式是为了修正 element plus 图标在大标签里的位置 */
.categories .el-icon {
  vertical-align: middle;
}

.flavours {
  margin-top: 5px;
}

.flavour-label {
  font-size: 16px;
  color: #888;
  font-weight: bold;
  margin-right: 8px;
}

.flavour-tag {
  font-size: 14px;
  padding: 18px 12px; /* 风味标签也稍微大一点 */
}

/* 底部区域通用标题 */
.section-title {
  font-size: 24px;
  border-bottom: 2px solid #eee;
  padding-bottom: 10px;
  margin-bottom: 20px;
}

/* 🌟 食材列表 (新增左右对齐排版) */
.ingredients-panel {
  background-color: #f9f9f9;
  padding: 30px;
  border-radius: 12px;
}
.ingredient-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.ingredient-list li {
  padding: 15px 0;
  border-bottom: 1px dashed #ddd;
  display: flex;
  flex-direction: column; /* 让备注换行显示 */
}
.ing-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start; /* 🌟 改成顶端对齐，这样如果左边换行了两行，右边依然在最上面好看 */
  width: 100%;
  gap: 15px; /* 🌟 给左右强制留出 15px 的安全距离，死也不能贴在一起 */
}

.ing-name {
  font-size: 16px;
  font-weight: bold;
  color: #444;
  display: flex;
  align-items: flex-start; /* 配合顶端对齐 */
  flex: 1; /* 🌟 让左边占据所有剩余空间 */
  word-break: break-word; /* 🌟 如果有一个极其长的英文单词，强制截断换行，不破坏布局 */
}

.ing-amount {
  font-size: 16px;
  color: #e64a19;
  font-weight: bold;
  white-space: nowrap; /* 🚨 核心防御 1：绝对不允许换行！(保住 400 g 永远在一行) */
  flex-shrink: 0; /* 🚨 核心防御 2：空间再挤，也绝对不允许被压缩！ */
  text-align: right;
}
.ing-note {
  font-size: 13px;
  color: #999;
  margin-top: 6px;
  margin-left: 20px; /* 和点点对齐 */
}
.dot {
  width: 8px;
  height: 8px;
  background-color: #ff7043;
  border-radius: 50%;
  margin-right: 12px;
}

/* 步骤列表 */
.steps-panel {
  padding: 10px;
}
.step-item {
  display: flex;
  gap: 20px;
  margin-bottom: 40px;
}
.step-number {
  flex: 0 0 40px;
  height: 40px;
  background-color: #5d007d; /* 你的主题紫 */
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
}
.step-content {
  flex: 1;
}
.step-text {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 15px;
}

/* 图片容器 */
.step-images {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}
.step-img {
  width: 200px;
  height: 150px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s;
}
.step-img:hover {
  transform: scale(1.03);
}

/* 手机端适配 */
@media (max-width: 768px) {
  .top-section {
    flex-direction: column;
  }
  .image-wrapper {
    flex: auto;
  }
  .step-img {
    width: 100%;
    height: auto;
    aspect-ratio: 4 / 3;
  }
}
</style>
