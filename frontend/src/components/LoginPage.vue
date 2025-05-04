<template>
  <div class="login-page">
    <LoginForm @login="handleLogin" />
    <p v-if="apiError" class="api-error">{{ apiError }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import LoginForm from './LoginForm.vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { login } from '../utils/authEvents'

const apiError = ref('')
const router = useRouter()

async function handleLogin({ username, password }: { username: string, password: string }) {
  apiError.value = ''
  try {
    console.log('Login attempt with username:', username)
    const res = await axios.post('http://localhost:8080/auth/login', {
      username,
      password
    }, {
      withCredentials: true,
      headers: { 'Content-Type': 'application/json' }
    })
    
    console.log('Login response status:', res.status)
    const data = res.data
    
    if (data.accessToken) {
      console.log('Login successful, received access token')

      login(data.accessToken, data.refreshToken)

      setTimeout(() => {
        console.log('Navigating to profile page')
        router.push('/profile')
      }, 300)
    } else {
      console.error('No access token in response')
      apiError.value = 'Authentication failed: Server did not return a token'
    }
  } catch (e: any) {
    console.error('Login error:', e)
    apiError.value = e?.response?.data?.message || 'Login failed.'
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}
.api-error {
  color: #ff4d4f;
  margin-top: 1rem;
}
</style>