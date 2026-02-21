<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  data: {
    type: Object,
    required: true,
  },
})

// 注意：原本代码里的 const cards = ref(...) 是没用的，
// 因为我们直接用了 props.data，所以我把它删了保持代码干净

const convertDifficultyToStars = (diff) => {
  if (diff === 'EASY') return 1
  if (diff === 'MEDIUM') return 2
  else return 3
}

const goToDetail = () => {}
</script>

<template>
  <div class="my-card" @click="goToDetail">
    <div class="image-wrapper">
      <img :src="data.coverImage" alt="food" />
    </div>

    <div class="card-body">
      <h3 class="title">{{ data.title }}</h3>
      <div class="rating">
        <el-rate
          :model-value="convertDifficultyToStars(data.difficulty)"
          disabled
          text-color="#ff9900"
          :max="3"
          :colors="{ 1: '#2E8B57', 2: '#D4AF37', 3: '#F56C6C' }"
        />
        <span class="difficulty-text">{{ data.difficulty }}</span>
      </div>
    </div>
    <div class="card-footer">
      <div class="footer-item">
        <span>⏰</span>
        <span> {{ data.cookingTimeMin }} min</span>
      </div>
      <div class="footer-item">
        <span>❤️</span><span>{{ data.likesCount || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.my-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
  /* margin-bottom由父组件控制更合理，或者保留也行 */
  margin-bottom: 20px;
  cursor: pointer;

  /* 确保卡片本身宽度跟随父容器 */
  width: 100%;
  display: flex;
  flex-direction: column;
}

.my-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.image-wrapper {
  width: 100%;

  aspect-ratio: 16 / 9;
  max-height: 220px;
  overflow: hidden;
}

.image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 保证图片填满且不变形 */
  display: block; /* 消除图片底部可能的微小缝隙 */
}

/* 下面的样式保持不变 */
.card-body {
  padding: 12px;
  text-align: left;
}

.title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 0 0 8px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-footer {
  background-color: #5d198f;
  color: white;
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  margin-top: auto; /* 确保footer始终沉底 */
}

.footer-item {
  display: flex;
  align-items: center;
  gap: 5px;
}
</style>
