<template>
  <div class="user-list-container">
    <h2>All Users</h2>
    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <table v-else class="user-table">
      <thead>
        <tr>
          <th>Username</th>
          <th>Email</th>
          <th>Role</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.username }}</td>
          <td>{{ user.email }}</td>
          <td>{{ user.role }}</td>
        </tr>
        <tr v-if="users.length === 0">
          <td colspan="3">No users found.</td>
        </tr>
      </tbody>
    </table>
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
import Cookies from 'js-cookie'

const users = ref<any[]>([])
const loading = ref(true)
const error = ref('')
const page = ref(0)
const size = ref(10)
const totalPages = ref(1)

async function fetchUsers() {
  loading.value = true
  error.value = ''
  try {
    const token = Cookies.get('accessToken')
    const res = await axios.get(`http://localhost:8080/api/users?page=${page.value}&size=${size.value}`,
      { headers: { 'Authorization': `Bearer ${token}` }, withCredentials: true })
    users.value = res.data.data || []
    totalPages.value = res.data.pagination?.totalPages || 1
  } catch (e: any) {
    error.value = e?.response?.data?.message || 'Failed to fetch users.'
  } finally {
    loading.value = false
  }
}

function prevPage() { if (page.value > 0) page.value-- }
function nextPage() { if (page.value < totalPages.value - 1) page.value++ }

onMounted(fetchUsers)
watch([page, size], fetchUsers)
</script>

<style scoped>
.user-list-container {
  background: #232526;
  padding: 2rem 2.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 16px 0 rgba(0,0,0,0.12);
  max-width: 700px;
  margin: 2rem auto;
  color: #e0e0e0;
}
.user-list-container h2 {
  color: var(--color-accent);
  margin-bottom: 1rem;
}
.user-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1.5rem;
}
.user-table th, .user-table td {
  padding: 0.7em 1em;
  border-bottom: 1px solid #333;
  text-align: left;
}
.user-table th {
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
</style>