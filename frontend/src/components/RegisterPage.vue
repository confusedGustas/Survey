<template>
  <form class="register-form" @submit.prevent="onRegister">
    <h2>Register</h2>
    <input v-model="username" type="text" placeholder="Username" required />
    <input v-model="email" type="email" placeholder="Email" required />
    <input v-model="password" type="password" placeholder="Password" required />
    <button type="submit">Register</button>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="success" class="success">Registration successful! You can now <router-link to='/login'>login</router-link>.</p>
  </form>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'

const username = ref('')
const email = ref('')
const password = ref('')
const error = ref('')
const success = ref(false)

async function onRegister() {
  error.value = ''
  success.value = false
  try {
    const res = await axios.post('http://localhost:8080/api/users', {
      username: username.value,
      email: email.value,
      password: password.value
    }, {
      headers: { 'Content-Type': 'application/json' },
      withCredentials: true
    })
    if (res.status === 201) {
      success.value = true
      username.value = ''
      email.value = ''
      password.value = ''
    }
  } catch (e: any) {
    error.value = e?.response?.data?.message || 'Registration failed.'
  }
}
</script>

<style scoped>
.register-form {
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
.register-form h2 {
  color: var(--color-accent);
  margin-bottom: 0.5rem;
}
.register-form .error {
  color: #ff4d4f;
  font-size: 0.95em;
  margin-top: 0.5rem;
}
.register-form .success {
  color: var(--color-secondary);
  font-size: 0.95em;
  margin-top: 0.5rem;
}
</style> 