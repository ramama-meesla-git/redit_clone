<template>
  <div>
    <!-- Subreddit Banner -->
    <div v-if="subreddit" class="subreddit-banner">
      <div class="banner-bg" :style="{ background: bannerGradient }"></div>
      <div class="banner-content container">
        <div class="banner-info">
          <div class="subreddit-icon-lg">{{ subreddit.name[0].toUpperCase() }}</div>
          <div class="banner-text">
            <h1>r/{{ subreddit.name }}</h1>
            <span class="text-secondary">r/{{ subreddit.name }}</span>
          </div>
          <button
            v-if="authStore.isAuthenticated"
            class="btn"
            :class="subreddit.isMember ? 'btn-secondary' : 'btn-primary'"
            @click="toggleMembership"
          >
            {{ subreddit.isMember ? 'Joined' : 'Join' }}
          </button>
        </div>
      </div>
    </div>

    <div class="page-layout">
      <div class="page-content">
        <!-- Sort Tabs -->
        <div class="sort-tabs card">
          <button
            v-for="s in sorts"
            :key="s.value"
            class="sort-tab"
            :class="{ active: currentSort === s.value }"
            @click="changeSort(s.value)"
          >{{ s.label }}</button>
        </div>

        <!-- Posts -->
        <PostCard v-for="post in postStore.posts" :key="post.id" :post="post" />

        <div v-if="postStore.loading" class="loading-state flex-center">
          <div class="spinner"></div>
        </div>

        <div v-if="postStore.hasMore && !postStore.loading" class="load-more flex-center">
          <button class="btn btn-secondary" @click="loadMore">Load More</button>
        </div>
      </div>

      <div class="page-sidebar">
        <Sidebar :subreddit="subreddit" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useSubredditStore } from '../stores/subreddit'
import { usePostStore } from '../stores/post'
import { useAuthStore } from '../stores/auth'
import PostCard from '../components/PostCard.vue'
import Sidebar from '../components/Sidebar.vue'

const route = useRoute()
const subredditStore = useSubredditStore()
const postStore = usePostStore()
const authStore = useAuthStore()

const subreddit = computed(() => subredditStore.currentSubreddit)
const currentSort = ref('hot')
const sorts = [
  { value: 'hot', label: 'Hot' },
  { value: 'new', label: 'New' },
  { value: 'top', label: 'Top' }
]

const bannerGradient = computed(() => {
  const hue = subreddit.value?.name?.charCodeAt(0) * 3 || 200
  return `linear-gradient(135deg, hsl(${hue}, 70%, 35%) 0%, hsl(${hue + 30}, 60%, 25%) 100%)`
})

async function loadSubreddit() {
  const name = route.params.name
  const sub = await subredditStore.fetchByName(name)
  if (sub) {
    await postStore.fetchBySubreddit(sub.id, 'hot', true)
  }
}

function changeSort(sort) {
  currentSort.value = sort
  if (subreddit.value) {
    postStore.fetchBySubreddit(subreddit.value.id, sort, true)
  }
}

function loadMore() {
  if (subreddit.value) {
    postStore.fetchBySubreddit(subreddit.value.id, currentSort.value)
  }
}

async function toggleMembership() {
  if (subreddit.value.isMember) {
    await subredditStore.leave(subreddit.value.id)
  } else {
    await subredditStore.join(subreddit.value.id)
  }
}

watch(() => route.params.name, loadSubreddit)
onMounted(loadSubreddit)
</script>

<style scoped>
.subreddit-banner {
  position: relative;
}

.banner-bg {
  height: 120px;
}

.banner-content {
  position: relative;
  padding: 0 24px;
  transform: translateY(-20px);
}

.banner-info {
  display: flex;
  align-items: flex-end;
  gap: 16px;
}

.subreddit-icon-lg {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--accent-secondary), #3e9ce1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 800;
  color: white;
  border: 4px solid var(--bg-primary);
}

.banner-text h1 {
  font-size: 1.714rem;
  font-weight: 800;
}

.sort-tabs {
  display: flex;
  gap: 4px;
  padding: 12px;
  margin-bottom: 12px;
}

.sort-tab {
  padding: 8px 14px;
  border-radius: var(--radius-xl);
  font-weight: 600;
  font-size: 0.929rem;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.sort-tab:hover { background: var(--bg-hover); color: var(--text-primary); }
.sort-tab.active { background: var(--bg-hover); color: var(--accent-primary); }

.loading-state, .load-more { padding: 24px; }
</style>
