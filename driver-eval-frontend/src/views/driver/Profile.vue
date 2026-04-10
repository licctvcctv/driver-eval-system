<template>
  <div class="page-container">
    <el-row :gutter="20">
      <!-- 个人信息卡片 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
              <el-button type="primary" link @click="openEditDialog">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
            </div>
          </template>
          <div class="profile-info">
            <el-avatar :size="80" :src="profile.avatar || ''" class="avatar">
              {{ profile.realName?.charAt(0) || '司' }}
            </el-avatar>
            <el-descriptions :column="1" border style="margin-top: 16px">
              <el-descriptions-item label="姓名">{{ profile.realName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ profile.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="用户名">{{ profile.username || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>

      <!-- 司机信息卡片 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>司机信息</span>
          </template>
          <div class="driver-stats">
            <div class="score-section">
              <div class="score-number" :style="{ color: scoreColor }">
                {{ profile.score ?? '-' }}
              </div>
              <div class="score-label">综合评分</div>
            </div>
            <el-tag :type="levelTagType" size="large" class="level-badge">
              {{ levelText }}
            </el-tag>
          </div>
          <el-descriptions :column="2" border style="margin-top: 16px">
            <el-descriptions-item label="总订单数">{{ profile.totalOrders ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="总投诉数">{{ profile.totalComplaints ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="本周投诉">{{ profile.weekComplaints ?? 0 }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 在线状态卡片 -->
    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <span>在线状态</span>
      </template>
      <div class="status-section">
        <div class="status-indicator">
          <span class="status-dot" :style="{ backgroundColor: statusDotColor }"></span>
          <span class="status-text">{{ statusText }}</span>
        </div>
        <div class="status-actions">
          <el-button-group v-if="profile.onlineStatus !== 2">
            <el-button
              :type="profile.onlineStatus === 1 ? 'success' : 'default'"
              @click="handleGoOnline"
              :loading="statusLoading"
              :disabled="profile.onlineStatus === 1"
            >
              上线
            </el-button>
            <el-button
              :type="profile.onlineStatus === 0 ? 'info' : 'default'"
              @click="handleGoOffline"
              :loading="statusLoading"
              :disabled="profile.onlineStatus === 0"
            >
              离线
            </el-button>
          </el-button-group>
          <el-button type="danger" disabled v-else>
            处罚中 - 无法切换状态
          </el-button>
        </div>
        <div v-if="showVehicleHint" class="vehicle-hint" style="margin-top: 8px">
          <el-alert type="warning" :closable="false" show-icon>
            请先绑定车辆信息再上线。
            <el-button type="primary" link @click="goToVehicle">前往车辆管理</el-button>
          </el-alert>
        </div>
        <div v-if="profile.onlineStatus === 2 && profile.punishEndTime" class="punish-info">
          <el-alert type="error" :closable="false" show-icon>
            处罚结束时间：{{ profile.punishEndTime }}
            <span v-if="punishCountdown">（剩余 {{ punishCountdown }}）</span>
          </el-alert>
        </div>
      </div>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑个人信息" width="450px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateProfile" :loading="editLoading">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Edit } from '@element-plus/icons-vue'
import { ElMessage, ElNotification } from 'element-plus'
import { getDriverProfile, goOnline, goOffline, updateProfile } from '@/api/user'

const router = useRouter()
const showVehicleHint = ref(false)

const profile = ref({})
const loading = ref(false)
const statusLoading = ref(false)
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref(null)
const punishCountdown = ref('')
let countdownTimer = null

const editForm = ref({
  realName: '',
  phone: ''
})

const editRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const scoreColor = computed(() => {
  const score = profile.value.score
  if (score >= 90) return '#67c23a'
  if (score >= 70) return '#e6a23c'
  return '#f56c6c'
})

const levelText = computed(() => {
  const level = profile.value.level
  if (level === '金牌') return '金牌司机'
  if (level === '银牌') return '银牌司机'
  if (level === '普通') return '普通司机'
  if (level === 3) return '金牌司机'
  if (level === 2) return '银牌司机'
  return '普通司机'
})

const levelTagType = computed(() => {
  const level = profile.value.level
  if (level === '金牌') return 'warning'
  if (level === '银牌') return ''
  if (level === '普通') return 'info'
  if (level === 3) return 'warning'
  if (level === 2) return ''
  return 'info'
})

const statusText = computed(() => {
  const s = profile.value.onlineStatus
  if (s === 1) return '在线'
  if (s === 2) return '处罚中'
  return '离线'
})

const statusDotColor = computed(() => {
  const s = profile.value.onlineStatus
  if (s === 1) return '#67c23a'
  if (s === 2) return '#f56c6c'
  return '#909399'
})

function startPunishCountdown() {
  if (countdownTimer) clearInterval(countdownTimer)
  if (profile.value.onlineStatus !== 2 || !profile.value.punishEndTime) return
  countdownTimer = setInterval(() => {
    const end = new Date(profile.value.punishEndTime).getTime()
    const now = Date.now()
    const diff = end - now
    if (diff <= 0) {
      punishCountdown.value = '已结束'
      clearInterval(countdownTimer)
      ElNotification({
        title: '处罚已解除',
        message: '处罚已解除，您现在可以重新上线',
        type: 'success',
        duration: 5000
      })
      fetchProfile()
      return
    }
    const hours = Math.floor(diff / 3600000)
    const minutes = Math.floor((diff % 3600000) / 60000)
    const seconds = Math.floor((diff % 60000) / 1000)
    punishCountdown.value = `${hours}小时${minutes}分${seconds}秒`
  }, 1000)
}

async function fetchProfile() {
  loading.value = true
  try {
    const res = await getDriverProfile()
    profile.value = res.data || res
    startPunishCountdown()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleGoOnline() {
  statusLoading.value = true
  showVehicleHint.value = false
  try {
    await goOnline()
    ElMessage.success('已上线')
    await fetchProfile()
  } catch (e) {
    const msg = e.message || '操作失败'
    ElMessage.error(msg)
    if (msg.includes('绑定车辆')) {
      showVehicleHint.value = true
    }
  } finally {
    statusLoading.value = false
  }
}

function goToVehicle() {
  router.push('/driver/vehicle')
}

async function handleGoOffline() {
  statusLoading.value = true
  try {
    await goOffline()
    ElMessage.success('已离线')
    await fetchProfile()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    statusLoading.value = false
  }
}

function openEditDialog() {
  editForm.value = {
    realName: profile.value.realName || '',
    phone: profile.value.phone || ''
  }
  editDialogVisible.value = true
}

async function handleUpdateProfile() {
  try {
    await editFormRef.value.validate()
  } catch { return }
  editLoading.value = true
  try {
    await updateProfile(editForm.value)
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    await fetchProfile()
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  } finally {
    editLoading.value = false
  }
}

onMounted(() => {
  fetchProfile()
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.profile-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.avatar {
  background-color: #409eff;
  color: #fff;
  font-size: 28px;
}
.driver-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
}
.score-section {
  text-align: center;
}
.score-number {
  font-size: 48px;
  font-weight: bold;
  line-height: 1.2;
}
.score-label {
  font-size: 14px;
  color: #909399;
}
.level-badge {
  font-size: 16px;
  padding: 8px 16px;
}
.status-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}
.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}
.status-actions {
  display: flex;
  gap: 12px;
}
.punish-info {
  margin-top: 8px;
}
</style>
