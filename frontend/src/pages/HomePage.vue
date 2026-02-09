<template>
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
        >
          <component :is="s.icon" />
          {{ s.label }}
        </button>
      </div>

      <!-- Posts -->
      <div class="posts-list">
        <PostCard v-for="post in postStore.posts" :key="post.id" :post="post" />
      </div>

      <!-- Loading -->
      <div v-if="postStore.loading" class="loading-state flex-center">
        <div class="spinner"></div>
      </div>

      <!-- Load More -->
      <div v-if="postStore.hasMore && !postStore.loading" class="load-more flex-center">
        <button class="btn btn-secondary" @click="loadMore">Load More</button>
      </div>

      <!-- Empty -->
      <div v-if="!postStore.loading && postStore.posts.length === 0" class="empty-state">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="var(--text-tertiary)" stroke-width="1.5">
          <rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/>
        </svg>
        <p>No posts yet. Be the first to create one!</p>
        <router-link to="/submit" class="btn btn-primary">Create Post</router-link>
      </div>
    </div>

    <div class="page-sidebar">
      <Sidebar />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { usePostStore } from '../stores/post'
import PostCard from '../components/PostCard.vue'
import Sidebar from '../components/Sidebar.vue'

const postStore = usePostStore()
const currentSort = ref('hot')

const sorts = [
  { value: 'hot', label: 'Hot' },
  { value: 'new', label: 'New' },
  { value: 'top', label: 'Top' }
]

function changeSort(sort) {
  currentSort.value = sort
  postStore.fetchFeed(sort, true)
}

function loadMore() {
  postStore.fetchFeed(currentSort.value)
}

onMounted(() => {
  postStore.fetchFeed('hot', true)
})
</script>

<style scoped>
.sort-tabs {
  display: flex;
  gap: 4px;
  padding: 12px;
  margin-bottom: 12px;
}

.sort-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: var(--radius-xl);
  font-weight: 600;
  font-size: 0.929rem;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.sort-tab:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.sort-tab.active {
  background: var(--bg-hover);
  color: var(--accent-primary);
}

.loading-state, .load-more {
  padding: 24px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 64px 24px;
  text-align: center;
  color: var(--text-secondary);
}
</style>
