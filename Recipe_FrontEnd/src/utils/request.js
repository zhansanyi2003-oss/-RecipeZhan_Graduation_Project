import axios from 'axios'

import router from '../router'

import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',

  timeout: 600000,
})
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    const status = error?.response?.status
    const backendMessage = error?.response?.data?.message || error?.response?.data?.msg

    if (status === 401) {
      localStorage.removeItem('loginUser')
      localStorage.removeItem('token_exp')

      ElMessage.warning(backendMessage || 'Your session expired. Please sign in again.')

      router.push('/login')
    }

    if (status === 403) {
      ElMessage.error(backendMessage || 'You do not have permission to access this page.')
    }

    return Promise.reject(error)
  },
)

request.interceptors.request.use(
  (config) => {
    const loginUser = JSON.parse(localStorage.getItem('loginUser'))
    if (loginUser && loginUser.Authorization) {
      config.headers.Authorization = loginUser.Authorization
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

export default request
