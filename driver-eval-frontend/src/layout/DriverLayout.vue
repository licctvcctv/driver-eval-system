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
          </template>
          <el-menu-item index="/driver/order/dispatch">派单管理</el-menu-item>
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserInfo, removeToken, removeUserInfo } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const userInfo = getUserInfo()

const activeMenu = computed(() => route.path)

function handleLogout() {
  removeToken()
  removeUserInfo()
  router.push('/login')
}
</script>
