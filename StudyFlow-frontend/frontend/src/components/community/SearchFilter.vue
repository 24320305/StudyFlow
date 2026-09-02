<template>
  <el-form class="search-filter" inline @submit.prevent>
    <el-form-item>
      <el-input
        v-model="keyword"
        clearable
        placeholder="搜索动态内容"
        :prefix-icon="Search"
        @clear="emitSearch"
        @keyup.enter="emitSearch"
      />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" :icon="Search" @click="emitSearch">搜索</el-button>
      <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { RefreshLeft, Search } from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  search: []
}>()

const keyword = ref(props.modelValue)

watch(
  () => props.modelValue,
  (value) => {
    keyword.value = value
  },
)

function emitSearch() {
  emit('update:modelValue', keyword.value.trim())
  emit('search')
}

function reset() {
  keyword.value = ''
  emitSearch()
}
</script>

<style scoped>
.search-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.search-filter :deep(.el-form-item) {
  margin-bottom: 0;
}

.search-filter :deep(.el-input) {
  width: 280px;
}
</style>
