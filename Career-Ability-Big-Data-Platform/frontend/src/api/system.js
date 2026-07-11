import request from '../utils/request'

export async function fetchUsers(params) {
  const response = await request.get('/admin/users', { params })
  return response.data
}

export async function createUser(payload) {
  const response = await request.post('/admin/users', payload)
  return response.data
}

export async function importUsers(file) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await request.post('/admin/users/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return response.data
}

export function downloadUserImportTemplate() {
  return request.get('/admin/users/import-template', { responseType: 'blob' })
}

export async function updateUser(id, payload) {
  const response = await request.put(`/admin/users/${id}`, payload)
  return response.data
}

export async function updateUserStatus(id, status) {
  const response = await request.patch(`/admin/users/${id}/status`, { status })
  return response.data
}

export function resetUserPassword(id, newPassword) {
  return request.post(`/admin/users/${id}/reset-password`, { newPassword })
}

export function deleteUser(id) {
  return request.delete(`/admin/users/${id}`)
}

export async function fetchRoles() {
  const response = await request.get('/admin/roles')
  return response.data
}

export async function createRole(payload) {
  const response = await request.post('/admin/roles', payload)
  return response.data
}

export async function updateRole(id, payload) {
  const response = await request.put(`/admin/roles/${id}`, payload)
  return response.data
}

export function deleteRole(id) {
  return request.delete(`/admin/roles/${id}`)
}

export async function fetchPermissionTree() {
  const response = await request.get('/admin/permissions/tree')
  return response.data
}

export async function replaceRolePermissions(id, permissionIds) {
  const response = await request.put(`/admin/roles/${id}/permissions`, { permissionIds })
  return response.data
}

export async function fetchOperationLogs(params) {
  const response = await request.get('/admin/operation-logs', { params })
  return response.data
}

export async function fetchOperationLog(id) {
  const response = await request.get(`/admin/operation-logs/${id}`)
  return response.data
}
