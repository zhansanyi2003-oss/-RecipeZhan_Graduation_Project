<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

// 同样，记得引入你的食谱卡片组件！
import RecipeCard from '../../component/recipeCard.vue'

const router = useRouter()

// 模拟打开口味偏好设置 (后期可以接一个真实的抽屉或弹窗)
const openPreferences = () => {
  ElMessage({
    message: 'Opening preferences setting panel...',
    type: 'success',
    icon: Setting,
  })
}

// ================== 模拟：按推荐理由分类的数据 ==================

// 1. 应季推荐
const seasonalRecipes = ref([
  {
    id: 201,
    title: 'Spring Asparagus Salad',
    difficulty: 'EASY',
    time: '15 min',
    likes: 890,
    image: '...',
  },
  {
    id: 202,
    title: 'Strawberry Shortcake',
    difficulty: 'MEDIUM',
    time: '40 min',
    likes: 654,
    image: '...',
  },
  {
    id: 203,
    title: 'Lemon Garlic Salmon',
    difficulty: 'EASY',
    time: '20 min',
    likes: 1023,
    image: '...',
  },
  {
    id: 204,
    title: 'Green Pea Soup',
    difficulty: 'EASY',
    time: '25 min',
    likes: 432,
    image: '...',
  },
])

// 2. 因为你收藏了某道菜
const basedOnSaves = ref([
  {
    id: 301,
    title: 'Creamy Mushroom Pasta',
    difficulty: 'MEDIUM',
    time: '30 min',
    likes: 2100,
    image: '...',
  },
  {
    id: 302,
    title: 'Garlic Butter Shrimp',
    difficulty: 'EASY',
    time: '15 min',
    likes: 1800,
    image: '...',
  },
  {
    id: 303,
    title: 'Classic Lasagna',
    difficulty: 'HARD',
    time: '90 min',
    likes: 3200,
    image: '...',
  },
  {
    id: 304,
    title: 'Truffle Risotto',
    difficulty: 'MEDIUM',
    time: '45 min',
    likes: 950,
    image: '...',
  },
])

// 3. 符合口味偏好的快手菜
const quickRecipes = ref([
  {
    id: 401,
    title: 'Avocado Egg Toast',
    difficulty: 'EASY',
    time: '5 min',
    likes: 5000,
    image: '...',
  },
  {
    id: 402,
    title: 'Tomato Basil Bruschetta',
    difficulty: 'EASY',
    time: '10 min',
    likes: 1200,
    image: '...',
  },
  {
    id: 403,
    title: '10-Min Chicken Tacos',
    difficulty: 'EASY',
    time: '10 min',
    likes: 3400,
    image: '...',
  },
  {
    id: 404,
    title: 'Fruit Smoothie Bowl',
    difficulty: 'EASY',
    time: '5 min',
    likes: 2800,
    image: '...',
  },
])
</script>

<template>
  <div class="recomm-page">
    <div class="container">
      <div class="engine-banner">
        <div class="banner-left">
          <div class="icon-wrapper">
            <el-icon :size="28"><Avatar /></el-icon>
          </div>
          <div class="banner-text">
            <h2>How our recommendation engine works?</h2>
            <p>
              We handpicked these recipes based on your <strong>saved items</strong>,
              <strong>seasonal ingredients</strong>, and your dietary <strong>preferences</strong>.
            </p>
          </div>
        </div>
        <div class="banner-right">
          <el-button color="#4ea685" class="pref-btn" round @click="openPreferences">
            <el-icon><Setting /></el-icon>

            Edit Preferences
          </el-button>
        </div>
      </div>

      <section class="recomm-section">
        <div class="section-header">
          <div class="title-group">
            <h2>
              <el-icon class="title-icon"><Bowl /></el-icon> Seasonal Picks
            </h2>
            <span class="subtitle">Fresh ingredients perfect for this Spring</span>
          </div>
        </div>
        <el-row :gutter="24">
          <el-col
            v-for="recipe in seasonalRecipes"
            :key="recipe.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <RecipeCard :data="recipe" />
          </el-col>
        </el-row>
      </section>

      <section class="recomm-section">
        <div class="section-header">
          <div class="title-group">
            <h2>
              <el-icon class="title-icon"><Star /></el-icon> Because you liked "Spaghetti"
            </h2>
            <span class="subtitle">Italian cuisine lovers also enjoyed these</span>
          </div>
        </div>
        <el-row :gutter="24">
          <el-col v-for="recipe in basedOnSaves" :key="recipe.id" :xs="24" :sm="12" :md="8" :lg="6">
            <RecipeCard :data="recipe" />
          </el-col>
        </el-row>
      </section>

      <section class="recomm-section">
        <div class="section-header">
          <div class="title-group">
            <h2>
              <el-icon class="title-icon"><Clock /></el-icon> Quick & Easy
            </h2>
            <span class="subtitle">Since you prefer meals under 30 minutes</span>
          </div>
        </div>
        <el-row :gutter="24">
          <el-col v-for="recipe in quickRecipes" :key="recipe.id" :xs="24" :sm="12" :md="8" :lg="6">
            <RecipeCard :data="recipe" />
          </el-col>
        </el-row>
      </section>
    </div>
  </div>
</template>

<style scoped>
.recomm-page {
  width: 100%;
  background-color: #f9fafb; /* 保持全站统一的浅灰底色 */
  min-height: calc(100vh - 64px); /* 减去顶部导航栏高度 */
  padding: 40px 0 80px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* ================= 引擎解析横幅 ================= */
.engine-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  /* 使用你最喜欢的极浅薄荷绿做背景 */
  background-color: #eef7f4;
  border: 1px solid #d5ebe1;
  border-radius: 16px;
  padding: 24px 32px;
  margin-bottom: 50px;
  box-shadow: 0 4px 12px rgba(78, 166, 133, 0.05);
}

.banner-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.icon-wrapper {
  background-color: #4ea685;
  color: white;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(78, 166, 133, 0.3);
}

.banner-text h2 {
  margin: 0 0 6px 0;
  font-size: 1.4rem;
  color: #2a5948; /* 深墨绿色，稳重 */
}

.banner-text p {
  margin: 0;
  color: #57b894;
  font-size: 1rem;
}
.banner-text strong {
  color: #4ea685;
}

.pref-btn {
  font-weight: bold;
  padding: 12px 24px;
  box-shadow: 0 4px 10px rgba(78, 166, 133, 0.2);
  transition: transform 0.2s;
}
.pref-btn:hover {
  transform: translateY(-2px);
}
.mr-1 {
  margin-right: 6px;
}

/* ================= 分类卡片区 ================= */
.recomm-section {
  margin-bottom: 50px;
}

.section-header {
  margin-bottom: 24px;
  border-bottom: 2px solid #eef7f4;
  padding-bottom: 12px;
}

.title-group h2 {
  margin: 0;
  font-size: 1.6rem;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  color: #4ea685;
}

.subtitle {
  display: block;
  margin-top: 6px;
  color: #909399;
  font-size: 1rem;
}

/* 响应式调整横幅 */
@media (max-width: 768px) {
  .engine-banner {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }
}
</style>
