<template>
  <div class="auth-wrap">
    <el-card class="auth-card">
      <div class="auth-head">
        <div class="auth-logo">S</div>
        <h2>登录 StudyFlow</h2>
        <p class="sub">记录学习计划，坚持每日打卡</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @keyup.enter="onSubmit"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="you@example.com" :prefix-icon="Message" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">
          登录
        </el-button>
      </el-form>

      <div class="auth-foot">
        还没有账号？
        <router-link to="/register" class="link">立即注册</router-link>
      </div>

      <el-alert
        class="demo-tip"
        type="info"
        :closable="false"
        title="演示账号"
        description="邮箱 demo@studyflow.com · 密码 123456（后端就绪后由真实账号替代）"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { Lock, Message } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  email: '',
  password: '',
})

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function onSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await auth.login(form.email, form.password)
      const redirect = (route.query.redirect as string) || '/home'
      router.push(redirect)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.auth-wrap {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f0fe 0%, #f5f7fa 100%);
}

.auth-card {
  width: 400px;
  padding: 8px 12px;
}

.auth-head {
  text-align: center;
  margin-bottom: 20px;
}

.auth-logo {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--sf-primary);
  color: #fff;
  font-size: 26px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.auth-head h2 {
  margin: 14px 0 4px;
}

.sub {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.submit {
  width: 100%;
}

.auth-foot {
  margin-top: 16px;
  text-align: center;
  color: #606266;
  font-size: 14px;
}

.link {
  color: var(--sf-primary);
}

.demo-tip {
  margin-top: 16px;
}
</style>
