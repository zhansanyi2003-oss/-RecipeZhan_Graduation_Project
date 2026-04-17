<script setup>
import { computed, onMounted, ref } from 'vue'
import { Avatar, Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import RecipeCardGrid from '../../component/RecipeCardGrid.vue'
import RecipeSwitch from '../../component/recipeSwitch.vue'
import {
  getRecommendationBehaviorApi,
  getRecommendationExplorePreviewApi,
  getRecommendationRightNowApi,
} from '../../api/recipe.js'
import {
  getAllCuisinesApi,
  getAllFlavoursApi,
  getTasteRecommendationsApi,
} from '../../api/recipeCard.js'
import { getPreferenceApi, getSavedRecipeApi } from '../../api/user.js'
import { parsePagedResult } from '../../utils/paginationResult.js'
import { buildExploreTags, buildRecommendationSections } from '../../utils/recommendationPageState.js'

const router = useRouter()

const emptyPreferences = () => ({
  dietary: [],
  allergies: [],
  skillLevel: '',
  timeAvailability: '',
  flavours: [],
  cuisines: [],
  ingredients: [],
})

const normalizePreferences = (value = {}) => ({
  ...emptyPreferences(),
  ...value,
  dietary: Array.isArray(value.dietary) ? value.dietary : [],
  allergies: Array.isArray(value.allergies) ? value.allergies : [],
  flavours: Array.isArray(value.flavours) ? value.flavours : [],
  cuisines: Array.isArray(value.cuisines) ? value.cuisines : [],
  ingredients: Array.isArray(value.ingredients) ? value.ingredients : [],
})

const uniqueById = (items) => {
  const seen = new Set()

  return items.filter((item) => {
    if (!item?.id || seen.has(item.id)) return false
    seen.add(item.id)
    return true
  })
}

const loading = ref(true)
const exploreLoading = ref(false)
const sectionVersion = ref(0)

const preferences = ref(emptyPreferences())
const savedRecipe = ref(null)
const sections = ref(buildRecommendationSections())

const exploreTags = ref([])
const activeExploreTag = ref(null)
const explorePreviewRecipes = ref([])

const sectionMap = computed(() =>
  Object.fromEntries(sections.value.map((section) => [section.key, section])),
)

const engineSummary = computed(() => {
  if (sectionMap.value.behavior?.mode === 'cta' && sectionMap.value.taste?.mode === 'explore') {
    return 'We are starting from the time of day. Save recipes or pick a taste to make this page more personal.'
  }

  return 'This page blends your saved recipes, cooking-time preferences, and taste signals to surface better ideas.'
})

const openPreferences = () => {
  router.push({ path: '/profile', query: { tab: 'preferences' } })
}

const openExplore = () => {
  router.push('/recipe')
}

const handleLikeToggled = (recipeId, newStatus) => {
  explorePreviewRecipes.value = explorePreviewRecipes.value.map((item) =>
    item.id === recipeId ? { ...item, isLiked: newStatus } : item,
  )
}

const fetchRightNowPage = (page, pageSize) => getRecommendationRightNowApi(page, pageSize)
const fetchBehaviorPage = (page, pageSize) => getRecommendationBehaviorApi(page, pageSize)
const fetchTastePage = (page, pageSize) => getTasteRecommendationsApi(page, pageSize)

const loadExplorePreview = async (tag) => {
  if (!tag) {
    activeExploreTag.value = null
    explorePreviewRecipes.value = []
    return
  }

  exploreLoading.value = true
  activeExploreTag.value = tag

  try {
    const result = await getRecommendationExplorePreviewApi(tag)
    explorePreviewRecipes.value = uniqueById(parsePagedResult(result).items)
  } catch (error) {
    console.error(error)
    explorePreviewRecipes.value = []
    ElMessage.error('Failed to load recipes for this taste.')
  } finally {
    exploreLoading.value = false
  }
}

const initializeExploreMode = async (availableCuisines, availableFlavours) => {
  exploreTags.value = buildExploreTags({
    availableCuisines,
    availableFlavours,
    limit: 6,
  })
  explorePreviewRecipes.value = []
  activeExploreTag.value = null

  for (const tag of exploreTags.value) {
    try {
      const result = await getRecommendationExplorePreviewApi(tag)
      const items = uniqueById(parsePagedResult(result).items)
      if (items.length) {
        activeExploreTag.value = tag
        explorePreviewRecipes.value = items
        return
      }
    } catch (error) {
      console.error(error)
    }
  }

  activeExploreTag.value = exploreTags.value[0] || null
}

const loadSavedSeed = async () => {
  try {
    const result = await getSavedRecipeApi(0, 1)
    const parsed = parsePagedResult(result)
    return parsed.items[0] || null
  } catch (error) {
    console.error(error)
    return null
  }
}

const loadPreferences = async () => {
  try {
    const result = await getPreferenceApi()
    if (result.code && result.data) {
      return normalizePreferences(result.data)
    }
  } catch (error) {
    console.error(error)
  }

  return emptyPreferences()
}

const loadAvailableTastes = async () => {
  const [cuisineResult, flavourResult] = await Promise.allSettled([
    getAllCuisinesApi(),
    getAllFlavoursApi(),
  ])

  return {
    cuisines:
      cuisineResult.status === 'fulfilled' && cuisineResult.value.code ? cuisineResult.value.data : [],
    flavours:
      flavourResult.status === 'fulfilled' && flavourResult.value.code ? flavourResult.value.data : [],
  }
}

const loadRecommendationPage = async () => {
  loading.value = true

  try {
    const [loadedPreferences, loadedSavedRecipe, availableTastes] = await Promise.all([
      loadPreferences(),
      loadSavedSeed(),
      loadAvailableTastes(),
    ])

    preferences.value = loadedPreferences
    savedRecipe.value = loadedSavedRecipe
    exploreTags.value = []
    activeExploreTag.value = null
    explorePreviewRecipes.value = []

    sections.value = buildRecommendationSections({
      preferences: loadedPreferences,
      savedRecipe: loadedSavedRecipe,
    })

    if (sectionMap.value.taste?.mode === 'explore') {
      await initializeExploreMode(availableTastes.cuisines, availableTastes.flavours)
    }

    sectionVersion.value += 1
  } catch (error) {
    console.error(error)
    ElMessage.error('Failed to load recommendations.')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRecommendationPage()
})
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
            <h2>How our recommendation engine works</h2>
            <p>{{ engineSummary }}</p>
          </div>
        </div>
        <div class="banner-right">
          <el-button class="pref-btn btn-ui btn-ui--brand" @click="openPreferences">
            <el-icon><Setting /></el-icon>
            Edit Preferences
          </el-button>
        </div>
      </div>

      <div v-if="loading" class="page-loading">
        <p>Loading recommendations...</p>
      </div>

      <template v-else>
        <section class="recomm-section">
          <RecipeSwitch
            :key="`right-now-${sectionVersion}`"
            :title="sectionMap['right-now']?.title || 'Good for this moment'"
            :subtitle="sectionMap['right-now']?.subtitle || ''"
            :fetch-page="fetchRightNowPage"
            :pool-size="12"
            :batch-size="4"
            @like-toggled="handleLikeToggled"
          />
        </section>

        <section class="recomm-section">
          <RecipeSwitch
            v-if="sectionMap.behavior?.mode === 'personalized'"
            :key="`behavior-${sectionVersion}`"
            :title="sectionMap.behavior.title"
            :subtitle="sectionMap.behavior.subtitle"
            :fetch-page="fetchBehaviorPage"
            :pool-size="12"
            :batch-size="4"
            empty-text="Save a few more recipes so we can find stronger matches for this section."
            @like-toggled="handleLikeToggled"
          />

          <div v-else-if="sectionMap.behavior?.mode === 'cta'" class="cta-panel">
            <div class="section-copy">
              <h2 class="section-title">{{ sectionMap.behavior.title }}</h2>
              <p class="section-subtitle">{{ sectionMap.behavior.subtitle }}</p>
              <div class="cta-actions">
                <el-button class="btn-ui btn-ui--brand" @click="openPreferences">
                  Set Preferences
                </el-button>
                <el-button class="btn-ui btn-ui--outline" @click="openExplore">
                  Save Recipes You Like
                </el-button>
              </div>
            </div>

            <div class="unlock-panel">
              <p class="unlock-label">Unlocks</p>
              <ul class="unlock-list">
                <li>Because you saved...</li>
                <li>Taste-based picks</li>
                <li>Time-aware meals</li>
              </ul>
            </div>
          </div>

          <div v-else class="empty-panel">
            <h2 class="section-title">{{ sectionMap.behavior?.title }}</h2>
            <p class="section-subtitle">
              Save a few more recipes so we can find stronger matches for this section.
            </p>
          </div>
        </section>

        <section class="recomm-section">
          <RecipeSwitch
            v-if="sectionMap.taste?.mode === 'personalized'"
            :key="`taste-${sectionVersion}`"
            :title="sectionMap.taste.title"
            :subtitle="sectionMap.taste.subtitle"
            :fetch-page="fetchTastePage"
            :pool-size="12"
            :batch-size="4"
            empty-text="Update your taste preferences so we can turn this section into a stronger match."
            @like-toggled="handleLikeToggled"
          />

          <div v-else-if="sectionMap.taste?.mode === 'explore'" class="explore-panel">
            <div class="custom-section-header">
              <div class="title-wrap">
                <h2 class="section-title">{{ sectionMap.taste.title }}</h2>
                <p class="section-subtitle">{{ sectionMap.taste.subtitle }}</p>
              </div>
            </div>

            <div class="tag-list">
              <button
                v-for="tag in exploreTags"
                :key="tag.key"
                type="button"
                class="taste-tag"
                :class="{ active: activeExploreTag?.key === tag.key }"
                @click="loadExplorePreview(tag)"
              >
                {{ tag.label }}
              </button>
            </div>

            <div v-if="exploreLoading" class="empty-panel compact">
              <p>Loading recipes for this taste...</p>
            </div>

            <RecipeCardGrid
              v-else-if="explorePreviewRecipes.length"
              :recipes="explorePreviewRecipes"
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
              :xl="6"
              @like-toggled="handleLikeToggled"
            />

            <div v-else class="empty-panel compact">
              <p>Try another taste tag to explore a different direction.</p>
            </div>
          </div>

          <div v-else class="empty-panel">
            <h2 class="section-title">{{ sectionMap.taste?.title }}</h2>
            <p class="section-subtitle">
              Update your taste preferences so we can turn this section into a stronger match.
            </p>
          </div>
        </section>
      </template>
    </div>
  </div>
</template>

<style scoped>
.recomm-page {
  width: 100%;
  min-height: calc(100vh - 64px);
  padding: 40px 0 80px 0;
  background-color: #f9fafb;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.engine-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-bottom: 40px;
  padding: 24px 32px;
  border: 1px solid #d5ebe1;
  border-radius: 16px;
  background-color: #eef7f4;
  box-shadow: 0 4px 12px rgba(78, 166, 133, 0.05);
}

.banner-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  color: #fff;
  background-color: #4ea685;
  box-shadow: 0 4px 10px rgba(78, 166, 133, 0.3);
}

