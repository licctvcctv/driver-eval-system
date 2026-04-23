<template>
  <div class="layout-container">
    <div class="layout-sidebar">
      <div class="logo">乘客端</div>
      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/passenger/home">
          <el-icon><HomeFilled /></el-icon>
          <span>个人首页</span>
        </el-menu-item>
        <el-menu-item index="/passenger/profile">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
        <el-menu-item index="/passenger/order/create">
          <el-icon><Plus /></el-icon>
          <span>立即下单</span>
        </el-menu-item>
        <el-menu-item index="/passenger/order/list">
          <el-icon><List /></el-icon>
          <span>乘车订单</span>
        </el-menu-item>
        <el-menu-item index="/passenger/order/completed">
          <el-icon><CircleCheckFilled /></el-icon>
          <span>已完成订单</span>
        </el-menu-item>
        <el-menu-item index="/passenger/order/cancelled">
          <el-icon><CircleCloseFilled /></el-icon>
          <span>取消订单</span>
        </el-menu-item>
        <el-menu-item index="/passenger/eval">
          <el-icon><Star /></el-icon>
          <span>评价管理</span>
        </el-menu-item>
        <el-menu-item index="/passenger/complaint">
          <el-icon><Warning /></el-icon>
          <span>投诉管理</span>
        </el-menu-item>
        <el-menu-item index="/passenger/notice">
          <el-icon><Bell /></el-icon>
          <span>通知公告</span>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="layout-main-wrapper">
      <div class="layout-header">
        <div class="user-info">
          <span>{{ userInfo?.realName || userInfo?.username || '乘客' }}</span>
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
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserInfo, removeToken, removeUserInfo } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const userInfo = ref(getUserInfo())

const activeMenu = computed(() => route.path)

function refreshUserInfo(event) {
  userInfo.value = event?.detail ?? getUserInfo()
}

function handleLogout() {
  removeToken()
  removeUserInfo()
  router.push('/login')
}

onMounted(() => {
  window.addEventListener('user-info-updated', refreshUserInfo)
})

onBeforeUnmount(() => {
  window.removeEventListener('user-info-updated', refreshUserInfo)
})
</script>
