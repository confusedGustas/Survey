<template>
  <div class="admin-dashboard-container">
    <h2>Admin Dashboard</h2>
    <div class="tabs">
      <button v-for="tab in tabs" :key="tab" :class="['tab-btn', { active: currentTab === tab }]" @click="currentTab = tab">
        {{ tab }}
      </button>
    </div>
    <div class="tab-content">
      <div v-if="currentTab === 'Global Search'">
        <h3>Global Search</h3>
        <form @submit.prevent="onSearch" class="search-form">
          <input v-model="searchQuery" type="text" placeholder="Search all entities..." class="search-input" />
          <button type="submit" class="search-btn">Search</button>
        </form>
        <div v-if="searchLoading" class="loading">Loading...</div>
        <div v-else-if="searchError" class="error">{{ searchError }}</div>
        <div v-else-if="searchResults.length" class="results-container">
          <div v-for="item in searchResults" :key="item.id + '-' + item.type" class="result-card">
            <div class="result-header">
              <span class="result-type">{{ item.type }}</span>
              <span class="result-id">ID: {{ item.id }}</span>
            </div>
            <div class="result-index">Index: {{ item.index }}</div>
            <div class="json-content">
              <pre v-if="isValidJson(item.content)" class="json-formatted">{{ formatJSON(item.content) }}</pre>
              <pre v-else class="json-raw">{{ item.content }}</pre>
            </div>
          </div>
        </div>
        <div v-else-if="searchResults.length === 0 && searchQuery && !searchLoading">No results found.</div>
        <div class="pagination" v-if="totalPages > 1">
          <button :disabled="page === 0" @click="prevPage">Prev</button>
          <span>Page {{ page + 1 }} of {{ totalPages }}</span>
          <button :disabled="page === totalPages - 1" @click="nextPage">Next</button>
        </div>
      </div>
      
      <div v-else-if="currentTab === 'Survey Search'">
        <h3>Survey Search</h3>
        <form @submit.prevent="onSurveySearch" class="search-form">
          <input v-model="surveySearchQuery" type="text" placeholder="Search surveys by title or description..." class="search-input" />
          <button type="submit" class="search-btn">Search</button>
        </form>
        <div v-if="surveySearchLoading" class="loading">Loading...</div>
        <div v-else-if="surveySearchError" class="error">{{ surveySearchError }}</div>
        <div v-else-if="surveySearchResults.length" class="results-container">
          <div v-for="survey in surveySearchResults" :key="survey.id" class="result-card">
            <div class="result-header">
              <span class="result-type">Survey</span>
              <span class="result-id">ID: {{ survey.id }}</span>
            </div>
            <div class="result-content">
              <div><strong>Title:</strong> {{ survey.title }}</div>
              <div><strong>Description:</strong> {{ survey.description }}</div>
            </div>
          </div>
        </div>
        <div v-else-if="surveySearchResults.length === 0 && surveySearchQuery && !surveySearchLoading">No results found.</div>
        <div class="pagination" v-if="surveyTotalPages > 1">
          <button :disabled="surveyPage === 0" @click="surveyPrevPage">Prev</button>
          <span>Page {{ surveyPage + 1 }} of {{ surveyTotalPages }}</span>
          <button :disabled="surveyPage === surveyTotalPages - 1" @click="surveyNextPage">Next</button>
        </div>
      </div>

      <div v-else-if="currentTab === 'Question Search'">
        <h3>Question Search</h3>
        <form @submit.prevent="onQuestionSearch" class="search-form">
          <input v-model="questionSearchQuery" type="text" placeholder="Search questions by content..." class="search-input" />
          <button type="submit" class="search-btn">Search</button>
        </form>
        <div v-if="questionSearchLoading" class="loading">Loading...</div>
        <div v-else-if="questionSearchError" class="error">{{ questionSearchError }}</div>
        <div v-else-if="questionSearchResults.length" class="results-container">
          <div v-for="q in questionSearchResults" :key="q.id" class="result-card">
            <div class="result-header">
              <span class="result-type">Question</span>
              <span class="result-id">ID: {{ q.id }}</span>
            </div>
            <div class="result-content">
              <div><strong>Survey ID:</strong> {{ q.surveyId }}</div>
              <div><strong>Content:</strong> {{ q.content }}</div>
              <div><strong>Type:</strong> {{ q.questionType }}</div>
            </div>
          </div>
        </div>
        <div v-else-if="questionSearchResults.length === 0 && questionSearchQuery && !questionSearchLoading">No results found.</div>
        <div class="pagination" v-if="questionTotalPages > 1">
          <button :disabled="questionPage === 0" @click="questionPrevPage">Prev</button>
          <span>Page {{ questionPage + 1 }} of {{ questionTotalPages }}</span>
          <button :disabled="questionPage === questionTotalPages - 1" @click="questionNextPage">Next</button>
        </div>
      </div>

      <div v-else-if="currentTab === 'Choice Search'">
        <h3>Choice Search</h3>
        <form @submit.prevent="onChoiceSearch" class="search-form">
          <input v-model="choiceSearchQuery" type="text" placeholder="Search choices by text..." class="search-input" />
          <button type="submit" class="search-btn">Search</button>
        </form>
        <div v-if="choiceSearchLoading" class="loading">Loading...</div>
        <div v-else-if="choiceSearchError" class="error">{{ choiceSearchError }}</div>
        <div v-else-if="choiceSearchResults.length" class="results-container">
          <div v-for="c in choiceSearchResults" :key="c.id" class="result-card">
            <div class="result-header">
              <span class="result-type">Choice</span>
              <span class="result-id">ID: {{ c.id }}</span>
            </div>
            <div class="result-content">
              <div><strong>Question ID:</strong> {{ c.questionId }}</div>
              <div><strong>Text:</strong> {{ c.choiceText || c.text }}</div>
            </div>
          </div>
        </div>
        <div v-else-if="choiceSearchResults.length === 0 && choiceSearchQuery && !choiceSearchLoading">No results found.</div>
        <div class="pagination" v-if="choiceTotalPages > 1">
          <button :disabled="choicePage === 0" @click="choicePrevPage">Prev</button>
          <span>Page {{ choicePage + 1 }} of {{ choiceTotalPages }}</span>
          <button :disabled="choicePage === choiceTotalPages - 1" @click="choiceNextPage">Next</button>
        </div>
      </div>

      <div v-else-if="currentTab === 'Answer Search'">
        <h3>Answer Search (by Question ID)</h3>
        <form @submit.prevent="onAnswerSearch" class="search-form">
          <input v-model="answerSearchQuestionId" type="number" min="1" placeholder="Enter Question ID..." class="search-input" />
          <button type="submit" class="search-btn">Search</button>
        </form>
        <div v-if="answerSearchLoading" class="loading">Loading...</div>
        <div v-else-if="answerSearchError" class="error">{{ answerSearchError }}</div>
        <div v-else-if="answerSearchResults.length" class="results-container">
          <div v-for="a in answerSearchResults" :key="a.id" class="result-card">
            <div class="result-header">
              <span class="result-type">Answer</span>
              <span class="result-id">ID: {{ a.id }}</span>
            </div>
            <div class="result-content">
              <div><strong>Question ID:</strong> {{ a.questionId }}</div>
              <div><strong>User ID:</strong> {{ a.userId }}</div>
              <div><strong>Choice ID:</strong> {{ a.choiceId }}</div>
              <div><strong>Public:</strong> {{ a.isPublic ? 'Yes' : 'No' }}</div>
            </div>
          </div>
        </div>
        <div v-else-if="answerSearchResults.length === 0 && answerSearchQuestionId && !answerSearchLoading">No results found.</div>
        <div class="pagination" v-if="answerTotalPages > 1">
          <button :disabled="answerPage === 0" @click="answerPrevPage">Prev</button>
          <span>Page {{ answerPage + 1 }} of {{ answerTotalPages }}</span>
          <button :disabled="answerPage === answerTotalPages - 1" @click="answerNextPage">Next</button>
        </div>
      </div>

      <div v-else-if="currentTab === 'Statistics'">
        <h3>Statistics</h3>
        <button class="search-btn" @click="fetchStatistics" :disabled="statisticsLoading">Refresh</button>
        <div v-if="statisticsLoading" class="loading">Loading...</div>
        <div v-else-if="statisticsError" class="error">{{ statisticsError }}</div>
        <div v-else class="stats-container">
          <div v-if="statistics" class="stats-section">
            <h4>General Statistics</h4>
            <div class="stats-grid">
              <div
                v-for="([key, value], index) in Object.entries(statistics).slice(0, -1)"
                :key="key"
                class="stat-card">
                <div class="stat-label">{{ formatStatKey(key) }}</div>
                <div class="stat-value">{{ value }}</div>
              </div>
            </div>
          </div>
          <div v-if="questionTypeStats" class="stats-section">
            <h4>Question Type Statistics</h4>
            <div class="stats-grid">
              <div v-for="(value, key) in questionTypeStats" :key="key" class="stat-card">
                <div class="stat-label">{{ key }}</div>
                <div class="stat-value">{{ value }}</div>
              </div>
            </div>
          </div>
          <div v-if="!statistics">No statistics available.</div>
        </div>
      </div>

      <div v-else-if="currentTab === 'Elasticsearch Sync'">
        <h3>Elasticsearch Sync</h3>
        <div class="sync-container">
          <button class="search-btn" @click="syncElasticsearch" :disabled="syncLoading">Sync Now</button>
          <div v-if="syncLoading" class="loading">Syncing...</div>
          <div v-else-if="syncError" class="error">{{ syncError }}</div>
          <div v-else-if="syncSuccess" class="success">{{ syncSuccess }}</div>
        </div>
      </div>
      <div v-else>
        <p>Section coming soon...</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import Cookies from 'js-cookie'

