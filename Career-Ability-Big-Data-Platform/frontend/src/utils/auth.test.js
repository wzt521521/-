import { beforeEach, describe, expect, it } from 'vitest'
import {
  clearSession,
  getAccessToken,
  getRefreshToken,
  getStoredUser,
  persistSession,
  updateAccessToken,
} from './auth'

describe('auth storage', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('persists and clears a complete session', () => {
    const userInfo = { id: 7, username: 'student01' }
    persistSession({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      userInfo,
    })

    expect(getAccessToken()).toBe('access-token')
    expect(getRefreshToken()).toBe('refresh-token')
    expect(getStoredUser()).toEqual(userInfo)

    clearSession()
    expect(getAccessToken()).toBeNull()
    expect(getRefreshToken()).toBeNull()
    expect(getStoredUser()).toBeNull()
  })

  it('rotates tokens without discarding the current refresh token', () => {
    persistSession({ accessToken: 'old-access', refreshToken: 'old-refresh', userInfo: {} })

    updateAccessToken('new-access')
    expect(getAccessToken()).toBe('new-access')
    expect(getRefreshToken()).toBe('old-refresh')

    updateAccessToken('latest-access', 'new-refresh')
    expect(getAccessToken()).toBe('latest-access')
    expect(getRefreshToken()).toBe('new-refresh')
  })

  it('drops malformed cached user data', () => {
    localStorage.setItem('career_user_info', '{invalid-json')

    expect(getStoredUser()).toBeNull()
    expect(localStorage.getItem('career_user_info')).toBeNull()
  })
})
