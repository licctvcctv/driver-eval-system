<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>派单管理</span>
          <el-button type="primary" @click="fetchOrders" :loading="loading">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>

      <el-table :data="orderList" v-loading="loading" stripe border>
        <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />
        <el-table-column label="乘客信息" width="120">
          <template #default="{ row }">
            {{ row.passengerName || row.passengerPhone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="departure" label="出发地" show-overflow-tooltip />
        <el-table-column prop="destination" label="目的地" show-overflow-tooltip />
        <el-table-column prop="distance" label="距离(km)" width="100">
          <template #default="{ row }">
            {{ row.distance ? Number(row.distance).toFixed(1) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="success"
              size="small"
              @click="handleComplete(row)"
            >
              完成
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="danger"
              size="small"
              @click="openCancelDialog(row)"
            >
              取消
            </el-button>
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

    <!-- 取消原因对话框 -->
    <el-dialog v-model="cancelDialogVisible" title="取消订单" width="450px">
      <el-form :model="cancelForm" :rules="cancelRules" ref="cancelFormRef" label-width="80px">
        <el-form-item label="取消原因" prop="reason">
          <el-input
            v-model="cancelForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入取消原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCancelOrder" :loading="cancelLoading">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDispatchOrders, completeOrder, cancelDriverOrder } from '@/api/order'

const orderList = ref([])
const loading = ref(false)
const total = ref(0)
const queryParams = ref({ page: 1, size: 10 })

const cancelDialogVisible = ref(false)
const cancelLoading = ref(false)
const cancelFormRef = ref(null)
const cancelForm = ref({ orderId: null, reason: '' })
const cancelRules = {
  reason: [{ required: true, message: '请输入取消原因', trigger: 'blur' }]
}

function statusText(status) {
  const map = { 0: '待派单', 1: '已派单', 2: '已完成', 3: '已取消' }
  return map[status] || '未知'
}

function statusTagType(status) {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

async function fetchOrders() {
  loading.value = true
  try {
    const res = await getDispatchOrders(queryParams.value)
    const data = res.data || res
    orderList.value = data.records || data.list || data || []
    total.value = data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleComplete(row) {
  try {
    await ElMessageBox.confirm('确认完成该订单？', '提示', { type: 'warning' })
    await completeOrder(row.id)
    ElMessage.success('订单已完成')
    await fetchOrders()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

function openCancelDialog(row) {
  cancelForm.value = { orderId: row.id, reason: '' }
  cancelDialogVisible.value = true
}

async function handleCancelOrder() {
  try {
    await cancelFormRef.value.validate()
  } catch { return }
  cancelLoading.value = true
  try {
    await cancelDriverOrder(cancelForm.value.orderId, cancelForm.value)
    ElMessage.success('订单已取消')
    cancelDialogVisible.value = false
    await fetchOrders()
  } catch (e) {
    ElMessage.error(e.message || '取消失败')
  } finally {
    cancelLoading.value = false
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
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
