import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'
import Cookies from 'js-cookie'
import { logout } from './authEvents'

const API_URL = 'http://localhost:8080'

interface QueueItem {
  config: AxiosRequestConfig;
  resolve: (value: unknown) => void;
  reject: (reason?: any) => void;
}

const apiClient = axios.create({
  baseURL: API_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

let isRefreshingToken = false

let requestsQueue: QueueItem[] = []

const processQueue = (token: string): void => {
  requestsQueue.forEach(({ config, resolve, reject }) => {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
    axios(config).then(resolve).catch(reject)
  })
  requestsQueue = []
}

const refreshAuthToken = async (): Promise<string | null> => {
  const refreshToken = Cookies.get('refreshToken')
  if (!refreshToken) return null

  try {
    const response = await axios.post(
      `${API_URL}/auth/refresh`,
      null,
      {
        params: { refreshToken },
        withCredentials: true
      }
    )

    if (response.data?.accessToken) {
      Cookies.set('accessToken', response.data.accessToken)
      if (response.data.refreshToken) {
        Cookies.set('refreshToken', response.data.refreshToken)
      }
      return response.data.accessToken
    }
    return null
  } catch (error) {
    console.error('Failed to refresh token', error)
    return null
  }
}

apiClient.interceptors.request.use(
  config => {
    const token = Cookies.get('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

apiClient.interceptors.response.use(
  response => {
    if (response.data?.accessToken) {
      Cookies.set('accessToken', response.data.accessToken)
    }
    if (response.data?.refreshToken) {
      Cookies.set('refreshToken', response.data.refreshToken)
    }
    return response
  },
  async error => {
    const originalRequest = error.config

    if (!error.response || error.response.status !== 401 || originalRequest._retry) {
      return Promise.reject(error)
    }

    originalRequest._retry = true

    if (isRefreshingToken) {
      return new Promise((resolve, reject) => {
        requestsQueue.push({ config: originalRequest, resolve, reject })
      })
    }

    isRefreshingToken = true

    try {
      const newToken = await refreshAuthToken()

      if (newToken) {
        originalRequest.headers.Authorization = `Bearer ${newToken}`

        processQueue(newToken)

        return axios(originalRequest)
      } else {
        requestsQueue.forEach(request => {
          request.reject(error)
        })
        requestsQueue = []
        
        logout()
        window.location.href = '/'
        return Promise.reject(error)
      }
    } catch (refreshError) {
      requestsQueue.forEach(request => {
        request.reject(error)
      })
      requestsQueue = []
      
      logout()
      window.location.href = '/'
      return Promise.reject(refreshError)
    } finally {
      isRefreshingToken = false
    }
  }
)

export default apiClient