<template>
  <div class="landing-container">
    <h1>All Surveys</h1>
    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <ul v-else class="survey-list" :class="{ 'surveys-visible': !isAuthenticated }">
      <li v-for="survey in surveys" :key="survey.id" class="survey-item">
        <router-link :to="isAuthenticated ? `/surveys/${survey.id}` : '#'" 
                     class="survey-title" 
                     @click.prevent="!isAuthenticated && showLoginMessage()">
          {{ survey.title }}
        </router-link>
        <div class="survey-desc">{{ survey.description }}</div>
      </li>
      <li v-if="surveys.length === 0">No surveys found.</li>
    </ul>
    <div class="pagination" v-if="totalPages > 1">
      <button :disabled="page === 0" @click="prevPage">Prev</button>
      <span>Page {{ page + 1 }} of {{ totalPages }}</span>
      <button :disabled="page === totalPages - 1" @click="nextPage">Next</button>
    </div>
    
    <div v-if="!isAuthenticated" class="login-message-overlay">
      <div class="login-message">
        <h2>Authentication Required</h2>
        <p>You need to log in to view and participate in surveys.</p>
        <div class="login-actions">
          <router-link to="/login" class="login-btn">Login</router-link>
          <router-link to="/register" class="register-btn">Register</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'
import { isAuthenticated } from '../utils/authEvents'

const surveys = ref<any[]>([])
const loading = ref(true)
const error = ref('')
const page = ref(0)
const size = ref(10)
const totalPages = ref(1)

function showLoginMessage() {
  console.log('User needs to log in to access surveys')
}

async function fetchSurveys() {
  loading.value = true
  error.value = ''
  try {
    const res = await axios.get(`http://localhost:8080/api/surveys/all?page=${page.value}&size=${size.value}`)
    surveys.value = res.data.data || []
    totalPages.value = res.data.pagination?.totalPages || res.data.totalPages || 1
  } catch (e: any) {
    error.value = e?.response?.data?.message || 'Failed to fetch surveys.'
  } finally {
    loading.value = false
  }
}

function prevPage() {
  if (page.value > 0) {
    page.value--
  }
}
function nextPage() {
  if (page.value < totalPages.value - 1) {
    page.value++
  }
}

onMounted(fetchSurveys)
watch([page, size], fetchSurveys)
</script>

<style scoped>
.landing-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding-top: 2rem;
  position: relative;
}
.landing-container h1 {
  color: var(--color-accent);
  margin-bottom: 1rem;
}
.survey-list {
  list-style: none;
  padding: 0;
  width: 100%;
  max-width: 600px;
}
.survey-item {
  background: #232526;
  margin-bottom: 1rem;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px 0 rgba(0,0,0,0.08);
}
.survey-title {
  color: var(--color-secondary);
  font-weight: 600;
  font-size: 1.2em;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.2s;
  display: block;
}

.survey-title:hover {
  color: var(--color-accent);
}
.survey-desc {
  color: #aaa;
  margin-top: 0.3em;
}
.loading {
  color: var(--color-secondary);
}
.error {
  color: #ff4d4f;
}
.pagination {
  margin-top: 1.5rem;
  display: flex;
  gap: 1.5rem;
  align-items: center;
}
.pagination button {
  background: var(--color-secondary);
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 0.5em 1.2em;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}
.pagination button:disabled {
  background: #444;
  color: #888;
  cursor: not-allowed;
}
.view-survey-btn {
  display: inline-block;
  margin-top: 0.8rem;
  background: var(--color-accent);
  color: var(--color-primary);
  text-decoration: none;
  border-radius: 6px;
  padding: 0.4em 1em;
  font-weight: 600;
  font-size: 0.9em;
  transition: background 0.2s;
}
.view-survey-btn:hover {
  background: var(--color-secondary);
  color: #fff;
}

.surveys-visible {
  pointer-events: none;
  opacity: 0.7;
}

.login-message-overlay {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 100;
  width: auto;
  max-width: 90%;
}

.login-message {
  background-color: #232526;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);
  border: 1px solid #333;
}

.login-message h2 {
  color: var(--color-accent);
  margin-bottom: 1rem;
}

.login-message p {
  margin-bottom: 1.5rem;
  color: #e0e0e0;
}

.login-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.login-btn, .register-btn {
  display: inline-block;
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  font-weight: 600;
  text-decoration: none;
  transition: background-color 0.2s, transform 0.2s;
}

.login-btn {
  background-color: var(--color-accent);
  color: var(--color-primary);
}

.register-btn {
  background-color: var(--color-secondary);
  color: #fff;
}

.login-btn:hover, .register-btn:hover {
  transform: translateY(-2px);
}

.login-btn:hover {
  background-color: var(--color-secondary);
  color: #fff;
}

.register-btn:hover {
  background-color: var(--color-accent);
  color: var(--color-primary);
}
</style>