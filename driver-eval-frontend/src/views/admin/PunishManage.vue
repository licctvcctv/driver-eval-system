<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">投诉处罚管理</h2>

    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="6">
        <el-select v-model="query.status" placeholder="状态筛选" clearable @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="生效中" :value="1" />
          <el-option label="已过期" :value="2" />
        </el-select>
      </el-col>
      <el-col :span="4">
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-col>
    </el-row>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column label="司机姓名">
        <template #default="{ row }">
          {{ row.driverName || row.driverId || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="punishReason" label="处罚原因" show-overflow-tooltip />
      <el-table-column prop="punishDays" label="处罚天数" width="100" />
      <el-table-column prop="punishStart" label="开始时间" width="180" />
      <el-table-column prop="punishEnd" label="结束时间" width="180" />
      <el-table-column prop="weekComplaints" label="周投诉数" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 16px; justify-content: flex-end"
      background
      layout="total, prev, pager, next, sizes"
      :total="total"
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      @current-change="loadData"
      @size-change="loadData"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getPunishList } from '@/api/punish'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ status: '', pageNum: 1, pageSize: 10 })

const statusLabel = (status) => {
  const value = Number(status)
  if (value === 1 || status === '生效中') return '生效中'
  if (value === 2 || status === '已过期') return '已过期'
  return status || '-'
}

const statusType = (status) => {
  const value = Number(status)
  if (value === 1 || status === '生效中') return 'danger'
  return 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPunishList(query)
    const d = res.data || res
    tableData.value = d.records || d.list || d
    total.value = d.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
