import axios from 'axios'
import { ElMessage } from 'element-plus'
import {
  clearSession,
  getAccessToken,
  getRefreshToken,
  updateAccessToken,
} from './auth'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
const service = axios.create({
  baseURL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

const PUBLIC_AUTH_PATHS = ['/auth/login', '/auth/register', '/auth/refresh']

export function isPublicAuthRequest(url = '') {
  const path = url.split('?')[0]
  return PUBLIC_AUTH_PATHS.some((publicPath) => path.endsWith(publicPath))
}

export function createRefreshCoordinator(refreshRequest) {
  let refreshPromise = null

  return (refreshToken) => {
    if (!refreshPromise) {
      refreshPromise = Promise.resolve()
        .then(() => refreshRequest(refreshToken))
        .finally(() => {
          refreshPromise = null
        })
    }
    return refreshPromise
  }
}

export const refreshSession = createRefreshCoordinator((refreshToken) => axios
  .post(`${baseURL}/auth/refresh`, { refreshToken })
  .then((response) => {
    const tokens = response.data.data
    updateAccessToken(tokens.accessToken, tokens.refreshToken)
    window.dispatchEvent(new CustomEvent('auth:tokens-refreshed', { detail: tokens }))
    return tokens
  }))

service.interceptors.request.use((config) => {
  const accessToken = getAccessToken()
  if (accessToken && !isPublicAuthRequest(config.url)) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

service.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const originalRequest = error.config
    const status = error.response?.status
    const isPublicAuth = isPublicAuthRequest(originalRequest?.url)

    if (status === 401 && originalRequest && !originalRequest._retry && !isPublicAuth) {
      const refreshToken = getRefreshToken()
      if (refreshToken) {
        originalRequest._retry = true
        try {
          const tokens = await refreshSession(refreshToken)
          originalRequest.headers.Authorization = `Bearer ${tokens.accessToken}`
          return service(originalRequest)
        } catch {
          redirectToLogin()
          return Promise.reject(error)
        }
      }
      redirectToLogin()
    }

    if (!originalRequest?.suppressErrorMessage && (status !== 401 || isPublicAuth)) {
      ElMessage.error(error.response?.data?.message || '请求失败，请稍后重试')
    }
    return Promise.reject(error)
  },
)

function redirectToLogin() {
  clearSession()
  if (window.location.pathname !== '/login') {
    const redirect = encodeURIComponent(window.location.pathname + window.location.search)
    window.location.assign(`/login?redirect=${redirect}`)
  }
}

export default service
