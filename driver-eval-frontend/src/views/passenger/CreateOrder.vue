<template>
  <div class="page-container">
    <h2>立即下单</h2>

    <el-row :gutter="20">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: bold">填写出行信息</span>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="出发地" prop="departure">
              <el-input v-model="form.departure" placeholder="请输入出发地名称" />
            </el-form-item>
            <el-form-item label="出发地经度" prop="departureLng">
              <el-input v-model="form.departureLng" placeholder="经度" />
            </el-form-item>
            <el-form-item label="出发地纬度" prop="departureLat">
              <el-input v-model="form.departureLat" placeholder="纬度" />
            </el-form-item>
            <el-form-item label="快速选择出发地">
              <div style="display: flex; gap: 8px; flex-wrap: wrap">
                <el-button
                  v-for="loc in presetLocations"
                  :key="'dep-' + loc.name"
                  size="small"
                  @click="fillDeparture(loc)"
                >{{ loc.name }}</el-button>
              </div>
            </el-form-item>

            <el-divider />

            <el-form-item label="目的地" prop="destination">
              <el-input v-model="form.destination" placeholder="请输入目的地名称" />
            </el-form-item>
            <el-form-item label="目的地经度" prop="destLng">
              <el-input v-model="form.destLng" placeholder="经度" />
            </el-form-item>
            <el-form-item label="目的地纬度" prop="destLat">
              <el-input v-model="form.destLat" placeholder="纬度" />
            </el-form-item>
            <el-form-item label="快速选择目的地">
              <div style="display: flex; gap: 8px; flex-wrap: wrap">
                <el-button
                  v-for="loc in presetLocations"
                  :key="'dest-' + loc.name"
                  size="small"
                  @click="fillDestination(loc)"
                >{{ loc.name }}</el-button>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmit">立即叫车</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card v-if="dispatchResult" shadow="hover">
          <template #header>
            <span style="font-weight: bold">派单结果</span>
          </template>

          <el-result
            v-if="dispatchResult.driverAssigned"
            icon="success"
            title="派单成功"
            sub-title="已为您分配司机"
          >
            <template #extra>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="司机姓名">{{ dispatchResult.driverName }}</el-descriptions-item>
                <el-descriptions-item label="综合评分">
                  <el-rate :model-value="dispatchResult.driverScore" disabled show-score score-template="{value}" />
                </el-descriptions-item>
                <el-descriptions-item label="司机等级">
                  <el-tag :type="getLevelTagType(dispatchResult.driverLevel)">
                    {{ dispatchResult.driverLevel }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="车辆信息">{{ dispatchResult.vehicleInfo }}</el-descriptions-item>
                <el-descriptions-item label="车牌号">{{ dispatchResult.plateNumber }}</el-descriptions-item>
                <el-descriptions-item label="订单号">{{ dispatchResult.orderNo }}</el-descriptions-item>
              </el-descriptions>
            </template>
          </el-result>

          <el-result
            v-else
            icon="warning"
            title="暂无可用司机"
            sub-title="当前没有空闲司机，请稍后再试"
          >
            <template #extra>
              <el-button type="primary" @click="handleSubmit">重新叫车</el-button>
            </template>
          </el-result>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { createOrder } from '@/api/order'

const formRef = ref(null)
const submitting = ref(false)
const dispatchResult = ref(null)

const presetLocations = [
  { name: '北京站', lng: '116.4267', lat: '39.9029' },
  { name: '天安门', lng: '116.3975', lat: '39.9087' },
  { name: '中关村', lng: '116.3106', lat: '39.9819' },
  { name: '望京SOHO', lng: '116.4808', lat: '39.9936' },
  { name: '三里屯', lng: '116.4551', lat: '39.9372' },
  { name: '国贸CBD', lng: '116.4609', lat: '39.9089' }
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
  departure: [{ required: true, message: '请输入出发地', trigger: 'blur' }],
  departureLng: [{ required: true, message: '请输入出发地经度', trigger: 'blur' }],
  departureLat: [{ required: true, message: '请输入出发地纬度', trigger: 'blur' }],
  destination: [{ required: true, message: '请输入目的地', trigger: 'blur' }],
  destLng: [{ required: true, message: '请输入目的地经度', trigger: 'blur' }],
  destLat: [{ required: true, message: '请输入目的地纬度', trigger: 'blur' }]
}

const fillDeparture = (loc) => {
  form.departure = loc.name
  form.departureLng = loc.lng
  form.departureLat = loc.lat
}

const fillDestination = (loc) => {
  form.destination = loc.name
  form.destLng = loc.lng
  form.destLat = loc.lat
}

const getLevelTagType = (level) => {
  if (level === '金牌') return 'warning'
  if (level === '银牌') return ''
  return 'info'
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
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
    if (data.driverId || data.driverName) {
      dispatchResult.value = {
        driverAssigned: true,
        driverName: data.driverName || '未知',
        driverScore: data.driverScore || data.score || 5,
        driverLevel: data.driverLevel || data.level || '普通',
        vehicleInfo: data.vehicleInfo || data.vehicleBrand || '未知车辆',
        plateNumber: data.plateNumber || '未知',
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
.page-container {
  padding: 20px;
}
</style>
