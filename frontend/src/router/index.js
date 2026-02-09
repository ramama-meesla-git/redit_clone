import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../pages/HomePage.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../pages/LoginPage.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../pages/RegisterPage.vue'),
    meta: { guest: true }
  },
  {
    path: '/r/:name',
    name: 'Subreddit',
    component: () => import('../pages/SubredditPage.vue')
  },
  {
    path: '/r/:name/submit',
    name: 'CreatePost',
    component: () => import('../pages/CreatePostPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/r/:subredditName/post/:id',
    name: 'PostDetail',
    component: () => import('../pages/PostDetailPage.vue')
  },
  {
    path: '/u/:username',
    name: 'UserProfile',
    component: () => import('../pages/UserProfilePage.vue')
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('../pages/SearchPage.vue')
  },
  {
    path: '/create-community',
    name: 'CreateCommunity',
    component: () => import('../pages/CreateCommunityPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/submit',
    name: 'Submit',
    component: () => import('../pages/CreatePostPage.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken')
  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.guest && token) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
