<template>
  <div class="page-layout">
    <div class="page-content">
      <div class="create-post card">
        <h2 class="create-title">Create a Post</h2>

        <!-- Subreddit Selector -->
        <div class="form-group">
          <select v-model="form.subredditId" class="input">
            <option value="">Choose a community</option>
            <option v-for="sub in mySubreddits" :key="sub.id" :value="sub.id">r/{{ sub.name }}</option>
          </select>
        </div>

        <!-- Post Type Tabs -->
        <div class="post-type-tabs">
          <button
            v-for="t in postTypes"
            :key="t.value"
            class="type-tab"
            :class="{ active: form.postType === t.value }"
            @click="form.postType = t.value"
          >
            <span v-html="t.icon"></span>
            {{ t.label }}
          </button>
        </div>

        <!-- Title -->
        <div class="form-group">
          <input v-model="form.title" type="text" class="input" placeholder="Title" maxlength="300" />
          <span class="char-count">{{ form.title.length }}/300</span>
        </div>

        <!-- Text Content -->
        <div v-if="form.postType === 'TEXT'" class="form-group">
          <textarea v-model="form.content" class="textarea" placeholder="Text (optional)" rows="6"></textarea>
        </div>

        <!-- Link -->
        <div v-if="form.postType === 'LINK'" class="form-group">
          <input v-model="form.url" type="url" class="input" placeholder="URL" />
        </div>

        <!-- Image Upload -->
        <div v-if="form.postType === 'IMAGE'" class="form-group">
          <label class="upload-area" @dragover.prevent @drop.prevent="handleDrop">
            <input type="file" accept="image/*" @change="handleFileSelect" hidden />
            <div v-if="!imagePreview" class="upload-placeholder">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="var(--text-tertiary)" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/>
              </svg>
              <p>Drag and drop or click to upload</p>
            </div>
            <img v-else :src="imagePreview" alt="Preview" class="image-preview" />
          </label>
        </div>

        <!-- Submit -->
        <div v-if="error" class="auth-error" style="margin-bottom:12px">{{ error }}</div>
        <div class="form-actions">
          <button class="btn btn-secondary" @click="$router.back()">Cancel</button>
          <button
            class="btn btn-primary"
            :disabled="!canSubmit || loading"
            @click="handleSubmit"
          >
            <span v-if="loading" class="spinner" style="width:16px;height:16px;border-width:2px"></span>
            <span v-else>Post</span>
          </button>
        </div>
      </div>
    </div>

    <div class="page-sidebar">
      <Sidebar />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { usePostStore } from '../stores/post'
import { useSubredditStore } from '../stores/subreddit'
import Sidebar from '../components/Sidebar.vue'

const router = useRouter()
const route = useRoute()
const postStore = usePostStore()
const subredditStore = useSubredditStore()

const form = reactive({
  title: '',
  content: '',
  url: '',
  postType: 'TEXT',
  subredditId: ''
})
const loading = ref(false)
const error = ref('')
const imageFile = ref(null)
const imagePreview = ref(null)
const mySubreddits = computed(() => subredditStore.mySubreddits)

const postTypes = [
  { value: 'TEXT', label: 'Post', icon: '📝' },
  { value: 'IMAGE', label: 'Image', icon: '🖼️' },
  { value: 'LINK', label: 'Link', icon: '🔗' }
]

const canSubmit = computed(() => {
  if (!form.title.trim() || !form.subredditId) return false
  if (form.postType === 'LINK' && !form.url) return false
  if (form.postType === 'IMAGE' && !imageFile.value) return false
  return true
})

function handleFileSelect(e) {
  const file = e.target.files[0]
  if (file) {
    imageFile.value = file
    imagePreview.value = URL.createObjectURL(file)
  }
}

function handleDrop(e) {
  const file = e.dataTransfer.files[0]
  if (file && file.type.startsWith('image/')) {
    imageFile.value = file
    imagePreview.value = URL.createObjectURL(file)
  }
}

async function handleSubmit() {
  loading.value = true
  error.value = ''
  try {
    const result = await postStore.createPost({
      title: form.title,
      content: form.content || null,
      url: form.url || null,
      postType: form.postType,
      subredditId: parseInt(form.subredditId)
    })
    router.push(`/r/${result.subredditName}/post/${result.id}`)
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to create post'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  subredditStore.fetchMine()
  if (route.params.name) {
    subredditStore.fetchByName(route.params.name).then(sub => {
      if (sub) form.subredditId = sub.id
    })
  }
})
</script>

<style scoped>
.create-post {
  padding: 20px;
  animation: fadeIn 0.3s ease;
}

.create-title {
  font-size: 1.286rem;
  font-weight: 700;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}

.form-group {
  margin-bottom: 16px;
  position: relative;
}

.char-count {
  position: absolute;
  right: 12px;
  bottom: 10px;
  font-size: 0.714rem;
  color: var(--text-tertiary);
}

.post-type-tabs {
  display: flex;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  overflow: hidden;
  margin-bottom: 16px;
}

.type-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  border-right: 1px solid var(--border-color);
  transition: all var(--transition-fast);
}

.type-tab:last-child { border-right: none; }
.type-tab:hover { background: var(--bg-hover); }
.type-tab.active {
  background: var(--bg-hover);
  color: var(--accent-primary);
  border-bottom: 2px solid var(--accent-primary);
}

.upload-area {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: border-color var(--transition-fast);
}

.upload-area:hover { border-color: var(--accent-primary); }

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: var(--text-tertiary);
}

.image-preview {
  max-height: 300px;
  border-radius: var(--radius-sm);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.auth-error {
  padding: 10px 14px;
  background: rgba(255, 69, 0, 0.1);
  border: 1px solid rgba(255, 69, 0, 0.3);
  border-radius: var(--radius-sm);
  color: var(--accent-primary);
  font-size: 0.857rem;
}
</style>
