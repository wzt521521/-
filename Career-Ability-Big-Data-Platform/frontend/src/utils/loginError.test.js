import { describe, expect, it } from 'vitest'
import { getLoginErrorMessage } from './loginError'

describe('getLoginErrorMessage', () => {
  it('treats network errors and proxy 5xx responses as unavailable service', () => {
    expect(getLoginErrorMessage(new Error('Network Error'))).toContain('认证服务暂时不可用')
    expect(getLoginErrorMessage({ response: { status: 500, data: 'Proxy error' } })).toContain('认证服务暂时不可用')
    expect(getLoginErrorMessage({ response: { status: 503 } })).toContain('认证服务暂时不可用')
  })

  it('maps authentication and rate-limit failures', () => {
    expect(getLoginErrorMessage({ response: { status: 401 } })).toBe('用户名或密码错误')
    expect(getLoginErrorMessage({ response: { status: 429 } })).toContain('15 分钟后重试')
  })

  it('keeps a structured business error message', () => {
    expect(getLoginErrorMessage({
      response: { status: 400, data: { message: '账号格式不正确' } },
    })).toBe('账号格式不正确')
  })
})
