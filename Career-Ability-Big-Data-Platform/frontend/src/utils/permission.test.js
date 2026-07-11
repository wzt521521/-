import { describe, expect, it } from 'vitest'
import { hasAnyPermission, hasPermission } from './permission'

describe('permission checks', () => {
  it('grants administrators every permission', () => {
    expect(hasPermission({ roles: ['ROLE_ADMIN'], permissions: [] }, 'user:delete')).toBe(true)
  })

  it('checks regular users against their explicit permissions', () => {
    const user = { roles: ['ROLE_TEACHER'], permissions: ['report:view'] }

    expect(hasPermission(user, 'report:view')).toBe(true)
    expect(hasPermission(user, 'user:delete')).toBe(false)
    expect(hasPermission(null, 'report:view')).toBe(false)
  })

  it('accepts any matching route permission and allows unrestricted routes', () => {
    const user = { roles: ['ROLE_STUDENT'], permissions: ['recommend:view'] }

    expect(hasAnyPermission(user, ['api:docs', 'recommend:view'])).toBe(true)
    expect(hasAnyPermission(user, ['api:docs'])).toBe(false)
    expect(hasAnyPermission(user)).toBe(true)
  })
})
