import request from '../utils/request'

export const getRecipeCardApi = (recipes, page, size) => {
  return request.post(`/recipes?page=${page}&pageSize=${size}`, recipes)
}
export const getAllCuisinesApi = () => {
  return request.get(`/recipes/cuisines`)
}
export const getAllFlavoursApi = () => {
  return request.get(`/recipes/flavours`)
}

export const getAllIngredientsApi = () => {
  return request.get(`/recipes/ingredients`)
}
export const getCarouselCardApi = () => {
  const currentLocalHour = new Date().getHours()
  return request.get(`/recipes/recc`, {
    params: {
      localHour: currentLocalHour,
    },
  })
}

export const getTrendingRecipesApi = (page, pageSize) => {
  return request.get(`/recipes/trending?page=${page}&pageSize=${pageSize}`)
}
