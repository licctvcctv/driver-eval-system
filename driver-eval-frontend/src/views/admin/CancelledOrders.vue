<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">取消订单管理</h2>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="passengerName" label="乘客" />
      <el-table-column prop="driverName" label="司机" />
      <el-table-column prop="departure" label="出发地" show-overflow-tooltip />
      <el-table-column prop="destination" label="目的地" show-overflow-tooltip />
      <el-table-column prop="cancelReason" label="取消原因" show-overflow-tooltip />
      <el-table-column prop="cancelTime" label="取消时间" width="180" />
      <el-table-column prop="status" label="取消方" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === '乘客取消' ? 'warning' : 'danger'">{{ row.status }}</el-tag>
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
import { getAllOrders } from '@/api/order'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ status: '5,6', pageNum: 1, pageSize: 10 })

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
