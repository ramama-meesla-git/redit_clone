<template>
  <div class="page-layout">
    <div class="page-content">
      <!-- User Banner -->
      <div v-if="userProfile" class="profile-banner card">
        <div class="profile-bg" :style="{ background: profileGradient }"></div>
        <div class="profile-info">
          <div class="profile-avatar">{{ userProfile.username[0].toUpperCase() }}</div>
          <div class="profile-details">
            <h1>{{ userProfile.displayName || userProfile.username }}</h1>
            <span class="text-secondary">u/{{ userProfile.username }}</span>
          </div>
        </div>
        <div class="profile-stats">
          <div class="stat"><span class="stat-value">{{ userProfile.karma || 0 }}</span><span class="stat-label">Karma</span></div>
          <div class="stat"><span class="stat-value">{{ formatDate(userProfile.createdAt) }}</span><span class="stat-label">Cake Day</span></div>
        </div>
        <p v-if="userProfile.bio" class="profile-bio">{{ userProfile.bio }}</p>
      </div>

      <!-- Tabs -->
      <div class="tabs">
        <button class="tab" :class="{ active: activeTab === 'posts' }" @click="activeTab = 'posts'">Posts</button>
        <button class="tab" :class="{ active: activeTab === 'comments' }" @click="activeTab = 'comments'">Comments</button>
      </div>

      <!-- Posts -->
      <div v-if="activeTab === 'posts'">
        <PostCard v-for="post in posts" :key="post.id" :post="post" />
        <div v-if="loading" class="loading-state flex-center"><div class="spinner"></div></div>
        <div v-if="!loading && posts.length === 0" class="empty-state"><p class="text-secondary">No posts yet</p></div>
      </div>

      <!-- Comments -->
      <div v-if="activeTab === 'comments'">
        <div v-for="comment in comments" :key="comment.id" class="user-comment card">
          <div class="user-comment-header">
            <span class="text-xs text-secondary">Comment on </span>
            <router-link :to="`/r/${comment.subredditName}/post/${comment.postId}`" class="comment-post-link">
              {{ comment.postTitle }}
            </router-link>
          </div>
          <p class="comment-text">{{ comment.content }}</p>
          <span class="text-xs text-secondary">{{ timeAgo(comment.createdAt) }}</span>
        </div>
        <div v-if="loading" class="loading-state flex-center"><div class="spinner"></div></div>
        <div v-if="!loading && comments.length === 0" class="empty-state"><p class="text-secondary">No comments yet</p></div>
      </div>
    </div>

    <div class="page-sidebar">
      <Sidebar />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'
import PostCard from '../components/PostCard.vue'
import Sidebar from '../components/Sidebar.vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
dayjs.extend(relativeTime)

const route = useRoute()
const userProfile = ref(null)
const posts = ref([])
const comments = ref([])
const activeTab = ref('posts')
const loading = ref(false)

const profileGradient = computed(() => {
  const hue = (userProfile.value?.username?.charCodeAt(0) || 65) * 5
  return `linear-gradient(135deg, hsl(${hue}, 65%, 40%), hsl(${hue + 40}, 55%, 30%))`
})

function formatDate(d) { return d ? dayjs(d).format('MMM D, YYYY') : '' }
function timeAgo(d) { return d ? dayjs(d).fromNow() : '' }

async function loadProfile() {
  const username = route.params.username
  loading.value = true
  try {
    const { data } = await api.get(`/users/${username}`)
    userProfile.value = data
    await loadPosts(username)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadPosts(username) {
  const { data } = await api.get(`/users/${username}/posts`)
  posts.value = data.content || []
}

async function loadComments(username) {
  const { data } = await api.get(`/users/${username}/comments`)
  comments.value = data.content || []
}

watch(activeTab, (tab) => {
  const username = route.params.username
  if (tab === 'comments' && comments.value.length === 0) loadComments(username)
})

watch(() => route.params.username, loadProfile)
onMounted(loadProfile)
</script>

<style scoped>
.profile-banner {
  margin-bottom: 12px;
  overflow: hidden;
  animation: fadeIn 0.3s ease;
}

.profile-bg { height: 100px; }

.profile-info {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  padding: 0 20px;
  transform: translateY(-24px);
  margin-bottom: -12px;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--accent-primary), #ff6b35);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 800;
  color: white;
  border: 4px solid var(--bg-card);
}

.profile-details h1 { font-size: 1.5rem; font-weight: 800; }

.profile-stats {
  display: flex;
  gap: 32px;
  padding: 12px 20px;
}

.stat { display: flex; flex-direction: column; }
.stat-value { font-weight: 700; }
.stat-label { font-size: 0.714rem; color: var(--text-secondary); }

.profile-bio {
  padding: 0 20px 16px;
  color: var(--text-secondary);
  font-size: 0.929rem;
}

.user-comment {
  padding: 12px 16px;
  margin-bottom: 8px;
}

.comment-post-link {
  font-size: 0.857rem;
  font-weight: 600;
}

.comment-text {
  margin: 4px 0;
  font-size: 0.929rem;
}

.loading-state { padding: 24px; }
.empty-state { padding: 48px; text-align: center; }
</style>
