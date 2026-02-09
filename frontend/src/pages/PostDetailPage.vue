<template>
  <div class="page-layout">
    <div class="page-content">
      <!-- Post -->
      <PostCard v-if="post" :post="post" :is-detail="true" />

      <!-- Comment Box -->
      <div v-if="post" class="comment-form card">
        <div class="comment-form-inner">
          <p v-if="!authStore.isAuthenticated" class="text-secondary">
            <router-link to="/login">Log in</router-link> to leave a comment
          </p>
          <template v-else>
            <textarea
              v-model="newComment"
              class="textarea"
              placeholder="What are your thoughts?"
              rows="4"
            ></textarea>
            <div class="comment-form-actions">
              <button class="btn btn-primary btn-sm" :disabled="!newComment.trim()" @click="submitComment">
                Comment
              </button>
            </div>
          </template>
        </div>
      </div>

      <!-- Comments -->
      <div class="comments-section">
        <div v-if="commentStore.loading" class="loading-state flex-center">
          <div class="spinner"></div>
        </div>
        <CommentItem
          v-for="comment in commentStore.comments"
          :key="comment.id"
          :comment="comment"
          :post-id="post?.id"
        />
        <div v-if="!commentStore.loading && commentStore.comments.length === 0" class="empty-comments">
          <p class="text-secondary">No comments yet. Start the discussion!</p>
        </div>
      </div>
    </div>

    <div class="page-sidebar">
      <Sidebar :subreddit="subredditInfo" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { usePostStore } from '../stores/post'
import { useCommentStore } from '../stores/comment'
import { useAuthStore } from '../stores/auth'
import PostCard from '../components/PostCard.vue'
import CommentItem from '../components/CommentItem.vue'
import Sidebar from '../components/Sidebar.vue'

const route = useRoute()
const postStore = usePostStore()
const commentStore = useCommentStore()
const authStore = useAuthStore()

const newComment = ref('')
const post = computed(() => postStore.currentPost)
const subredditInfo = computed(() => post.value ? { name: post.value.subredditName, id: post.value.subredditId } : null)

async function loadPost() {
  const postId = route.params.id
  await postStore.fetchPost(postId)
  await commentStore.fetchByPost(postId)
}

async function submitComment() {
  if (!newComment.value.trim()) return
  await commentStore.create(post.value.id, newComment.value)
  newComment.value = ''
}

onMounted(loadPost)
</script>

<style scoped>
.comment-form {
  margin: 12px 0;
}

.comment-form-inner {
  padding: 16px;
}

.comment-form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.comments-section {
  padding: 0 16px;
}

.empty-comments {
  padding: 32px;
  text-align: center;
}

.loading-state { padding: 24px; }
</style>
