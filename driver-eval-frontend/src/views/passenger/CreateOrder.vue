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
                style="width: 100%"
                @select="handleSelectDeparture"
              >
                <template #prefix>
                  <span class="dot dot--green"></span>
                </template>
                <template #default="{ item }">
                  <div class="location-item">
                    <el-icon><Location /></el-icon>
                    <span>{{ item.name }}</span>
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
              >
                <template #prefix>
                  <span class="dot dot--red"></span>
                </template>
                <template #default="{ item }">
                  <div class="location-item">
                    <el-icon><Location /></el-icon>
                    <span>{{ item.name }}</span>
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
                <span class="route-distance">约 {{ estimatedDistance }} km</span>
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
            <el-descriptions :column="1" border style="margin-top: 16px" size="small">
              <el-descriptions-item label="车辆">
                {{ dispatchResult.vehicleInfo || '未知车辆' }}
              </el-descriptions-item>
              <el-descriptions-item label="车牌号">
                <span style="font-weight: 700; color: #1d1e2c; letter-spacing: 1px">
                  {{ dispatchResult.plateNumber || '未知' }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="订单号">
                <span style="font-family: monospace; font-size: 12px">{{ dispatchResult.orderNo }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </template>

          <template v-else>
            <div style="text-align: center; padding: 20px 0">
              <p style="color: #909399; margin-bottom: 16px">当前没有空闲司机，请稍后再试</p>
              <el-button type="primary" @click="handleSubmit">
                <el-icon style="margin-right: 4px"><Refresh /></el-icon>
                重新叫车
              </el-button>
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
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { createOrder } from '@/api/order'
import { Promotion, Location, Refresh, InfoFilled, SuccessFilled, WarningFilled } from '@element-plus/icons-vue'

const formRef = ref(null)
const submitting = ref(false)
const dispatchResult = ref(null)

const locations = [
  { name: '北京西站', lat: 39.8959, lng: 116.3228 },
  { name: '北京南站', lat: 39.8652, lng: 116.3789 },
  { name: '北京站', lat: 39.9028, lng: 116.4271 },
  { name: '天安门', lat: 39.9087, lng: 116.3975 },
  { name: '中关村', lat: 39.9818, lng: 116.3117 },
  { name: '望京SOHO', lat: 39.9977, lng: 116.4802 },
  { name: '三里屯', lat: 39.9317, lng: 116.4551 },
  { name: '国贸CBD', lat: 39.9087, lng: 116.4605 },
  { name: '五道口', lat: 39.9926, lng: 116.3381 },
  { name: '西单', lat: 39.9132, lng: 116.3752 },
  { name: '王府井', lat: 39.9149, lng: 116.4107 },
  { name: '颐和园', lat: 39.9998, lng: 116.2754 },
  { name: '北京大学', lat: 39.9869, lng: 116.3059 },
  { name: '首都机场T3', lat: 40.0799, lng: 116.6031 },
  { name: '大兴机场', lat: 39.5098, lng: 116.4105 },
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

const filterLocations = (query, cb) => {
  const results = query
    ? locations.filter(loc => loc.name.toLowerCase().includes(query.toLowerCase()))
    : locations
  cb(results)
}

const queryDeparture = (query, cb) => filterLocations(query, cb)
const queryDestination = (query, cb) => filterLocations(query, cb)

const handleSelectDeparture = (loc) => {
  form.departure = loc.name
  form.departureLat = String(loc.lat)
  form.departureLng = String(loc.lng)
}

const handleSelectDestination = (loc) => {
  form.destination = loc.name
  form.destLat = String(loc.lat)
  form.destLng = String(loc.lng)
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
    if (data.driverId || data.driverName || data.driverLevel != null) {
      dispatchResult.value = {
        driverAssigned: true,
        driverName: data.driverName || '未知',
        driverScore: data.driverScore || data.score || 5,
        driverLevel: data.driverLevel || data.level || '普通',
        vehicleInfo: data.vehicleInfo || data.vehicleBrand || data.vehicleTypeName || '未知车辆',
        plateNumber: data.plateNumber || data.vehiclePlateNumber || '未知',
        orderNo: data.orderNo || ''
      }
      ElMessage.success('叫车成功，已为您分配司机！')
    } else {
      dispatchResult.value = { driverAssigned: false }
      ElMessage.warning('暂无可用司机，请稍后再试')
    }
  } catch (e) {
    ElMessage.error(e.message || '下单失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  dispatchResult.value = null
}
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
  align-items: center;
  gap: 8px;
  padding: 4px 0;
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
.route-distance {
  font-size: 13px;
  color: #4A90D9;
  font-weight: 600;
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
