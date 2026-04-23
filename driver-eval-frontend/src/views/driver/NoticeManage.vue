<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <span>通知公告</span>
      </template>

      <el-row :gutter="16" style="margin-bottom: 16px">
        <el-col :span="6">
          <el-select v-model="queryType" placeholder="类型筛选" clearable @change="loadData">
            <el-option label="全部" value="" />
            <el-option label="公告" :value="1" />
            <el-option label="资讯" :value="2" />
            <el-option label="通知" :value="3" />
          </el-select>
        </el-col>
      </el-row>

      <el-table :data="noticeList" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发布时间" width="180" />
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAnnouncements } from '@/api/common'

const noticeList = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const queryType = ref('')

function typeText(type) {
  const map = { 1: '公告', 2: '资讯', 3: '通知' }
  return map[type] || '未知'
}

function typeTagType(type) {
  const map = { 1: 'danger', 2: '', 3: 'warning' }
  return map[type] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (queryType.value !== '') {
      params.type = queryType.value
    }
    const res = await getAnnouncements(params)
    const data = res.data || res
    if (Array.isArray(data)) {
      noticeList.value = data
      total.value = data.length
    } else {
      noticeList.value = data.records || data.list || data || []
      total.value = data.total || noticeList.value.length
    }
  } catch (e) {
    console.error('加载通知失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
