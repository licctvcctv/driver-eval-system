<template>
  <div class="page-container">
    <h2>乘车订单</h2>

    <el-card shadow="hover">
      <el-table :data="orders" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="departure" label="出发地" min-width="120" show-overflow-tooltip />
        <el-table-column prop="destination" label="目的地" min-width="120" show-overflow-tooltip />
        <el-table-column label="订单状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="司机信息" min-width="180">
          <template #default="{ row }">
            <template v-if="row.driverName">
              <span class="driver-link" @click="showDriverDetail(row)">{{ row.driverName }}</span>
              <el-tag
                v-if="row.driverLevel"
                :type="levelTagType(row.driverLevel)"
                size="small"
                style="margin-left: 6px"
              >{{ levelText(row.driverLevel) }}</el-tag>
              <div v-if="row.driverScore" style="margin-top: 4px">
                <el-rate :model-value="row.driverScore" disabled show-score score-template="{value}" size="small" />
              </div>
            </template>
            <span v-else style="color: #909399">待分配</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0 || row.status === 1"
              type="danger"
              size="small"
              link
              @click="handleCancel(row)"
            >取消</el-button>
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

    <!-- Driver Detail Dialog -->
    <el-dialog v-model="driverDialogVisible" title="司机详情" width="480px" align-center>
      <div v-if="selectedDriver" class="driver-detail">
        <div class="driver-detail-header">
          <el-avatar :size="64" icon="User" style="background: #4A90D9" />
          <div class="driver-detail-meta">
            <div class="driver-detail-name">{{ selectedDriver.driverName }}</div>
            <el-tag
              :type="levelTagType(selectedDriver.driverLevel)"
              size="large"
              style="margin-top: 4px"
            >{{ levelText(selectedDriver.driverLevel) }}</el-tag>
          </div>
        </div>

        <div class="driver-detail-score">
          <span class="score-label">综合评分</span>
          <el-rate
            :model-value="selectedDriver.driverScore || 0"
            disabled
            show-score
            score-template="{value}"
            size="large"
          />
        </div>

        <el-descriptions :column="1" border size="default" style="margin-top: 16px">
          <el-descriptions-item label="车牌号">
            <span style="font-weight: 700; letter-spacing: 1px">
              {{ selectedDriver.plateNumber || selectedDriver.vehiclePlateNumber || '未知' }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="车辆品牌">
            {{ selectedDriver.vehicleBrand || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="车辆型号">
            {{ selectedDriver.vehicleModel || selectedDriver.vehicleTypeName || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="总完成订单">
            <span style="font-weight: 600; color: #4A90D9">
              {{ selectedDriver.totalOrders ?? selectedDriver.orderCount ?? '—' }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="投诉次数">
            <span :style="{ fontWeight: 600, color: (selectedDriver.complaintCount || 0) > 0 ? '#F56C6C' : '#52c41a' }">
              {{ selectedDriver.complaintCount ?? selectedDriver.complaints ?? '—' }}
            </span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button type="primary" @click="driverDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Cancel Dialog -->
    <el-dialog v-model="cancelDialogVisible" title="取消订单" width="400px">
      <el-form>
        <el-form-item label="取消原因">
          <el-input
            v-model="cancelReason"
            type="textarea"
            :rows="3"
            placeholder="请输入取消原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">返回</el-button>
        <el-button type="danger" :loading="cancelLoading" @click="confirmCancel">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrders, cancelOrder } from '@/api/order'

const loading = ref(false)
const orders = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const cancelDialogVisible = ref(false)
const cancelReason = ref('')
const cancelLoading = ref(false)
const currentCancelOrder = ref(null)

const driverDialogVisible = ref(false)
const selectedDriver = ref(null)

const statusText = (status) => {
  const map = { 0: '待接单', 1: '已接单', 2: '进行中', 3: '待确认' }
  return map[status] || '未知'
}

const statusTagType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: '', 3: 'success' }
  return map[status] || 'info'
}

const levelTagType = (level) => {
  if (level === 3 || level === '金牌') return 'warning'
  if (level === 2 || level === '银牌') return ''
  return 'info'
}

const levelText = (level) => {
  if (level === '金牌') return '金牌司机'
  if (level === '银牌') return '银牌司机'
  if (level === '普通') return '普通司机'
  if (level === 3) return '金牌司机'
  if (level === 2) return '银牌司机'
  if (level === 1) return '普通司机'
  return level
}

const showDriverDetail = (row) => {
  selectedDriver.value = row
  driverDialogVisible.value = true
}

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getOrders({
      statusList: '0,1,2,3',
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

const handleCancel = (row) => {
  currentCancelOrder.value = row
  cancelReason.value = ''
  cancelDialogVisible.value = true
}

const confirmCancel = async () => {
  if (!cancelReason.value.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }
  cancelLoading.value = true
  try {
    await cancelOrder(currentCancelOrder.value.id, { reason: cancelReason.value })
    ElMessage.success('订单已取消')
    cancelDialogVisible.value = false
    loadOrders()
  } catch (e) {
    ElMessage.error(e.message || '取消失败')
  } finally {
    cancelLoading.value = false
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.driver-link {
  color: #4A90D9;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.2s;
}
.driver-link:hover {
  color: #357abd;
  text-decoration: underline;
}

.driver-detail-header {
  display: flex;
  align-items: center;
  gap: 18px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f2f5;
}
.driver-detail-name {
  font-size: 20px;
  font-weight: 700;
  color: #1d1e2c;
}
.driver-detail-score {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  padding: 12px 16px;
  background: #f8f9fc;
  border-radius: 8px;
}
.score-label {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}
</style>
