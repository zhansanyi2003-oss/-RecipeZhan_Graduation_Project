import request from '../utils/request'

export const getMyRecipeApi = (page, size) => {
  return request.get(`/users/myRecipe?page=${page}&pageSize=${size}`)
}

export const getSavedRecipeApi = (page, size) => {
  return request.get(`/users/saved?page=${page}&pageSize=${size}`)
}

export const sumbmitSaveRecipeApi = (id, status) => {
  return request.post(`/users/save/${id}`, { status: status })
}

export const getUserInfoApi = () => {
  return request.get(`/users`)
}

export const deleteAvatarApi = () => {
  return request.delete(`/users/avatar`)
}

export const updatePreferenceApi = (data) => {
  return request.post('/users/preference', data)
}

export const getPreferenceApi = () => {
  return request.get('/users/preference')
}

export const getAdminRecipesApi = (page, pageSize, keyword = '') => {
  const params = new URLSearchParams({
    page: String(page),
    pageSize: String(pageSize),
  })
  if (keyword && keyword.trim()) {
    params.append('keyword', keyword.trim())
  }
  return request.get(`/admin/recipes?${params.toString()}`)
}

export const updateAdminRecipeApi = (id, data) => {
  return request.put(`/admin/recipes/${id}`, data)
}

export const deleteAdminRecipeApi = (id) => {
  return request.delete(`/admin/recipes/${id}`)
}
