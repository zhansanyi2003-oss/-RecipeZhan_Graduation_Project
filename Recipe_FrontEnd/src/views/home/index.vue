<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ArrowRight } from '@element-plus/icons-vue'
// 假设你之前写好的食谱卡片组件在这里引入

import { getCarouselCardApi } from '../../api/recipeCard'
import RecipeCard from '../../component/recipeCard.vue'

const router = useRouter()
const searchKeyword = ref('')

// 1. 模拟：每日推荐数据 (大图走马灯用)
const recommendedRecipes = ref([])

// 2. 模拟：最新发布的普通食谱 (可以复用你的 RecipeCard)
// 这里随便塞几条假数据占位，后期接后端接口
const trendingRecipes = ref([
  {
    id: 101,
    title: 'Spaghetti',
    coverImage:
      'https://images.unsplash.com/photo-1521389508051-d7ffb5dc8e5c?auto=format&fit=crop&w=1200&q=80',
    difficulty: 'EASY',
    cookingTimeMin: 20,
    averageRating: 4.3,
    ratingCount: 120,
    isLiked: false,
  },
  {
    id: 102,
    title: 'Black Pepper Steak',
    coverImage:
      'https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=1200&q=80',
    difficulty: 'MEDIUM',
    cookingTimeMin: 25,
    averageRating: 4.6,
    ratingCount: 340,
    isLiked: false,
  },
  {
    id: 103,
    title: 'Mushroom Soup',
    coverImage:
      'https://images.unsplash.com/photo-1476718406336-bb5a9690ee2a?auto=format&fit=crop&w=1200&q=80',
    difficulty: 'EASY',
    cookingTimeMin: 15,
    averageRating: 4.1,
    ratingCount: 89,
    isLiked: false,
  },
  {
    id: 104,
    title: 'Caesar Salad',
    coverImage:
      'https://images.unsplash.com/photo-1551248429-40975aa4de74?auto=format&fit=crop&w=1200&q=80',
    difficulty: 'EASY',
    cookingTimeMin: 10,
    averageRating: 4.4,
    ratingCount: 210,
    isLiked: false,
  },
])

const viewportWidth = ref(window.innerWidth)
const isMobile = computed(() => viewportWidth.value < 768)
const carouselType = computed(() => (isMobile.value ? '' : 'card'))
const carouselHeight = computed(() => (isMobile.value ? '220px' : '340px'))

const handleResize = () => {
  viewportWidth.value = window.innerWidth
}

// 3. 首页大搜索框触发的方法
const handleHeroSearch = () => {
  if (searchKeyword.value.trim()) {
    // 🌟 大厂交互：带上搜索词，直接跳转到你的 Explore (搜索大面板) 页面
    router.push({ path: '/recipe', query: { keyword: searchKeyword.value } })
  } else {
    router.push('/recipe')
  }
}

const getCarouselCard = async () => {
  const result = await getCarouselCardApi()
  if (result.code) {
    recommendedRecipes.value = result.data
  }
}
onMounted(() => {
  getCarouselCard()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <h1 class="hero-title">What are you craving today?</h1>
        <p class="hero-subtitle">Discover thousands of healthy and delicious recipes.</p>

        <div class="hero-search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="Search for ingredients, dishes, or cuisines..."
            size="large"
            class="massive-input"
            @keyup.enter="handleHeroSearch"
          >
            <template #prefix>
              <el-icon class="search-icon"><Search /></el-icon>
            </template>
            <template #append>
              <el-button color="#4ea685" class="search-btn" @click="handleHeroSearch">
                Search
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </section>

    <div class="main-container">
      <section class="recommendation-section">
        <div class="section-header">
          <h2>🌟 Today's Top Picks</h2>
          <span class="view-all" @click="router.push('/recomm')"
            >See why we chose these <el-icon><ArrowRight /></el-icon
          ></span>
        </div>

        <el-carousel
          :interval="4000"
          :type="carouselType"
          :height="carouselHeight"
          class="custom-carousel"
        >
          <el-carousel-item v-for="item in recommendedRecipes" :key="item.id">
            <div
              class="carousel-card"
              :style="{ backgroundImage: `url(${item.coverImage})` }"
              @click="router.push(`/recipe/${item.id}`)"
            >
              <div class="card-gradient-overlay"></div>
              <div class="card-info">
                <h3>{{ item.title }}</h3>
                <div class="card-meta">
                  <span class="rating">⭐ {{ item.averageRating }}</span>
                  <span class="time">⏱️ {{ item.cookingTimeMin }}</span>
                </div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </section>

      <section class="trending-section">
        <div class="section-header">
          <h2>🔥 Trending Now</h2>
        </div>

        <el-row :gutter="24">
          <el-col
            v-for="recipe in trendingRecipes"
            :key="recipe.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <RecipeCard :data="recipe" />
          </el-col>
        </el-row>

        <div class="explore-more-wrapper">
          <el-button
            color="#4ea685"
            size="large"
            round
            plain
            class="explore-btn"
            @click="router.push('/recipe')"
          >
            Explore All Recipes <el-icon class="ml-2"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  width: 100%;
  background-color: #f9fafb; /* 极浅的灰底，让白色卡片更凸显 */
  min-height: 100vh;
  padding-bottom: 60px;
}

/* ================== Hero巨幕 ================== */
.hero-section {
  position: relative;
  height: 480px;
  /* 使用了国外超火的美食无版权背景图，配合你的绿色叠加层 */
  background-image: url('https://images.unsplash.com/photo-1495195134817-aeb325a55b65?auto=format&fit=crop&w=1920&q=80');
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40px;
}
.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  /* 绝美渐变遮罩：让背景图变暗，同时透出一点点你的品牌绿 */
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.7) 0%, rgba(78, 166, 133, 0.5) 100%);
}
.hero-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: white;
  width: 100%;
  max-width: 800px;
  padding: 0 20px;
}
.hero-title {
  font-size: 3.5rem;
  font-weight: 900;
  margin-bottom: 15px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}
