<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">乘客投诉管理</h2>

    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="6">
        <el-select v-model="query.status" placeholder="状态筛选" clearable @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="2" />
        </el-select>
      </el-col>
      <el-col :span="4">
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-col>
    </el-row>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column label="订单号" width="180">
        <template #default="{ row }">
          {{ row.orderNo || row.orderId || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="乘客" width="100">
        <template #default="{ row }">
          {{ row.passengerName || row.passengerId || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="司机" width="100">
        <template #default="{ row }">
          {{ row.driverName || row.driverId || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="content" label="投诉内容" show-overflow-tooltip />
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <el-image
            v-if="row.images && row.images.length"
            :src="row.images[0]"
            :preview-src-list="row.images"
            style="width: 40px; height: 40px"
            fit="cover"
            preview-teleported
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="complaintStatusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="adminRemark" label="管理员备注" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 0"
            size="small"
            type="primary"
            @click="openReview(row)"
          >审核</el-button>
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

    <el-dialog v-model="reviewVisible" title="审核投诉" width="450px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="reviewForm.status">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="reviewForm.remark" type="textarea" :rows="3" placeholder="请输入审核备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getAllComplaints, reviewComplaint } from '@/api/complaint'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ status: '', pageNum: 1, pageSize: 10 })
const reviewVisible = ref(false)
const reviewForm = reactive({ complaintId: null, status: 1, remark: '' })

const complaintStatusType = (s) => {
  const value = Number(s)
  if (value === 0 || s === '待审核') return 'warning'
  if (value === 1 || s === '已通过') return 'success'
  if (value === 2 || s === '已驳回') return 'info'
  return ''
}

const statusLabel = (s) => {
  const value = Number(s)
  if (value === 0 || s === '待审核') return '待审核'
  if (value === 1 || s === '已通过') return '已通过'
  if (value === 2 || s === '已驳回') return '已驳回'
  return s || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAllComplaints(query)
    const d = res.data || res
    tableData.value = d.records || d.list || d
    total.value = d.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openReview = (row) => {
  Object.assign(reviewForm, { complaintId: row.id, status: 1, remark: '' })
  reviewVisible.value = true
}

const handleReview = async () => {
  try {
    await reviewComplaint({ ...reviewForm })
    reviewVisible.value = false
    if (reviewForm.status === 1) {
      ElMessage.success('投诉已通过，该司机本周投诉次数+1。累计5次将自动处罚下线3天。')
    } else {
      ElMessage.success('审核成功')
    }
    loadData()
  } catch (e) {
    ElMessage.error('审核失败')
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
