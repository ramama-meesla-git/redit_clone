<template>
  <div class="page-layout">
    <div class="page-content">
      <div class="search-header">
        <h2>Search results for "{{ query }}"</h2>
      </div>

      <!-- Type Tabs -->
      <div class="tabs">
        <button class="tab" :class="{ active: type === 'posts' }" @click="type = 'posts'; doSearch()">Posts</button>
        <button class="tab" :class="{ active: type === 'communities' }" @click="type = 'communities'; doSearch()">Communities</button>
      </div>

      <!-- Posts Results -->
      <div v-if="type === 'posts'">
        <PostCard v-for="post in results.posts" :key="post.id" :post="post" />
        <div v-if="!loading && (!results.posts || results.posts.length === 0)" class="empty-state">
          <p class="text-secondary">No posts found</p>
        </div>
      </div>

      <!-- Communities Results -->
      <div v-if="type === 'communities'">
        <router-link
          v-for="sub in results.communities" :key="sub.id"
          :to="`/r/${sub.name}`"
          class="community-result card"
        >
          <div class="community-icon-sm">{{ sub.name[0].toUpperCase() }}</div>
          <div>
            <h4>r/{{ sub.name }}</h4>
            <span class="text-xs text-secondary">{{ sub.memberCount }} members</span>
            <p class="text-sm text-secondary" v-if="sub.description">{{ sub.description }}</p>
          </div>
        </router-link>
        <div v-if="!loading && (!results.communities || results.communities.length === 0)" class="empty-state">
          <p class="text-secondary">No communities found</p>
        </div>
      </div>

      <div v-if="loading" class="loading-state flex-center"><div class="spinner"></div></div>
    </div>

    <div class="page-sidebar">
      <Sidebar />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'
import PostCard from '../components/PostCard.vue'
import Sidebar from '../components/Sidebar.vue'

const route = useRoute()
const query = computed(() => route.query.q || '')
const type = ref('posts')
const loading = ref(false)
const results = ref({ posts: [], communities: [] })

async function doSearch() {
  if (!query.value) return
  loading.value = true
  try {
    const { data } = await api.get('/search', { params: { q: query.value, type: type.value } })
    results.value = { ...results.value, ...data }
  } finally {
    loading.value = false
  }
}

watch(query, doSearch)
onMounted(doSearch)
</script>

<style scoped>
.search-header {
  padding: 16px 0;
}

.search-header h2 {
  font-size: 1.286rem;
  font-weight: 700;
}

.community-result {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  margin-bottom: 8px;
  text-decoration: none;
  color: var(--text-primary);
}

.community-result:hover { text-decoration: none; }

.community-icon-sm {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--accent-secondary), #3e9ce1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.loading-state { padding: 24px; }
.empty-state { padding: 48px; text-align: center; }
</style>
