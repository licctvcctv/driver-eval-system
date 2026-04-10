<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">派单管理</h2>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="passengerName" label="乘客" />
      <el-table-column prop="driverName" label="司机" />
      <el-table-column prop="departure" label="出发地" show-overflow-tooltip />
      <el-table-column prop="destination" label="目的地" show-overflow-tooltip />
      <el-table-column prop="dispatchScore" label="派单评分" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="orderStatusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
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
import { getAllOrders } from '@/api/order'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

const orderStatusType = (s) => {
  const map = { '待接单': 'warning', '已接单': '', '进行中': 'primary', '已完成': 'success', '已取消': 'info' }
  return map[s] || ''
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAllOrders({ ...query })
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
