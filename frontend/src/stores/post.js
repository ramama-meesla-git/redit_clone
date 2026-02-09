import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const usePostStore = defineStore('post', () => {
  const posts = ref([])
  const currentPost = ref(null)
  const loading = ref(false)
  const hasMore = ref(true)
  const page = ref(0)
  const sort = ref('hot')

  async function fetchFeed(sortBy = 'hot', reset = false) {
    if (reset) {
      page.value = 0
      posts.value = []
      hasMore.value = true
    }
    sort.value = sortBy
    loading.value = true
    try {
      const { data } = await api.get('/posts', {
        params: { sort: sortBy, page: page.value, size: 20 }
      })
      if (data.content.length < 20) hasMore.value = false
      posts.value = reset ? data.content : [...posts.value, ...data.content]
      page.value++
    } finally {
      loading.value = false
    }
  }

  async function fetchBySubreddit(subredditId, sortBy = 'hot', reset = false) {
    if (reset) {
      page.value = 0
      posts.value = []
      hasMore.value = true
    }
    sort.value = sortBy
    loading.value = true
    try {
      const { data } = await api.get(`/posts/subreddit/${subredditId}`, {
        params: { sort: sortBy, page: page.value, size: 20 }
      })
      if (data.content.length < 20) hasMore.value = false
      posts.value = reset ? data.content : [...posts.value, ...data.content]
      page.value++
    } finally {
      loading.value = false
    }
  }

  async function fetchPost(id) {
    loading.value = true
    try {
      const { data } = await api.get(`/posts/${id}`)
      currentPost.value = data
      return data
    } finally {
      loading.value = false
    }
  }

  async function createPost(postData) {
    const { data } = await api.post('/posts', postData)
    return data
  }

  async function deletePost(id) {
    await api.delete(`/posts/${id}`)
    posts.value = posts.value.filter(p => p.id !== id)
  }

  async function vote(postId, voteType) {
    const { data } = await api.post('/votes', { postId, voteType })
    // Update in list
    const idx = posts.value.findIndex(p => p.id === postId)
    if (idx !== -1) {
      posts.value[idx].voteCount = data.voteCount
      posts.value[idx].userVote = data.userVote
    }
    // Update current
    if (currentPost.value?.id === postId) {
      currentPost.value.voteCount = data.voteCount
      currentPost.value.userVote = data.userVote
    }
    return data
  }

  return { posts, currentPost, loading, hasMore, page, sort, fetchFeed, fetchBySubreddit, fetchPost, createPost, deletePost, vote }
})
