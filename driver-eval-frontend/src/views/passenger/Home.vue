<template>
  <div class="page-container">
    <!-- Welcome Banner -->
    <div class="welcome-banner">
      <div class="welcome-left">
        <el-avatar :size="64" :src="userInfo?.avatar" icon="User" />
        <div class="welcome-text">
          <h2>欢迎回来，{{ userInfo?.realName || userInfo?.username || '用户' }}</h2>
          <p>祝您出行愉快，随时为您服务</p>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <el-row :gutter="20" class="quick-actions">
      <el-col :span="6">
        <router-link to="/passenger/order/create" class="action-link">
          <el-card shadow="hover" class="action-card action-card--blue">
            <el-icon :size="36"><Promotion /></el-icon>
            <span class="action-title">立即叫车</span>
            <span class="action-desc">快速下单出行</span>
          </el-card>
        </router-link>
      </el-col>
      <el-col :span="6">
        <router-link to="/passenger/order/list" class="action-link">
          <el-card shadow="hover" class="action-card action-card--green">
            <el-icon :size="36"><Document /></el-icon>
            <span class="action-title">我的订单</span>
            <span class="action-desc">查看行程记录</span>
          </el-card>
        </router-link>
      </el-col>
      <el-col :span="6">
        <router-link to="/passenger/eval" class="action-link">
          <el-card shadow="hover" class="action-card action-card--orange">
            <el-icon :size="36"><Star /></el-icon>
            <span class="action-title">评价管理</span>
            <span class="action-desc">评价您的行程</span>
          </el-card>
        </router-link>
      </el-col>
      <el-col :span="6">
        <router-link to="/passenger/complaint" class="action-link">
          <el-card shadow="hover" class="action-card action-card--red">
            <el-icon :size="36"><Warning /></el-icon>
            <span class="action-title">投诉管理</span>
            <span class="action-desc">问题反馈处理</span>
          </el-card>
        </router-link>
      </el-col>
    </el-row>

    <!-- Announcements & News -->
    <el-row :gutter="20" style="margin-top: 24px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon :size="18" color="#4A90D9"><Bell /></el-icon>
              <span>系统公告</span>
            </div>
          </template>
          <div v-if="announcements.length === 0" class="empty-state">暂无公告</div>
          <div v-for="item in announcements" :key="item.id" class="announcement-item">
            <div class="announcement-row">
              <span class="announcement-title">{{ item.title }}</span>
              <el-tag size="small" type="info" round>{{ item.createTime }}</el-tag>
            </div>
            <p class="announcement-content">{{ item.content }}</p>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon :size="18" color="#52c41a"><Notebook /></el-icon>
              <span>最新资讯</span>
            </div>
          </template>
          <div v-if="newsList.length === 0" class="empty-state">暂无资讯</div>
          <div v-for="item in newsList" :key="item.id" class="announcement-item">
            <div class="announcement-row">
              <span class="announcement-title">{{ item.title }}</span>
              <el-tag size="small" round>{{ item.createTime }}</el-tag>
            </div>
            <p class="announcement-content">{{ item.content }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAnnouncements } from '@/api/common'
import { getUserInfo } from '@/utils/auth'
import { Promotion, Document, Star, Warning, Bell, Notebook } from '@element-plus/icons-vue'

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
.welcome-banner {
  background: linear-gradient(135deg, #4A90D9 0%, #357abd 100%);
  border-radius: 12px;
  padding: 28px 32px;
  margin-bottom: 24px;
  color: #fff;
}
.welcome-left {
  display: flex;
  align-items: center;
  gap: 20px;
}
.welcome-text h2 {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
}
.welcome-text p {
  margin: 0;
  font-size: 14px;
  opacity: 0.85;
}

.quick-actions {
  margin-top: 0;
}
.action-link {
  text-decoration: none;
  display: block;
}
.action-card {
  text-align: center;
  padding: 8px 0;
  cursor: pointer;
  transition: transform 0.25s ease;
}
.action-card:hover {
  transform: translateY(-4px);
}
.action-card .el-icon {
  margin-bottom: 10px;
}
.action-title {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #1d1e2c;
  margin-bottom: 4px;
}
.action-desc {
  display: block;
  font-size: 12px;
  color: #909399;
}
.action-card--blue .el-icon { color: #4A90D9; }
.action-card--green .el-icon { color: #52c41a; }
.action-card--orange .el-icon { color: #E6A23C; }
.action-card--red .el-icon { color: #F56C6C; }

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 15px;
}
.empty-state {
  text-align: center;
  color: #909399;
  padding: 32px 0;
}
.announcement-item {
  padding: 14px 0;
  border-bottom: 1px solid #f0f2f5;
}
.announcement-item:last-child {
  border-bottom: none;
}
.announcement-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.announcement-title {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}
.announcement-content {
  color: #606266;
  font-size: 13px;
  margin: 6px 0 0;
  line-height: 1.5;
}
</style>
