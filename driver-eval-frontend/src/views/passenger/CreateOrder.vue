<template>
  <div class="page-container">
    <h2>立即叫车</h2>

    <el-row :gutter="24">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px; font-weight: 700">
              <el-icon :size="18" color="#4A90D9"><Promotion /></el-icon>
              <span>填写出行信息</span>
            </div>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" size="large">
            <el-form-item label="出发地" prop="departure">
              <el-autocomplete
                v-model="form.departure"
                :fetch-suggestions="queryDeparture"
                placeholder="输入出发地，如：北京西站"
                value-key="name"
                :trigger-on-focus="true"
                :debounce="0"
                style="width: 100%"
                @select="handleSelectDeparture"
                @input="handleDepartureInput"
              >
                <template #prefix>
                  <span class="dot dot--green"></span>
                </template>
                <template #default="{ item }">
                  <div class="location-item">
                    <el-icon><Location /></el-icon>
                    <div>
                      <div>{{ item.name }}</div>
                      <div v-if="item.address" class="location-address">{{ item.address }}</div>
                    </div>
                  </div>
                </template>
              </el-autocomplete>
            </el-form-item>

            <el-form-item label="目的地" prop="destination">
              <el-autocomplete
                v-model="form.destination"
                :fetch-suggestions="queryDestination"
                placeholder="输入目的地，如：首都机场T3"
                value-key="name"
                :trigger-on-focus="true"
                style="width: 100%"
                @select="handleSelectDestination"
                @input="handleDestinationInput"
              >
                <template #prefix>
                  <span class="dot dot--red"></span>
                </template>
                <template #default="{ item }">
                  <div class="location-item">
                    <el-icon><Location /></el-icon>
                    <div>
                      <div>{{ item.name }}</div>
                      <div v-if="item.address" class="location-address">{{ item.address }}</div>
                    </div>
                  </div>
                </template>
              </el-autocomplete>
            </el-form-item>

            <!-- Hidden coordinate fields for validation -->
            <el-form-item prop="departureLat" style="display:none">
              <el-input v-model="form.departureLat" />
            </el-form-item>
            <el-form-item prop="departureLng" style="display:none">
              <el-input v-model="form.departureLng" />
            </el-form-item>
            <el-form-item prop="destLat" style="display:none">
              <el-input v-model="form.destLat" />
            </el-form-item>
            <el-form-item prop="destLng" style="display:none">
              <el-input v-model="form.destLng" />
            </el-form-item>

            <!-- Route Summary -->
            <div v-if="form.departureLat && form.destLat" class="route-summary">
              <div class="route-point">
                <span class="dot dot--green"></span>
                <span>{{ form.departure }}</span>
              </div>
              <div class="route-line">
                <div class="route-dash"></div>
                <div class="route-info">
                  <span class="route-distance">约 {{ estimatedDistance }} km</span>
                  <span v-if="estimatedPrice > 0" class="route-price">预估 ¥{{ estimatedPrice }}</span>
                </div>
                <div class="route-dash"></div>
              </div>
              <div class="route-point">
                <span class="dot dot--red"></span>
                <span>{{ form.destination }}</span>
              </div>
            </div>

            <el-form-item style="margin-top: 24px">
              <el-button
                type="primary"
                :loading="submitting"
                @click="handleSubmit"
                size="large"
                style="width: 160px; height: 44px; font-size: 16px"
              >
                <el-icon style="margin-right: 6px"><Promotion /></el-icon>
                立即叫车
              </el-button>
              <el-button @click="resetForm" size="large">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="10">
        <!-- Dispatch Result -->
        <el-card v-if="dispatchResult" shadow="hover" :class="dispatchResult.driverAssigned ? 'result-success' : 'result-warning'">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px; font-weight: 700">
              <el-icon :size="18" :color="dispatchResult.driverAssigned ? '#52c41a' : '#E6A23C'">
                <component :is="dispatchResult.driverAssigned ? 'SuccessFilled' : 'WarningFilled'" />
              </el-icon>
              <span>{{ dispatchResult.driverAssigned ? '派单成功' : '暂无司机' }}</span>
            </div>
          </template>

          <template v-if="dispatchResult.driverAssigned">
            <div class="driver-card">
              <el-avatar :size="48" icon="User" style="background: #4A90D9" />
              <div class="driver-info">
                <div class="driver-name">{{ dispatchResult.driverName }}</div>
                <el-rate :model-value="dispatchResult.driverScore" disabled show-score score-template="{value}" size="small" />
              </div>
              <el-tag :type="getLevelTagType(dispatchResult.driverLevel)" size="large">
                {{ levelText(dispatchResult.driverLevel) }}
              </el-tag>
            </div>
            <el-descriptions :column="2" border style="margin-top: 16px" size="small">
              <el-descriptions-item label="距离">
                <span style="font-weight: 700; color: #4A90D9">{{ dispatchResult.distance || estimatedDistance }} km</span>
              </el-descriptions-item>
              <el-descriptions-item label="预估费用">
                <span style="font-weight: 700; color: #F56C6C; font-size: 15px">¥{{ dispatchResult.price || estimatedPrice }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="车辆">
                {{ dispatchResult.vehicleInfo || '未知车辆' }}
              </el-descriptions-item>
              <el-descriptions-item label="车牌号">
                <span style="font-weight: 700; color: #1d1e2c; letter-spacing: 1px">
                  {{ dispatchResult.plateNumber || '未知' }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="订单号" :span="2">
                <span style="font-family: monospace; font-size: 12px">{{ dispatchResult.orderNo }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </template>

          <template v-else>
            <div style="text-align: center; padding: 20px 0">
              <p style="color: #909399; margin-bottom: 16px">当前没有空闲司机</p>

              <!-- Auto-retry countdown -->
              <div v-if="retryCountdown > 0 && retryCount < maxRetries" style="margin-bottom: 16px">
                <el-progress
                  :percentage="(retryCountdown / retryInterval) * 100"
                  :format="() => retryCountdown + 's'"
                  :stroke-width="10"
                  style="max-width: 300px; margin: 0 auto 12px"
                />
                <p style="color: #606266; font-size: 13px">
                  系统将在 <span style="color: #E6A23C; font-weight: 700">{{ retryCountdown }}秒</span> 后自动重新匹配
                  <span style="color: #909399; margin-left: 4px">(第 {{ retryCount + 1 }}/{{ maxRetries }} 次)</span>
                </p>
              </div>

              <!-- Max retries reached -->
              <div v-if="retryCount >= maxRetries" style="margin-bottom: 16px">
                <p style="color: #F56C6C; font-size: 13px; font-weight: 500">
                  已自动重试 {{ maxRetries }} 次仍无可用司机，建议稍后再试或取消订单
                </p>
              </div>

              <div style="display: flex; justify-content: center; gap: 12px">
                <el-button type="primary" @click="manualRetry" :loading="submitting">
                  <el-icon style="margin-right: 4px"><Refresh /></el-icon>
                  {{ retryCount > 0 ? '再次重试' : '重新叫车' }}
                </el-button>
                <el-button v-if="retryCountdown > 0" @click="cancelAutoRetry">
                  取消自动重试
                </el-button>
                <el-button v-if="retryCount >= maxRetries" type="info" @click="resetForm">
                  取消叫车
                </el-button>
              </div>
            </div>
          </template>
        </el-card>

        <!-- Tips card when no result -->
        <el-card v-else shadow="hover" class="tips-card">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px; font-weight: 700; color: #909399">
              <el-icon :size="18"><InfoFilled /></el-icon>
              <span>出行提示</span>
            </div>
          </template>
          <ul class="tips-list">
            <li>输入地点名称时会自动匹配已知地点</li>
            <li>选择地点后坐标将自动填入</li>
            <li>系统将为您匹配评分最优的司机</li>
            <li>下单后请耐心等待司机接单</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { createOrder, getOrders } from '@/api/order'
import { Promotion, Location, Refresh, InfoFilled, SuccessFilled, WarningFilled } from '@element-plus/icons-vue'

const formRef = ref(null)
const submitting = ref(false)
const dispatchResult = ref(null)

// Track the created order to avoid duplicates on retry
const createdOrderId = ref(null)
const createdOrderNo = ref(null)

// Auto-retry state
const retryCount = ref(0)
const retryCountdown = ref(0)
const maxRetries = 3
const retryInterval = 30
let countdownTimer = null

// 高德地图 Web 服务 Key（用于 POI 搜索）
const AMAP_WEB_KEY = 'bcafb11917e1c0c24268fa0e3c69aa5b'

// 热门地点（输入为空时展示，同时作为高德 API 不可用时的兜底）
const hotLocations = [
  { name: '北京西站', lat: 39.8959, lng: 116.3228, address: '北京市丰台区' },
  { name: '北京南站', lat: 39.8652, lng: 116.3789, address: '北京市丰台区' },
  { name: '天安门', lat: 39.9087, lng: 116.3975, address: '北京市东城区' },
  { name: '中关村', lat: 39.9818, lng: 116.3117, address: '北京市海淀区' },
  { name: '望京SOHO', lat: 39.9977, lng: 116.4802, address: '北京市朝阳区' },
  { name: '三里屯', lat: 39.9317, lng: 116.4551, address: '北京市朝阳区' },
  { name: '国贸CBD', lat: 39.9087, lng: 116.4605, address: '北京市朝阳区' },
  { name: '首都机场T3', lat: 40.0799, lng: 116.6031, address: '北京市顺义区' },
]

const form = reactive({
  departure: '',
  departureLng: '',
  departureLat: '',
  destination: '',
  destLng: '',
  destLat: ''
})

const rules = {
  departure: [{ required: true, message: '请选择出发地', trigger: 'change' }],
  departureLng: [{ required: true, message: '请选择出发地', trigger: 'change' }],
  departureLat: [{ required: true, message: '请选择出发地', trigger: 'change' }],
  destination: [{ required: true, message: '请选择目的地', trigger: 'change' }],
  destLng: [{ required: true, message: '请选择目的地', trigger: 'change' }],
  destLat: [{ required: true, message: '请选择目的地', trigger: 'change' }]
}

// 调用高德 POI 搜索 API（每个输入框独立防抖）
const searchTimers = { departure: null, destination: null }
const createSearchPoi = (field) => (keyword, cb) => {
  if (!keyword || keyword.trim().length === 0) {
    cb(hotLocations)
    return
  }
  clearTimeout(searchTimers[field])
  searchTimers[field] = setTimeout(async () => {
    try {
      const res = await fetch(
        `https://restapi.amap.com/v3/place/text?key=${AMAP_WEB_KEY}&keywords=${encodeURIComponent(keyword)}&city=北京&offset=10&page=1&extensions=all&output=JSON`
      )
      const data = await res.json()
      if (data.status === '1' && Array.isArray(data.pois) && data.pois.length > 0) {
        const results = data.pois.map(poi => {
          const [lngStr, latStr] = (poi.location || '').split(',')
          return {
            name: poi.name,
            address: `${poi.cityname || ''}${poi.adname || ''}${poi.address || ''}`,
            lat: parseFloat(latStr) || 0,
            lng: parseFloat(lngStr) || 0
          }
        })
        cb(results)
      } else {
        // API 无结果时用本地热门过滤
        const fallback = hotLocations.filter(loc => loc.name.includes(keyword))
        cb(fallback.length > 0 ? fallback : [])
      }
    } catch (e) {
      // 网络异常时用本地热门兜底
      const fallback = hotLocations.filter(loc => loc.name.includes(keyword))
      cb(fallback.length > 0 ? fallback : hotLocations)
    }
  }, 300)
}

const queryDeparture = createSearchPoi('departure')
const queryDestination = createSearchPoi('destination')

const handleSelectDeparture = (loc) => {
  form.departure = loc.name
  form.departureLat = String(loc.lat)
  form.departureLng = String(loc.lng)
}
const handleDepartureInput = () => {
  form.departureLat = ''
  form.departureLng = ''
}

const handleSelectDestination = (loc) => {
  form.destination = loc.name
  form.destLat = String(loc.lat)
  form.destLng = String(loc.lng)
}
const handleDestinationInput = () => {
  form.destLat = ''
  form.destLng = ''
}

const estimatedDistance = computed(() => {
  if (!form.departureLat || !form.destLat) return 0
  const R = 6371
  const dLat = (parseFloat(form.destLat) - parseFloat(form.departureLat)) * Math.PI / 180
  const dLng = (parseFloat(form.destLng) - parseFloat(form.departureLng)) * Math.PI / 180
  const a = Math.sin(dLat/2)**2 +
    Math.cos(parseFloat(form.departureLat)*Math.PI/180) *
    Math.cos(parseFloat(form.destLat)*Math.PI/180) *
    Math.sin(dLng/2)**2
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
  return (R * c).toFixed(1)
})

// 预估价格：起步价5元 + 每公里2.5元
const estimatedPrice = computed(() => {
  const dist = parseFloat(estimatedDistance.value)
  if (!dist) return 0
  return (5 + dist * 2.5).toFixed(2)
})

const getLevelTagType = (level) => {
  const value = Number(level)
  if (value === 3 || level === '金牌') return 'warning'
  if (value === 2 || level === '银牌') return ''
  return 'info'
}

const levelText = (level) => {
  const value = Number(level)
  if (value === 3) return '金牌司机'
  if (value === 2) return '银牌司机'
  if (value === 1) return '普通司机'
  if (level === '金牌' || level === '银牌' || level === '普通') return `${level}司机`
  return level || '普通司机'
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    if (!form.departureLat || !form.destLat) {
      ElMessage.warning('请从下拉列表中选择地点以自动填入坐标')
    }
    return
  }
  submitting.value = true
  dispatchResult.value = null
  try {
    const res = await createOrder({
      departure: form.departure,
      departureLng: parseFloat(form.departureLng),
      departureLat: parseFloat(form.departureLat),
      destination: form.destination,
      destLng: parseFloat(form.destLng),
      destLat: parseFloat(form.destLat)
    })
    const data = res.data || res
    // Store order identifiers for retry polling
    createdOrderId.value = data.orderId || data.id || null
    createdOrderNo.value = data.orderNo || null
    if (data.driverId || data.driverName || data.driverLevel != null) {
      dispatchResult.value = {
        driverAssigned: true,
        driverName: data.driverName || '未知',
        driverScore: data.driverScore || data.score || 5,
        driverLevel: data.driverLevel || data.level || '普通',
        vehicleInfo: data.vehicleInfo || data.vehicleBrand || data.vehicleTypeName || '未知车辆',
        plateNumber: data.plateNumber || data.vehiclePlateNumber || '未知',
        orderNo: data.orderNo || '',
        distance: data.distance || null,
        price: data.price || null
      }
      ElMessage.success('叫车成功，已为您分配司机！')
    } else {
      dispatchResult.value = { driverAssigned: false }
      ElMessage.warning('暂无可用司机，请稍后再试')
      // Start auto-retry countdown if under max retries
      if (retryCount.value < maxRetries) {
        startRetryCountdown()
      }
    }
  } catch (e) {
    ElMessage.error(e.message || '下单失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// Poll existing order status instead of creating a new one on retry
async function pollExistingOrder() {
  submitting.value = true
  try {
    // Query all statuses to detect auto-cancellation by backend
    const res = await getOrders({ statusList: '0,1,2,3,4,5,6', pageNum: 1, pageSize: 50 })
    const data = res.data || res
    const list = data.records || data.list || []
    // Find the order we created by ID or orderNo
    const match = list.find(o =>
      (createdOrderId.value && (o.id === createdOrderId.value || o.orderId === createdOrderId.value)) ||
      (createdOrderNo.value && o.orderNo === createdOrderNo.value)
    )
    if (match && (Number(match.status) === 1 || Number(match.status) === 3) && match.driverName) {
      // Driver has been assigned
      dispatchResult.value = {
        driverAssigned: true,
        driverName: match.driverName || '未知',
        driverScore: match.driverScore || match.score || 5,
        driverLevel: match.driverLevel || match.level || '普通',
        vehicleInfo: match.vehicleInfo || match.vehicleBrand || match.vehicleTypeName || '未知车辆',
        plateNumber: match.plateNumber || match.vehiclePlateNumber || '未知',
        orderNo: match.orderNo || '',
        distance: match.distance || null,
        price: match.price || null
      }
      createdOrderId.value = null
      createdOrderNo.value = null
      ElMessage.success('已为您分配司机！')
    } else if (match && Number(match.status) >= 4) {
      // Order was auto-cancelled by backend (e.g. 10min timeout) or otherwise terminated
      clearCountdown()
      createdOrderId.value = null
      createdOrderNo.value = null
      dispatchResult.value = null
      ElMessage.warning('该订单已被系统自动取消（长时间未匹配到司机），请重新下单')
    } else {
      // Still not dispatched
      dispatchResult.value = { driverAssigned: false }
      ElMessage.info('仍在匹配中，系统将继续为您寻找司机')
      if (retryCount.value < maxRetries) {
        startRetryCountdown()
      }
    }
  } catch (e) {
    dispatchResult.value = { driverAssigned: false }
    ElMessage.warning('查询订单状态失败，将继续重试')
    if (retryCount.value < maxRetries) {
      startRetryCountdown()
    }
  } finally {
    submitting.value = false
  }
}

function startRetryCountdown() {
  clearCountdown()
  retryCountdown.value = retryInterval
  countdownTimer = setInterval(() => {
    retryCountdown.value--
    if (retryCountdown.value <= 0) {
      clearCountdown()
      retryCount.value++
      // Poll existing order instead of creating a new one
      if (createdOrderId.value || createdOrderNo.value) {
        pollExistingOrder()
      } else {
        handleSubmit()
      }
    }
  }, 1000)
}

function clearCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  retryCountdown.value = 0
}

function manualRetry() {
  clearCountdown()
  retryCount.value++
  // Poll existing order instead of creating a new one
  if (createdOrderId.value || createdOrderNo.value) {
    pollExistingOrder()
  } else {
    handleSubmit()
  }
}

function cancelAutoRetry() {
  clearCountdown()
  ElMessage.info('已取消自动重试')
}

const resetForm = () => {
  clearCountdown()
  retryCount.value = 0
  createdOrderId.value = null
  createdOrderNo.value = null
  formRef.value?.resetFields()
  dispatchResult.value = null
}

onBeforeUnmount(() => {
  clearCountdown()
})
</script>

<style scoped>
.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.dot--green { background: #52c41a; }
.dot--red { background: #F56C6C; }

.location-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 4px 0;
}
.location-address {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.route-summary {
  background: #f8f9fc;
  border-radius: 10px;
  padding: 20px 24px;
  margin: 8px 0 0 80px;
}
.route-point {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.route-line {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0 10px 4px;
}
.route-dash {
  flex: 1;
  height: 1px;
  border-top: 2px dashed #dcdfe6;
}
.route-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.route-distance {
  font-size: 13px;
  color: #4A90D9;
  font-weight: 600;
  white-space: nowrap;
}
.route-price {
  font-size: 14px;
  color: #F56C6C;
  font-weight: 700;
  white-space: nowrap;
}

.driver-card {
  display: flex;
  align-items: center;
  gap: 14px;
}
.driver-info {
  flex: 1;
}
.driver-name {
  font-size: 17px;
  font-weight: 700;
  color: #1d1e2c;
  margin-bottom: 2px;
}

.result-success {
  border-top: 3px solid #52c41a !important;
}
.result-warning {
  border-top: 3px solid #E6A23C !important;
}

.tips-card {
  border-top: 3px solid #e6e8ed !important;
}
.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.tips-list li {
  padding: 8px 0;
  color: #606266;
  font-size: 13px;
  border-bottom: 1px solid #f5f5f5;
  display: flex;
  align-items: center;
  gap: 8px;
}
.tips-list li::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #4A90D9;
  flex-shrink: 0;
}
.tips-list li:last-child {
  border-bottom: none;
}
</style>
