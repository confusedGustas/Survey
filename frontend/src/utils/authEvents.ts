import {ref} from 'vue'
import Cookies from 'js-cookie'
import axios from 'axios'

export const isAuthenticated = ref(false)
export const isRefreshing = ref(false)

const API_URL = 'http://localhost:8080'

export function validateToken() {
  const token = Cookies.get('accessToken')
  if (!token) return false
  try {
    const parts = token.split('.')
    if (parts.length !== 3) {
      return false
    }

    const payload = JSON.parse(
      decodeURIComponent(
        atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'))
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      )
    )
    
    const now = Math.floor(Date.now() / 1000)
    return payload.exp > now
  } catch (error) {
    return false
  }
}

export function getTokenExpiration(): number {
  const token = Cookies.get('accessToken')
  if (!token) return 0
  
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return 0
    
    const payload = JSON.parse(
      decodeURIComponent(
        atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'))
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      )
    )
    
    return payload.exp;
  } catch (error) {
    return 0;
  }
}

export function updateAuthState() {
  const wasAuthenticated = isAuthenticated.value
  isAuthenticated.value = validateToken()
  
  if (wasAuthenticated !== isAuthenticated.value) {
    window.dispatchEvent(new CustomEvent('auth-state-changed'))
  }
  
  return isAuthenticated.value
}

export async function refreshAccessToken(): Promise<boolean> {
  if (isRefreshing.value) return false
  
  const refreshToken = Cookies.get('refreshToken')
  if (!refreshToken) return false
  
  isRefreshing.value = true
  try {
    const response = await axios.post(
      `${API_URL}/auth/refresh`,
      {},
      { 
        params: { refreshToken },
        withCredentials: true 
      }
    )
    
    if (response.data.accessToken) {
      login(response.data.accessToken, response.data.refreshToken)
      return true
    }
    return false
  } catch (error) {
    return false
  } finally {
    isRefreshing.value = false
  }
}

export function login(accessToken: string, refreshToken?: string) {
  if (accessToken) {
    Cookies.set('accessToken', accessToken)
  }
  if (refreshToken) {
    Cookies.set('refreshToken', refreshToken)
  }
  updateAuthState()
  window.dispatchEvent(new CustomEvent('auth-state-changed'))
}

export function logout() {
  Cookies.remove('accessToken')
  Cookies.remove('refreshToken')
  updateAuthState()
  window.dispatchEvent(new CustomEvent('auth-state-changed'))
}

export function isTokenExpiringSoon(thresholdSeconds: number = 60): boolean {
  const expiration = getTokenExpiration()
  const now = Math.floor(Date.now() / 1000)
  return expiration - now < thresholdSeconds
}

updateAuthState()

setInterval(() => {
  if (isAuthenticated.value && isTokenExpiringSoon(300) && !isRefreshing.value) {
    refreshAccessToken().then(r => console.log(r))
  }
}, 60000)