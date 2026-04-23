<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">系统用户</h2>

    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="6">
        <el-select v-model="query.role" placeholder="角色筛选" clearable @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="乘客" :value="1" />
          <el-option label="司机" :value="2" />
          <el-option label="管理员" :value="3" />
        </el-select>
      </el-col>
      <el-col :span="6">
        <el-input v-model="query.keyword" placeholder="搜索用户名/姓名/手机" clearable @keyup.enter="loadData" />
      </el-col>
      <el-col :span="4">
        <el-button type="primary" @click="loadData">搜索</el-button>
      </el-col>
    </el-row>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="真实姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag>{{ roleLabel(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            :type="isNormal(row.status) ? 'danger' : 'success'"
            @click="handleToggle(row)"
          >
            {{ isNormal(row.status) ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 16px; justify-content: flex-end"
      background
      layout="total, prev, pager, next, sizes"
      :total="total"
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      @current-change="loadData"
      @size-change="loadData"
    />

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" width="450px" destroy-on-close>
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateUser" :loading="editLoading">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getUserList, toggleStatus, updateUser, deleteUser } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ role: '', keyword: '', pageNum: 1, pageSize: 10 })

const editDialogVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref(null)
const editForm = ref({ id: null, username: '', realName: '', phone: '' })

const editRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const roleLabel = (role) => {
  const value = Number(role)
  if (value === 1 || role === '乘客') return '乘客'
  if (value === 2 || role === '司机') return '司机'
  if (value === 3 || role === '管理员') return '管理员'
  return role || '-'
}

const isNormal = (status) => Number(status) === 1 || status === '正常'

const statusType = (s) => {
  if (isNormal(s)) return 'success'
  if (Number(s) === 0 || s === '禁用') return 'info'
  if (Number(s) === 2 || s === '处罚中') return 'danger'
  return ''
}

const statusLabel = (status) => {
  const value = Number(status)
  if (value === 1 || status === '正常') return '正常'
  if (value === 0 || status === '禁用') return '禁用'
  if (value === 2 || status === '处罚中') return '处罚中'
  return status || '-'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList(query)
    const d = res.data || res
    tableData.value = d.records || d.list || d
    total.value = d.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleToggle = async (row) => {
  try {
    await toggleStatus({
      userId: row.id,
      status: isNormal(row.status) ? 0 : 1
    })
    ElMessage.success('操作成功')
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

function openEditDialog(row) {
  editForm.value = {
    id: row.id,
    username: row.username || '',
    realName: row.realName || '',
    phone: row.phone || ''
  }
  editDialogVisible.value = true
}

async function handleUpdateUser() {
  try {
    await editFormRef.value.validate()
  } catch { return }

  editLoading.value = true
  try {
    await updateUser(editForm.value)
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  } finally {
    editLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户「${row.realName || row.username}」吗？`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
