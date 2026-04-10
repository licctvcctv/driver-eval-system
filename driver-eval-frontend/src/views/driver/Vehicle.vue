<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>车辆信息管理</span>
          <el-button v-if="hasVehicle && !editing" type="primary" link @click="startEdit">
            <el-icon><Edit /></el-icon> 编辑
          </el-button>
        </div>
      </template>

      <!-- 车辆信息展示 -->
      <div v-if="hasVehicle && !editing" v-loading="loading">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="车牌号">{{ vehicle.plateNumber }}</el-descriptions-item>
          <el-descriptions-item label="品牌">{{ vehicle.brand }}</el-descriptions-item>
          <el-descriptions-item label="型号">{{ vehicle.model }}</el-descriptions-item>
          <el-descriptions-item label="颜色">{{ vehicle.color }}</el-descriptions-item>
          <el-descriptions-item label="车辆类型">{{ vehicle.vehicleTypeName || vehicle.vehicleType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="座位数">{{ vehicle.seats }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 车辆表单（添加/编辑） -->
      <div v-if="!hasVehicle || editing">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          style="max-width: 500px"
        >
          <el-form-item label="车牌号" prop="plateNumber">
            <el-input v-model="form.plateNumber" placeholder="请输入车牌号，如：京A12345" />
          </el-form-item>
          <el-form-item label="品牌" prop="brand">
            <el-input v-model="form.brand" placeholder="请输入品牌，如：丰田" />
          </el-form-item>
          <el-form-item label="型号" prop="model">
            <el-input v-model="form.model" placeholder="请输入型号，如：凯美瑞" />
          </el-form-item>
          <el-form-item label="颜色" prop="color">
            <el-input v-model="form.color" placeholder="请输入颜色，如：白色" />
          </el-form-item>
          <el-form-item label="车辆类型" prop="vehicleTypeId">
            <el-select v-model="form.vehicleTypeId" placeholder="请选择车辆类型" style="width: 100%">
              <el-option
                v-for="item in vehicleTypes"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="座位数" prop="seats">
            <el-input-number v-model="form.seats" :min="1" :max="50" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
              {{ hasVehicle ? '保存修改' : '添加车辆' }}
            </el-button>
            <el-button v-if="editing" @click="cancelEdit">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getMyVehicle, saveVehicle } from '@/api/vehicle'
import { getCommonVehicleTypes } from '@/api/common'

const vehicle = ref({})
const vehicleTypes = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const editing = ref(false)
const formRef = ref(null)

const hasVehicle = computed(() => vehicle.value && vehicle.value.id)

const form = ref({
  plateNumber: '',
  brand: '',
  model: '',
  color: '',
  vehicleTypeId: null,
  seats: 4
})

const rules = {
  plateNumber: [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
  brand: [{ required: true, message: '请输入品牌', trigger: 'blur' }],
  model: [{ required: true, message: '请输入型号', trigger: 'blur' }],
  color: [{ required: true, message: '请输入颜色', trigger: 'blur' }],
  vehicleTypeId: [{ required: true, message: '请选择车辆类型', trigger: 'change' }],
  seats: [{ required: true, message: '请输入座位数', trigger: 'blur' }]
}

function startEdit() {
  form.value = {
    id: vehicle.value.id,
    plateNumber: vehicle.value.plateNumber || '',
    brand: vehicle.value.brand || '',
    model: vehicle.value.model || '',
    color: vehicle.value.color || '',
    vehicleTypeId: vehicle.value.vehicleTypeId || null,
    seats: vehicle.value.seats || 4
  }
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

async function fetchVehicle() {
  loading.value = true
  try {
    const res = await getMyVehicle()
    vehicle.value = res.data || res || {}
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function fetchVehicleTypes() {
  try {
    const res = await getCommonVehicleTypes()
    vehicleTypes.value = res.data || res || []
  } catch (e) {
    console.error(e)
  }
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch { return }
  submitLoading.value = true
  try {
    await saveVehicle(form.value)
    ElMessage.success(hasVehicle.value ? '修改成功' : '添加成功')
    editing.value = false
    await fetchVehicle()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchVehicle()
  fetchVehicleTypes()
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
</style>