.banner-text h2,
.section-title {
  margin: 0 0 6px 0;
  color: #2a5948;
  font-size: 1.4rem;
}

.banner-text p,
.section-subtitle {
  margin: 0;
  color: #5e7d71;
  font-size: 1rem;
  line-height: 1.6;
}

.pref-btn {
  padding-inline: 22px;
}

.page-loading,
.empty-panel,
.cta-panel,
.explore-panel {
  border: 1px solid #e4ebeb;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 8px 20px rgba(25, 50, 45, 0.04);
}

.page-loading,
.empty-panel {
  padding: 32px 28px;
}

.page-loading {
  text-align: center;
  color: #5e7d71;
}

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

.cta-panel {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px;
  background: linear-gradient(135deg, #fffaf1, #fff4e6);
  border-color: #edd8b8;
}

.section-copy {
  flex: 1;
}

.cta-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
}

.unlock-panel {
  min-width: 220px;
  padding: 16px;
  border: 1px dashed #dbbe90;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.72);
}

.unlock-label {
  margin: 0 0 12px 0;
  color: #8a6a38;
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.unlock-list {
  margin: 0;
  padding-left: 18px;
  color: #5c4720;
  line-height: 1.8;
}

.explore-panel {
  padding: 28px;
}

.custom-section-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}

