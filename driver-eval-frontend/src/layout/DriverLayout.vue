<template>
  <div class="layout-container">
    <div class="layout-sidebar">
      <div class="logo">司机端</div>
      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/driver/profile">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
        <el-menu-item index="/driver/vehicle">
          <el-icon><Van /></el-icon>
          <span>车辆管理</span>
        </el-menu-item>
        <el-sub-menu index="driver-order">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>运营管理</span>
            <el-badge v-if="pendingOrderCount > 0" :value="pendingOrderCount" :max="99" class="sidebar-badge" />
          </template>
          <el-menu-item index="/driver/order/dispatch">
            <span>派单管理</span>
            <el-badge v-if="pendingOrderCount > 0" :value="pendingOrderCount" :max="99" class="menu-item-badge" />
          </el-menu-item>
          <el-menu-item index="/driver/order/completed">完成订单</el-menu-item>
          <el-menu-item index="/driver/order/cancelled">取消订单</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/driver/eval">
          <el-icon><Star /></el-icon>
          <span>乘客评价管理</span>
        </el-menu-item>
        <el-menu-item index="/driver/tag-stats">
          <el-icon><PieChart /></el-icon>
          <span>评价标签统计</span>
        </el-menu-item>
        <el-menu-item index="/driver/star-chart">
          <el-icon><TrendCharts /></el-icon>
          <span>星级评价折线图</span>
        </el-menu-item>
        <el-menu-item index="/driver/complaint">
          <el-icon><Warning /></el-icon>
          <span>乘客投诉管理</span>
        </el-menu-item>
        <el-menu-item index="/driver/appeal">
          <el-icon><ChatDotSquare /></el-icon>
          <span>司机申诉管理</span>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="layout-main-wrapper">
      <div class="layout-header">
        <div class="user-info">
          <span>{{ userInfo?.realName || userInfo?.username || '司机' }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
      <div class="layout-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserInfo, removeToken, removeUserInfo } from '../utils/auth'
import { getDispatchOrders } from '@/api/order'

const route = useRoute()
const router = useRouter()
const userInfo = getUserInfo()

const activeMenu = computed(() => route.path)

const pendingOrderCount = ref(0)
let badgePollingTimer = null

async function fetchPendingCount() {
  try {
    const res = await getDispatchOrders({ pageNum: 1, pageSize: 50 })
    const data = res.data || res
    const list = data.records || data.list || data || []
    const count = Array.isArray(list) ? list.filter(o => Number(o.status) === 1).length : 0
    pendingOrderCount.value = count
  } catch (e) {
    // Silently ignore polling errors
  }
}

function startBadgePolling() {
  fetchPendingCount()
  badgePollingTimer = setInterval(fetchPendingCount, 15000)
}

function stopBadgePolling() {
  if (badgePollingTimer) {
    clearInterval(badgePollingTimer)
    badgePollingTimer = null
  }
}

function handleLogout() {
  stopBadgePolling()
  removeToken()
  removeUserInfo()
  router.push('/login')
}

function handleVisibilityChange() {
  if (document.hidden) {
    stopBadgePolling()
  } else {
    fetchPendingCount()
    startBadgePolling()
  }
}

onMounted(() => {
  startBadgePolling()
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onBeforeUnmount(() => {
  stopBadgePolling()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped>
.sidebar-badge,
.menu-item-badge {
  margin-left: 8px;
  vertical-align: middle;
  display: inline-flex;
  align-items: center;
}
:deep(.sidebar-badge .el-badge__content),
:deep(.menu-item-badge .el-badge__content) {
  border: none;
  position: static;
  transform: none;
  vertical-align: middle;
}
</style>