.hero-subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
  margin-bottom: 40px;
}

/* 巨型搜索框样式 */
.hero-search-box {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  border-radius: 50px;
  overflow: hidden;
}
:deep(.massive-input .el-input__wrapper) {
  padding: 8px 20px;
  font-size: 1.1rem;
}
:deep(.massive-input .el-input-group__append) {
  background-color: #4ea685;
  border: none;
  padding: 0;
}
.search-btn {
  height: 100%;
  border-radius: 0;
  padding: 0 30px;
  font-size: 1.1rem;
  font-weight: bold;
}
.search-icon {
  font-size: 20px;
  color: #4ea685;
}

/* ================== 主体内容区 ================== */
.main-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 25px;
  margin-top: 50px;
}
.section-header h2 {
  font-size: 1.8rem;
  color: #2c3e50;
  margin: 0;
}
.view-all {
  color: #4ea685;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: opacity 0.2s;
}
.view-all:hover {
  opacity: 0.8;
  text-decoration: underline;
}

/* ================== 走马灯卡片 ================== */
.custom-carousel {
  margin: 20px 0;
}
.carousel-card {
  height: 100%;
  width: 100%;
  border-radius: 16px;
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}
/* 底部渐变变黑，保证文字看得清 */
.card-gradient-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8) 0%, transparent 100%);
}
.card-info {
  position: absolute;
  bottom: 20px;
  left: 20px;
  right: 20px;
  color: white;
  z-index: 2;
}
.card-info h3 {
  margin: 0 0 8px 0;
  font-size: 1.5rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}
.card-meta {
  display: flex;
  gap: 15px;
  font-size: 0.95rem;
  opacity: 0.9;
}

/* ================== 底部探索按钮 ================== */
.explore-more-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
.explore-btn {
  font-size: 1.1rem;
  padding: 24px 40px;
  font-weight: bold;
}
.ml-2 {
  margin-left: 8px;
}

@media (max-width: 992px) {
  .hero-section {
    height: 420px;
  }

  .hero-title {
    font-size: 2.6rem;
  }

  .section-header h2 {
    font-size: 1.5rem;
  }
}

@media (max-width: 768px) {
  .home-page {
    padding-bottom: 36px;
  }

  .hero-section {
    height: 360px;
    margin-bottom: 24px;
  }

  .hero-title {
    font-size: 2rem;
    margin-bottom: 10px;
  }

  .hero-subtitle {
    font-size: 1rem;
    margin-bottom: 22px;
  }

  .hero-search-box {
    border-radius: 14px;
  }

  :deep(.massive-input .el-input__wrapper) {
    padding: 6px 12px;
    font-size: 16px;
  }

  .search-btn {
    min-height: 44px;
    padding: 0 16px;
    font-size: 1rem;
  }

  .main-container {
    padding: 0 12px;
  }

  .section-header {
    margin-top: 26px;
    margin-bottom: 16px;
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }

  .card-info h3 {
    font-size: 1.15rem;
  }

  .card-meta {
    font-size: 0.85rem;
    gap: 10px;
  }

  .explore-btn {
    width: 100%;
    padding: 16px 18px;
    font-size: 1rem;
  }
}
</style>
