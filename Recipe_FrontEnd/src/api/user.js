import request from '../utils/request'

export const getMyRecipeApi = () => {
  return request.get(`/users/myRecipe`)
}

export const getSavedRecipeApi = (page, size) => {
  return request.get(`/users/saved?page=${page.value}&pageSize=${size.value}`)
}

export const sumbmitSaveRecipeApi = (id, status) => {
  return request.post(`/users/save/${id}`, { status: status })
}
