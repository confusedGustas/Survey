import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import { updateAuthState } from './utils/authEvents'

const app = createApp(App)

app.use(router)

window.addEventListener('auth-state-changed', () => {
  updateAuthState()
})

app.mount('#app')
