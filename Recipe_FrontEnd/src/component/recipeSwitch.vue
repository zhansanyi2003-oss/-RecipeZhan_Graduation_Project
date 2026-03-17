<script setup>
import { computed, onMounted, ref } from 'vue'
import RecipeCardGrid from './RecipeCardGrid.vue'
import { parsePagedResult } from '../utils/paginationResult.js'

const props = defineProps({
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  titleIcon: { type: String, default: '' },
  switchButtonText: { type: String, default: 'Show more' },
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

const pool = ref([])
const page = ref(0)
const batchIndex = ref(0)
const hasNext = ref(true)
const loading = ref(false)

const totalBatches = computed(() => Math.max(1, Math.ceil(pool.value.length / props.batchSize)))

const visibleItems = computed(() => {
  const start = batchIndex.value * props.batchSize
  return pool.value.slice(start, start + props.batchSize)
})

const loadPool = async (targetPage = 0) => {
  if (loading.value) return
  loading.value = true
  try {
    const result = await props.fetchPage(targetPage, props.poolSize)
    const parsed = parsePagedResult(result)
    pool.value = parsed.items
    hasNext.value = parsed.hasNext
    page.value = targetPage
    batchIndex.value = 0
  } catch (error) {
    console.error(error)
    pool.value = []
    hasNext.value = false
  } finally {
    loading.value = false
  }
}

const switchBatch = async () => {
  if (loading.value) return

  const next = batchIndex.value + 1
  if (next < totalBatches.value) {
    batchIndex.value = next
    return
  }

  if (hasNext.value) {
    await loadPool(page.value + 1)
    return
  }

  await loadPool(0)
}

onMounted(() => loadPool(0))

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
      <el-button color="#4ea685" round plain :loading="loading" @click="switchBatch">
        {{ switchButtonText }}
      </el-button>
    </div>

    <RecipeCardGrid
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
  color: #4ea685;
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
