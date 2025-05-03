<template>
  <div class="survey-container">
    <div v-if="loading" class="loading">Loading survey...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="submitted" class="success">
      <h2>Thank you for your response!</h2>
      <p>Your survey submission has been recorded.</p>
      <router-link to="/" class="back-link">Return to surveys</router-link>
    </div>
    <div v-else class="survey-content">
      <h2 class="survey-title">{{ survey.title }}</h2>
      <p class="survey-description">{{ survey.description }}</p>

      <form @submit.prevent="submitSurvey" class="survey-form">
        <div v-for="question in survey.questions" :key="question.id" class="question-container">
          <h3 class="question-text">{{ question.content }}</h3>

          <!-- Text input question type -->
          <div v-if="question.questionType === 'TEXT'" class="answer-text">
            <textarea 
              v-model="answers[question.id]" 
              placeholder="Enter your answer..."
              rows="3"
              maxlength="500"
              required
            ></textarea>
            <div class="char-counter" v-if="answers[question.id]">
              {{ answers[question.id].length }}/500 characters
            </div>
          </div>

          <!-- Single choice question type -->
          <div v-else-if="question.questionType === 'SINGLE'" class="answer-single">
            <div v-for="choice in question.choices" :key="choice.id" class="choice-option">
              <input 
                type="radio" 
                :id="'choice-'+choice.id" 
                :name="'question-'+question.id" 
                :value="choice.id"
                v-model="answers[question.id]" 
                required
              />
              <label :for="'choice-'+choice.id">{{ choice.choiceText }}</label>
            </div>
          </div>

          <!-- Multiple choice question type -->
          <div v-else-if="question.questionType === 'MULTIPLE'" class="answer-multiple">
            <div v-for="choice in question.choices" :key="choice.id" class="choice-option">
              <input 
                type="checkbox" 
                :id="'choice-'+choice.id" 
                :value="choice.id"
                v-model="multipleChoiceAnswers[question.id]"
              />
              <label :for="'choice-'+choice.id">{{ choice.choiceText }}</label>
            </div>
          </div>

          <div v-else class="error">
            Unknown question type: {{ question.questionType }}
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" class="submit-btn" :disabled="submitting">
            {{ submitting ? 'Submitting...' : 'Submit Survey' }}
          </button>
          <router-link to="/" class="cancel-btn">Cancel</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import Cookies from 'js-cookie'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const submitting = ref(false)
const submitted = ref(false)
const error = ref('')
const survey = ref<any>({
  id: null,
  title: '',
  description: '',
  questions: []
})

// Store answers in different formats depending on question type
const answers = reactive<Record<number, any>>({})
const multipleChoiceAnswers = reactive<Record<number, Array<number>>>({})

// Get survey ID from route params
const surveyId = computed(() => route.params.id)

// Fetch survey data on component mount
onMounted(async () => {
  if (!surveyId.value) {
    error.value = 'Survey ID is required'
    loading.value = false
    return
  }

  try {
    loading.value = true
    const token = Cookies.get('accessToken')
    const res = await axios.get(`http://localhost:8080/api/surveys/${surveyId.value}`, {
      headers: token ? { 'Authorization': `Bearer ${token}` } : {},
      withCredentials: true
    })

    if (res.data && res.data.data) {
      survey.value = res.data.data
      
      // Initialize answers object for multiple choice questions
      survey.value.questions.forEach((question: any) => {
        if (question.questionType === 'MULTIPLE') {
          multipleChoiceAnswers[question.id] = []
        }
      })
    } else {
      error.value = 'Invalid survey data received from server'
    }
  } catch (err: any) {
    console.error('Error fetching survey:', err)
    error.value = err?.response?.data?.message || 'Failed to load survey'
    if (err?.response?.status === 404) {
      error.value = 'Survey not found'
    }
  } finally {
    loading.value = false
  }
})

