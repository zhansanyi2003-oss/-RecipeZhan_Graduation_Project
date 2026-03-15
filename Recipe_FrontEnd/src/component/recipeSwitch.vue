<script setup>
import { computed, onMounted, ref } from 'vue'
import RecipeCardGrid from './RecipeCardGrid.vue'

const props = defineProps({
  fetchPage: { type: Function, required: true },
  poolSize: { type: Number, default: 12 },
  batchSize: { type: Number, default: 4 },
  showSwitchButton: { type: Boolean, default: true },
  switchButtonText: { type: String, default: 'Show more' },
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
    if (!result?.code) return

    const slice = result.data || {}
    pool.value = Array.isArray(slice.content) ? slice.content : []
    hasNext.value = typeof slice.hasNext === 'boolean' ? slice.hasNext : !slice.last
    page.value = targetPage
    batchIndex.value = 0
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
  <div>
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
  </div>
</template>

<style scoped>
.switch-wrap {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}
</style>
