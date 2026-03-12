import axios from 'axios'

import router from '../router'

const request = axios.create({
  baseURL: '/api',

  timeout: 600000,
})
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response.status === 403) {
      router.push('/login')
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