// Format answers for submission
const formatAnswersForSubmission = () => {
  const formattedAnswers = survey.value.questions.map((question: any) => {
    if (question.questionType === 'TEXT') {
      return {
        questionId: question.id,
        textResponse: answers[question.id] || ''
      }
    } else if (question.questionType === 'SINGLE') {
      return {
        questionId: question.id,
        choiceId: answers[question.id] || null
      }
    } else if (question.questionType === 'MULTIPLE') {
      return {
        questionId: question.id,
        choiceIds: multipleChoiceAnswers[question.id] || []
      }
    } else {
      return {
        questionId: question.id,
        textResponse: 'Unsupported question type'
      }
    }
  }).filter((answer: any) => {
    // Filter out incomplete answers
    if (answer.textResponse !== undefined && answer.textResponse !== '') return true
    if (answer.choiceId !== undefined && answer.choiceId !== null) return true
    if (answer.choiceIds !== undefined && answer.choiceIds.length > 0) return true
    return false
  })

  return {
    surveyId: Number(surveyId.value),
    answers: formattedAnswers
  }
}

// Submit survey answers
const submitSurvey = async () => {
  try {
    submitting.value = true
    
    const submissionData = formatAnswersForSubmission()
    console.log('Submitting survey answers:', submissionData)
    
    if (submissionData.answers.length === 0) {
      error.value = 'Please answer at least one question'
      submitting.value = false
      return
    }
    
    const token = Cookies.get('accessToken')
    const res = await axios.post('http://localhost:8080/api/answers', submissionData, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      withCredentials: true
    })
    
    console.log('Survey response submitted successfully:', res.data)
    submitted.value = true
    
  } catch (err: any) {
    console.error('Error submitting survey:', err)
    error.value = err?.response?.data?.message || 'Failed to submit survey'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.survey-container {
  max-width: 800px;
  margin: 2rem auto;
  background: #232526;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 16px 0 rgba(0,0,0,0.12);
  color: #e0e0e0;
}

.survey-title {
  color: var(--color-accent);
  margin-bottom: 0.5rem;
  font-size: 1.8rem;
}

.survey-description {
  color: #aaa;
  margin-bottom: 2rem;
  font-size: 1.1rem;
}

.question-container {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: #29292e;
  border-radius: 6px;
}

.question-text {
  margin-bottom: 1rem;
  color: var(--color-secondary);
}

.answer-text textarea {
  width: 400px;
  max-width: 100%;
  padding: 0.8rem;
  background: #373740;
  border: 1px solid #444;
  border-radius: 4px;
  color: #e0e0e0;
  font-family: inherit;
  font-size: 1rem;
  resize: vertical;
}

.answer-text textarea:focus {
  outline: none;
  border-color: var(--color-accent);
}

.char-counter {
  margin-top: 0.5rem;
  font-size: 0.9rem;
  color: #aaa;
}

.choice-option {
  display: flex;
  align-items: center;
  margin-bottom: 0.6rem;
}

.choice-option input {
  margin-right: 0.7rem;
}

.choice-option label {
  cursor: pointer;
}

.form-actions {
  display: flex;
  justify-content: flex-start;
  gap: 1rem;
  margin-top: 2rem;
}

.submit-btn {
  background: var(--color-accent);
  color: var(--color-primary);
  border: none;
  border-radius: 6px;
  padding: 0.8rem 1.5rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, transform 0.1s;
}

.submit-btn:hover {
  background: var(--color-secondary);
  color: #fff;
}

.submit-btn:disabled {
  background: #555;
  cursor: not-allowed;
}

.cancel-btn {
  background: transparent;
  color: #aaa;
  border: 1px solid #555;
  border-radius: 6px;
  padding: 0.8rem 1.5rem;
  font-weight: 600;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  transition: all 0.2s;
}

.cancel-btn:hover {
  color: #fff;
  border-color: #777;
}

.loading, .error {
  text-align: center;
  padding: 2rem;
}

.loading {
  color: var(--color-secondary);
}

.error {
  color: #ff4d4f;
}

.success {
  text-align: center;
  padding: 2rem 0;
}

.success h2 {
  color: var(--color-accent);
  margin-bottom: 1rem;
}

.back-link {
  display: inline-block;
  margin-top: 1.5rem;
  color: var(--color-secondary);
  text-decoration: none;
  padding: 0.5rem 1rem;
  border: 1px solid var(--color-secondary);
  border-radius: 4px;
  transition: all 0.2s;
}

.back-link:hover {
  background: var(--color-secondary);
  color: #232526;
}
</style>