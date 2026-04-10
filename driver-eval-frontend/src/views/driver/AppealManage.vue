<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <span>司机申诉管理</span>
      </template>

      <el-table :data="appealList" v-loading="loading" stripe border>
        <el-table-column label="投诉信息" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.complaintContent || row.orderNo || row.complaintId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="申诉内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="appealStatusType(row.status)">
              {{ appealStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminRemark" label="管理员备注" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.adminRemark || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申诉时间" width="170" />
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchAppeals"
          @current-change="fetchAppeals"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDriverAppeals } from '@/api/appeal'

const appealList = ref([])
const loading = ref(false)
const total = ref(0)
const queryParams = ref({ page: 1, size: 10 })

function appealStatusText(status) {
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回' }
  return map[status] || '未知'
}

function appealStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

async function fetchAppeals() {
  loading.value = true
  try {
    const res = await getDriverAppeals(queryParams.value)
    const data = res.data || res
    appealList.value = data.records || data.list || data || []
    total.value = data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAppeals()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
