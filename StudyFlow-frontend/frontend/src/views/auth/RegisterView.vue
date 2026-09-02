<template>
  <div class="auth-wrap">
    <el-card class="auth-card">
      <div class="auth-head">
        <div class="auth-logo">S</div>
        <h2>注册账号</h2>
        <p class="sub">创建你的 StudyFlow 学习账号</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @keyup.enter="onSubmit"
      >
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="给自己起个名字" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="you@example.com" :prefix-icon="Message" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="至少 8 位"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次输入密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">
          注册
        </el-button>
      </el-form>

      <div class="auth-foot">
        已有账号？
        <router-link to="/login" class="link">去登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { Lock, Message, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
})

function validateConfirm(_rule: unknown, value: string, callback: (err?: Error) => void) {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码至少 8 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
}

async function onSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      // 注册成功即视为已登录，直接进入首页
      await auth.register({
        email: form.email,
        password: form.password,
        nickname: form.nickname,
      })
      router.push('/home')
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
</style>
