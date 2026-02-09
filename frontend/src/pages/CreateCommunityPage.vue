<template>
  <div class="page-layout">
    <div class="page-content">
      <div class="create-community card">
        <h2 class="create-title">Create a Community</h2>

        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label class="form-label">Name</label>
            <div class="name-input-wrapper">
              <span class="name-prefix">r/</span>
              <input
                v-model="form.name"
                type="text"
                class="input name-input"
                placeholder="community_name"
                maxlength="21"
                required
              />
            </div>
            <span class="form-hint">Community names cannot be changed after creation</span>
          </div>

          <div class="form-group">
            <label class="form-label">Description</label>
            <textarea
              v-model="form.description"
              class="textarea"
              placeholder="Tell people what your community is about"
              rows="4"
            ></textarea>
          </div>

          <div v-if="error" class="auth-error" style="margin-bottom:12px">{{ error }}</div>

          <div class="form-actions">
            <button type="button" class="btn btn-secondary" @click="$router.back()">Cancel</button>
            <button type="submit" class="btn btn-primary" :disabled="!form.name.trim() || loading">
              <span v-if="loading" class="spinner" style="width:16px;height:16px;border-width:2px"></span>
              <span v-else>Create Community</span>
            </button>
          </div>
        </form>
      </div>
    </div>

    <div class="page-sidebar">
      <Sidebar />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useSubredditStore } from '../stores/subreddit'
import Sidebar from '../components/Sidebar.vue'

const router = useRouter()
const subredditStore = useSubredditStore()

const form = reactive({ name: '', description: '' })
const loading = ref(false)
const error = ref('')

async function handleSubmit() {
  loading.value = true
  error.value = ''
  try {
    const result = await subredditStore.create(form.name, form.description)
    router.push(`/r/${result.name}`)
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to create community'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.create-community {
  padding: 24px;
  max-width: 600px;
  animation: fadeIn 0.3s ease;
}

.create-title {
  font-size: 1.286rem;
  font-weight: 700;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.name-input-wrapper {
  display: flex;
  align-items: center;
  background: var(--bg-input);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.name-prefix {
  padding: 10px 0 10px 14px;
  color: var(--text-secondary);
  font-weight: 600;
  flex-shrink: 0;
}

.name-input {
  border: none !important;
  background: transparent !important;
  padding-left: 0 !important;
}

.name-input:focus {
  box-shadow: none !important;
}

.form-hint {
  display: block;
  margin-top: 6px;
  font-size: 0.714rem;
  color: var(--text-tertiary);
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
