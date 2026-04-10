<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">车辆类型</h2>

    <el-button type="primary" style="margin-bottom: 16px" @click="openDialog()">新增类型</el-button>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="typeName" label="类型名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑类型' : '新增类型'" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="类型名称">
          <el-input v-model="form.typeName" placeholder="请输入类型名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getVehicleTypes, saveVehicleType, deleteVehicleType } from '@/api/vehicle'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, typeName: '', description: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getVehicleTypes()
    tableData.value = res.data || res
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, { id: row.id, typeName: row.typeName, description: row.description })
  } else {
    Object.assign(form, { id: null, typeName: '', description: '' })
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    await saveVehicleType({ ...form })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该车辆类型？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteVehicleType(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
