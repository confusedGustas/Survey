import { ref } from 'vue'
import Cookies from 'js-cookie'

export const isAuthenticated = ref(false)

export function validateToken() {
  const token = Cookies.get('accessToken')
  if (!token) return false
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const now = Math.floor(Date.now() / 1000)
    return payload.exp > now
  } catch {
    return false
  }
}

export function updateAuthState() {
  const wasAuthenticated = isAuthenticated.value
  isAuthenticated.value = validateToken()
  
  if (wasAuthenticated !== isAuthenticated.value) {
    window.dispatchEvent(new CustomEvent('auth-state-changed'))
    console.log('Auth state changed:', isAuthenticated.value ? 'logged in' : 'logged out')
  }
  
  return isAuthenticated.value
}

export function login(accessToken: string, refreshToken?: string) {
  if (accessToken) {
    console.log('Setting access token in cookie')
    Cookies.set('accessToken', accessToken)
  }
  if (refreshToken) {
    console.log('Setting refresh token in cookie')
    Cookies.set('refreshToken', refreshToken)
  }
  updateAuthState()
  window.dispatchEvent(new CustomEvent('auth-state-changed'))
}

export function logout() {
  console.log('Removing auth tokens from cookies')
  Cookies.remove('accessToken')
  Cookies.remove('refreshToken')
  updateAuthState()
  window.dispatchEvent(new CustomEvent('auth-state-changed'))
}

updateAuthState()