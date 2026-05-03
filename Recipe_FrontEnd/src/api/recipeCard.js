import request from '../utils/request'

export const getRecipeCardApi = (recipes, page, size) => {
  return request.post(`/recipes?page=${page}&pageSize=${size}`, recipes)
}
export const getAllCuisinesApi = () => {
  return request.get(`/taxonomy/cuisines`)
}
export const getAllFlavoursApi = () => {
  return request.get(`/taxonomy/flavours`)
}

export const getAllIngredientsApi = () => {
  return request.get(`/recipes/ingredients`)
}
export const getCarouselCardApi = () => {
  const currentLocalHour = new Date().getHours()
  return request.get(`/recipes/hero`, {
    params: {
      localHour: currentLocalHour,
    },
  })
}

export const getTrendingRecipesApi = (page, pageSize) => {
  return request.get(`/recipes/trending?page=${page}&pageSize=${pageSize}`)
}

export const getTasteRecommendationsApi = (page, pageSize) => {
  return request.get(`/recipes/taste?page=${page}&pageSize=${pageSize}`)
}
