<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">个人信息</h2>
    <el-card shadow="hover">
      <div class="profile-info">
        <!-- 头像 -->
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            :http-request="handleAvatarUpload"
            :show-file-list="false"
            accept="image/*"
          >
            <el-avatar :size="100" :src="fullAvatarUrl" class="avatar">
              {{ profile.realName?.charAt(0) || '乘' }}
            </el-avatar>
            <div class="avatar-overlay">
              <el-icon :size="20"><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </el-upload>
        </div>

        <!-- 信息展示 -->
        <el-descriptions :column="1" border style="margin-top: 20px; width: 100%; max-width: 500px">
          <el-descriptions-item label="用户名">{{ profile.username || '-' }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ profile.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ profile.phone || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-button type="primary" style="margin-top: 20px" @click="openEditDialog">
          <el-icon><Edit /></el-icon> 编辑信息
        </el-button>
      </div>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑个人信息" width="450px" destroy-on-close>
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateProfile" :loading="editLoading">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera, Edit } from '@element-plus/icons-vue'
import { getPassengerProfile, updatePassengerProfile } from '@/api/user'
import { upload } from '@/api/common'
import { getUserInfo, setUserInfo } from '@/utils/auth'

const profile = ref({})
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref(null)

const editForm = ref({
  realName: '',
  username: '',
  phone: ''
})

const editRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 30, message: '用户名长度必须在3-30个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const fullAvatarUrl = computed(() => {
  const avatar = profile.value.avatar
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  return avatar
})

async function fetchProfile() {
  try {
    const res = await getPassengerProfile()
    profile.value = res.data || res
    syncStoredUserInfo()
  } catch (e) {
    console.error(e)
  }
}

function syncStoredUserInfo() {
  const current = getUserInfo() || {}
  setUserInfo({
    ...current,
    id: profile.value.id,
    username: profile.value.username,
    realName: profile.value.realName,
    phone: profile.value.phone,
    avatar: profile.value.avatar,
    role: profile.value.role ?? current.role
  })
}

async function handleAvatarUpload(options) {
  try {
    const res = await upload(options.file)
    const url = res.data?.url || res.data || res.url
    // 更新头像
    await updatePassengerProfile({ avatar: url })
    ElMessage.success('头像更新成功')
    await fetchProfile()
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

function openEditDialog() {
  editForm.value = {
    realName: profile.value.realName || '',
    username: profile.value.username || '',
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
    await updatePassengerProfile(editForm.value)
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
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.profile-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.avatar-section {
  position: relative;
  cursor: pointer;
}
.avatar {
  background-color: #409eff;
  color: #fff;
  font-size: 36px;
}
.avatar-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  text-align: center;
  padding: 4px 0;
  border-radius: 0 0 50px 50px;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
</style>
