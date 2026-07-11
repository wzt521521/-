export function getLoginErrorMessage(error) {
  const status = error?.response?.status
  const serverMessage = error?.response?.data?.message

  if (!error?.response || status >= 500) {
    return '认证服务暂时不可用，请确认后端服务状态后重试'
  }
  if (status === 401) return '用户名或密码错误'
  if (status === 429) return '登录失败次数过多，请 15 分钟后重试'
  return serverMessage || '登录失败，请稍后重试'
}
