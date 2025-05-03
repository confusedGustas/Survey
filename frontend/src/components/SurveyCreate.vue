<template>
  <div class="survey-create-form">
    <h2>Create Survey</h2>
    <form @submit.prevent="onCreate">
      <div class="form-row">
        <label>Survey Title</label>
        <input v-model="title" type="text" placeholder="Survey Title" required maxlength="100" />
      </div>
      <div class="form-row">
        <label>Survey Description</label>
        <textarea v-model="description" placeholder="Survey Description" rows="3" required maxlength="500"></textarea>
      </div>
      <div class="questions-section">
        <div class="questions-header">
          <h3>Questions</h3>
        </div>
        <div v-for="(q, idx) in questions" :key="idx" class="question-card">
          <div class="question-top">
            <span class="question-label">Question {{ idx + 1 }}</span>
            <button type="button" class="icon-btn remove-question-btn" @click="removeQuestion(idx)" title="Remove question">
              <span aria-hidden="true">✕</span>
            </button>
          </div>
          <div class="form-row">
            <label>Content</label>
            <input v-model="q.content" type="text" placeholder="Question content" required maxlength="200" />
          </div>
          <div class="form-row">
            <label>Type</label>
            <select v-model="q.questionType" required>
              <option value="SINGLE">Single Choice</option>
              <option value="MULTIPLE">Multiple Choice</option>
              <option value="TEXT">Text</option>
            </select>
          </div>
          <div v-if="q.questionType !== 'TEXT'" class="choices-section">
            <div class="form-row">
              <label>Choices</label>
              <div class="choices-list">
                <div v-for="(choice, cidx) in q.choices" :key="cidx" class="choice-row">
                  <input v-model="q.choices[cidx]" type="text" placeholder="Choice text" required class="choice-input" maxlength="100" />
                  <button type="button" class="icon-btn remove-choice-btn" @click="removeChoice(idx, cidx)" title="Remove choice">
                    <span aria-hidden="true">✕</span>
                  </button>
                </div>
              </div>
              <button type="button" class="add-choice-btn" @click="addChoice(idx)" title="Add choice">
                <span class="material-icons">add</span> Add Choice
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="form-footer">
        <button type="submit" class="submit-btn">Create Survey</button>
      </div>
      <p v-if="error" class="error">{{ error }}</p>
      <p v-if="success" class="success">Survey created! Redirecting...</p>
    </form>
    <button type="button" class="fab-add-question" @click="addQuestion" title="Add Question">
      <span class="material-icons">add</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import Cookies from 'js-cookie'

const title = ref('')
const description = ref('')
const error = ref('')
const success = ref(false)
const router = useRouter()

const questions = ref([
  { content: '', questionType: 'SINGLE', choices: [''] }
])

function addQuestion() {
  questions.value.push({ content: '', questionType: 'SINGLE', choices: [''] })
}
function removeQuestion(idx: number) {
  questions.value.splice(idx, 1)
}
function addChoice(qIdx: number) {
  questions.value[qIdx].choices.push('')
}
function removeChoice(qIdx: number, cIdx: number) {
  questions.value[qIdx].choices.splice(cIdx, 1)
}

async function onCreate() {
  error.value = ''
  success.value = false
  if (!questions.value.length) {
    error.value = 'At least one question is required.'
    return
  }
  for (const q of questions.value) {
    if (q.questionType !== 'TEXT' && (!q.choices.length || q.choices.some(c => !c))) {
      error.value = 'All choices must be filled for non-text questions.'
      return
    }
  }
  try {
    const token = Cookies.get('accessToken')
    const payload = {
      title: title.value,
      description: description.value,
      questions: questions.value.map(q => ({
        content: q.content,
        questionType: q.questionType,
        choices: q.questionType === 'TEXT' ? [] : q.choices
      }))
    }
    const res = await axios.post('http://localhost:8080/api/surveys', payload, {
      headers: { 'Authorization': `Bearer ${token}` },
      withCredentials: true
    })
    if (res.status === 201) {
      success.value = true
      setTimeout(() => router.push('/profile'), 1200)
    }
  } catch (e: any) {
    error.value = e?.response?.data?.message || 'Failed to create survey.'
  }
}
</script>

