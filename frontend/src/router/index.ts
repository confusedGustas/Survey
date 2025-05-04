import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import LandingPage from '../components/LandingPage.vue';
import LoginPage from '../components/LoginPage.vue';
import RegisterPage from '../components/RegisterPage.vue';
import ProfilePage from '../components/ProfilePage.vue';
import SurveyCreate from '../components/SurveyCreate.vue';
import UserList from '../components/UserList.vue';
import AdminDashboard from '../components/AdminDashboard.vue';
import SurveyPage from '../components/SurveyPage.vue';
import { validateToken, refreshAccessToken } from '../utils/authEvents';
import Cookies from 'js-cookie';

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
    path: '/surveys/:id',
    name: 'SurveyView',
    component: SurveyPage,
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

function isTokenAboutToExpire(): boolean {
  const token = Cookies.get('accessToken');
  if (!token) return true;
  
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return true;
    
    const payload = JSON.parse(
      decodeURIComponent(
        atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'))
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      )
    );
    
    const now = Math.floor(Date.now() / 1000);
    const timeRemaining = payload.exp - now;

    return timeRemaining < 120;
  } catch (error) {
    console.error('Error checking token expiration:', error);
    return true;
  }
}

router.beforeEach(async (to, from, next) => {
  console.log(from)
  const publicPages = ['/', '/login', '/register'];
  const authRequired = !publicPages.includes(to.path);

  if (authRequired && Cookies.get('accessToken') && isTokenAboutToExpire()) {
    console.log('Token is about to expire, attempting refresh before navigation');
    const refreshToken = Cookies.get('refreshToken');
    
    if (refreshToken) {
      try {
        await refreshAccessToken();
      } catch (error) {
        console.error('Failed to refresh token during navigation:', error);
      }
    }
  }

  const tokenValid = validateToken();
  
  console.log(`Route navigation to ${to.path}, auth required: ${authRequired}, token valid: ${tokenValid}`);

  if (authRequired && !tokenValid) {
    console.log('Unauthorized access attempt, redirecting to landing page');
    return next('/');
  }

  next();
});

export default router;