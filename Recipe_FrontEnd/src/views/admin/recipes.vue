<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Search } from '@element-plus/icons-vue'
import RecipeSliceList from '../../component/RecipeSliceList.vue'
import { deleteAdminRecipeApi, getAdminRecipesApi } from '../../api/user'

const router = useRouter()
const sliceRef = ref()
const reloadKey = ref(0)
const searchKeyword = ref('')
const committedKeyword = ref('')
const totalShown = ref(0)

const sectionTitle = computed(() =>
  committedKeyword.value
    ? `Admin Recipe Management - "${committedKeyword.value}"`
    : 'Admin Recipe Management',
)

const fetchAdminRecipePage = async (page, pageSize) => {
  return await getAdminRecipesApi(page, pageSize, committedKeyword.value)
}

const applySearch = () => {
  committedKeyword.value = searchKeyword.value.trim()
  reloadKey.value += 1
}

const resetSearch = () => {
  searchKeyword.value = ''
  committedKeyword.value = ''
  reloadKey.value += 1
}

const onItemsChange = (items) => {
  totalShown.value = items.length
}

const goToEditRecipe = (id) => {
  router.push(`/recipe/edit/${id}`)
}

const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleDateString()
}

const deleteRecipe = async (id, title) => {
  try {
    await ElMessageBox.confirm(
      `Delete recipe "${title}"? This action cannot be undone.`,
      'Confirm Delete',
      {
        type: 'warning',
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
      },
    )
  } catch (error) {
    return
  }

  try {
    const res = await deleteAdminRecipeApi(id)
    if (!res.code) {
      ElMessage.error(res.msg || 'Failed to delete recipe.')
      return
    }
    await sliceRef.value?.refresh()
    ElMessage.success('Recipe deleted successfully.')
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || 'Failed to delete recipe.')
  }
}
</script>

<template>
  <div class="admin-page">
    <section class="admin-hero">
      <div class="hero-left">
        <h1>{{ sectionTitle }}</h1>
        <p>Review, edit, and moderate all recipes from one place.</p>
      </div>
      <div class="hero-metrics">
        <span class="metric-label">Loaded Cards</span>
        <strong>{{ totalShown }}</strong>
      </div>
    </section>

    <section class="admin-toolbar">
      <el-input
        v-model="searchKeyword"
        clearable
        size="large"
        placeholder="Search recipes by title..."
        class="search-input"
        @keyup.enter="applySearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <div class="toolbar-actions">
        <el-button color="#4ea685" size="large" round @click="applySearch">Search</el-button>
        <el-button size="large" round plain @click="resetSearch">Reset</el-button>
      </div>
    </section>

    <RecipeSliceList
      ref="sliceRef"
      :fetch-page="fetchAdminRecipePage"
      :reload-key="reloadKey"
      :page-size="12"
      :xs="24"
      :sm="12"
      :md="8"
      :lg="6"
      :xl="6"
      load-more-text="Load More Recipes ..."
      no-more-text="No more recipes to manage."
      empty-description="No matching recipes."
      @items-change="onItemsChange"
    >
      <template #item="{ recipe }">
        <article class="admin-card">
          <div class="cover-wrap">
            <img :src="recipe.coverImage" :alt="recipe.title" class="cover" />
          </div>

          <div class="admin-card-body">
            <h3 :title="recipe.title">{{ recipe.title }}</h3>
            <div class="meta-line">
              <span>Author: {{ recipe.authorName || 'Unknown' }}</span>
              <span>{{ recipe.cookingTimeMin || '-' }} mins</span>
            </div>
            <div class="meta-line muted">
              <span>Rating: {{ recipe.averageRating ?? 0 }} ({{ recipe.ratingCount ?? 0 }})</span>
              <span>Updated: {{ formatDateTime(recipe.updatedAt) }}</span>
            </div>
            <div class="admin-actions">
              <el-button
                type="primary"
                size="small"
                class="admin-action-btn edit-btn"
                :icon="Edit"
                @click.stop="goToEditRecipe(recipe.id)"
              >
                Edit
              </el-button>
              <el-button
                type="danger"
                size="small"
                class="admin-action-btn delete-btn"
                :icon="Delete"
                @click.stop="deleteRecipe(recipe.id, recipe.title)"
              >
                Delete
              </el-button>
            </div>
          </div>
        </article>
      </template>
    </RecipeSliceList>
  </div>
