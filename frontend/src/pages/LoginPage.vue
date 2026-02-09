<template>
  <div class="auth-page flex-center">
    <div class="auth-card">
      <div class="auth-header">
        <h1>Log In</h1>
        <p>Welcome back to the community</p>
      </div>

      <form @submit.prevent="handleLogin" class="auth-form">
        <div v-if="error" class="auth-error">{{ error }}</div>

        <div class="form-group">
          <input
            v-model="form.usernameOrEmail"
            type="text"
            class="input"
            placeholder="Username or Email"
            required
          />
        </div>

        <div class="form-group">
          <input
            v-model="form.password"
            type="password"
            class="input"
            placeholder="Password"
            required
          />
        </div>

        <button type="submit" class="btn btn-primary btn-lg" :disabled="loading" style="width:100%">
          <span v-if="loading" class="spinner" style="width:18px;height:18px;border-width:2px"></span>
          <span v-else>Log In</span>
        </button>
      </form>

      <div class="auth-footer">
        <p>New to Reddit? <router-link to="/register">Sign Up</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const form = reactive({ usernameOrEmail: '', password: '' })
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  loading.value = true
  error.value = ''
  try {
    await authStore.login(form.usernameOrEmail, form.password)
    router.push(route.query.redirect || '/')
  } catch (e) {
    error.value = e.response?.data?.message || 'Invalid credentials. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - var(--navbar-height));
  padding: 24px;
}

.auth-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 32px;
  width: 100%;
  max-width: 400px;
  animation: fadeIn 0.3s ease;
}

.auth-header {
  text-align: center;
  margin-bottom: 24px;
}

.auth-header h1 {
  font-size: 1.5rem;
  font-weight: 800;
  margin-bottom: 4px;
}

.auth-header p {
  color: var(--text-secondary);
  font-size: 0.929rem;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.auth-error {
  padding: 10px 14px;
  background: rgba(255, 69, 0, 0.1);
  border: 1px solid rgba(255, 69, 0, 0.3);
  border-radius: var(--radius-sm);
  color: var(--accent-primary);
  font-size: 0.857rem;
}

.auth-footer {
  margin-top: 24px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 0.857rem;
}
</style>