const tabs = [
  'Global Search',
  'Survey Search',
  'Question Search',
  'Choice Search',
  'Answer Search',
  'Statistics',
  'Elasticsearch Sync',
]
const currentTab = ref(tabs[0])

// Global Search State
const searchQuery = ref('')
const searchResults = ref<any[]>([])
const searchLoading = ref(false)
const searchError = ref('')
const page = ref(0)
const size = ref(10)
const totalPages = ref(1)

// Survey Search State
const surveySearchQuery = ref('')
const surveySearchResults = ref<any[]>([])
const surveySearchLoading = ref(false)
const surveySearchError = ref('')
const surveyPage = ref(0)
const surveySize = ref(10)
const surveyTotalPages = ref(1)

// Question Search State
const questionSearchQuery = ref('')
const questionSearchResults = ref<any[]>([])
const questionSearchLoading = ref(false)
const questionSearchError = ref('')
const questionPage = ref(0)
const questionSize = ref(10)
const questionTotalPages = ref(1)

// Choice Search State
const choiceSearchQuery = ref('')
const choiceSearchResults = ref<any[]>([])
const choiceSearchLoading = ref(false)
const choiceSearchError = ref('')
const choicePage = ref(0)
const choiceSize = ref(10)
const choiceTotalPages = ref(1)

