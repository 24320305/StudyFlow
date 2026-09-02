<template>
  <el-button
    link
    :type="liked ? 'primary' : 'info'"
    :icon="liked ? StarFilled : Star"
    :loading="loading"
    @click.stop="toggle"
  >
    {{ likeCount }}
  </el-button>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { likePost, unlikePost } from '@/api/community'

const props = defineProps<{
  postId: number
  liked: boolean
  likeCount: number
}>()

const emit = defineEmits<{
  changed: [liked: boolean, likeCount: number]
}>()

const liked = ref(props.liked)
const likeCount = ref(props.likeCount)
const loading = ref(false)

watch(
  () => [props.liked, props.likeCount] as const,
  ([nextLiked, nextCount]) => {
    liked.value = nextLiked
    likeCount.value = nextCount
  },
)

async function toggle() {
  loading.value = true
  try {
    const state = liked.value ? await unlikePost(props.postId) : await likePost(props.postId)
    liked.value = state.liked
    likeCount.value = state.likeCount
    emit('changed', state.liked, state.likeCount)
  } finally {
    loading.value = false
  }
}
</script>
