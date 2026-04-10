<template>
  <div class="page-container">
    <h2>取消订单</h2>

    <el-card shadow="hover">
      <el-table :data="orders" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="departure" label="出发地" min-width="120" show-overflow-tooltip />
        <el-table-column prop="destination" label="目的地" min-width="120" show-overflow-tooltip />
        <el-table-column prop="cancelTime" label="取消时间" width="170" />
        <el-table-column prop="cancelReason" label="取消原因" min-width="160" show-overflow-tooltip />
        <el-table-column label="取消方" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 5 ? 'warning' : 'danger'" size="small">
              {{ cancelByText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 16px">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadOrders"
          @current-change="loadOrders"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrders } from '@/api/order'

const loading = ref(false)
const orders = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const cancelByText = (status) => {
  const map = { 5: '乘客取消', 6: '司机取消', 7: '系统取消' }
  return map[status] || '已取消'
}

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getOrders({
      statusList: '5,6,7',
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    const data = res.data || res
    orders.value = data.records || data.list || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
