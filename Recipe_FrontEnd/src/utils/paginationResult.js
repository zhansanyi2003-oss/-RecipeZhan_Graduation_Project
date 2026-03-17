const isSuccessCode = (code) => code === 1 || code === '1' || code === true

const parseHasNext = (data, fallback = false) => {
  if (typeof data?.hasNext === 'boolean') return data.hasNext
  if (typeof data?.last === 'boolean') return !data.last
  return fallback
}

export const parsePagedResult = (result, options = {}) => {
  const defaultHasNext = options.defaultHasNext ?? false

  if (!result || !isSuccessCode(result.code)) {
    throw new Error(result?.msg || 'Failed to load recipes.')
  }

  const data = result.data

  if (data && Array.isArray(data.content)) {
    return {
      shape: 'slice',
      items: data.content,
      hasNext: parseHasNext(data, defaultHasNext),
    }
  }

  if (data && Array.isArray(data.items)) {
    return {
      shape: 'items',
      items: data.items,
      hasNext: parseHasNext(data, defaultHasNext),
    }
  }

  if (Array.isArray(data)) {
    return {
      shape: 'array',
      items: data,
      hasNext: false,
    }
  }

  throw new Error('Unexpected paged response format.')
}
