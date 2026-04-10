<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">车辆信息列表</h2>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="司机姓名">
        <template #default="{ row }">
          {{ row.driverName || row.driverId || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="plateNumber" label="车牌号" />
      <el-table-column prop="brand" label="品牌" />
      <el-table-column prop="model" label="型号" />
      <el-table-column prop="color" label="颜色" width="80" />
      <el-table-column label="车辆类型">
        <template #default="{ row }">
          {{ row.vehicleType || row.vehicleTypeName || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="seats" label="座位数" width="80" />
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
import { getVehicleList } from '@/api/vehicle'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

const statusLabel = (status) => {
  const value = Number(status)
  if (value === 1 || status === '正常') return '正常'
  if (value === 0 || status === '停用') return '停用'
  return status || '-'
}

const statusType = (status) => {
  const value = Number(status)
  if (value === 1 || status === '正常') return 'success'
  return 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getVehicleList(query)
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
