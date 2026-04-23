<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="card-header-left">
            <span>派单管理</span>
            <el-tag v-if="newOrderCount > 0" type="danger" effect="dark" size="small" style="margin-left: 8px">
              {{ newOrderCount }} 条新订单
            </el-tag>
          </div>
          <div class="card-header-right">
            <span v-if="lastRefreshTime" class="last-refresh-text">最后刷新: {{ lastRefreshTime }}</span>
            <el-switch
              v-model="autoRefreshEnabled"
              active-text="自动刷新"
              inactive-text=""
              style="margin-right: 12px"
              @change="toggleAutoRefresh"
            />
            <el-button type="primary" @click="manualRefresh" :loading="loading">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="orderList" v-loading="loading" stripe border :row-class-name="tableRowClassName">
        <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />
        <el-table-column label="乘客信息" width="120">
          <template #default="{ row }">
            {{ row.passengerName || row.passengerPhone || '-' }}
          </template>
        </el-table-column>
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
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="Number(row.status) === 1"
              type="primary"
              size="small"
              @click="handleAccept(row)"
            >
              接单
            </el-button>
            <el-button
              v-if="Number(row.status) === 3"
              type="success"
              size="small"
              @click="handleComplete(row)"
            >
              完成
            </el-button>
            <el-button
              v-if="Number(row.status) === 1 || Number(row.status) === 3"
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
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
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
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { getDispatchOrders, acceptOrder, completeOrder, cancelDriverOrder } from '@/api/order'

const orderList = ref([])
const loading = ref(false)
const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10 })

const cancelDialogVisible = ref(false)
const cancelLoading = ref(false)
const cancelFormRef = ref(null)
const cancelForm = ref({ orderId: null, reason: '' })
const cancelRules = {
  reason: [{ required: true, message: '请输入取消原因', trigger: 'blur' }]
}

// Auto-refresh state
const autoRefreshEnabled = ref(true)
const lastRefreshTime = ref('')
const newOrderCount = ref(0)
const newOrderIds = ref(new Set())
let pollingTimer = null
let previousOrderIds = new Set()
let sharedAudioCtx = null
let highlightTimer = null

function formatTime(date) {
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${h}:${m}:${s}`
}

function statusText(status) {
  // Note: status 2 ("已接单") is reserved but currently unused in the demo flow.
  // Accept goes directly to IN_PROGRESS (3). Kept here for future two-step accept flow.
  const map = { 0: '待派单', 1: '已派单', 2: '已接单(预留)', 3: '进行中', 4: '已完成', 5: '乘客取消', 6: '司机取消' }
  return map[Number(status)] || '未知'
}

function statusTagType(status) {
  const map = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'primary', 4: 'success', 5: 'danger', 6: 'danger' }
  return map[Number(status)] || 'info'
}

function tableRowClassName({ row }) {
  if (newOrderIds.value.has(row.id)) {
    return 'new-order-row'
  }
  return ''
}

function getAudioContext() {
  if (!sharedAudioCtx) {
    try {
      sharedAudioCtx = new (window.AudioContext || window.webkitAudioContext)()
    } catch (e) {
      // Audio not supported
    }
  }
  return sharedAudioCtx
}

function playNotificationSound() {
  try {
    const audioCtx = getAudioContext()
    if (!audioCtx) return
    // Resume if suspended (browser autoplay policy)
    if (audioCtx.state === 'suspended') {
      audioCtx.resume()
    }
    const oscillator = audioCtx.createOscillator()
    const gainNode = audioCtx.createGain()
    oscillator.connect(gainNode)
    gainNode.connect(audioCtx.destination)
    oscillator.frequency.value = 800
    oscillator.type = 'sine'
    gainNode.gain.value = 0.3
    oscillator.start()
    oscillator.stop(audioCtx.currentTime + 0.2)
  } catch (e) {
    // Audio not supported, silently ignore
  }
}

async function fetchOrders(isPolling = false) {
  if (!isPolling) {
    loading.value = true
  }
  try {
    const res = await getDispatchOrders(queryParams.value)
    const data = res.data || res
    const newList = data.records || data.list || data || []

    // Detect new orders
    if (isPolling && previousOrderIds.size > 0) {
      const currentIds = new Set(newList.map(o => o.id))
      const freshIds = new Set()
      let freshCount = 0
      for (const id of currentIds) {
        if (!previousOrderIds.has(id)) {
          freshCount++
          freshIds.add(id)
        }
      }
      if (freshCount > 0) {
        newOrderCount.value = freshCount
        newOrderIds.value = freshIds
        playNotificationSound()
        ElNotification({
          title: '新订单提醒',
          message: `您有 ${freshCount} 条新订单，请及时处理！`,
          type: 'warning',
          duration: 5000
        })
        // Clear highlight after 10 seconds
        if (highlightTimer) clearTimeout(highlightTimer)
        highlightTimer = setTimeout(() => {
          newOrderIds.value = new Set()
          newOrderCount.value = 0
          highlightTimer = null
        }, 10000)
      }
    }

    // Update previous order IDs for next comparison
    previousOrderIds = new Set(newList.map(o => o.id))

    orderList.value = newList
    total.value = data.total || 0
    lastRefreshTime.value = formatTime(new Date())
  } catch (e) {
    console.error(e)
  } finally {
    if (!isPolling) {
      loading.value = false
    }
  }
}

function manualRefresh() {
  newOrderCount.value = 0
  newOrderIds.value = new Set()
  fetchOrders(false)
}

function startPolling() {
  stopPolling()
  pollingTimer = setInterval(() => {
    fetchOrders(true)
  }, 5000)
}

function stopPolling() {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

function toggleAutoRefresh(val) {
  if (val) {
    startPolling()
  } else {
    stopPolling()
  }
}

async function handleAccept(row) {
  try {
    await ElMessageBox.confirm('确认接单？', '提示', { type: 'warning' })
    await acceptOrder(row.id)
    ElMessage.success('接单成功')
    await fetchOrders()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
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

function handleVisibilityChange() {
  if (document.hidden) {
    stopPolling()
  } else if (autoRefreshEnabled.value) {
    fetchOrders(true)
    startPolling()
  }
}

onMounted(() => {
  fetchOrders()
  startPolling()
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onBeforeUnmount(() => {
  stopPolling()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  if (highlightTimer) {
    clearTimeout(highlightTimer)
    highlightTimer = null
  }
  if (sharedAudioCtx) {
    sharedAudioCtx.close().catch(() => {})
    sharedAudioCtx = null
  }
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
.card-header-left {
  display: flex;
  align-items: center;
}
.card-header-right {
  display: flex;
  align-items: center;
}
.last-refresh-text {
  font-size: 12px;
  color: #909399;
  margin-right: 12px;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
:deep(.new-order-row) {
  animation: highlight-fade 2s ease-in-out infinite;
}
@keyframes highlight-fade {
  0%, 100% { background-color: transparent; }
  50% { background-color: #fdf6ec; }
}
</style>
