<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="login-brand">
      <div class="brand-content">
        <!-- 车辆 SVG 图标 -->
        <svg class="brand-car" viewBox="0 0 200 100" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M160 65H175C178 65 180 63 180 60V52C180 49 178 47 175 47H165L155 30C153 27 149 25 146 25H74C70 25 67 27 65 30L52 47H40C37 47 35 49 35 52V60C35 63 37 65 40 65H55" stroke="rgba(255,255,255,0.9)" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
          <circle cx="75" cy="68" r="12" stroke="rgba(255,255,255,0.9)" stroke-width="3"/>
          <circle cx="75" cy="68" r="5" fill="rgba(255,255,255,0.4)"/>
          <circle cx="145" cy="68" r="12" stroke="rgba(255,255,255,0.9)" stroke-width="3"/>
          <circle cx="145" cy="68" r="5" fill="rgba(255,255,255,0.4)"/>
          <path d="M70 47L80 30H140L152 47" stroke="rgba(255,255,255,0.5)" stroke-width="2" stroke-linecap="round"/>
          <line x1="110" y1="30" x2="110" y2="47" stroke="rgba(255,255,255,0.3)" stroke-width="2"/>
          <!-- 信号波纹 -->
          <path d="M105 15 Q110 8 115 15" stroke="rgba(255,255,255,0.5)" stroke-width="2" fill="none"/>
          <path d="M100 10 Q110 0 120 10" stroke="rgba(255,255,255,0.3)" stroke-width="2" fill="none"/>
        </svg>
        <h1 class="brand-title">网约车平台司机评价<br/>信息管理系统</h1>
        <p class="brand-desc">智能派单 / 动态评分 / 投诉处罚 / 数据管理</p>
        <div class="brand-features">
          <div class="feature-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/></svg>
            <span>多维度司机评分体系</span>
          </div>
          <div class="feature-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
            <span>基于高德地图实时定位</span>
          </div>
          <div class="feature-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            <span>完善的投诉与处罚机制</span>
          </div>
        </div>
      </div>
      <!-- 底部道路装饰 -->
      <div class="road-decoration">
        <div class="road-line"></div>
        <div class="road-line"></div>
        <div class="road-line"></div>
      </div>
    </div>

    <!-- 右侧登录区 -->
    <div class="login-form-side">
      <div class="login-card">
        <div class="login-header">
          <h2>欢迎登录</h2>
          <p>请选择您的角色并登录系统</p>
        </div>
        <el-tabs v-model="form.role" stretch class="role-tabs">
          <el-tab-pane label="乘客" name="PASSENGER" />
          <el-tab-pane label="司机" name="DRIVER" />
          <el-tab-pane label="管理员" name="ADMIN" />
        </el-tabs>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="0" style="margin-top: 24px">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">登 录</el-button>
          </el-form-item>
        </el-form>
        <div class="login-footer">
          <span>还没有账号？</span>
          <a @click="$router.push('/register')">立即注册</a>
        </div>
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
      phone: user.phone,
      avatar: user.avatar
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

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

/* 左侧品牌区 */
.login-brand {
  flex: 1;
  background: linear-gradient(160deg, #1a1f36 0%, #2b3a67 50%, #1a2a4a 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px;
  position: relative;
  overflow: hidden;
}

.login-brand::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -30%;
  width: 600px;
  height: 600px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(74,144,217,0.15) 0%, transparent 70%);
}

.login-brand::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -20%;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(74,144,217,0.1) 0%, transparent 70%);
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.brand-car {
  width: 180px;
  height: 90px;
  margin-bottom: 32px;
  opacity: 0.9;
}

.brand-title {
  color: #fff;
  font-size: 32px;
  font-weight: 700;
  line-height: 1.4;
  letter-spacing: 2px;
  margin-bottom: 16px;
}

.brand-desc {
  color: rgba(255,255,255,0.5);
  font-size: 14px;
  letter-spacing: 4px;
  margin-bottom: 48px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: flex-start;
  margin: 0 auto;
  max-width: 280px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255,255,255,0.7);
  font-size: 14px;
}

.feature-item svg {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  color: rgba(74,144,217,0.9);
}

/* 底部道路装饰 */
.road-decoration {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40px;
  background: rgba(0,0,0,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 30px;
  z-index: 1;
}

.road-line {
  width: 60px;
  height: 3px;
  background: rgba(255,255,255,0.25);
  border-radius: 2px;
}

/* 右侧登录区 */
.login-form-side {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #fff;
}

.login-card {
  width: 100%;
  max-width: 380px;
}

.login-header {
  margin-bottom: 32px;
}

.login-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1f36;
  margin-bottom: 8px;
}

.login-header p {
  font-size: 14px;
  color: #909399;
}

.role-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
}

.role-tabs :deep(.el-tabs__active-bar) {
  background-color: #2b3a67;
}

.role-tabs :deep(.el-tabs__item.is-active) {
  color: #1a1f36;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  background: #1a1f36;
  border-color: #1a1f36;
  letter-spacing: 4px;
}

.login-btn:hover,
.login-btn:focus {
  background: #2b3a67;
  border-color: #2b3a67;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #909399;
  font-size: 14px;
}

.login-footer a {
  color: #2b3a67;
  text-decoration: none;
  cursor: pointer;
  font-weight: 500;
  margin-left: 4px;
}

.login-footer a:hover {
  color: #4A90D9;
}

/* 响应式：小屏隐藏左侧 */
@media (max-width: 900px) {
  .login-brand {
    display: none;
  }
  .login-form-side {
    width: 100%;
  }
}
</style>
