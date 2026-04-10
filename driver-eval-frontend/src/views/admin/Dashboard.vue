<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">后台首页</h2>
    <el-row :gutter="20">
      <el-col :span="4" v-for="item in statCards" :key="item.key">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
          <div class="stat-card" :style="{ borderLeft: `4px solid ${item.color}` }">
            <div class="stat-icon" :style="{ color: item.color }">
              <el-icon :size="36"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value" :style="{ color: item.color }">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getStats } from '@/api/dashboard'
import { User, Van, List, ChatLineSquare, WarnTriangleFilled, Calendar } from '@element-plus/icons-vue'

const stats = ref({})

const statCards = computed(() => [
  { key: 'passengers', label: '乘客总数', value: stats.value.totalPassengers ?? 0, color: '#409EFF', icon: User },
  { key: 'drivers', label: '司机总数', value: stats.value.totalDrivers ?? 0, color: '#409EFF', icon: Van },
  { key: 'orders', label: '订单总数', value: stats.value.totalOrders ?? 0, color: '#67C23A', icon: List },
  { key: 'complaints', label: '投诉总数', value: stats.value.totalComplaints ?? 0, color: '#E6A23C', icon: ChatLineSquare },
  { key: 'punishments', label: '生效处罚', value: stats.value.activePunishments ?? 0, color: '#F56C6C', icon: WarnTriangleFilled },
  { key: 'todayOrders', label: '今日订单', value: stats.value.todayOrders ?? 0, color: '#67C23A', icon: Calendar }
])

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
.page-container {
  padding: 20px;
}
.stat-card {
  display: flex;
  align-items: center;
  padding-left: 16px;
}
.stat-icon {
  margin-right: 16px;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  line-height: 1.4;
}
.stat-label {
  font-size: 14px;
  color: #909399;
}
</style>
