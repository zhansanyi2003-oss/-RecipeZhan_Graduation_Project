<script setup>
import { ref } from 'vue'
import { Avatar, Setting } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

import RecipeSwitch from '../../component/recipeSwitch.vue'
const router = useRouter()

// 模拟打开口味偏好设置 (后期可以接一个真实的抽屉或弹窗)
const openPreferences = () => {
  router.push({ path: '/profile', query: { tab: 'preferences' } })
}

// ================== 模拟：按推荐理由分类的数据 ==================

// 1. 应季推荐
const seasonalRecipes = ref([
  {
    id: 201,
    title: 'Spring Asparagus Salad',
    coverImage:
      'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=1200&q=80',
    difficulty: 'EASY',
    cookingTimeMin: 15,
    averageRating: 4.5,
    ratingCount: 890,
    isLiked: false,
  },
  {
    id: 202,
    title: 'Strawberry Shortcake',
    coverImage:
      'https://images.unsplash.com/photo-1464306076886-da185f6a9d05?auto=format&fit=crop&w=1200&q=80',
    difficulty: 'MEDIUM',
    cookingTimeMin: 40,
    averageRating: 4.2,
    ratingCount: 654,
    isLiked: false,
  },
  {
    id: 203,
    title: 'Lemon Garlic Salmon',
    coverImage:
      'https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?auto=format&fit=crop&w=1200&q=80',
    difficulty: 'EASY',
    cookingTimeMin: 20,
    averageRating: 4.7,
    ratingCount: 1023,
    isLiked: false,
  },
  {
    id: 204,
    title: 'Green Pea Soup',
    coverImage:
      'https://images.unsplash.com/photo-1547592166-23ac45744acd?auto=format&fit=crop&w=1200&q=80',
    difficulty: 'EASY',
    cookingTimeMin: 25,
    averageRating: 4.1,
    ratingCount: 432,
    isLiked: false,
  },
])

// 2. 因为你收藏了某道菜
const basedOnSaves = ref([
  {
    id: 301,
    title: 'Creamy Mushroom Pasta',
    coverImage:
      'https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?auto=format&fit=crop&w=1200&q=80',
    difficulty: 'MEDIUM',
    cookingTimeMin: 30,
    averageRating: 4.8,
    ratingCount: 2100,
    isLiked: false,
  },
  {
    id: 302,
    title: 'Garlic Butter Shrimp',
    coverImage:
      'https://images.unsplash.com/photo-1563379091339-03246963d96c?auto=format&fit=crop&w=1200&q=80',
    difficulty: 'EASY',
    cookingTimeMin: 15,
    averageRating: 4.7,
    ratingCount: 1800,
    isLiked: false,
  },
  {
    id: 303,
    title: 'Classic Lasagna',
    coverImage:
      'https://images.unsplash.com/photo-1619895092538-128341789043?auto=format&fit=crop&w=1200&q=80',
    difficulty: 'HARD',
    cookingTimeMin: 90,
    averageRating: 4.9,
    ratingCount: 3200,
    isLiked: false,
  },
  {
    id: 304,
    title: 'Truffle Risotto',
    coverImage:
      'https://images.unsplash.com/photo-1476124369491-e7addf5db371?auto=format&fit=crop&w=1200&q=80',
    difficulty: 'MEDIUM',
    cookingTimeMin: 45,
    averageRating: 4.4,
    ratingCount: 950,
    isLiked: false,
  },
])

