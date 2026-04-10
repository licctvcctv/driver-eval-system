<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <span>取消订单管理</span>
      </template>

      <el-table :data="orderList" v-loading="loading" stripe border>
        <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />
        <el-table-column prop="departure" label="出发地" show-overflow-tooltip />
        <el-table-column prop="destination" label="目的地" show-overflow-tooltip />
        <el-table-column prop="cancelTime" label="取消时间" width="170" />
        <el-table-column prop="cancelReason" label="取消原因" show-overflow-tooltip />
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchOrders"
          @current-change="fetchOrders"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDriverCancelledOrders } from '@/api/order'

const orderList = ref([])
const loading = ref(false)
const total = ref(0)
const queryParams = ref({ page: 1, size: 10 })

async function fetchOrders() {
  loading.value = true
  try {
    const res = await getDriverCancelledOrders(queryParams.value)
    const data = res.data || res
    orderList.value = data.records || data.list || data || []
    total.value = data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrders()
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