// Answer Search State
const answerSearchQuestionId = ref('')
const answerSearchResults = ref<any[]>([])
const answerSearchLoading = ref(false)
const answerSearchError = ref('')
const answerPage = ref(0)
const answerSize = ref(10)
const answerTotalPages = ref(1)

// Statistics State
const statistics = ref<any>(null)
const questionTypeStats = ref<any>(null)
const statisticsLoading = ref(false)
const statisticsError = ref('')

// Elasticsearch Sync State
const syncLoading = ref(false)
const syncError = ref('')
const syncSuccess = ref('')

// Helper functions for JSON formatting
function isValidJson(str: string) {
  try {
    if (typeof str === 'object') return true;
    JSON.parse(str);
    return true;
  } catch (e) {
    return false;
  }
}

function formatJSON(json: string) {
  try {
    if (typeof json === 'object') {
      return JSON.stringify(json, null, 2);
    }
    return JSON.stringify(JSON.parse(json), null, 2);
  } catch (e) {
    return json;
  }
}

function formatStatKey(key: string) {
  return key.replace(/([A-Z])/g, ' $1')
    .replace(/^./, str => str.toUpperCase())
    .replace(/_/g, ' ');
}

async function fetchGlobalSearch() {
  if (!searchQuery.value) return
  searchLoading.value = true
  searchError.value = ''
  try {
    const token = Cookies.get('accessToken')
    const res = await axios.get(`http://localhost:8080/api/admin/search?query=${encodeURIComponent(searchQuery.value)}&page=${page.value}&size=${size.value}`,
      { headers: { 'Authorization': `Bearer ${token}` }, withCredentials: true })
    searchResults.value = res.data.data || []
    totalPages.value = res.data.pagination?.totalPages || 1
  } catch (e: any) {
    searchError.value = e?.response?.data?.message || 'Failed to search.'
  } finally {
    searchLoading.value = false
  }
}

