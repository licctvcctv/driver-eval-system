<template>
  <div class="page-container">
    <h2>乘车订单</h2>

    <el-card shadow="hover">
      <template #header>
        <div style="display: flex; justify-content: flex-end; align-items: center;">
          <span v-if="lastRefreshTime" style="font-size: 12px; color: #909399; margin-right: 8px">最后刷新: {{ lastRefreshTime }}</span>
        </div>
      </template>

      <el-table :data="orders" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="departure" label="出发地" min-width="120" show-overflow-tooltip />
        <el-table-column prop="destination" label="目的地" min-width="120" show-overflow-tooltip />
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
              v-if="Number(row.status) < 4"
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
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
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

// Polling state
const lastRefreshTime = ref('')
let pollingTimer = null
let previousStatusMap = new Map()

function formatTime(date) {
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${h}:${m}:${s}`
}

const statusText = (status) => {
  const s = Number(status)
  // Note: status 2 ("已接单") is reserved but currently unused in the demo flow.
  // Accept goes directly to IN_PROGRESS (3). Kept here for future two-step accept flow.
  const map = { 0: '待派单', 1: '已派单', 2: '已接单(预留)', 3: '进行中', 4: '已完成', 5: '乘客取消', 6: '司机取消' }
  return map[s] || status || '未知'
}

const statusTagType = (status) => {
  const s = Number(status)
  const map = { 0: 'info', 1: 'warning', 2: '', 3: 'primary', 4: 'success', 5: 'danger', 6: 'danger' }
  return map[s] || 'info'
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

function getStatusChangeMessage(oldStatus, newStatus) {
  const o = Number(oldStatus)
  const n = Number(newStatus)
  if (o === 0 && n === 1) return '订单已派单，正在等待司机接单'
  if ((o === 0 || o === 1) && n === 2) return '司机已接单'
  if ((o === 1 || o === 2) && n === 3) return '司机已接单，行程开始'
  if (n === 3 && o !== 3) return '行程进行中'
  if (n === 4) return '行程已完成'
  if (n === 6) return '司机已取消订单'
  return null
}

function getStatusChangeType(newStatus) {
  const n = Number(newStatus)
  if (n === 3 || n === 2) return 'success'
  if (n === 4) return 'success'
  if (n === 6) return 'warning'
  return 'info'
}

const loadOrders = async (isPolling = false) => {
  if (!isPolling) {
    loading.value = true
  }
  try {
    // Poll all statuses to catch terminal transitions; display only active ones
    const trackedIds = isPolling ? [...previousStatusMap.keys()] : []
    const res = await getOrders({
      statusList: '0,1,2,3',
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    const data = res.data || res
    const newList = data.records || data.list || []

    // Detect status changes on polling
    if (isPolling && previousStatusMap.size > 0) {
      // Check orders still in the active list for status changes
      for (const order of newList) {
        const prevStatus = previousStatusMap.get(order.id)
        if (prevStatus !== undefined && Number(prevStatus) !== Number(order.status)) {
          const msg = getStatusChangeMessage(prevStatus, order.status)
          if (msg) {
            ElNotification({
              title: '订单状态更新',
              message: `订单 ${order.orderNo || ''}: ${msg}`,
              type: getStatusChangeType(order.status),
              duration: 5000
            })
          }
        }
      }
      // Detect orders that disappeared from active list (went to terminal state 4/5/6)
      const currentIds = new Set(newList.map(o => o.id))
      for (const trackedId of trackedIds) {
        if (!currentIds.has(trackedId)) {
          // Order disappeared — fetch its current state to notify
          try {
            const detailRes = await getOrders({ statusList: '0,1,2,3,4,5,6', pageNum: 1, pageSize: 50 })
            const detailData = detailRes.data || detailRes
            const detailList = detailData.records || detailData.list || []
            const gone = detailList.find(o => o.id === trackedId)
            if (gone) {
              const prevStatus = previousStatusMap.get(trackedId)
              const msg = getStatusChangeMessage(prevStatus, gone.status)
              if (msg) {
                ElNotification({
                  title: '订单状态更新',
                  message: `订单 ${gone.orderNo || ''}: ${msg}`,
                  type: getStatusChangeType(gone.status),
                  duration: 6000
                })
              }
            }
          } catch (_) { /* ignore */ }
          // Only need one extra fetch for all disappeared orders
          break
        }
      }
    }

    // Update status map from active orders only
    previousStatusMap = new Map()
    for (const order of newList) {
      previousStatusMap.set(order.id, order.status)
    }

    orders.value = newList
    total.value = data.total || 0
    lastRefreshTime.value = formatTime(new Date())
  } catch (e) {
    if (!isPolling) {
      ElMessage.error('加载订单失败')
    }
  } finally {
    if (!isPolling) {
      loading.value = false
    }
  }
}

function startPolling() {
  stopPolling()
  pollingTimer = setInterval(() => {
    loadOrders(true)
  }, 10000)
}

function stopPolling() {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
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

function handleVisibilityChange() {
  if (document.hidden) {
    stopPolling()
  } else {
    loadOrders(true)
    startPolling()
  }
}

onMounted(() => {
  loadOrders()
  startPolling()
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onBeforeUnmount(() => {
  stopPolling()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
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
