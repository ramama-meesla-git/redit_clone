import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useSubredditStore = defineStore('subreddit', () => {
  const currentSubreddit = ref(null)
  const mySubreddits = ref([])
  const topSubreddits = ref([])
  const loading = ref(false)

  async function fetchByName(name) {
    loading.value = true
    try {
      const { data } = await api.get(`/subreddits/${name}`)
      currentSubreddit.value = data
      return data
    } finally {
      loading.value = false
    }
  }

  async function fetchTop(limit = 10) {
    try {
      const { data } = await api.get('/subreddits/top', { params: { limit } })
      topSubreddits.value = data
      return data
    } catch { return [] }
  }

  async function fetchMine() {
    try {
      const { data } = await api.get('/subreddits/mine')
      mySubreddits.value = data
      return data
    } catch { return [] }
  }

  async function create(name, description) {
    const { data } = await api.post('/subreddits', { name, description })
    return data
  }

  async function join(id) {
    await api.post(`/subreddits/${id}/join`)
    if (currentSubreddit.value?.id === id) {
      currentSubreddit.value.isMember = true
      currentSubreddit.value.memberCount++
    }
    await fetchMine()
  }

  async function leave(id) {
    await api.post(`/subreddits/${id}/leave`)
    if (currentSubreddit.value?.id === id) {
      currentSubreddit.value.isMember = false
      currentSubreddit.value.memberCount--
    }
    await fetchMine()
  }

  return { currentSubreddit, mySubreddits, topSubreddits, loading, fetchByName, fetchTop, fetchMine, create, join, leave }
})