async function fetchSurveySearch() {
  if (!surveySearchQuery.value) return
  surveySearchLoading.value = true
  surveySearchError.value = ''
  try {
    const token = Cookies.get('accessToken')
    const res = await axios.get(`http://localhost:8080/api/admin/search/surveys?query=${encodeURIComponent(surveySearchQuery.value)}&page=${surveyPage.value}&size=${surveySize.value}`,
      { headers: { 'Authorization': `Bearer ${token}` }, withCredentials: true })
    surveySearchResults.value = res.data.data || []
    surveyTotalPages.value = res.data.pagination?.totalPages || 1
  } catch (e: any) {
    surveySearchError.value = e?.response?.data?.message || 'Failed to search surveys.'
  } finally {
    surveySearchLoading.value = false
  }
}

async function fetchQuestionSearch() {
  if (!questionSearchQuery.value) return
  questionSearchLoading.value = true
  questionSearchError.value = ''
  try {
    const token = Cookies.get('accessToken')
    const res = await axios.get(`http://localhost:8080/api/admin/search/questions?query=${encodeURIComponent(questionSearchQuery.value)}&page=${questionPage.value}&size=${questionSize.value}`,
      { headers: { 'Authorization': `Bearer ${token}` }, withCredentials: true })
    questionSearchResults.value = res.data.data || []
    questionTotalPages.value = res.data.pagination?.totalPages || 1
  } catch (e: any) {
    questionSearchError.value = e?.response?.data?.message || 'Failed to search questions.'
  } finally {
    questionSearchLoading.value = false
  }
}

async function fetchChoiceSearch() {
  if (!choiceSearchQuery.value) return
  choiceSearchLoading.value = true
  choiceSearchError.value = ''
  try {
    const token = Cookies.get('accessToken')
    const res = await axios.get(`http://localhost:8080/api/admin/search/choices?query=${encodeURIComponent(choiceSearchQuery.value)}&page=${choicePage.value}&size=${choiceSize.value}`,
      { headers: { 'Authorization': `Bearer ${token}` }, withCredentials: true })
    choiceSearchResults.value = res.data.data || []
    choiceTotalPages.value = res.data.pagination?.totalPages || 1
  } catch (e: any) {
    choiceSearchError.value = e?.response?.data?.message || 'Failed to search choices.'
  } finally {
    choiceSearchLoading.value = false
  }
}

