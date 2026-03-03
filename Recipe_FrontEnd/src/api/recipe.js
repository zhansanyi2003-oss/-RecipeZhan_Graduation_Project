import request from '../utils/request'

export const getRecipeApi = (id) => {
  return request.get(`/recipes/${id}`)
}

export const createRecipeApi = (recipe) => {
  return request.post('recipes/create', recipe)
}

export const submitRatingApi = (data) => {
  return request.post('/recipes/rate', data)
}
