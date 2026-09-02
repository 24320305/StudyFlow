<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <span class="logo-mark">S</span>
        <span class="logo-text">StudyFlow</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="menu"
        background-color="#001529"
        text-color="#a6adb4"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/plans">
          <el-icon><Notebook /></el-icon>
          <span>学习计划</span>
        </el-menu-item>
        <el-menu-item index="/checkins">
          <el-icon><EditPen /></el-icon>
          <span>每日打卡</span>
        </el-menu-item>
        <el-menu-item index="/statistics">
          <el-icon><TrendCharts /></el-icon>
          <span>学习统计</span>
        </el-menu-item>
        <el-menu-item index="/community">
          <el-icon><ChatDotRound /></el-icon>
          <span>学习社区</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-title">{{ pageTitle }}</div>
        <el-dropdown @command="onCommand">
          <span class="user-chip">
            <el-avatar :size="30">{{ avatarText }}</el-avatar>
            <span class="nickname">{{ auth.user?.nickname ?? '用户' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>
                {{ auth.user?.email }}
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  ChatDotRound,
  EditPen,
  HomeFilled,
  Notebook,
  SwitchButton,
  TrendCharts,
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => (route.meta.title as string | undefined) ?? '')
const avatarText = computed(() => {
  const nickname = auth.user?.nickname ?? '用'
  return nickname.charAt(0).toUpperCase()
})

async function onCommand(command: string) {
  if (command !== 'logout') return
  await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning',
  })
  await auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.aside {
  background-color: #001529;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  color: #fff;
}

.logo-mark {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: var(--sf-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
}

.logo-text {
  font-weight: 600;
  font-size: 18px;
}

.menu {
  border-right: none;
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
  padding: 0 20px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #303133;
  outline: none;
}

.nickname {
  font-size: 14px;
}

.main {
  padding: 20px;
  overflow: auto;
}
</style>
