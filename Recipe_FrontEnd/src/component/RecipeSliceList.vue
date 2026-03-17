<script setup>
import { onMounted, ref, watch } from 'vue'
import RecipeCardGrid from './RecipeCardGrid.vue'
import { parsePagedResult } from '../utils/paginationResult.js'

const props = defineProps({
  fetchPage: {
    type: Function,
    required: true,
  },
  pageSize: {
    type: Number,
    default: 12,
  },
  reloadKey: {
    type: [Number, String],
    default: 0,
  },
  enabled: {
    type: Boolean,
    default: true,
  },
  showEmpty: {
    type: Boolean,
    default: true,
  },
  emptyDescription: {
    type: String,
    default: 'No recipes found.',
  },
  loadMoreText: {
    type: String,
    default: 'Load More Recipes ...',
  },
  noMoreText: {
    type: String,
    default: 'Oops, you have reached the end!',
  },
  showNoMoreText: {
    type: Boolean,
    default: true,
  },
  gutter: {
    type: Number,
    default: 24,
  },
  xs: {
    type: Number,
    default: 24,
  },
  sm: {
    type: Number,
    default: 12,
  },
  md: {
    type: Number,
    default: 8,
  },
  lg: {
    type: Number,
    default: 6,
  },
  xl: {
    type: Number,
    default: 6,
  },
})

const emit = defineEmits(['items-change', 'like-toggled', 'error', 'state-change'])

const items = ref([])
const currentPage = ref(0)
const hasNext = ref(false)
const loading = ref(false)
const initialized = ref(false)
const fullListCache = ref(null)

const emitItemsChange = () => {
  emit('items-change', items.value)
}

const emitStateChange = () => {
  emit('state-change', {
    hasNext: hasNext.value,
    loading: loading.value,
    count: items.value.length,
    initialized: initialized.value,
  })
}

const resetState = () => {
  items.value = []
  currentPage.value = 0
  hasNext.value = false
  loading.value = false
  initialized.value = false
  fullListCache.value = null
  emitItemsChange()
  emitStateChange()
}

const parseResult = (result, page) => {
  const parsed = parsePagedResult(result)

  if (parsed.shape === 'array') {
    if (!fullListCache.value) {
      fullListCache.value = parsed.items
    }
    const start = page * props.pageSize
    const end = start + props.pageSize
    return {
      pageItems: fullListCache.value.slice(start, end),
      next: end < fullListCache.value.length,
    }
  }

  return {
    pageItems: parsed.items,
    next: parsed.hasNext,
  }
}

const loadPage = async (page, append = false) => {
  if (loading.value || !props.enabled) return

  loading.value = true
  emitStateChange()
  try {
    const result = fullListCache.value
      ? { code: 1, data: fullListCache.value }
      : await props.fetchPage(page, props.pageSize)

    const { pageItems, next } = parseResult(result, page)
    items.value = append ? [...items.value, ...pageItems] : pageItems
    hasNext.value = next
    currentPage.value = page
    initialized.value = true
    emitItemsChange()
    emitStateChange()
  } catch (error) {
    emit('error', error)
    console.error(error)
  } finally {
    loading.value = false
    emitStateChange()
  }
}

const refresh = async () => {
  resetState()
  if (props.enabled) {
    await loadPage(0, false)
  }
}

const loadMore = async () => {
  if (!hasNext.value) return
  await loadPage(currentPage.value + 1, true)
}

const removeItemById = (recipeId) => {
  items.value = items.value.filter((item) => item.id !== recipeId)
  if (fullListCache.value) {
    fullListCache.value = fullListCache.value.filter((item) => item.id !== recipeId)
  }
  emitItemsChange()
  emitStateChange()
}

const forwardLikeToggled = (recipeId, newStatus) => {
  emit('like-toggled', recipeId, newStatus)
}

defineExpose({
  refresh,
  loadMore,
  removeItemById,
})

onMounted(() => {
  if (props.enabled) {
    loadPage(0, false)
  }
})

watch(
  () => props.reloadKey,
  () => {
    if (props.enabled) {
      refresh()
    }
  },
)

watch(
  () => props.enabled,
  (enabled, prevEnabled) => {
    if (!enabled) return
    if (!prevEnabled && !initialized.value) {
      loadPage(0, false)
    }
  },
)
</script>

<template>
  <div class="slice-list-wrapper" v-loading="loading && items.length === 0">
    <RecipeCardGrid
      :recipes="items"
      :gutter="gutter"
      :xs="xs"
      :sm="sm"
      :md="md"
      :lg="lg"
      :xl="xl"
      @like-toggled="forwardLikeToggled"
    >
      <template v-if="$slots.item" #item="slotProps">
        <slot name="item" v-bind="slotProps" />
      </template>
      <template v-if="$slots.after" #after>
        <slot name="after" />
      </template>
    </RecipeCardGrid>

    <div v-if="showEmpty && !loading && items.length === 0" class="slice-empty">
      <slot name="empty">
        <el-empty :description="emptyDescription" />
      </slot>
    </div>

    <div v-if="items.length > 0" class="slice-footer">
      <el-button
        v-if="hasNext"
        type="primary"
        size="large"
        round
        class="slice-load-more-btn"
        @click="loadMore"
      >
        {{ loadMoreText }}
      </el-button>
      <el-divider v-else-if="showNoMoreText" class="slice-no-more">
        {{ noMoreText }}
      </el-divider>
    </div>
  </div>
</template>

<style scoped>
.slice-footer {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-bottom: 20px;
}

.slice-load-more-btn {
  width: 220px;
  font-weight: 700;
  border: none !important;
  color: #fff !important;
  background-color: #4ea685 !important;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.12);
  transition:
    transform 0.2s ease,
    background-color 0.2s ease;
}

.slice-load-more-btn:hover {
  background-color: #57b894 !important;
  transform: scale(1.05);
}

.slice-no-more {
  color: #999;
  font-size: 14px;
}

@media (max-width: 768px) {
  .slice-footer {
    margin-top: 12px;
    padding-bottom: 10px;
  }

  .slice-load-more-btn {
    width: 100%;
    max-width: 280px;
    min-height: 44px;
  }
}
</style>
