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

const apiError = ref('')

async function handleLogin({ username, password }: { username: string, password: string }) {
  apiError.value = ''
  try {
    const res = await axios.post('http://localhost:8080/auth/login', {
      username,
      password
    }, {
      withCredentials: true,
      headers: { 'Content-Type': 'application/json' }
    })
    const data = res.data
    if (data.accessToken) {
      localStorage.setItem('accessToken', data.accessToken)
    }
    if (data.refreshToken) {
      localStorage.setItem('refreshToken', data.refreshToken)
    }
    window.location.href = '/profile'
  } catch (e: any) {
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