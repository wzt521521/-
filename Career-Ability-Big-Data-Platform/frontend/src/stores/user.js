import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  changeCurrentPassword,
  getCurrentUser,
  loginAccount,
  logoutAccount,
  registerAccount,
  updateCurrentProfile,
} from '../api/auth'
import {
  clearSession,
  getAccessToken,
  getRefreshToken,
  getStoredUser,
  persistSession,
  updateStoredUser,
} from '../utils/auth'
import { hasPermission as checkPermission } from '../utils/permission'

export const useUserStore = defineStore('user', () => {
  const accessToken = ref(getAccessToken())
  const refreshToken = ref(getRefreshToken())
  const userInfo = ref(getStoredUser())
  const validated = ref(false)

  const syncRefreshedTokens = (event) => {
    accessToken.value = event.detail.accessToken
    refreshToken.value = event.detail.refreshToken
  }
  window.addEventListener('auth:tokens-refreshed', syncRefreshedTokens)

  const isAuthenticated = computed(() => Boolean(accessToken.value))
  const displayName = computed(() => userInfo.value?.realName || userInfo.value?.username || '')

  async function login(payload) {
    clearLocalSession()
    const session = await loginAccount(payload)
    setSession(session)
    validated.value = true
    return session
  }

  function register(payload) {
    return registerAccount(payload)
  }

  async function validateSession() {
    if (!accessToken.value) return null
    const currentUser = await getCurrentUser()
    userInfo.value = currentUser
    updateStoredUser(currentUser)
    validated.value = true
    return currentUser
  }

  async function updateProfile(payload) {
    const currentUser = await updateCurrentProfile(payload)
    userInfo.value = currentUser
    updateStoredUser(currentUser)
    return currentUser
  }

  function changePassword(payload) {
    return changeCurrentPassword(payload)
  }

  async function logout() {
    try {
      if (accessToken.value) {
        await logoutAccount(refreshToken.value)
      }
    } finally {
      clearLocalSession()
    }
  }

  function clearLocalSession() {
    clearSession()
    accessToken.value = null
    refreshToken.value = null
    userInfo.value = null
    validated.value = false
  }

  function hasPermission(permission) {
    return checkPermission(userInfo.value, permission)
  }

  function setSession(session) {
    accessToken.value = session.accessToken
    refreshToken.value = session.refreshToken
    userInfo.value = session.userInfo
    persistSession(session)
  }

  return {
    accessToken,
    refreshToken,
    userInfo,
    validated,
    isAuthenticated,
    displayName,
    login,
    register,
    validateSession,
    updateProfile,
    changePassword,
    logout,
    clearLocalSession,
    hasPermission,
  }
})