</template>

<style scoped>
.admin-page {
  width: 100%;
  padding: 8px 0 24px;
}

.admin-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding: 18px 20px;
  border-radius: 14px;
  border: 1px solid #d4ebe1;
  background: linear-gradient(135deg, #eef7f4 0%, #f8fcfa 100%);
}

.hero-left h1 {
  margin: 0;
  font-size: 1.35rem;
  color: #1f3b33;
}

.hero-left p {
  margin: 6px 0 0;
  color: #4f6b62;
  font-size: 0.95rem;
}

.hero-metrics {
  min-width: 120px;
  text-align: right;
}

.metric-label {
  display: block;
  color: #6f8b82;
  font-size: 0.78rem;
}

.hero-metrics strong {
  color: #2a5d4c;
  font-size: 1.35rem;
}

.admin-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.search-input {
  flex: 1;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-card {
  width: 100%;
  min-height: 320px;
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e8ecef;
  background: #fff;
  box-shadow: 0 6px 16px rgba(29, 60, 50, 0.08);
}

.cover-wrap {
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: #f4f7f8;
}

.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.admin-card-body {
  padding: 14px 14px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.admin-card-body h3 {
  margin: 0;
  font-size: 1.05rem;
  line-height: 1.35;
  color: #1f2f46;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta-line {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  color: #4b5d71;
  font-size: 0.84rem;
}

.meta-line.muted {
  color: #7a8796;
}

.admin-actions {
  margin-top: auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.admin-action-btn {
  min-height: 38px;
  border-radius: 11px;
  font-weight: 700;
  letter-spacing: 0.2px;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    background-color 0.18s ease,
    border-color 0.18s ease;
}

.edit-btn {
  border: none !important;
  color: #ffffff !important;
  background: linear-gradient(135deg, #4ea685 0%, #5fbc98 100%) !important;
  box-shadow: 0 6px 14px rgba(78, 166, 133, 0.28);
}

.edit-btn:hover {
  transform: translateY(-1px);
  background: linear-gradient(135deg, #459a7a 0%, #53b38f 100%) !important;
  box-shadow: 0 8px 16px rgba(78, 166, 133, 0.32);
}

.delete-btn {
  color: #b63c3c !important;
  border: 1px solid #efb0b0 !important;
  background: linear-gradient(180deg, #fffefe 0%, #fff5f5 100%) !important;
}

.delete-btn:hover {
  transform: translateY(-1px);
  color: #a62d2d !important;
  border-color: #e99595 !important;
  background: linear-gradient(180deg, #fff8f8 0%, #ffecec 100%) !important;
  box-shadow: 0 6px 12px rgba(214, 93, 93, 0.16);
}

.admin-action-btn:active {
  transform: translateY(0);
}

@media (max-width: 768px) {
  .admin-page {
    padding-top: 0;
  }

  .admin-hero {
    flex-direction: column;
    align-items: flex-start;
    margin-bottom: 14px;
    padding: 14px;
  }

  .hero-left h1 {
    font-size: 1.1rem;
  }

  .hero-metrics {
    min-width: auto;
    text-align: left;
  }

  .admin-toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  .toolbar-actions {
    width: 100%;
  }

  .toolbar-actions :deep(.el-button) {
    flex: 1;
  }

  .admin-card {
    min-height: 300px;
  }

  .admin-action-btn {
    min-height: 40px;
  }
}
</style>
