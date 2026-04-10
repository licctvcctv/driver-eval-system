<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <span>完成订单管理</span>
      </template>

      <el-table :data="orderList" v-loading="loading" stripe border>
        <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />
        <el-table-column prop="departure" label="出发地" show-overflow-tooltip />
        <el-table-column prop="destination" label="目的地" show-overflow-tooltip />
        <el-table-column label="距离" width="90" align="center">
          <template #default="{ row }">
            {{ row.distance ? Number(row.distance).toFixed(1) + ' km' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="费用" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.price" style="color: #F56C6C; font-weight: 600">¥{{ Number(row.price).toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="completeTime" label="完成时间" width="170" />
          <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!isDriverEvaluated(row)"
              type="primary"
              size="small"
              @click="openEvalDialog(row)"
            >
              评价乘客
            </el-button>
            <el-tag v-else type="success" size="small">已评价</el-tag>
          </template>
        </el-table-column>
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

    <!-- 评价乘客对话框 -->
    <el-dialog v-model="evalDialogVisible" title="评价乘客" width="500px">
      <el-form :model="evalForm" label-width="80px">
        <el-form-item label="订单号">
          <span>{{ evalForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="evalForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入对乘客的评价"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evalDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEvaluate" :loading="evalLoading">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDriverCompletedOrders, evaluatePassenger } from '@/api/order'

const orderList = ref([])
const loading = ref(false)
const total = ref(0)
const queryParams = ref({ page: 1, size: 10 })

const evalDialogVisible = ref(false)
const evalLoading = ref(false)
const evalForm = ref({ orderId: null, orderNo: '', content: '' })

const isDriverEvaluated = (row) => {
  return Boolean(
    row.driverEvaluated ||
    row.isDriverEvaluated ||
    row.driverEvaluationId ||
    row.driverEvaluation
  )
}

async function fetchOrders() {
  loading.value = true
  try {
    const res = await getDriverCompletedOrders(queryParams.value)
    const data = res.data || res
    orderList.value = data.records || data.list || data || []
    total.value = data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openEvalDialog(row) {
  evalForm.value = { orderId: row.id, orderNo: row.orderNo, content: '' }
  evalDialogVisible.value = true
}

async function handleEvaluate() {
  if (!evalForm.value.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  evalLoading.value = true
  try {
    await evaluatePassenger({ orderId: evalForm.value.orderId, content: evalForm.value.content })
    ElMessage.success('评价成功')
    evalDialogVisible.value = false
    await fetchOrders()
  } catch (e) {
    ElMessage.error(e.message || '评价失败')
  } finally {
    evalLoading.value = false
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
