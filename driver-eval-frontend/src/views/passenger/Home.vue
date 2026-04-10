<template>
  <div class="page-container">
    <h2>个人首页</h2>

    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; gap: 16px">
            <el-avatar :size="56" :src="userInfo?.avatar" icon="User" />
            <div>
              <h3 style="margin: 0">欢迎回来，{{ userInfo?.realName || userInfo?.username || '用户' }}</h3>
              <p style="margin: 4px 0 0; color: #909399; font-size: 14px">祝您出行愉快！</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: bold">系统公告</span>
          </template>
          <div v-if="announcements.length === 0" style="text-align: center; color: #909399; padding: 20px 0">
            暂无公告
          </div>
          <div v-for="item in announcements" :key="item.id" class="announcement-item">
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: 500">{{ item.title }}</span>
              <el-tag size="small" type="info">{{ item.createTime }}</el-tag>
            </div>
            <p style="color: #606266; font-size: 13px; margin: 6px 0 0">{{ item.content }}</p>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: bold">最新资讯</span>
          </template>
          <div v-if="newsList.length === 0" style="text-align: center; color: #909399; padding: 20px 0">
            暂无资讯
          </div>
          <el-card
            v-for="item in newsList"
            :key="item.id"
            shadow="never"
            style="margin-bottom: 12px"
            body-style="padding: 12px"
          >
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: 500">{{ item.title }}</span>
              <el-tag size="small">{{ item.createTime }}</el-tag>
            </div>
            <p style="color: #606266; font-size: 13px; margin: 6px 0 0">{{ item.content }}</p>
          </el-card>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAnnouncements } from '@/api/common'
import { getUserInfo } from '@/utils/auth'

const userInfo = ref(getUserInfo())
const announcements = ref([])
const newsList = ref([])

const loadAnnouncements = async () => {
  try {
    const res = await getAnnouncements({ type: 1, pageNum: 1, pageSize: 10 })
    announcements.value = res.data?.records || res.data?.list || res.data || []
  } catch (e) {
    console.error('加载公告失败', e)
  }
}

const loadNews = async () => {
  try {
    const res = await getAnnouncements({ type: 2, pageNum: 1, pageSize: 5 })
    newsList.value = res.data?.records || res.data?.list || res.data || []
  } catch (e) {
    console.error('加载资讯失败', e)
  }
}

onMounted(() => {
  loadAnnouncements()
  loadNews()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.announcement-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.announcement-item:last-child {
  border-bottom: none;
}
</style>
