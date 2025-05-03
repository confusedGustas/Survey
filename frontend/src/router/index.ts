import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import LandingPage from '../components/LandingPage.vue';
import LoginPage from '../components/LoginPage.vue';
import RegisterPage from '../components/RegisterPage.vue';
import ProfilePage from '../components/ProfilePage.vue';
import SurveyCreate from '../components/SurveyCreate.vue';
import UserList from '../components/UserList.vue';
import AdminDashboard from '../components/AdminDashboard.vue';
import { validateToken } from '../utils/authEvents';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Landing',
    component: LandingPage,
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginPage,
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterPage,
  },
  {
    path: '/surveys/create',
    name: 'SurveyCreate',
    component: SurveyCreate,
  },
  {
    path: '/profile',
    name: 'Profile',
    component: ProfilePage,
  },
  {
    path: '/users',
    name: 'UserList',
    component: UserList,
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const publicPages = ['/', '/login', '/register']; // Define public pages
  const authRequired = !publicPages.includes(to.path);
  const tokenValid = validateToken();
  
  console.log(`Route navigation to ${to.path}, auth required: ${authRequired}, token valid: ${tokenValid}`);

  if (authRequired && !tokenValid) {
    console.log('Unauthorized access attempt, redirecting to landing page');
    // Redirect to the landing page if the token is invalid or missing
    return next('/');
  }

  next();
});

export default router;