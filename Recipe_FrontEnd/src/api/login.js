import request from '../utils/request'

export const loginApi = (user) => {
  return request.post(`/login`, user)
}

export const registerApi = (user) => {
  return request.post(`/register`, user)
}
export const logOutApi = () => {
  return request.post(`/logout`)
}
