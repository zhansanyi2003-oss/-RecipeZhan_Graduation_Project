import assert from 'node:assert/strict'
import { parsePagedResult } from './paginationResult.js'

const runSliceHasNext = () => {
  const result = {
    code: 1,
    data: {
      content: [{ id: 1 }],
      hasNext: true,
      last: false,
    },
  }

  const parsed = parsePagedResult(result)
  assert.deepEqual(parsed.items, [{ id: 1 }])
  assert.equal(parsed.hasNext, true)
  assert.equal(parsed.shape, 'slice')
}

const runSliceLastFallback = () => {
  const result = {
    code: 1,
    data: {
      content: [{ id: 1 }],
      last: true,
    },
  }

  const parsed = parsePagedResult(result)
  assert.equal(parsed.hasNext, false)
}

const runItemsShape = () => {
  const result = {
    code: 1,
    data: {
      items: [{ id: 7 }],
      hasNext: false,
    },
  }

  const parsed = parsePagedResult(result)
  assert.deepEqual(parsed.items, [{ id: 7 }])
  assert.equal(parsed.hasNext, false)
  assert.equal(parsed.shape, 'items')
}

const runArrayShape = () => {
  const result = {
    code: 1,
    data: [{ id: 2 }, { id: 3 }],
  }

  const parsed = parsePagedResult(result)
  assert.deepEqual(parsed.items, [{ id: 2 }, { id: 3 }])
  assert.equal(parsed.hasNext, false)
  assert.equal(parsed.shape, 'array')
}

const runFailedResult = () => {
  assert.throws(
    () =>
      parsePagedResult({
        code: 0,
        msg: 'error',
      }),
    /error/,
  )
}

runSliceHasNext()
runSliceLastFallback()
runItemsShape()
runArrayShape()
runFailedResult()

console.log('paginationResult tests passed')
