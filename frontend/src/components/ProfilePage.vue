<template>
  <div class="profile-container">
    <h2>Profile</h2>
    <div v-if="redirecting" class="loading">Redirecting...</div>
    <div v-else-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else class="profile-info">
      <div><strong>Username:</strong> {{ user.username }}</div>
      <div><strong>Email:</strong> {{ user.email }}</div>
      <div><strong>Role:</strong> {{ user.role }}</div>
      <button @click="logout" class="logout-btn">Logout</button>
    </div>
    <div v-if="isLoggedIn && !redirecting" class="user-surveys">
      <div class="survey-list-header">
        <h3>Your Surveys</h3>
      </div>
      <div class="create-btn-wrapper">
        <router-link to="/surveys/create" class="create-btn">+ Create Survey</router-link>
      </div>
      <div v-if="surveysLoading" class="loading">Loading...</div>
      <div v-else-if="surveysError" class="error">{{ surveysError }}</div>
      <ul v-else class="survey-list">
        <li v-for="survey in surveys" :key="survey.id" class="survey-item">
          <router-link :to="`/surveys/${survey.id}`" class="survey-title">{{ survey.title }}</router-link>
        </li>
        <li v-if="surveys.length === 0">No surveys found.</li>
      </ul>
      <div class="pagination" v-if="totalPages > 1">
        <button :disabled="page === 0" @click="prevPage">Prev</button>
        <span>Page {{ page + 1 }} of {{ totalPages }}</span>
        <button :disabled="page === totalPages - 1" @click="nextPage">Next</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import Cookies from 'js-cookie'

const user = ref<any>({})
const loading = ref(true)
const error = ref('')
const redirecting = ref(false)
const router = useRouter()

const surveys = ref<any[]>([])
const surveysLoading = ref(true)
const surveysError = ref('')
const page = ref(0)
const size = ref(10)
const totalPages = ref(1)
const isLoggedIn = computed(() => !!Cookies.get('accessToken'))

function getUsernameFromToken() {
  const token = Cookies.get('accessToken')
  if (!token) return ''
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.sub || payload.username || ''
  } catch {
    return ''
  }
}

function validateToken() {
  const token = Cookies.get('accessToken');
  if (!token) return false;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const now = Math.floor(Date.now() / 1000);
    return payload.exp > now; // Check if token is expired
  } catch {
    return false;
  }
}

function logout() {
  Cookies.remove('accessToken')
  Cookies.remove('refreshToken')
  router.push('/login')
}

async function fetchUserSurveys() {
  surveysLoading.value = true
  surveysError.value = ''
  try {
    const token = Cookies.get('accessToken')
    const res = await axios.get(`http://localhost:8080/api/surveys/user?page=${page.value}&size=${size.value}`,
      {
        headers: { 'Authorization': `Bearer ${token}` },
        withCredentials: true
      })
    surveys.value = res.data.data || []
    totalPages.value = res.data.pagination?.totalPages || 1

    // Store tokens in cookies if present in the response
    if (res.data.accessToken) Cookies.set('accessToken', res.data.accessToken)
    if (res.data.refreshToken) Cookies.set('refreshToken', res.data.refreshToken)
  } catch (e: any) {
    surveysError.value = e?.response?.data?.message || 'Failed to fetch surveys.'
  } finally {
    surveysLoading.value = false
  }
}

function prevPage() {
  if (page.value > 0) page.value--
}
function nextPage() {
  if (page.value < totalPages.value - 1) page.value++
}

onMounted(async () => {
  if (!isLoggedIn.value || !validateToken()) {
    redirecting.value = true
    await router.replace('/')
    return
  }
  loading.value = true
  error.value = ''
  const username = getUsernameFromToken()
  if (!username) {
    redirecting.value = true
    await router.replace('/')
    return
  }
  try {
    const token = Cookies.get('accessToken')
    const res = await axios.get(`http://localhost:8080/api/users/username/${username}`, {
      headers: { 'Authorization': `Bearer ${token}` },
      withCredentials: true
    })
    user.value = res.data.data || {}

    // Store tokens in cookies if present in the response
    if (res.data.accessToken) Cookies.set('accessToken', res.data.accessToken)
    if (res.data.refreshToken) Cookies.set('refreshToken', res.data.refreshToken)

    await fetchUserSurveys()
  } catch (e: any) {
    error.value = e?.response?.data?.message || 'Failed to fetch profile.'
    redirecting.value = true
    setTimeout(() => router.replace('/'), 1000)
  } finally {
    loading.value = false
  }
})

watch([page, size], fetchUserSurveys)
</script>

<style scoped>
.profile-container {
  background: #232526;
  padding: 2rem 2.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 16px 0 rgba(0,0,0,0.12);
  max-width: 500px;
  margin: 2rem auto;
  color: #e0e0e0;
}
.profile-container h2 {
  color: var(--color-accent);
  margin-bottom: 1rem;
}
.profile-info > div {
  margin-bottom: 0.7em;
}
.logout-btn {
  background: var(--color-accent);
  color: var(--color-primary);
  border: none;
  border-radius: 6px;
  padding: 0.6em 1.2em;
  font-weight: 600;
  cursor: pointer;
  margin-top: 1rem;
  transition: background 0.2s, color 0.2s;
}
.logout-btn:hover {
  background: var(--color-secondary);
  color: #fff;
}
.user-surveys {
  margin-top: 2rem;
}
.survey-list-header {
  margin-bottom: 0.5rem;
}
.survey-list-header h3 {
  text-align: center;
  width: 100%;
  color: var(--color-accent);
  margin-bottom: 0.5rem;
}
.survey-list {
  list-style: none;
  padding: 0;
}
.survey-item {
  padding: 0.7em 0;
  border-bottom: 1px solid #333;
  color: var(--color-secondary);
}
.survey-item:last-child {
  border-bottom: none;
}
.survey-title {
  color: var(--color-secondary);
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.2s;
  display: block;
}

.survey-title:hover {
  color: var(--color-accent);
}
.survey-actions {
  margin-top: 0.5rem;
}
.view-survey-btn {
  background: var(--color-accent);
  color: var(--color-primary);
  border-radius: 6px;
  padding: 0.3em 0.8em;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.2s, color 0.2s;
}
.view-survey-btn:hover {
  background: var(--color-secondary);
  color: #fff;
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
.create-btn-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 1.2rem;
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
</style>