async function fetchAnswerSearch() {
  if (!answerSearchQuestionId.value) return
  answerSearchLoading.value = true
  answerSearchError.value = ''
  try {
    const token = Cookies.get('accessToken')
    const res = await axios.get(`http://localhost:8080/api/admin/search/answers/question?questionId=${answerSearchQuestionId.value}&page=${answerPage.value}&size=${answerSize.value}`,
      { headers: { 'Authorization': `Bearer ${token}` }, withCredentials: true })
    answerSearchResults.value = res.data.data || []
    answerTotalPages.value = res.data.pagination?.totalPages || 1
  } catch (e: any) {
    answerSearchError.value = e?.response?.data?.message || 'Failed to search answers.'
  } finally {
    answerSearchLoading.value = false
  }
}

async function fetchStatistics() {
  statisticsLoading.value = true
  statisticsError.value = ''
  try {
    const token = Cookies.get('accessToken')
    const [statsRes, typeRes] = await Promise.all([
      axios.get('http://localhost:8080/api/admin/statistics', { headers: { 'Authorization': `Bearer ${token}` }, withCredentials: true }),
      axios.get('http://localhost:8080/api/admin/statistics/question-types', { headers: { 'Authorization': `Bearer ${token}` }, withCredentials: true })
    ])
    statistics.value = statsRes.data.data || statsRes.data || null
    questionTypeStats.value = typeRes.data.data || typeRes.data || null
  } catch (e: any) {
    statisticsError.value = e?.response?.data?.message || 'Failed to fetch statistics.'
  } finally {
    statisticsLoading.value = false
  }
}

async function syncElasticsearch() {
  syncLoading.value = true
  syncError.value = ''
  syncSuccess.value = ''
  try {
    const token = Cookies.get('accessToken')
    const res = await axios.post('http://localhost:8080/api/admin/elasticsearch/sync', {}, { headers: { 'Authorization': `Bearer ${token}` }, withCredentials: true })
    syncSuccess.value = res.data.message || 'Elasticsearch synchronization completed successfully.'
  } catch (e: any) {
    syncError.value = e?.response?.data?.message || 'Failed to sync Elasticsearch.'
  } finally {
    syncLoading.value = false
  }
}

function onSearch() {
  page.value = 0
  fetchGlobalSearch()
}

function onSurveySearch() {
  surveyPage.value = 0
  fetchSurveySearch()
}

function onQuestionSearch() {
  questionPage.value = 0
  fetchQuestionSearch()
}

function onChoiceSearch() {
  choicePage.value = 0
  fetchChoiceSearch()
}

function onAnswerSearch() {
  answerPage.value = 0
  fetchAnswerSearch()
}

function prevPage() { if (page.value > 0) { page.value--; fetchGlobalSearch() } }
function nextPage() { if (page.value < totalPages.value - 1) { page.value++; fetchGlobalSearch() } }
function surveyPrevPage() { if (surveyPage.value > 0) { surveyPage.value--; fetchSurveySearch() } }
function surveyNextPage() { if (surveyPage.value < surveyTotalPages.value - 1) { surveyPage.value++; fetchSurveySearch() } }
function questionPrevPage() { if (questionPage.value > 0) { questionPage.value--; fetchQuestionSearch() } }
function questionNextPage() { if (questionPage.value < questionTotalPages.value - 1) { questionPage.value++; fetchQuestionSearch() } }
function choicePrevPage() { if (choicePage.value > 0) { choicePage.value--; fetchChoiceSearch() } }
function choiceNextPage() { if (choicePage.value < choiceTotalPages.value - 1) { choicePage.value++; fetchChoiceSearch() } }
function answerPrevPage() { if (answerPage.value > 0) { answerPage.value--; fetchAnswerSearch() } }
function answerNextPage() { if (answerPage.value < answerTotalPages.value - 1) { answerPage.value++; fetchAnswerSearch() } }
</script>

<style scoped>
.admin-dashboard-container {
  background: #232526;
  padding: 2rem 2.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 16px 0 rgba(0,0,0,0.12);
  width: 1100px;
  max-width: 95vw;
  margin: 2rem auto;
  color: #e0e0e0;
}

.admin-dashboard-container h2 {
  color: var(--color-accent);
  margin-bottom: 1.5rem;
  text-align: center;
}