<style scoped>
.survey-create-form {
  background: #232526;
  padding: 2.5rem 2.5rem 2rem 2.5rem;
  border-radius: 18px;
  box-shadow: 0 6px 32px 0 rgba(0,0,0,0.22);
  width: 900px;
  max-width: 90vw;
  margin: 2.5rem auto;
  position: relative;
  overflow: hidden;
}
.survey-create-form h2 {
  color: var(--color-accent);
  margin-bottom: 2.2rem;
  text-align: center;
  font-size: 2.2rem;
  font-weight: 700;
  letter-spacing: 0.5px;
}
.form-row {
  display: flex;
  flex-direction: column;
  margin-bottom: 1.5rem;
}
.form-row label {
  color: var(--color-secondary);
  font-weight: 600;
  margin-bottom: 0.4rem;
  font-size: 1.08em;
  letter-spacing: 0.1px;
}
.form-row input,
.form-row textarea,
.form-row select {
  background: #181a1b;
  color: #e0e0e0;
  border: 1.5px solid #333;
  border-radius: 8px;
  padding: 0.7em 1em;
  font-size: 1.08em;
  margin-bottom: 0.1rem;
  transition: border 0.2s, box-shadow 0.2s;
  outline: none;
  font-family: inherit;
  width: 100%;
  box-sizing: border-box;
}
.form-row textarea {
  resize: vertical;
  max-height: 200px;
  min-height: 80px;
}
.questions-section {
  margin: 2.5rem 0 2.2rem 0;
}
.questions-header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-bottom: 1.5rem;
}
.questions-header h3 {
  color: var(--color-accent);
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  letter-spacing: 0.2px;
}
.question-card {
  background: #26282b;
  border-radius: 16px;
  padding: 1.7rem 1.5rem 1.3rem 1.5rem;
  margin-bottom: 2.2rem;
  box-shadow: 0 4px 18px 0 rgba(0,0,0,0.13);
  border: 1.5px solid #292929;
  position: relative;
  transition: box-shadow 0.2s, border 0.2s;
  overflow: hidden;
}
.question-card:hover {
  box-shadow: 0 8px 32px 0 rgba(255,140,0,0.10);
  border: 1.5px solid var(--color-accent);
}
.question-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.2rem;
}
.question-label {
  color: var(--color-accent);
  font-weight: 700;
  font-size: 1.13em;
  letter-spacing: 0.1px;
}
.icon-btn {
  background: none;
  border: none;
  color: #ff4d4f;
  font-size: 1.3em;
  cursor: pointer;
  padding: 0.2em 0.5em;
  border-radius: 4px;
  transition: background 0.2s, color 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-btn:hover {
  background: #ff4d4f;
  color: #fff;
}
.choices-section {
  margin-top: 0.9rem;
}
.choices-section .form-row {
  margin-bottom: 0;
}
.choices-section label {
  color: var(--color-secondary);
  font-weight: 600;
  margin-bottom: 0.4rem;
  font-size: 1.08em;
  letter-spacing: 0.1px;
}
.choices-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  margin-top: 0.1rem;
}
.choice-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 0.7rem;
  align-items: center;
}
.choice-input {
  background: #181a1b;
  color: #e0e0e0;
  border: 1.5px solid #333;
  border-radius: 8px;
  padding: 0.7em 1em;
  font-size: 1.08em;
  margin-bottom: 0.1rem;
  transition: border 0.2s, box-shadow 0.2s;
  outline: none;
  width: 100%;
  box-sizing: border-box;
}
.choice-input:focus {
  border: 1.5px solid var(--color-accent);
  box-shadow: 0 0 0 2px rgba(255, 140, 0, 0.12);
}
.remove-choice-btn {
  color: #ff4d4f;
  font-size: 1.15em;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.2em 0.5em;
  border-radius: 4px;
  transition: background 0.2s, color 0.2s;
}
.remove-choice-btn:hover {
  background: #ff4d4f;
  color: #fff;
}
.add-choice-btn {
  background: var(--color-secondary);
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 0.5em 1.2em;
  font-weight: 600;
  font-size: 1em;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  margin-top: 0.3rem;
  display: flex;
  align-items: center;
  gap: 0.4em;
}
.add-choice-btn:hover {
  background: var(--color-accent);
  color: var(--color-primary);
}
.form-footer {
  display: flex;
  justify-content: center;
  margin-top: 2.5rem;
}
.submit-btn {
  background: var(--color-accent);
  color: var(--color-primary);
  border: none;
  border-radius: 10px;
  padding: 1em 2.5em;
  font-weight: 700;
  font-size: 1.15em;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  box-shadow: 0 2px 8px 0 rgba(0,0,0,0.10);
}
.submit-btn:hover {
  background: var(--color-secondary);
  color: #fff;
}
.survey-create-form .error {
  color: #ff4d4f;
  font-size: 1em;
  margin-top: 1.2rem;
  text-align: center;
}
.survey-create-form .success {
  color: var(--color-secondary);
  font-size: 1em;
  margin-top: 1.2rem;
  text-align: center;
}
.fab-add-question {
  position: fixed;
  bottom: 2.5rem;
  right: 3.5rem;
  background: var(--color-accent);
  color: var(--color-primary);
  border: none;
  border-radius: 50%;
  width: 60px;
  height: 60px;
  box-shadow: 0 4px 18px 0 rgba(255,140,0,0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.2em;
  cursor: pointer;
  z-index: 100;
  transition: background 0.2s, color 0.2s, box-shadow 0.2s;
}
.fab-add-question:hover {
  background: var(--color-secondary);
  color: #fff;
  box-shadow: 0 8px 32px 0 rgba(0,255,255,0.10);
}
.material-icons {
  font-family: 'Material Icons';
  font-style: normal;
  font-weight: normal;
  font-size: 1.2em;
  line-height: 1;
  letter-spacing: normal;
  text-transform: none;
  display: inline-block;
  direction: ltr;
  -webkit-font-feature-settings: 'liga';
  -webkit-font-smoothing: antialiased;
}
@media (max-width: 900px) {
  .survey-create-form {
    padding: 1.2rem 1rem 1.2rem 1rem;
    max-width: 95vw;
    width: auto;
  }
  .fab-add-question {
    right: 1.2rem;
    bottom: 1.2rem;
  }
}
.add-question-btn-top {
  background: var(--color-secondary);
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 0.5em 1.2em;
  font-weight: 600;
  font-size: 1em;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  margin-left: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.4em;
}
.add-question-btn-top:hover {
  background: var(--color-accent);
  color: var(--color-primary);
}
</style>