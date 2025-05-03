<template>
  <div class="landing-container">
    <h1>All Surveys</h1>
    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <ul v-else class="survey-list">
      <li v-for="survey in surveys" :key="survey.id" class="survey-item">
        <div class="survey-title">{{ survey.title }}</div>
        <div class="survey-desc">{{ survey.description }}</div>
        <!-- Future: <router-link :to="`/surveys/${survey.id}`">View</router-link> -->
      </li>
      <li v-if="surveys.length === 0">No surveys found.</li>
    </ul>
    <div class="pagination" v-if="totalPages > 1">
      <button :disabled="page === 0" @click="prevPage">Prev</button>
      <span>Page {{ page + 1 }} of {{ totalPages }}</span>
      <button :disabled="page === totalPages - 1" @click="nextPage">Next</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'

const surveys = ref<any[]>([])
const loading = ref(true)
const error = ref('')
const page = ref(0)
const size = ref(10)
const totalPages = ref(1)

async function fetchSurveys() {
  loading.value = true
  error.value = ''
  try {
    const res = await axios.get(`http://localhost:8080/api/surveys/all?page=${page.value}&size=${size.value}`)
    surveys.value = res.data.data || []
    totalPages.value = res.data.totalPages || 1
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
  justify-content: center;
  min-height: 70vh;
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
</style> 