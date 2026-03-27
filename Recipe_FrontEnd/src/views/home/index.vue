<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ArrowRight } from '@element-plus/icons-vue'
// 假设你之前写好的食谱卡片组件在这里引入

import { getCarouselCardApi, getTrendingRecipesApi } from '../../api/recipeCard'
import RecipeSwitch from '../../component/recipeSwitch.vue'

const router = useRouter()
const searchKeyword = ref('')

// 1. 模拟：每日推荐数据 (大图走马灯用)
const recommendedRecipes = ref([])

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
    router.push({ path: '/recipe', query: { keyword: searchKeyword.value.trim() } })
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
const fetchTrendingPage = (page, size) => getTrendingRecipesApi(page, size)

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
          <div class="hero-search-shell">
            <el-input
              v-model="searchKeyword"
              placeholder="Search for ingredients, dishes, or cuisines..."
              size="large"
              class="massive-input hero-main-input"
              @keyup.enter="handleHeroSearch"
            >
              <template #prefix>
                <el-icon class="search-icon"><Search /></el-icon>
              </template>
            </el-input>
            <el-button class="search-btn btn-ui btn-ui--brand btn-ui--large" @click="handleHeroSearch">
              <span>Search</span>
              <el-icon class="search-btn-icon"><ArrowRight /></el-icon>
            </el-button>
          </div>
          <p class="hero-search-hint">
            Try "pasta", "chicken", or "Italian" to jump straight into results.
          </p>
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
        <RecipeSwitch
          class="trending-switch"
          title="🔥 Trending Now"
          :fetch-page="fetchTrendingPage"
          :pool-size="12"
          :batch-size="4"
          switch-button-text="Show more"
        />
        <div class="explore-more-wrapper">
          <el-button
            class="explore-btn btn-ui btn-ui--outline btn-ui--large"
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
  max-width: 720px;
  margin: 0 auto;
}

.hero-search-shell {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.18) 0%, rgba(255, 255, 255, 0.09) 100%);
  backdrop-filter: blur(18px);
  box-shadow:
    0 18px 42px rgba(0, 0, 0, 0.18),
    inset 0 1px 0 rgba(255, 255, 255, 0.22);
  transition:
    transform var(--transition-fast),
    box-shadow var(--transition-fast),
    border-color var(--transition-fast);
}

.hero-search-shell::before {
  content: '';
  position: absolute;
  inset: 1px;
  border-radius: inherit;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.18) 0%, rgba(255, 255, 255, 0.02) 100%);
  pointer-events: none;
}

.hero-search-shell:focus-within {
  transform: translateY(-1px);
  border-color: rgba(255, 255, 255, 0.55);
  box-shadow:
    0 22px 48px rgba(0, 0, 0, 0.22),
    0 0 0 4px rgba(255, 255, 255, 0.08);
}
:deep(.massive-input .el-input__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
  padding: 0 14px !important;
  min-height: 58px;
  border-radius: 999px;
  font-size: 1.08rem;
}
.search-btn {
  position: relative;
  z-index: 1;
  min-width: 150px;
  padding-inline: 24px;
  font-size: 1rem;
  flex-shrink: 0;
}
.search-icon {
  font-size: 20px;
  color: var(--color-primary);
}

.search-btn-icon {
  font-size: 16px;
}

.hero-search-hint {
  margin: 14px 0 0;
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.82);
  letter-spacing: 0.01em;
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
  color: var(--color-primary);
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
  padding-inline: 28px;
}
.ml-2 {
  margin-left: 8px;
}

.trending-switch {
  width: 100%;
}

@media (max-width: 992px) {
  .hero-section {
    height: 420px;
  }

  .hero-title {
    font-size: 2.6rem;
  }

  .hero-search-box {
    max-width: 680px;
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
    max-width: 100%;
  }

  .hero-search-shell {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
    padding: 10px;
    border-radius: 24px;
  }

  .search-btn {
    width: 100%;
    min-height: 46px;
    padding-inline: 18px;
    font-size: 1rem;
  }

  :deep(.massive-input .el-input__wrapper) {
    min-height: 50px;
    padding: 0 10px !important;
    font-size: 16px;
  }

  .hero-search-hint {
    margin-top: 10px;
    font-size: 0.88rem;
    line-height: 1.45;
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
