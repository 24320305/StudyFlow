<template>
  <el-button
    v-if="!isSelf"
    size="small"
    :type="following ? 'info' : 'primary'"
    :plain="following"
    :icon="following ? Check : Plus"
    :loading="loading"
    @click.stop="toggle"
  >
    {{ following ? '已关注' : '关注' }}
  </el-button>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Check, Plus } from '@element-plus/icons-vue'
import { followUser, unfollowUser } from '@/api/community'

const props = defineProps<{
  userId: number
  isSelf?: boolean
  initialFollowing?: boolean
}>()

const following = ref(Boolean(props.initialFollowing))
const loading = ref(false)

async function toggle() {
  loading.value = true
  try {
    const state = following.value ? await unfollowUser(props.userId) : await followUser(props.userId)
    following.value = state.following
  } finally {
    loading.value = false
  }
}
</script>
