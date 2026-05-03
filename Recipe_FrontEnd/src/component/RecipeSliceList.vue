<script setup>
import { onMounted, watch } from 'vue'
import RecipeCardGrid from './RecipeCardGrid.vue'
import { usePagedRecipeFeed } from '../composables/usePagedRecipeFeed.js'

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

const { items, hasNext, loading, initialized, loadPage, refresh, loadMore, removeItemById } =
  usePagedRecipeFeed({
    fetchPage: (page, pageSize) => props.fetchPage(page, pageSize),
    pageSize: () => props.pageSize,
    enabled: () => props.enabled,
    onItemsChange: (nextItems) => emit('items-change', nextItems),
    onStateChange: (state) => emit('state-change', state),
    onError: (error) => {
      emit('error', error)
      console.error(error)
    },
  })

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
    loadPage(0)
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
      loadPage(0)
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
        size="large"
        class="slice-load-more-btn btn-ui btn-ui--brand btn-ui--large"
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

