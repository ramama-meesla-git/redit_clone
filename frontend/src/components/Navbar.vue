<template>
  <nav class="navbar">
    <div class="navbar-inner">
      <!-- Logo -->
      <router-link to="/" class="navbar-logo">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" width="32" height="32" fill="var(--accent-primary)">
            <circle cx="12" cy="12" r="11" fill="var(--accent-primary)"/>
            <circle cx="8.5" cy="10" r="2" fill="white"/>
            <circle cx="15.5" cy="10" r="2" fill="white"/>
            <path d="M8 15c0 0 2 2 4 2s4-2 4-2" stroke="white" stroke-width="1.5" fill="none" stroke-linecap="round"/>
            <circle cx="19" cy="5" r="2" fill="var(--accent-primary)" stroke="var(--text-primary)" stroke-width="1"/>
            <line x1="17.5" y1="6" x2="14" y2="8.5" stroke="var(--text-primary)" stroke-width="1"/>
          </svg>
        </div>
        <span class="logo-text">reddit</span>
      </router-link>

      <!-- Search -->
      <div class="navbar-search">
        <div class="search-icon">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
        </div>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Search Reddit"
          class="search-input"
          @keydown.enter="handleSearch"
        />
      </div>

      <!-- Actions -->
      <div class="navbar-actions">
        <template v-if="authStore.isAuthenticated">
          <router-link to="/submit" class="btn btn-ghost nav-action-btn" title="Create Post">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
          </router-link>

          <!-- Notifications -->
          <button class="btn btn-ghost nav-action-btn notification-btn" @click="showNotifications = !showNotifications">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</span>
          </button>

          <!-- User Menu -->
          <div class="dropdown">
            <button class="user-menu-trigger" @click="showUserMenu = !showUserMenu">
              <div class="user-avatar-sm">
                {{ authStore.currentUser?.username?.[0]?.toUpperCase() || 'U' }}
              </div>
              <span class="user-name">{{ authStore.currentUser?.username }}</span>
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </button>
            <div v-if="showUserMenu" class="dropdown-menu" @click="showUserMenu = false">
              <router-link :to="`/u/${authStore.currentUser?.username}`" class="dropdown-item">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                </svg>
                My Profile
              </router-link>
              <router-link to="/create-community" class="dropdown-item">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>
                  <line x1="23" y1="11" x2="17" y2="11"/><line x1="20" y1="8" x2="20" y2="14"/>
                </svg>
                Create Community
              </router-link>
              <div class="dropdown-divider"></div>
              <div class="dropdown-item karma-display">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--accent-primary)" stroke-width="2">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                </svg>
                <span>{{ authStore.currentUser?.karma || 0 }} karma</span>
              </div>
              <div class="dropdown-divider"></div>
              <button class="dropdown-item" @click="handleLogout">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
                </svg>
                Log Out
              </button>
            </div>
          </div>
        </template>

        <template v-else>
          <router-link to="/login" class="btn btn-secondary btn-sm">Log In</router-link>
          <router-link to="/register" class="btn btn-primary btn-sm">Sign Up</router-link>
        </template>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const searchQuery = ref('')
const showUserMenu = ref(false)
const showNotifications = ref(false)
const unreadCount = ref(0)

function handleSearch() {
  if (searchQuery.value.trim()) {
    router.push({ path: '/search', query: { q: searchQuery.value.trim() } })
    searchQuery.value = ''
  }
}

function handleLogout() {
  authStore.logout()
  router.push('/')
}

function closeMenus(e) {
  if (!e.target.closest('.dropdown') && !e.target.closest('.notification-btn')) {
    showUserMenu.value = false
    showNotifications.value = false
  }
}

onMounted(() => document.addEventListener('click', closeMenus))
onUnmounted(() => document.removeEventListener('click', closeMenus))
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--navbar-height);
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  z-index: 900;
}

.navbar-inner {
  display: flex;
  align-items: center;
  gap: 16px;
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.navbar-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  flex-shrink: 0;
}

.logo-text {
  font-size: 1.286rem;
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -0.5px;
}

.navbar-search {
  flex: 1;
  max-width: 600px;
  position: relative;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-tertiary);
}

.search-input {
  width: 100%;
  padding: 8px 12px 8px 38px;
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  color: var(--text-primary);
  font-size: 0.929rem;
  transition: all var(--transition-fast);
}

.search-input:focus {
  border-color: var(--accent-primary);
  background: var(--bg-primary);
}

.search-input::placeholder {
  color: var(--text-tertiary);
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.nav-action-btn {
  position: relative;
  color: var(--text-secondary);
}

.nav-action-btn:hover {
  color: var(--text-primary);
}

.notification-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  background: var(--accent-primary);
  color: white;
  font-size: 0.643rem;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: var(--radius-xl);
  min-width: 16px;
  text-align: center;
}

.user-menu-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  cursor: pointer;
  transition: border-color var(--transition-fast);
}

.user-menu-trigger:hover {
  border-color: var(--text-secondary);
}

.user-avatar-sm {
  width: 24px;
  height: 24px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--accent-primary), #ff6b35);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.786rem;
  font-weight: 700;
  color: white;
}

.user-name {
  font-size: 0.857rem;
  font-weight: 600;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.karma-display {
  color: var(--text-secondary) !important;
  font-size: 0.857rem;
  cursor: default;
}

.karma-display:hover {
  background: transparent;
}

@media (max-width: 640px) {
  .logo-text { display: none; }
  .user-name { display: none; }
  .navbar-search { max-width: 300px; }
}
</style>
