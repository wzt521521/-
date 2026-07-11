const ACCESS_TOKEN_KEY = 'career_access_token'
const REFRESH_TOKEN_KEY = 'career_refresh_token'
const USER_INFO_KEY = 'career_user_info'

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function getStoredUser() {
  const value = localStorage.getItem(USER_INFO_KEY)
  if (!value) return null
  try {
    return JSON.parse(value)
  } catch {
    localStorage.removeItem(USER_INFO_KEY)
    return null
  }
}

export function persistSession({ accessToken, refreshToken, userInfo }) {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

export function updateAccessToken(accessToken, refreshToken) {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  }
}

export function updateStoredUser(userInfo) {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

export function clearSession() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(USER_INFO_KEY)
}
