<script setup>
import { computed, onMounted, ref } from 'vue'
import RecipeCardGrid from './RecipeCardGrid.vue'
import { usePagedRecipeFeed } from '../composables/usePagedRecipeFeed.js'

const props = defineProps({
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  titleIcon: { type: String, default: '' },
  switchButtonText: { type: String, default: 'Show more' },
  emptyText: { type: String, default: 'No recipes available right now.' },
  fetchPage: { type: Function, required: true },
  poolSize: { type: Number, default: 12 },
  batchSize: { type: Number, default: 4 },
  gutter: { type: Number, default: 24 },
  xs: { type: Number, default: 24 },
  sm: { type: Number, default: 12 },
  md: { type: Number, default: 8 },
  lg: { type: Number, default: 6 },
  xl: { type: Number, default: 6 },
})

const emit = defineEmits(['like-toggled'])
const batchIndex = ref(0)

const { items: pool, currentPage: page, hasNext, loading, loadPage } = usePagedRecipeFeed({
  fetchPage: (targetPage, pageSize) => props.fetchPage(targetPage, pageSize),
  pageSize: () => props.poolSize,
  enabled: () => true,
  onError: (error) => {
    console.error(error)
  },
})

const totalBatches = computed(() => Math.max(1, Math.ceil(pool.value.length / props.batchSize)))

const visibleItems = computed(() => {
  const start = batchIndex.value * props.batchSize
  return pool.value.slice(start, start + props.batchSize)
})

const switchBatch = async () => {
  if (loading.value) return

  const next = batchIndex.value + 1
  if (next < totalBatches.value) {
    batchIndex.value = next
    return
  }

  if (hasNext.value) {
    await loadPage(page.value + 1)
    batchIndex.value = 0
    return
  }

  await loadPage(0)
  batchIndex.value = 0
}

onMounted(async () => {
  await loadPage(0)
  batchIndex.value = 0
})

defineExpose({
  switchBatch,
})
</script>

<template>
  <section class="recipe-switch-section">
    <div class="section-header">
      <div class="title-wrap">
        <h2 class="section-title">
          <span v-if="titleIcon" class="title-icon">{{ titleIcon }}</span>
          <span>{{ title }}</span>
        </h2>
        <p v-if="subtitle" class="section-subtitle">{{ subtitle }}</p>
      </div>
      <el-button
        class="btn-ui btn-ui--outline btn-ui--section"
        :loading="loading"
        @click="switchBatch"
      >
        {{ switchButtonText }}
      </el-button>
    </div>

    <div v-if="!loading && !visibleItems.length" class="empty-state">
      <p>{{ emptyText }}</p>
    </div>

    <RecipeCardGrid
      v-else
      :recipes="visibleItems"
      :gutter="gutter"
      :xs="xs"
      :sm="sm"
      :md="md"
      :lg="lg"
      :xl="xl"
      @like-toggled="(...args) => emit('like-toggled', ...args)"
    />
  </section>
</template>

<style scoped>
.recipe-switch-section {
  width: 100%;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-top: 50px;
  margin-bottom: 24px;
}

.title-wrap {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.section-title {
  margin: 0;
  font-size: 1.8rem;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  color: var(--color-primary);
  line-height: 1;
}

.section-subtitle {
  margin: 0;
  color: #909399;
  font-size: 1rem;
}

.switch-wrap {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

.empty-state {
  padding: 28px 24px;
  border: 1px solid #e4ebeb;
  border-radius: 18px;
  background: #fff;
  color: #6b7280;
  text-align: center;
}

@media (max-width: 768px) {
  .section-header {
    margin-top: 26px;
    margin-bottom: 16px;
  }

  .section-title {
    font-size: 1.5rem;
  }

  .section-subtitle {
    font-size: 0.9rem;
  }
}
</style>

