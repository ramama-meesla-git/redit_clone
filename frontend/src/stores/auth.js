import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const accessToken = ref(localStorage.getItem('accessToken') || '')

  const isAuthenticated = computed(() => !!accessToken.value)
  const currentUser = computed(() => user.value)

  async function register(username, email, password) {
    const { data } = await api.post('/auth/register', { username, email, password })
    setAuth(data)
    return data
  }

  async function login(usernameOrEmail, password) {
    const { data } = await api.post('/auth/login', { usernameOrEmail, password })
    setAuth(data)
    return data
  }

  async function fetchCurrentUser() {
    try {
      const { data } = await api.get('/auth/me')
      user.value = data
      localStorage.setItem('user', JSON.stringify(data))
    } catch (e) {
      logout()
    }
  }

  function setAuth(authData) {
    accessToken.value = authData.accessToken
    user.value = authData.user
    localStorage.setItem('accessToken', authData.accessToken)
    localStorage.setItem('refreshToken', authData.refreshToken)
    localStorage.setItem('user', JSON.stringify(authData.user))
  }

  function logout() {
    api.post('/auth/logout').catch(() => {})
    accessToken.value = ''
    user.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  return { user, accessToken, isAuthenticated, currentUser, register, login, fetchCurrentUser, logout }
})
