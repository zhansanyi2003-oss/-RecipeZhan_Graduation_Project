<script setup>
import { defineProps, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
// 🌟 记得引入 Element Plus 的图标
import { Star, StarFilled, Timer } from '@element-plus/icons-vue'
import { sumbmitSaveRecipeApi } from '../api/user'
import { ElMessage } from 'element-plus'

const props = defineProps({
  data: {
    type: Object,
    required: true,
  },
})

const router = useRouter()

const goToDetail = () => {
  router.push(`/recipe/${props.data.id}`)
}

// 🌟 本地状态：控制右上角是否已收藏/点赞
// 如果后端传来了 data.isLiked，可以直接用 props.data.isLiked 初始化
const isLiked = ref(props.data.isLiked || false)
const emit = defineEmits(['like-toggled'])
watch(
  () => props.data.isLiked,
  (newVal) => {
    isLiked.value = newVal
  },
)

const toggleLike = async () => {
  isLiked.value = !isLiked.value

  try {
    await sumbmitSaveRecipeApi(props.data.id, isLiked.value)
    emit('like-toggled', props.data.id, isLiked.value)
  } catch (error) {
    isLiked.value = !isLiked.value
    ElMessage.error('Network error, failed to save recipe.')
  }
}

// 根据难度返回不同的颜色主题
const getDifficultyType = (diff) => {
  if (!diff) return 'info'
  const d = diff.toUpperCase()
  if (d === 'HARD') return 'danger'
  if (d === 'MEDIUM') return 'warning'
  if (d === 'EASY') return 'success'
  return 'primary'
}
</script>

<template>
  <div class="modern-card" @click="goToDetail">
    <div class="image-wrapper">
      <img :src="data.coverImage" alt="food" class="cover-img" />

      <div class="floating-like-btn" @click.stop="toggleLike">
        <el-icon :class="{ 'is-liked': isLiked }">
          <component :is="isLiked ? StarFilled : Star" />
        </el-icon>
      </div>
    </div>

    <div class="card-body">
      <h3 class="title" :title="data.title">{{ data.title }}</h3>
      <div class="meta-row">
        <div class="meta-item">
          <el-icon><Timer /></el-icon>
          <span>{{ data.cookingTimeMin }} mins</span>
        </div>

        <el-tag
          :type="getDifficultyType(data.difficulty)"
          size="small"
          effect="plain"
          round
          class="diff-tag"
        >
          {{ data.difficulty }}
        </el-tag>
      </div>

      <div class="rating-row">
        <template v-if="data.ratingCount > 0">
          <el-rate
            :model-value="data.averageRating"
            disabled
            allow-half
            text-color="#ff9900"
            class="card-rate"
          />
          <span class="score-text">{{
            data.averageRating ? data.averageRating.toFixed(1) : '0.0'
          }}</span>
          <span class="review-count">({{ data.ratingCount }})</span>
        </template>

        <template v-else>
          <el-tag type="success" effect="light" round size="small" class="new-badge">
            ✨ New Recipe
          </el-tag>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ================= 整个卡片容器 ================= */
.modern-card {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);

  /* 1. 【修改这里】：注释或删除 margin-bottom */
  /* margin-bottom: 20px; 因为父组件 el-col 已经加了下边距，这里再加会导致卡片和新增按钮底部对不齐 */

  cursor: pointer;
  width: 100%;

  /* 2. 【新增这里】：确保卡片能占满父容器的高度 */
  height: 100%;

  display: flex;
  flex-direction: column;
  border: 1px solid #f0f2f5;
}
.modern-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

/* ================= 顶部图片区 ================= */
.image-wrapper {
  position: relative; /* 为悬浮按钮提供基准 */
  width: 100%;
  aspect-ratio: 4 / 3; /* 大厂最爱的图片比例，比 16:9 更能展示食物细节 */
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.5s ease;
}

.modern-card:hover .cover-img {
  transform: scale(1.05); /* 悬停时图片微微放大，极其吸睛 */
}

/* 🌟 右上角毛玻璃悬浮按钮 */
.floating-like-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(8px); /* 苹果风毛玻璃 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
  transition: all 0.2s ease;
}

.floating-like-btn:hover {
  background-color: rgba(255, 255, 255, 0.95);
  transform: scale(1.1);
}

.floating-like-btn .el-icon {
  font-size: 18px;
  color: #909399; /* 默认灰色星星 */
  transition: color 0.3s ease;
}

/* 收藏后的变色效果 */
.floating-like-btn .is-liked {
  color: #f56c6c; /* 可以换成你的主题绿 #4ea685，但我这里用红色强调心动感 */
}

/* ================= 底部内容区 ================= */
.card-body {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.title {
  font-size: 18px;
  font-weight: 800;
  color: #2c3e50;
  margin: 0 0 12px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}

/* 元数据：时间和难度 */
.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
  font-weight: 600;
}

.diff-tag {
  font-weight: bold;
  border: none; /* 去掉 tag 边框更干净 */
}

/* ================= 评分区 ================= */
.rating-row {
  display: flex;
  align-items: center;
  margin-top: auto; /* 把评分强制推到卡片最底部对齐 */
  gap: 6px;
}

/* 稍微缩小卡片上的星星，防止太占地方 */
:deep(.card-rate .el-rate__icon) {
  font-size: 16px;
  margin-right: 2px;
}

.score-text {
  font-size: 14px;
  font-weight: 800;
  color: #333;
  margin-left: 2px;
}

.review-count {
  font-size: 12px;
  color: #999;
}

/* 无人评分时的 New 标签 */
.new-badge {
  font-weight: 700;
  color: #4ea685;
  background-color: #eef7f4;
  border-color: #d5ebe1;
}

@media (max-width: 768px) {
  .card-body {
    padding: 14px 14px;
  }

  .title {
    font-size: 16px;
  }

  .meta-row {
    gap: 8px;
    margin-bottom: 10px;
  }

  .floating-like-btn {
    width: 44px;
    height: 44px;
  }
}
</style>
