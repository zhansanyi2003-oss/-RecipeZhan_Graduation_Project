import { ref } from 'vue'
import { parsePagedResult } from '../utils/paginationResult.js'

export const usePagedRecipeFeed = ({
  fetchPage,
  pageSize = () => 12,
  enabled = () => true,
  onItemsChange = null,
  onStateChange = null,
  onError = null,
} = {}) => {
  const items = ref([])
  const currentPage = ref(0)
  const hasNext = ref(false)
  const loading = ref(false)
  const initialized = ref(false)
  const fullListCache = ref(null)

  const emitItemsChange = () => {
    onItemsChange?.(items.value)
  }

  const emitStateChange = () => {
    onStateChange?.({
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

      const size = pageSize()
      const start = page * size
      const end = start + size

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

  const loadPage = async (page, { append = false } = {}) => {
    if (loading.value || !enabled()) return

    loading.value = true
    emitStateChange()

    try {
      const result = fullListCache.value
        ? { code: 1, data: fullListCache.value }
        : await fetchPage(page, pageSize())

      const { pageItems, next } = parseResult(result, page)
      items.value = append ? [...items.value, ...pageItems] : pageItems
      hasNext.value = next
      currentPage.value = page
      initialized.value = true
      emitItemsChange()
      emitStateChange()
    } catch (error) {
      onError?.(error)
      throw error
    } finally {
      loading.value = false
      emitStateChange()
    }
  }

  const refresh = async () => {
    resetState()
    if (enabled()) {
      await loadPage(0)
    }
  }

  const loadMore = async () => {
    if (!hasNext.value) return
    await loadPage(currentPage.value + 1, { append: true })
  }

  const removeItemById = (recipeId) => {
    items.value = items.value.filter((item) => item.id !== recipeId)
    if (fullListCache.value) {
      fullListCache.value = fullListCache.value.filter((item) => item.id !== recipeId)
    }
    emitItemsChange()
    emitStateChange()
  }

  return {
    items,
    currentPage,
    hasNext,
    loading,
    initialized,
    resetState,
    loadPage,
    refresh,
    loadMore,
    removeItemById,
  }
}