.title-wrap {
  min-width: 0;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 22px;
}

.taste-tag {
  padding: 10px 16px;
  border: 1px solid #cedbe8;
  border-radius: 999px;
  background: #fff;
  color: #48657c;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    background-color 0.2s ease,
    color 0.2s ease;
}

.taste-tag:hover {
  transform: translateY(-1px);
  border-color: #91b6d8;
}

.taste-tag.active {
  border-color: #4ea685;
  background: #4ea685;
  color: #fff;
}

.compact {
  padding: 20px;
}

@media (max-width: 768px) {
  .recomm-page {
    padding: 20px 0 40px 0;
  }

  .container {
    padding: 0 12px;
  }

  .engine-banner,
  .cta-panel {
    flex-direction: column;
    align-items: flex-start;
  }

  .engine-banner {
    padding: 18px 16px;
    margin-bottom: 28px;
  }

  .banner-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .banner-right,
  .pref-btn,
  .unlock-panel {
    width: 100%;
  }

  .icon-wrapper {
    width: 44px;
    height: 44px;
  }

  .banner-text h2,
  .section-title,
  .recomm-section :deep(.section-title) {
    font-size: 1.15rem;
  }

  .banner-text p,
  .section-subtitle,
  .recomm-section :deep(.section-subtitle) {
    font-size: 0.92rem;
  }

  .explore-panel,
  .page-loading,
  .empty-panel {
    padding: 20px 16px;
  }

  .recomm-section {
    margin-bottom: 30px;
  }
}
</style>

