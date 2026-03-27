import request from '../utils/request'

const toEmptySliceResult = () => ({
  code: 1,
  data: {
    content: [],
    hasNext: false,
    last: true,
  },
})

export const getRecipeApi = (id) => {
  return request.get(`/recipes/${id}`)
}

export const createRecipeApi = (recipe) => {
  return request.post('recipes/create', recipe)
}

export const updateRecipeApi = (id, recipe) => {
  return request.put(`/recipes/${id}`, recipe)
}

export const submitRatingApi = (data) => {
  return request.post('/recipes/rate', data)
}

export const deleteRatingApi = (id) => {
  return request.delete(`/recipes/rate?id=${id}`)
}

export const getRecommendationPreferencesApi = () => {
  return request.get('/users/preference')
}

export const getRecommendationSavedSeedApi = () => {
  return request.get('/users/saved?page=0&pageSize=1')
}

export const getRecommendationRightNowApi = (
  page = 0,
  pageSize = 12,
  localHour = new Date().getHours(),
) => {
  return request.get('/recipes/recc', {
    params: {
      page,
      pageSize,
      localHour,
    },
  })
}

export const getRecommendationTasteApi = (page = 0, pageSize = 12) => {
  return request.get(`/recipes/taste?page=${page}&pageSize=${pageSize}`)
}

export const getRecommendationExploreCuisinesApi = () => {
  return request.get('/recipes/cuisines')
}

export const getRecommendationExploreFlavoursApi = () => {
  return request.get('/recipes/flavours')
}

export const getRecommendationBehaviorApi = (page = 0, pageSize = 12) => {
  return request.get(`/recipes/behavior?page=${page}&pageSize=${pageSize}`)
}

export const getRecommendationExplorePreviewApi = (tag, page = 0, pageSize = 4) => {
  if (!tag?.type || !tag?.value) {
    return Promise.resolve(toEmptySliceResult())
  }

  if (tag.type === 'cuisine') {
    return request.post(`/recipes?page=${page}&pageSize=${pageSize}`, {
      cuisines: [tag.value],
    })
  }

  if (tag.type === 'flavour') {
    return request.post(`/recipes?page=${page}&pageSize=${pageSize}`, {
      flavours: [tag.value],
    })
  }

  return Promise.resolve(toEmptySliceResult())
}
