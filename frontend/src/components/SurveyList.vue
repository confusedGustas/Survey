<template>
  <div class="survey-list">
    <div class="survey-list-header">
      <h2>Your Surveys</h2>
      <router-link v-if="isLoggedIn" to="/surveys/create" class="create-btn">+ Create Survey</router-link>
    </div>
    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <ul v-else>
      <li v-for="survey in surveys" :key="survey.id" class="survey-item">
        <span>{{ survey.title }}</span>
      </li>
      <li v-if="surveys.length === 0">No surveys found.</li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import Cookies from 'js-cookie'

const surveys = ref<any[]>([])
const loading = ref(true)
const error = ref('')
const isLoggedIn = computed(() => !!Cookies.get('accessToken'))

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const token = Cookies.get('accessToken')
    const res = await axios.get('http://localhost:8080/api/surveys/user', {
      headers: { 'Authorization': `Bearer ${token}` },
      withCredentials: true
    })
    surveys.value = res.data.data || []
  } catch (e: any) {
    error.value = e?.response?.data?.message || 'Failed to fetch surveys.'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.survey-list {
  background: #232526;
  padding: 2rem 2.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 16px 0 rgba(0,0,0,0.12);
  max-width: 500px;
  margin: 2rem auto;
}
.survey-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}
.create-btn {
  background: var(--color-accent);
  color: var(--color-primary);
  border-radius: 6px;
  padding: 0.5em 1.2em;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.2s, color 0.2s;
}
.create-btn:hover {
  background: var(--color-secondary);
  color: #fff;
}
.survey-list h2 {
  color: var(--color-accent);
  margin-bottom: 0;
}
.survey-item {
  padding: 0.7em 0;
  border-bottom: 1px solid #333;
  color: var(--color-secondary);
}
.survey-item:last-child {
  border-bottom: none;
}
.loading {
  color: var(--color-secondary);
}
.error {
  color: #ff4d4f;
}
</style>