// 3. 符合口味偏好的快手菜
const quickRecipes = ref([
  {
    id: 401,
    title: 'Avocado Egg Toast',
    coverImage:
      'https://images.unsplash.com/photo-1525351484163-7529414344d8?auto=format&fit=crop&w=1200&q=80',
    difficulty: 'EASY',
    cookingTimeMin: 5,
    averageRating: 4.9,
    ratingCount: 5000,
    isLiked: false,
  },
  {
    id: 402,
    title: 'Tomato Basil Bruschetta',
    coverImage:
      'https://images.unsplash.com/photo-1572695157366-5e585ab2b69f?auto=format&fit=crop&w=1200&q=80',
    difficulty: 'EASY',
    cookingTimeMin: 10,
    averageRating: 4.6,
    ratingCount: 1200,
    isLiked: false,
  },
  {
    id: 403,
    title: '10-Min Chicken Tacos',
    coverImage:
      'https://images.unsplash.com/photo-1613514785940-daed07799d9b?auto=format&fit=crop&w=1200&q=80',
    difficulty: 'EASY',
    cookingTimeMin: 10,
    averageRating: 4.8,
    ratingCount: 3400,
    isLiked: false,
  },
  {
    id: 404,
    title: 'Fruit Smoothie Bowl',
    coverImage:
      'https://images.unsplash.com/photo-1494597564530-871f2b93ac55?auto=format&fit=crop&w=1200&q=80',
    difficulty: 'EASY',
    cookingTimeMin: 5,
    averageRating: 4.7,
    ratingCount: 2800,
    isLiked: false,
  },
])

const toSliceResult = (list, page, pageSize) => {
  const start = page * pageSize
  const end = start + pageSize
  const content = list.slice(start, end)
  const hasNext = end < list.length
  return {
    code: 1,
    data: {
      content,
      hasNext,
      last: !hasNext,
    },
  }
}

const fetchSeasonalPage = async (page, pageSize) => {
  return toSliceResult(seasonalRecipes.value, page, pageSize)
}

const fetchSavedPage = async (page, pageSize) => {
  return toSliceResult(basedOnSaves.value, page, pageSize)
}

const fetchQuickPage = async (page, pageSize) => {
  return toSliceResult(quickRecipes.value, page, pageSize)
}
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
        <RecipeSwitch
          title="Seasonal Picks"
          subtitle="Fresh ingredients perfect for this Spring"
          title-icon="🌱"
          :fetch-page="fetchSeasonalPage"
          :pool-size="12"
          :batch-size="4"
        />
      </section>

      <section class="recomm-section">
        <RecipeSwitch
          title='Because you liked "Spaghetti"'
          subtitle="Italian cuisine lovers also enjoyed these"
          title-icon="☆"
          :fetch-page="fetchSavedPage"
          :pool-size="12"
          :batch-size="4"
        />
      </section>

      <section class="recomm-section">
        <RecipeSwitch
          title="Quick & Easy"
          subtitle="Since you prefer meals under 30 minutes"
          title-icon="⏱"
          :fetch-page="fetchQuickPage"
          :pool-size="12"
          :batch-size="4"
        />
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

/* ================= 分类卡片区 ================= */
.recomm-section {
  margin-bottom: 50px;
}

.recomm-section :deep(.section-header) {
  margin-top: 0;
  margin-bottom: 16px;
}

.recomm-section :deep(.section-subtitle) {
  margin-top: 2px;
}

/* 响应式调整横幅 */
@media (max-width: 768px) {
  .recomm-page {
    padding: 20px 0 40px 0;
  }

  .container {
    padding: 0 12px;
  }

  .engine-banner {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
    padding: 16px 14px;
    margin-bottom: 28px;
  }

  .banner-left {
    width: 100%;
    gap: 10px;
    flex-direction: column;
    align-items: flex-start;
  }

  .icon-wrapper {
    width: 44px;
    height: 44px;
  }

  .banner-text {
    width: 100%;
  }

  .banner-text h2 {
    margin-bottom: 8px;
    font-size: 1rem;
    line-height: 1.45;
  }

  .banner-text p {
    font-size: 0.9rem;
    line-height: 1.6;
  }

  .banner-right {
    width: 100%;
  }

  .pref-btn {
    width: 100%;
    min-height: 44px;
    justify-content: center;
  }

  .recomm-section :deep(.section-title) {
    font-size: 1.2rem;
  }

  .recomm-section :deep(.section-subtitle) {
    font-size: 0.9rem;
  }

  .recomm-section {
    margin-bottom: 30px;
  }
}
</style>
