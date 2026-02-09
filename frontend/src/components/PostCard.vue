<template>
  <div class="post-card card" :class="{ 'post-card--detail': isDetail }">
    <!-- Vote Column -->
    <div class="post-votes">
      <button
        class="vote-btn"
        :class="{ active: post.userVote === 1 }"
        @click.stop="handleVote(1)"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
          <path d="M12 4L3 15h6v5h6v-5h6L12 4z" :fill="post.userVote === 1 ? 'var(--upvote)' : 'currentColor'" stroke="none"/>
        </svg>
      </button>
      <span class="vote-count" :class="{ upvoted: post.userVote === 1, downvoted: post.userVote === -1 }">
        {{ formatVotes(post.voteCount) }}
      </span>
      <button
        class="vote-btn"
        :class="{ active: post.userVote === -1 }"
        @click.stop="handleVote(-1)"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
          <path d="M12 20L3 9h6V4h6v5h6L12 20z" :fill="post.userVote === -1 ? 'var(--downvote)' : 'currentColor'" stroke="none"/>
        </svg>
      </button>
    </div>

    <!-- Content -->
    <div class="post-content" @click="goToPost">
      <!-- Meta -->
      <div class="post-meta">
        <router-link :to="`/r/${post.subredditName}`" class="post-subreddit" @click.stop>
          r/{{ post.subredditName }}
        </router-link>
        <span class="meta-dot">•</span>
        <span class="text-xs text-secondary">Posted by </span>
        <router-link :to="`/u/${post.authorUsername}`" class="post-author" @click.stop>
          u/{{ post.authorUsername }}
        </router-link>
        <span class="meta-dot">•</span>
        <span class="post-time text-xs text-secondary">{{ timeAgo(post.createdAt) }}</span>
      </div>

      <!-- Title -->
      <h3 class="post-title">{{ post.title }}</h3>

      <!-- Content Preview -->
      <div v-if="post.postType === 'TEXT' && post.content && !isDetail" class="post-text-preview">
        {{ post.content.substring(0, 250) }}{{ post.content.length > 250 ? '...' : '' }}
      </div>
      <div v-if="post.postType === 'TEXT' && post.content && isDetail" class="post-text-full">
        {{ post.content }}
      </div>

      <!-- Link Preview -->
      <div v-if="post.postType === 'LINK' && post.url" class="post-link">
        <a :href="post.url" target="_blank" rel="noopener" @click.stop>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/>
          </svg>
          {{ post.url }}
        </a>
      </div>

      <!-- Image -->
      <div v-if="post.postType === 'IMAGE' && post.imageUrl" class="post-image">
        <img :src="post.imageUrl" :alt="post.title" loading="lazy" />
      </div>

      <!-- Actions -->
      <div class="post-actions">
        <button class="action-btn" @click.stop="goToPost">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          {{ post.commentCount }} {{ post.commentCount === 1 ? 'Comment' : 'Comments' }}
        </button>
        <button class="action-btn" @click.stop>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"/><polyline points="16 6 12 2 8 6"/><line x1="12" y1="2" x2="12" y2="15"/>
          </svg>
          Share
        </button>
        <button class="action-btn" @click.stop>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
          </svg>
          Save
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { usePostStore } from '../stores/post'
import { useAuthStore } from '../stores/auth'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
dayjs.extend(relativeTime)

const props = defineProps({
  post: { type: Object, required: true },
  isDetail: { type: Boolean, default: false }
})

const router = useRouter()
const postStore = usePostStore()
const authStore = useAuthStore()

function handleVote(voteType) {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  postStore.vote(props.post.id, voteType)
}

function goToPost() {
  if (!props.isDetail) {
    router.push(`/r/${props.post.subredditName}/post/${props.post.id}`)
  }
}

function timeAgo(date) {
  return date ? dayjs(date).fromNow() : ''
}

function formatVotes(count) {
  if (!count) return '0'
  if (count >= 10000) return (count / 1000).toFixed(1) + 'k'
  return count.toString()
}
</script>

<style scoped>
.post-card {
  display: flex;
  cursor: pointer;
  transition: border-color var(--transition-fast);
  margin-bottom: 10px;
}

.post-card:hover {
  border-color: var(--text-secondary);
}

.post-card--detail {
  cursor: default;
}

.post-card--detail:hover {
  border-color: var(--border-color);
}

.post-votes {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 8px 4px;
  background: rgba(0,0,0,0.1);
  border-radius: var(--radius-sm) 0 0 var(--radius-sm);
  min-width: 40px;
}

.vote-btn {
  color: var(--text-secondary);
  padding: 4px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.vote-btn:hover {
  background: var(--bg-hover);
}

.vote-btn.active svg {
  transform: scale(1.1);
}

.vote-count {
  font-size: 0.857rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.vote-count.upvoted { color: var(--upvote); }
.vote-count.downvoted { color: var(--downvote); }

.post-content {
  flex: 1;
  padding: 8px 12px;
  min-width: 0;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
  margin-bottom: 4px;
}

.post-subreddit {
  font-size: 0.786rem;
  font-weight: 700;
  color: var(--text-primary);
}

.post-subreddit:hover {
  text-decoration: underline;
}

.post-author {
  font-size: 0.786rem;
  color: var(--text-secondary);
}

.post-author:hover {
  text-decoration: underline;
}

.meta-dot {
  color: var(--text-tertiary);
  font-size: 0.786rem;
}

.post-title {
  font-size: 1.143rem;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.3;
  margin-bottom: 8px;
}

.post-text-preview {
  font-size: 0.929rem;
  color: var(--text-secondary);
  line-height: 1.5;
  max-height: 100px;
  overflow: hidden;
  mask-image: linear-gradient(to bottom, black 60%, transparent);
  -webkit-mask-image: linear-gradient(to bottom, black 60%, transparent);
  margin-bottom: 8px;
}

.post-text-full {
  font-size: 1rem;
  color: var(--text-primary);
  line-height: 1.7;
  white-space: pre-wrap;
  margin-bottom: 12px;
}

.post-link a {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 0.857rem;
  color: var(--text-link);
  word-break: break-all;
}

.post-image {
  margin: 8px 0;
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.post-image img {
  max-height: 512px;
  object-fit: contain;
  background: var(--bg-primary);
}

.post-actions {
  display: flex;
  gap: 4px;
  margin-top: 4px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  font-size: 0.786rem;
  font-weight: 700;
  color: var(--text-secondary);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.action-btn:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}
</style>
