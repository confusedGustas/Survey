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
        <form @submit.prevent="onSearch">
          <input v-model="searchQuery" type="text" placeholder="Search all entities..." class="search-input" />
          <button type="submit" class="search-btn">Search</button>
        </form>
        <div v-if="searchLoading" class="loading">Loading...</div>
        <div v-else-if="searchError" class="error">{{ searchError }}</div>
        <table v-else-if="searchResults.length" class="result-table">
          <thead>
            <tr>
              <th>Index</th>
              <th>Type</th>
              <th>ID</th>
              <th>Content</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in searchResults" :key="item.id + '-' + item.type">
              <td>{{ item.index }}</td>
              <td>{{ item.type }}</td>
              <td>{{ item.id }}</td>
              <td><pre style="white-space: pre-wrap; word-break: break-all; max-width: 300px;">{{ item.content }}</pre></td>
            </tr>
          </tbody>
        </table>
        <div v-else-if="searchResults.length === 0 && searchQuery && !searchLoading">No results found.</div>
        <div class="pagination" v-if="totalPages > 1">
          <button :disabled="page === 0" @click="prevPage">Prev</button>
          <span>Page {{ page + 1 }} of {{ totalPages }}</span>
          <button :disabled="page === totalPages - 1" @click="nextPage">Next</button>
        </div>
      </div>
      <div v-else-if="currentTab === 'Survey Search'">
        <h3>Survey Search</h3>
        <form @submit.prevent="onSurveySearch">
          <input v-model="surveySearchQuery" type="text" placeholder="Search surveys by title or description..." class="search-input" />
          <button type="submit" class="search-btn">Search</button>
        </form>
        <div v-if="surveySearchLoading" class="loading">Loading...</div>
        <div v-else-if="surveySearchError" class="error">{{ surveySearchError }}</div>
        <table v-else-if="surveySearchResults.length" class="result-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Title</th>
              <th>Description</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="survey in surveySearchResults" :key="survey.id">
              <td>{{ survey.id }}</td>
              <td>{{ survey.title }}</td>
              <td>{{ survey.description }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else-if="surveySearchResults.length === 0 && surveySearchQuery && !surveySearchLoading">No results found.</div>
        <div class="pagination" v-if="surveyTotalPages > 1">
          <button :disabled="surveyPage === 0" @click="surveyPrevPage">Prev</button>
          <span>Page {{ surveyPage + 1 }} of {{ surveyTotalPages }}</span>
          <button :disabled="surveyPage === surveyTotalPages - 1" @click="surveyNextPage">Next</button>
        </div>
      </div>
      <div v-else-if="currentTab === 'Question Search'">
        <h3>Question Search</h3>
        <form @submit.prevent="onQuestionSearch">
          <input v-model="questionSearchQuery" type="text" placeholder="Search questions by content..." class="search-input" />
          <button type="submit" class="search-btn">Search</button>
        </form>
        <div v-if="questionSearchLoading" class="loading">Loading...</div>
        <div v-else-if="questionSearchError" class="error">{{ questionSearchError }}</div>
        <table v-else-if="questionSearchResults.length" class="result-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Survey ID</th>
              <th>Content</th>
              <th>Type</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="q in questionSearchResults" :key="q.id">
              <td>{{ q.id }}</td>
              <td>{{ q.surveyId }}</td>
              <td>{{ q.content }}</td>
              <td>{{ q.questionType }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else-if="questionSearchResults.length === 0 && questionSearchQuery && !questionSearchLoading">No results found.</div>
        <div class="pagination" v-if="questionTotalPages > 1">
          <button :disabled="questionPage === 0" @click="questionPrevPage">Prev</button>
          <span>Page {{ questionPage + 1 }} of {{ questionTotalPages }}</span>
          <button :disabled="questionPage === questionTotalPages - 1" @click="questionNextPage">Next</button>
        </div>
      </div>
      <div v-else-if="currentTab === 'Choice Search'">
        <h3>Choice Search</h3>
        <form @submit.prevent="onChoiceSearch">
          <input v-model="choiceSearchQuery" type="text" placeholder="Search choices by text..." class="search-input" />
          <button type="submit" class="search-btn">Search</button>
        </form>
        <div v-if="choiceSearchLoading" class="loading">Loading...</div>
        <div v-else-if="choiceSearchError" class="error">{{ choiceSearchError }}</div>
        <table v-else-if="choiceSearchResults.length" class="result-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Question ID</th>
              <th>Choice Text</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in choiceSearchResults" :key="c.id">
              <td>{{ c.id }}</td>
              <td>{{ c.questionId }}</td>
              <td>{{ c.choiceText || c.text }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else-if="choiceSearchResults.length === 0 && choiceSearchQuery && !choiceSearchLoading">No results found.</div>
        <div class="pagination" v-if="choiceTotalPages > 1">
          <button :disabled="choicePage === 0" @click="choicePrevPage">Prev</button>
          <span>Page {{ choicePage + 1 }} of {{ choiceTotalPages }}</span>
          <button :disabled="choicePage === choiceTotalPages - 1" @click="choiceNextPage">Next</button>
        </div>
      </div>
      <div v-else-if="currentTab === 'Answer Search'">
        <h3>Answer Search (by Question ID)</h3>
        <form @submit.prevent="onAnswerSearch">
          <input v-model="answerSearchQuestionId" type="number" min="1" placeholder="Enter Question ID..." class="search-input" />
          <button type="submit" class="search-btn">Search</button>
        </form>
        <div v-if="answerSearchLoading" class="loading">Loading...</div>
        <div v-else-if="answerSearchError" class="error">{{ answerSearchError }}</div>
        <table v-else-if="answerSearchResults.length" class="result-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Question ID</th>
              <th>User ID</th>
              <th>Choice ID</th>
              <th>Is Public</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="a in answerSearchResults" :key="a.id">
              <td>{{ a.id }}</td>
              <td>{{ a.questionId }}</td>
              <td>{{ a.userId }}</td>
              <td>{{ a.choiceId }}</td>
              <td>{{ a.isPublic ? 'Yes' : 'No' }}</td>
            </tr>
          </tbody>
        </table>
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
        <div v-else>
          <div v-if="statistics">
            <h4>General Statistics</h4>
            <table class="result-table">
              <tbody>
                <tr v-for="(value, key) in statistics" :key="key">
                  <td style="font-weight:600; color:var(--color-accent)">{{ key }}</td>
                  <td>{{ value }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-if="questionTypeStats">
            <h4>Question Type Statistics</h4>
            <table class="result-table">
              <thead>
                <tr>
                  <th>Type</th>
                  <th>Count</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(count, type) in questionTypeStats" :key="type">
                  <td>{{ type }}</td>
                  <td>{{ count }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-if="!statistics && !questionTypeStats">No statistics available.</div>
        </div>
      </div>
      <div v-else-if="currentTab === 'Elasticsearch Sync'">
        <h3>Elasticsearch Sync</h3>
        <button class="search-btn" @click="syncElasticsearch" :disabled="syncLoading">Sync Now</button>
        <div v-if="syncLoading" class="loading">Syncing...</div>
        <div v-else-if="syncError" class="error">{{ syncError }}</div>
        <div v-else-if="syncSuccess" class="success">{{ syncSuccess }}</div>
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

async function fetchGlobalSearch() {
  if (!searchQuery.value) return
  searchLoading.value = true
  searchError.value = ''
  try {
    const token = localStorage.getItem('accessToken')
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
    const token = localStorage.getItem('accessToken')
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
    const token = localStorage.getItem('accessToken')
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
    const token = localStorage.getItem('accessToken')
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
    const token = localStorage.getItem('accessToken')
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
    const token = localStorage.getItem('accessToken')
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
    const token = localStorage.getItem('accessToken')
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
  max-width: 900px;
  margin: 2rem auto;
  color: #e0e0e0;
}
.admin-dashboard-container h2 {
  color: var(--color-accent);
  margin-bottom: 1.5rem;
}
.tabs {
  display: flex;
  gap: 1.2rem;
  margin-bottom: 2rem;
}
.tab-btn {
  background: #181a1b;
  color: var(--color-secondary);
  border: none;
  border-radius: 6px 6px 0 0;
  padding: 0.7em 1.5em;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}
.tab-btn.active {
  background: var(--color-accent);
  color: var(--color-primary);
}
.tab-content {
  background: #232526;
  border-radius: 0 0 8px 8px;
  padding: 2rem 1.5rem 1.5rem 1.5rem;
  box-shadow: 0 2px 8px 0 rgba(0,0,0,0.08);
}
.search-input {
  background: #181a1b;
  color: #e0e0e0;
  border: 1.5px solid #333;
  border-radius: 8px;
  padding: 0.7em 1em;
  font-size: 1.08em;
  margin-right: 1rem;
  outline: none;
}
.search-btn {
  background: var(--color-accent);
  color: var(--color-primary);
  border: none;
  border-radius: 6px;
  padding: 0.6em 1.2em;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}
.search-btn:hover {
  background: var(--color-secondary);
  color: #fff;
}
.result-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 1.5rem;
  margin-bottom: 1.5rem;
}
.result-table th, .result-table td {
  padding: 0.7em 1em;
  border-bottom: 1px solid #333;
  text-align: left;
}
.result-table th {
  color: var(--color-accent);
  font-weight: 700;
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
.success {
  color: var(--color-secondary);
  margin-top: 1rem;
}
</style> 