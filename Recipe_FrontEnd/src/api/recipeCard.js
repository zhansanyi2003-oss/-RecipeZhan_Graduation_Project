import request from '../utils/reques'

export const getRecipeCardApi = (recipes, page, size) => {
  return request.get(`/recipes?page=${page}&pageSize=${size}`, recipes)
}
export const getAllCuisinesApi = () => {
  return request.get(`/recipes/cuisines`)
}
export const getAllFlavoursApi = () => {
  return request.get(`/recipes/flavours`)
}
