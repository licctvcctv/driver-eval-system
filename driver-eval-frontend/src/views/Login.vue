<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2>网约车司机评价管理系统</h2>
      <el-tabs v-model="form.role" stretch>
        <el-tab-pane label="乘客登录" name="PASSENGER" />
        <el-tab-pane label="司机登录" name="DRIVER" />
        <el-tab-pane label="管理员登录" name="ADMIN" />
      </el-tabs>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" style="margin-top: 20px">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="auth-btn" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        <span>还没有账号？</span>
        <a @click="$router.push('/register')">立即注册</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { setToken, setUserInfo } from '../utils/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  role: 'PASSENGER'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const roleHomeMap = {
  PASSENGER: '/passenger/home',
  DRIVER: '/driver/profile',
  ADMIN: '/admin/dashboard'
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    const data = res.data || res
    setToken(data.token)
    const user = data.user || data
    setUserInfo({
      id: user.id,
      username: user.username,
      realName: user.realName,
      role: user.role || form.role,
      phone: user.phone
    })
    ElMessage.success('登录成功')
    const roleMap = { 1: 'PASSENGER', 2: 'DRIVER', 3: 'ADMIN' }
    const roleKey = roleMap[user.role] || form.role
    router.push(roleHomeMap[roleKey])
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>