.admin-dashboard-container h3 {
  color: var(--color-accent);
  margin-bottom: 1.2rem;
  text-align: left;
}

.tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: center;
  margin-bottom: 2rem;
}

.tab-btn {
  background: #181a1b;
  color: var(--color-secondary);
  border: none;
  border-radius: 6px;
  padding: 0.7em 1em;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  margin-bottom: 0.5rem;
}

.tab-btn.active {
  background: var(--color-accent);
  color: var(--color-primary);
}

.tab-content {
  background: #1f2122;
  border-radius: 10px;
  padding: 2rem 1.5rem 1.5rem 1.5rem;
  box-shadow: 0 2px 8px 0 rgba(0,0,0,0.08);
  text-align: left;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  margin-bottom: 1.5rem;
  align-items: center;
  justify-content: flex-start;
}

.search-input {
  background: #181a1b;
  color: #e0e0e0;
  border: 1px solid #333;
  border-radius: 4px;
  padding: 0 10px;
  font-size: 1em;
  outline: none;
  flex: 1;
  min-width: 200px;
  height: 35px;
  box-sizing: border-box;
  margin-right: 5px;
}

.search-btn {
  background: #f0ad4e;
  color: #212529;
  border: none;
  border-radius: 4px;
  padding: 0 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  height: 35px;
  width: auto;
  min-width: 60px;
  display: inline-block;
  line-height: 35px;
  text-align: center;
  box-sizing: border-box;
  vertical-align: middle;
  position: relative;
  top: 0;
  margin-bottom: 16px;
}

.search-btn:hover {
  background: #ec971f;
}

.results-container {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
  margin-top: 1.5rem;
  margin-bottom: 1.5rem;
}

.result-card {
  background: #26282b;
  border-radius: 8px;
  padding: 1.2rem;
  box-shadow: 0 2px 8px 0 rgba(0,0,0,0.1);
  border-left: 3px solid var(--color-accent);
  text-align: left;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.8rem;
  padding-bottom: 0.8rem;
  border-bottom: 1px solid #333;
}

.result-type {
  font-weight: 600;
  color: var(--color-accent);
  font-size: 1.1em;
}

.result-id {
  color: #a0a0a0;
}

.result-index {
  color: #a0a0a0;
  margin-bottom: 0.8rem;
}

.result-content {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  text-align: left;
}

.json-content {
  background: #1a1c1d;
  padding: 1rem;
  border-radius: 6px;
  overflow: auto;
  max-height: 300px;
  text-align: left;
}

.json-formatted {
  margin: 0;
  color: #d4d4d4;
  white-space: pre-wrap;
  text-align: left;
}

.json-raw {
  margin: 0;
  color: #d4d4d4;
  white-space: pre-wrap;
  word-break: break-word;
  text-align: left;
}

.loading {
  color: var(--color-secondary);
  margin: 1.5rem 0;
}

.error {
  color: #ff4d4f;
  margin: 1.5rem 0;
}

.pagination {
  margin-top: 1.5rem;
  display: flex;
  gap: 1.5rem;
  align-items: center;
  justify-content: center;
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

.success {
  color: var(--color-secondary);
  margin-top: 1rem;
}

.stats-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  text-align: left;
}

.stats-section h4 {
  color: var(--color-accent);
  margin-bottom: 1rem;
  text-align: left;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.stat-card {
  background: #26282b;
  padding: 1rem;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 8px 0 rgba(0,0,0,0.1);
}

.stat-label {
  color: var(--color-secondary);
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.stat-value {
  font-size: 1.5em;
  font-weight: 700;
  color: var(--color-accent);
}

.sync-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 1rem;
  margin-top: 1rem;
}

@media (max-width: 768px) {
  .admin-dashboard-container {
    padding: 1.5rem 1rem;
  }
  
  .tabs {
    overflow-x: auto;
    padding-bottom: 0.5rem;
  }
  
  .tab-btn {
    font-size: 0.9em;
    padding: 0.6em 0.8em;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
``` 