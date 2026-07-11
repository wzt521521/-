import { afterEach, describe, expect, it, vi } from 'vitest'
import axios from 'axios'
import { createRefreshCoordinator, isPublicAuthRequest, refreshSession } from './request'
import { getAccessToken, getRefreshToken } from './auth'

afterEach(() => {
  vi.restoreAllMocks()
  localStorage.clear()
})

describe('public authentication requests', () => {
  it('identifies endpoints that must not receive stale access tokens', () => {
    expect(isPublicAuthRequest('/auth/login')).toBe(true)
    expect(isPublicAuthRequest('/api/auth/register?source=web')).toBe(true)
    expect(isPublicAuthRequest('/auth/refresh')).toBe(true)
    expect(isPublicAuthRequest('/auth/me')).toBe(false)
  })
})

describe('token refresh coordination', () => {
  it('shares one refresh request across concurrent callers', async () => {
    let resolveRefresh
    const refreshRequest = vi.fn(() => new Promise((resolve) => {
      resolveRefresh = resolve
    }))
    const refresh = createRefreshCoordinator(refreshRequest)

    const first = refresh('refresh-token')
    const second = refresh('refresh-token')
    await Promise.resolve()

    expect(refreshRequest).toHaveBeenCalledTimes(1)
    resolveRefresh({ accessToken: 'new-access', refreshToken: 'new-refresh' })
    await expect(Promise.all([first, second])).resolves.toEqual([
      { accessToken: 'new-access', refreshToken: 'new-refresh' },
      { accessToken: 'new-access', refreshToken: 'new-refresh' },
    ])
  })

  it('persists and publishes rotated tokens', async () => {
    const dispatchEvent = vi.spyOn(window, 'dispatchEvent')
    vi.spyOn(axios, 'post').mockResolvedValueOnce({
      data: {
        data: { accessToken: 'rotated-access', refreshToken: 'rotated-refresh' },
      },
    })

    await refreshSession('old-refresh')

    expect(getAccessToken()).toBe('rotated-access')
    expect(getRefreshToken()).toBe('rotated-refresh')
    expect(dispatchEvent).toHaveBeenCalledWith(expect.objectContaining({
      type: 'auth:tokens-refreshed',
    }))
  })

  it('permits a new attempt after a failed refresh', async () => {
    const refreshRequest = vi.fn()
      .mockRejectedValueOnce(new Error('expired'))
      .mockResolvedValueOnce({ accessToken: 'recovered' })
    const refresh = createRefreshCoordinator(refreshRequest)

    await expect(refresh('expired-token')).rejects.toThrow('expired')
    await expect(refresh('valid-token')).resolves.toEqual({ accessToken: 'recovered' })
    expect(refreshRequest).toHaveBeenCalledTimes(2)
  })
})
