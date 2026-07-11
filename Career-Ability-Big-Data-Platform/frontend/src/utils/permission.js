export function hasPermission(userInfo, permission) {
  if (!permission) return true
  if (!userInfo) return false
  if (userInfo.roles?.includes('ROLE_ADMIN')) return true
  return userInfo.permissions?.includes(permission) ?? false
}

export function hasAnyPermission(userInfo, permissions = []) {
  return permissions.length === 0 || permissions.some((permission) => hasPermission(userInfo, permission))
}
