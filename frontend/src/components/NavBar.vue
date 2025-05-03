<template>
  <nav class="navbar">
    <router-link to="/" class="nav-link">Home</router-link>
    <template v-if="!isLoggedIn">
      <router-link to="/login" class="nav-link">Login</router-link>
      <router-link to="/register" class="nav-link">Register</router-link>
    </template>
    <template v-else>
      <router-link to="/profile" class="nav-link">Profile</router-link>
      <router-link v-if="isAdmin" to="/users" class="nav-link">Users</router-link>
      <router-link v-if="isAdmin" to="/admin" class="nav-link">Admin</router-link>
      <a href="#" class="nav-link" @click.prevent="logout">Logout</a>
    </template>
  </nav>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isLoggedIn = ref(!!localStorage.getItem('accessToken'))

function getRoleFromToken() {
  const token = localStorage.getItem('accessToken')
  if (!token) return ''
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.role || ''
  } catch {
    return ''
  }
}

const isAdmin = computed(() => isLoggedIn.value && getRoleFromToken() === 'ADMIN')

function updateLoginState() {
  isLoggedIn.value = !!localStorage.getItem('accessToken')
}

function logout() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  updateLoginState()
  router.push('/login')
}

onMounted(() => {
  window.addEventListener('storage', updateLoginState)
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
.nav-link.router-link-exact-active {
  color: var(--color-accent);
}
.nav-link:hover {
  color: var(--color-accent);
}
</style> 