import request from '../utils/request'

export async function fetchApiKeys(params) {
  const response = await request.get('/admin/api-keys', { params })
  return response.data
}

export async function createApiKey(payload) {
  const response = await request.post('/admin/api-keys', payload)
  return response.data
}

export async function updateApiKeyStatus(id, status) {
  const response = await request.patch(`/admin/api-keys/${id}/status`, { status })
  return response.data
}

export function deleteApiKey(id) {
  return request.delete(`/admin/api-keys/${id}`)
}

export async function fetchApiCallLogs(params) {
  const response = await request.get('/admin/api-call-logs', { params })
  return response.data
}

export async function fetchApiCallStatistics() {
  const response = await request.get('/admin/api-call-statistics')
  return response.data
}
