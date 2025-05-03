import { ref } from 'vue'
import Cookies from 'js-cookie'

// Create a reactive state for authentication that can be imported by any component
export const isAuthenticated = ref(false)

// Function to validate token
export function validateToken() {
  const token = Cookies.get('accessToken')
  if (!token) return false
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const now = Math.floor(Date.now() / 1000)
    return payload.exp > now // Check if token is expired
  } catch {
    return false
  }
}

// Update authentication state
export function updateAuthState() {
  const wasAuthenticated = isAuthenticated.value
  isAuthenticated.value = validateToken()
  
  // If authentication state changed, dispatch a custom event
  if (wasAuthenticated !== isAuthenticated.value) {
    window.dispatchEvent(new CustomEvent('auth-state-changed'))
    console.log('Auth state changed:', isAuthenticated.value ? 'logged in' : 'logged out')
  }
  
  return isAuthenticated.value
}

// Login handler
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
  // Always dispatch event on login attempt for debugging
  window.dispatchEvent(new CustomEvent('auth-state-changed'))
}

// Logout handler
export function logout() {
  console.log('Removing auth tokens from cookies')
  Cookies.remove('accessToken')
  Cookies.remove('refreshToken')
  updateAuthState()
  window.dispatchEvent(new CustomEvent('auth-state-changed'))
}

// Initialize auth state
updateAuthState()