<template>
  <div class="comment-item" :style="{ marginLeft: (comment.depth || 0) * 20 + 'px' }">
    <div class="comment-thread-line" v-if="comment.depth > 0"></div>
    <div class="comment-main">
      <!-- Vote -->
      <div class="comment-votes">
        <button class="vote-btn-sm" :class="{ active: comment.userVote === 1 }" @click="handleVote(1)">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M12 4L3 15h6v5h6v-5h6L12 4z" :fill="comment.userVote === 1 ? 'var(--upvote)' : 'currentColor'" stroke="none"/>
          </svg>
        </button>
        <span class="vote-count-sm" :class="{ upvoted: comment.userVote === 1, downvoted: comment.userVote === -1 }">
          {{ comment.voteCount }}
        </span>
        <button class="vote-btn-sm" :class="{ active: comment.userVote === -1 }" @click="handleVote(-1)">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M12 20L3 9h6V4h6v5h6L12 20z" :fill="comment.userVote === -1 ? 'var(--downvote)' : 'currentColor'" stroke="none"/>
          </svg>
        </button>
      </div>

      <!-- Body -->
      <div class="comment-body">
        <div class="comment-header">
          <router-link :to="`/u/${comment.authorUsername}`" class="comment-author">
            {{ comment.authorUsername }}
          </router-link>
          <span class="comment-time">{{ timeAgo(comment.createdAt) }}</span>
        </div>
        <p class="comment-text">{{ comment.content }}</p>
        <div class="comment-actions">
          <button class="action-btn-sm" @click="showReplyBox = !showReplyBox">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            Reply
          </button>
        </div>

        <!-- Reply Box -->
        <div v-if="showReplyBox" class="reply-box">
          <textarea
            v-model="replyContent"
            class="textarea"
            placeholder="What are your thoughts?"
            rows="3"
          ></textarea>
          <div class="reply-actions">
            <button class="btn btn-sm btn-secondary" @click="showReplyBox = false">Cancel</button>
            <button class="btn btn-sm btn-primary" :disabled="!replyContent.trim()" @click="submitReply">Reply</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Children -->
    <div v-if="comment.children && comment.children.length > 0" class="comment-children">
      <CommentItem
        v-for="child in comment.children"
        :key="child.id"
        :comment="child"
        :post-id="postId"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useCommentStore } from '../stores/comment'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
dayjs.extend(relativeTime)

const props = defineProps({
  comment: { type: Object, required: true },
  postId: { type: Number, required: true }
})

const commentStore = useCommentStore()
const authStore = useAuthStore()
const router = useRouter()

const showReplyBox = ref(false)
const replyContent = ref('')

function timeAgo(date) {
  return date ? dayjs(date).fromNow() : ''
}

async function handleVote(voteType) {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  await commentStore.voteComment(props.comment.id, voteType)
}

async function submitReply() {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  if (!replyContent.value.trim()) return
  await commentStore.create(props.postId, replyContent.value, props.comment.id)
  replyContent.value = ''
  showReplyBox.value = false
}
</script>

<style scoped>
.comment-item {
  position: relative;
}

.comment-thread-line {
  position: absolute;
  left: -12px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: var(--border-color);
  border-radius: 1px;
}

.comment-thread-line:hover {
  background: var(--accent-primary);
}

.comment-main {
  display: flex;
  gap: 8px;
  padding: 8px 0;
}

.comment-votes {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0;
  flex-shrink: 0;
}

.vote-btn-sm {
  color: var(--text-secondary);
  padding: 2px;
  border-radius: 2px;
}

.vote-btn-sm:hover {
  background: var(--bg-hover);
}

.vote-count-sm {
  font-size: 0.714rem;
  font-weight: 700;
  color: var(--text-primary);
}

.vote-count-sm.upvoted { color: var(--upvote); }
.vote-count-sm.downvoted { color: var(--downvote); }

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.comment-author {
  font-size: 0.786rem;
  font-weight: 600;
  color: var(--text-primary);
}

.comment-time {
  font-size: 0.714rem;
  color: var(--text-tertiary);
}

.comment-text {
  font-size: 0.929rem;
  color: var(--text-primary);
  line-height: 1.5;
  white-space: pre-wrap;
}

.comment-actions {
  display: flex;
  gap: 4px;
  margin-top: 4px;
}

.action-btn-sm {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 6px;
  font-size: 0.714rem;
  font-weight: 700;
  color: var(--text-secondary);
  border-radius: 2px;
}

.action-btn-sm:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.reply-box {
  margin-top: 8px;
}

.reply-box .textarea {
  min-height: 80px;
  font-size: 0.857rem;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

.comment-children {
  padding-left: 12px;
}
</style>
