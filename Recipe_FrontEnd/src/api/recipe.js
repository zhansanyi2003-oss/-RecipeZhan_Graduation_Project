import request from '../utils/request'

export const getRecipeApi = (id) => {
  return request.get(`/recipes/${id}`)
}

export const createRecipeApi = (recipe) => {
  return request.post('recipes/create', recipe)
}
