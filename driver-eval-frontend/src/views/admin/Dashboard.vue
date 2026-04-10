<template>
  <div class="page-container">
    <h2>管理后台</h2>

    <!-- Stats Cards -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="4" v-for="item in statCards" :key="item.key">
        <el-card shadow="hover" :body-style="{ padding: '0' }">
          <div class="stat-card" :style="{ '--card-color': item.color }">
            <div class="stat-icon-wrap" :style="{ background: item.color + '18' }">
              <el-icon :size="32" :style="{ color: item.color }"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value" :style="{ color: item.color }">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Quick Actions -->
    <el-card shadow="hover" style="margin-top: 24px">
      <template #header>
        <div class="section-header">
          <el-icon :size="18" color="#4A90D9"><Operation /></el-icon>
          <span>快捷操作</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="action in quickActions" :key="action.label">
          <router-link :to="action.path" class="quick-action-link">
            <div class="quick-action" :style="{ '--action-color': action.color }">
              <el-icon :size="24" :style="{ color: action.color }"><component :is="action.icon" /></el-icon>
              <span>{{ action.label }}</span>
            </div>
          </router-link>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getStats } from '@/api/dashboard'
import {
  User, Van, List, ChatLineSquare, WarnTriangleFilled, Calendar,
  Operation, Setting, DataAnalysis, Tickets
} from '@element-plus/icons-vue'

const stats = ref({})

const statCards = computed(() => [
  { key: 'passengers', label: '乘客总数', value: stats.value.totalPassengers ?? 0, color: '#4A90D9', icon: User },
  { key: 'drivers', label: '司机总数', value: stats.value.totalDrivers ?? 0, color: '#9b59b6', icon: Van },
  { key: 'orders', label: '订单总数', value: stats.value.totalOrders ?? 0, color: '#52c41a', icon: List },
  { key: 'complaints', label: '投诉总数', value: stats.value.totalComplaints ?? 0, color: '#E6A23C', icon: ChatLineSquare },
  { key: 'punishments', label: '生效处罚', value: stats.value.activePunishments ?? 0, color: '#F56C6C', icon: WarnTriangleFilled },
  { key: 'todayOrders', label: '今日订单', value: stats.value.todayOrders ?? 0, color: '#1abc9c', icon: Calendar }
])

const quickActions = [
  { label: '用户管理', path: '/admin/users', color: '#4A90D9', icon: User },
  { label: '订单管理', path: '/admin/order/dispatch', color: '#52c41a', icon: Tickets },
  { label: '投诉处理', path: '/admin/complaint', color: '#E6A23C', icon: ChatLineSquare },
  { label: '数据统计', path: '/admin/tag-stats', color: '#9b59b6', icon: DataAnalysis },
]

const loadStats = async () => {
  try {
    const res = await getStats()
    stats.value = res.data || res
  } catch (e) {
    console.error('获取统计数据失败', e)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.stats-row .el-col {
  margin-bottom: 16px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  border-left: 4px solid var(--card-color);
  transition: transform 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
}
.stat-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-info {
  flex: 1;
  min-width: 0;
}
.stat-value {
  font-size: 28px;
  font-weight: 800;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
  font-weight: 500;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 15px;
}

.quick-action-link {
  text-decoration: none;
  display: block;
}
.quick-action {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  border-radius: 10px;
  background: #f8f9fc;
  cursor: pointer;
  transition: all 0.25s ease;
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}
.quick-action:hover {
  background: color-mix(in srgb, var(--action-color) 10%, white);
  transform: translateX(4px);
}
</style>
