<template>
  <nav class="navbar">
    <router-link to="/" class="nav-link">Home</router-link>
    <template v-if="!isAuthenticated">
      <router-link to="/login" class="nav-link">Login</router-link>
      <router-link to="/register" class="nav-link">Register</router-link>
    </template>
    <template v-else>
      <router-link to="/profile" class="nav-link">Profile</router-link>
      <router-link v-if="isAdmin" to="/users" class="nav-link">Users</router-link>
      <router-link v-if="isAdmin" to="/admin" class="nav-link">Admin</router-link>
      <a href="#" class="nav-link" @click.prevent="handleLogout">Logout</a>
    </template>
  </nav>
</template>

<script setup lang="ts">
import { computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { isAuthenticated, updateAuthState, logout } from '../utils/authEvents'
import Cookies from 'js-cookie'
import apiClient from '../utils/apiClient'

const router = useRouter()
const route = useRoute()

function getRoleFromToken() {
  const token = Cookies.get('accessToken')
  if (!token) return ''
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.role || ''
  } catch {
    return ''
  }
}

const isAdmin = computed(() => isAuthenticated.value && getRoleFromToken() === 'ADMIN')

function handleLogout() {
  logout()
  router.push('/')
}

async function checkApiHealth() {
  try {
    await apiClient.get('/auth/health')
    console.log('API health check successful')
  } catch (error) {
    console.error('API health check failed:', error)
  }
}

watch(() => route.path, () => {
  updateAuthState()
}, { immediate: true })

onMounted(() => {
  updateAuthState()
  checkApiHealth()

  window.addEventListener('auth-state-changed', () => {
    updateAuthState()
  })
})

onUnmounted(() => {
  window.removeEventListener('auth-state-changed', updateAuthState)
})
</script>

<style scoped>
.navbar {
  display: flex;
  gap: 2rem;
  background: var(--color-primary);
  padding: 1rem 2rem;
  border-bottom: 1px solid var(--color-secondary);
  align-items: center;
}
.nav-link {
  color: var(--color-secondary);
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s;
}
.nav-link:hover {
  color: var(--color-accent);
}
</style>