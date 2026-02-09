<template>
  <aside class="sidebar card">
    <!-- About Community / Home Widget -->
    <div class="sidebar-widget">
      <div class="widget-header">
        <h3>{{ subreddit ? `About r/${subreddit.name}` : 'Home' }}</h3>
      </div>
      <div class="widget-body">
        <p v-if="subreddit" class="widget-desc">{{ subreddit.description || 'Welcome to this community!' }}</p>
        <p v-else class="widget-desc">Your personal Reddit frontpage. Come here to check in with your favorite communities.</p>

        <div v-if="subreddit" class="community-stats">
          <div class="stat">
            <span class="stat-value">{{ formatNumber(subreddit.memberCount) }}</span>
            <span class="stat-label">Members</span>
          </div>
          <div class="stat">
            <span class="stat-value">{{ formatDate(subreddit.createdAt) }}</span>
            <span class="stat-label">Created</span>
          </div>
        </div>

        <div class="widget-actions">
          <router-link
            :to="subreddit ? `/r/${subreddit.name}/submit` : '/submit'"
            class="btn btn-primary"
            style="width:100%"
          >Create Post</router-link>
          <router-link
            v-if="!subreddit"
            to="/create-community"
            class="btn btn-secondary"
            style="width:100%"
          >Create Community</router-link>
        </div>
      </div>
    </div>

    <!-- Top Communities -->
    <div v-if="!subreddit && topCommunities.length" class="sidebar-widget">
      <div class="widget-header">
        <h3>Top Communities</h3>
      </div>
      <div class="widget-body community-list">
        <router-link
          v-for="(c, i) in topCommunities"
          :key="c.id"
          :to="`/r/${c.name}`"
          class="community-item"
        >
          <span class="community-rank">{{ i + 1 }}</span>
          <div class="community-icon-sm">{{ c.name[0].toUpperCase() }}</div>
          <div class="community-info">
            <span class="community-name">r/{{ c.name }}</span>
            <span class="community-members">{{ formatNumber(c.memberCount) }} members</span>
          </div>
        </router-link>
      </div>
    </div>

    <!-- My Communities -->
    <div v-if="myCommunities.length" class="sidebar-widget">
      <div class="widget-header">
        <h3>My Communities</h3>
      </div>
      <div class="widget-body community-list">
        <router-link
          v-for="c in myCommunities"
          :key="c.id"
          :to="`/r/${c.name}`"
          class="community-item"
        >
          <div class="community-icon-sm">{{ c.name[0].toUpperCase() }}</div>
          <span class="community-name">r/{{ c.name }}</span>
        </router-link>
      </div>
    </div>

    <!-- Footer -->
    <div class="sidebar-footer">
      <p>Reddit Clone © 2026. Built with Vue.js & Spring Boot</p>
    </div>
  </aside>
</template>

<script setup>
import { onMounted, computed } from 'vue'
import { useSubredditStore } from '../stores/subreddit'
import { useAuthStore } from '../stores/auth'
import dayjs from 'dayjs'

const props = defineProps({
  subreddit: { type: Object, default: null }
})

const subredditStore = useSubredditStore()
const authStore = useAuthStore()

const topCommunities = computed(() => subredditStore.topSubreddits)
const myCommunities = computed(() => subredditStore.mySubreddits)

function formatNumber(n) {
  if (!n) return '0'
  if (n >= 1000000) return (n / 1000000).toFixed(1) + 'm'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return n.toString()
}

function formatDate(d) {
  return d ? dayjs(d).format('MMM D, YYYY') : ''
}

onMounted(() => {
  subredditStore.fetchTop()
  if (authStore.isAuthenticated) {
    subredditStore.fetchMine()
  }
})
</script>

<style scoped>
.sidebar {
  position: sticky;
  top: calc(var(--navbar-height) + 20px);
  max-height: calc(100vh - var(--navbar-height) - 40px);
  overflow-y: auto;
  border: none;
  background: none;
}

.sidebar-widget {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  margin-bottom: 16px;
  overflow: hidden;
}

.widget-header {
  padding: 12px 16px;
  background: linear-gradient(135deg, var(--accent-primary) 0%, #ff6b35 100%);
}

.widget-header h3 {
  font-size: 0.857rem;
  font-weight: 700;
  color: white;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.widget-body {
  padding: 16px;
}

.widget-desc {
  font-size: 0.929rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
}

.community-stats {
  display: flex;
  gap: 24px;
  padding: 12px 0;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 16px;
}

.stat {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 0.714rem;
  color: var(--text-secondary);
}

.widget-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.community-list {
  padding: 8px 0;
}

.community-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  text-decoration: none;
  color: var(--text-primary);
  transition: background var(--transition-fast);
}

.community-item:hover {
  background: var(--bg-hover);
  text-decoration: none;
}

.community-rank {
  width: 20px;
  font-size: 0.857rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.community-icon-sm {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--accent-secondary), #3e9ce1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.786rem;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.community-info {
  display: flex;
  flex-direction: column;
}

.community-name {
  font-size: 0.857rem;
  font-weight: 600;
}

.community-members {
  font-size: 0.714rem;
  color: var(--text-secondary);
}

.sidebar-footer {
  padding: 16px;
  text-align: center;
}

.sidebar-footer p {
  font-size: 0.714rem;
  color: var(--text-tertiary);
}
</style>
