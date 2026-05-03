import assert from 'node:assert/strict'
import { usePagedRecipeFeed } from './usePagedRecipeFeed.js'

const runSliceLoading = async () => {
  const state = usePagedRecipeFeed({
    fetchPage: async () => ({
      code: 1,
      data: {
        content: [{ id: 1 }, { id: 2 }],
        hasNext: true,
      },
    }),
    pageSize: () => 2,
    enabled: () => true,
  })

  await state.loadPage(0)

  assert.deepEqual(state.items.value, [{ id: 1 }, { id: 2 }])
  assert.equal(state.hasNext.value, true)
  assert.equal(state.currentPage.value, 0)
}

const runArrayCaching = async () => {
  let loadCount = 0
  const state = usePagedRecipeFeed({
    fetchPage: async () => {
      loadCount += 1
      return {
        code: 1,
        data: [{ id: 1 }, { id: 2 }, { id: 3 }],
      }
    },
    pageSize: () => 2,
    enabled: () => true,
  })

  await state.loadPage(0)
  await state.loadPage(1)

  assert.equal(loadCount, 1)
  assert.deepEqual(state.items.value, [{ id: 3 }])
  assert.equal(state.hasNext.value, false)
}

const runRemoveItem = async () => {
  const state = usePagedRecipeFeed({
    fetchPage: async () => ({
      code: 1,
      data: {
        content: [{ id: 1 }, { id: 2 }],
        hasNext: false,
      },
    }),
    pageSize: () => 2,
    enabled: () => true,
  })

  await state.loadPage(0)
  state.removeItemById(1)

  assert.deepEqual(state.items.value, [{ id: 2 }])
}

await runSliceLoading()
await runArrayCaching()
await runRemoveItem()

console.log('usePagedRecipeFeed tests passed')
