<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">司机申诉管理</h2>

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
      <el-table-column label="司机" width="100">
        <template #default="{ row }">
          {{ row.driverName || row.driverId || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="投诉内容" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.complaintContent || row.orderNo || row.complaintId || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="申诉内容" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.appealContent || row.content || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="申诉图片" width="120">
        <template #default="{ row }">
          <div v-if="parseImages(row.images).length" class="image-list">
            <el-image
              v-for="(img, idx) in parseImages(row.images)"
              :key="idx"
              :src="img"
              :preview-src-list="parseImages(row.images)"
              :initial-index="idx"
              fit="cover"
              class="thumb-image"
              preview-teleported
            />
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="appealStatusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
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

    <el-dialog v-model="reviewVisible" title="审核申诉" width="550px">
      <div v-if="currentRow" style="margin-bottom: 16px">
        <el-descriptions :column="2" border size="small" title="司机信息">
          <el-descriptions-item label="当前评分">{{ currentRow.driverScore ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="总投诉数">{{ currentRow.driverTotalComplaints ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="本周投诉">{{ currentRow.driverWeekComplaints ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="订单号">{{ currentRow.orderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="出发地" v-if="currentRow.departure">{{ currentRow.departure }}</el-descriptions-item>
          <el-descriptions-item label="目的地" v-if="currentRow.destination">{{ currentRow.destination }}</el-descriptions-item>
        </el-descriptions>
        <el-alert
          v-if="currentRow.driverOnlineStatus === 2"
          type="warning"
          :closable="false"
          show-icon
          style="margin-top: 12px"
        >
          该司机当前处于处罚中，通过申诉将解除处罚
        </el-alert>
        <div v-if="parseImages(currentRow.images).length" class="review-images">
          <div class="review-images-title">申诉图片</div>
          <el-image
            v-for="(img, idx) in parseImages(currentRow.images)"
            :key="idx"
            :src="img"
            :preview-src-list="parseImages(currentRow.images)"
            :initial-index="idx"
            fit="cover"
            class="review-image"
            preview-teleported
          />
        </div>
      </div>
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
import { getAllAppeals, reviewAppeal } from '@/api/appeal'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ status: '', pageNum: 1, pageSize: 10 })
const reviewVisible = ref(false)
const reviewForm = reactive({ appealId: null, status: 1, remark: '' })
const currentRow = ref(null)

const appealStatusType = (s) => {
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

const parseImages = (images) => {
  if (!images) return []
  if (Array.isArray(images)) return images
  return images.split(',').map(s => s.trim()).filter(Boolean)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAllAppeals(query)
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
  currentRow.value = row
  Object.assign(reviewForm, { appealId: row.id, status: 1, remark: '' })
  reviewVisible.value = true
}

const handleReview = async () => {
  try {
    await reviewAppeal({ ...reviewForm })
    ElMessage.success('审核成功')
    reviewVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('审核失败')
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 20px; }
.image-list {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
.thumb-image {
  width: 40px;
  height: 40px;
  border-radius: 4px;
}
.review-images {
  margin-top: 12px;
}
.review-images-title {
  margin-bottom: 8px;
  color: #606266;
  font-size: 13px;
}
.review-image {
  width: 72px;
  height: 72px;
  margin-right: 8px;
  border-radius: 4px;
}
</style>
