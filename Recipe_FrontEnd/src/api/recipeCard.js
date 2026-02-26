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
