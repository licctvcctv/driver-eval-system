<template>
  <div class="layout-container">
    <div class="layout-sidebar">
      <div class="logo">管理后台</div>
      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>后台首页</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><UserFilled /></el-icon>
          <span>系统用户</span>
        </el-menu-item>
        <el-sub-menu index="admin-vehicle">
          <template #title>
            <el-icon><Van /></el-icon>
            <span>车辆管理</span>
          </template>
          <el-menu-item index="/admin/vehicle/list">车辆列表</el-menu-item>
          <el-menu-item index="/admin/vehicle/types">车辆类型</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="admin-operation">
          <template #title>
            <el-icon><Operation /></el-icon>
            <span>运营管理</span>
          </template>
          <el-menu-item index="/admin/order/dispatch">派单管理</el-menu-item>
          <el-menu-item index="/admin/order/completed">完成订单</el-menu-item>
          <el-menu-item index="/admin/order/cancelled">取消订单</el-menu-item>
          <el-menu-item index="/admin/score">司机评分管理</el-menu-item>
          <el-menu-item index="/admin/complaint">乘客投诉管理</el-menu-item>
          <el-menu-item index="/admin/punish">投诉处罚管理</el-menu-item>
          <el-menu-item index="/admin/eval">乘客评价管理</el-menu-item>
          <el-menu-item index="/admin/tag-stats">评价标签统计</el-menu-item>
          <el-menu-item index="/admin/appeal">司机申诉管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/tags">
          <el-icon><CollectionTag /></el-icon>
          <span>评价标签管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/sensitive-words">
          <el-icon><EditPen /></el-icon>
          <span>敏感词管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/announcement">
          <el-icon><Bell /></el-icon>
          <span>系统公告管理</span>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="layout-main-wrapper">
      <div class="layout-header">
        <div class="user-info">
          <span>{{ userInfo?.realName || userInfo?.username || '管理员' }}</span>
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
