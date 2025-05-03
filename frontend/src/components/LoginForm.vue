<template>
  <form class="login-form" @submit.prevent="onLogin">
    <h2>Login</h2>
    <input v-model="username" type="text" placeholder="Username" required />
    <input v-model="password" type="password" placeholder="Password" required />
    <button type="submit">Login</button>
    <p v-if="error" class="error">{{ error }}</p>
  </form>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const username = ref('')
const password = ref('')
const error = ref('')

const emit = defineEmits(['login'])

function onLogin() {
  if (!username.value || !password.value) {
    error.value = 'Please enter both username and password.'
    return
  }
  error.value = ''
  emit('login', { username: username.value, password: password.value })
}
</script>

<style scoped>
.login-form {
  background: #232526;
  padding: 2rem 2.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 16px 0 rgba(0,0,0,0.12);
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 350px;
  margin: 2rem auto;
}
.login-form h2 {
  color: var(--color-accent);
  margin-bottom: 0.5rem;
}
.login-form .error {
  color: #ff4d4f;
  font-size: 0.95em;
  margin-top: 0.5rem;
}
</style> 