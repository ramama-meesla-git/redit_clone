import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useCommentStore = defineStore('comment', () => {
  const comments = ref([])
  const loading = ref(false)

  async function fetchByPost(postId) {
    loading.value = true
    try {
      const { data } = await api.get(`/posts/${postId}/comments`)
      comments.value = data
    } finally {
      loading.value = false
    }
  }

  async function create(postId, content, parentId = null) {
    const { data } = await api.post(`/posts/${postId}/comments`, { content, parentId })
    if (parentId) {
      insertReply(comments.value, parentId, data)
    } else {
      comments.value.unshift(data)
    }
    return data
  }

  async function voteComment(commentId, voteType) {
    const { data } = await api.post('/votes', { commentId, voteType })
    updateVoteInTree(comments.value, commentId, data.voteCount, data.userVote)
    return data
  }

  function insertReply(list, parentId, newComment) {
    for (const c of list) {
      if (c.id === parentId) {
        if (!c.children) c.children = []
        c.children.unshift(newComment)
        return true
      }
      if (c.children && insertReply(c.children, parentId, newComment)) return true
    }
    return false
  }

  function updateVoteInTree(list, commentId, voteCount, userVote) {
    for (const c of list) {
      if (c.id === commentId) {
        c.voteCount = voteCount
        c.userVote = userVote
        return true
      }
      if (c.children && updateVoteInTree(c.children, commentId, voteCount, userVote)) return true
    }
    return false
  }

  return { comments, loading, fetchByPost, create, voteComment }
})
