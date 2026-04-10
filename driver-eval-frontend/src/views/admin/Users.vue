<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">系统用户</h2>

    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="6">
        <el-select v-model="query.role" placeholder="角色筛选" clearable @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="乘客" value="乘客" />
          <el-option label="司机" value="司机" />
          <el-option label="管理员" value="管理员" />
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
          <el-tag>{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            :type="row.status === '正常' ? 'danger' : 'success'"
            @click="handleToggle(row)"
          >
            {{ row.status === '正常' ? '禁用' : '启用' }}
          </el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getUserList, toggleStatus } from '@/api/user'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ role: '', keyword: '', pageNum: 1, pageSize: 10 })

const statusType = (s) => {
  if (s === '正常') return 'success'
  if (s === '禁用') return 'info'
  if (s === '处罚中') return 'danger'
  return ''
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
    await toggleStatus({ id: row.id, status: row.status === '正常' ? '禁用' : '正常' })
    ElMessage.success('操作成功')
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
