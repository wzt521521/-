import request from '../utils/request'

export async function registerAccount(payload) {
  const response = await request.post('/auth/register', payload)
  return response.data
}

export async function loginAccount(payload) {
  const response = await request.post('/auth/login', payload, { suppressErrorMessage: true })
  return response.data
}

export async function getCurrentUser() {
  const response = await request.get('/auth/me')
  return response.data
}

export async function updateCurrentProfile(payload) {
  const response = await request.put('/auth/profile', payload)
  return response.data
}

export function changeCurrentPassword(payload) {
  return request.put('/auth/password', payload)
}

export function logoutAccount(refreshToken) {
  return request.post('/auth/logout', { refreshToken })
}
