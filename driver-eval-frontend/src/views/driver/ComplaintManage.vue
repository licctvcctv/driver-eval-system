<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <span>乘客投诉管理</span>
      </template>

      <el-table :data="complaintList" v-loading="loading" stripe border>
        <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />
        <el-table-column label="乘客" width="120">
          <template #default="{ row }">
            {{ row.anonymous ? '匿名用户' : (row.passengerName || '-') }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="投诉内容" show-overflow-tooltip />
        <el-table-column label="图片" width="120">
          <template #default="{ row }">
            <div v-if="row.images" class="image-list">
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
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="complaintStatusType(row.status)">
              {{ complaintStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminRemark" label="管理员备注" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.adminRemark || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="投诉时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1 && !row.hasAppeal"
              type="warning"
              size="small"
              @click="openAppealDialog(row)"
            >
              申诉
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchComplaints"
          @current-change="fetchComplaints"
        />
      </div>
    </el-card>

    <!-- 申诉对话框 -->
    <el-dialog v-model="appealDialogVisible" title="提交申诉" width="500px">
      <el-form :model="appealForm" :rules="appealRules" ref="appealFormRef" label-width="80px">
        <el-form-item label="申诉内容" prop="content">
          <el-input
            v-model="appealForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入申诉内容"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appealDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAppeal" :loading="appealLoading">提交申诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDriverComplaints } from '@/api/complaint'
import { submitAppeal } from '@/api/appeal'
import { checkSensitive } from '@/api/common'

const complaintList = ref([])
const loading = ref(false)
const total = ref(0)
const queryParams = ref({ page: 1, size: 10 })

const appealDialogVisible = ref(false)
const appealLoading = ref(false)
const appealFormRef = ref(null)
const appealForm = ref({ complaintId: null, content: '' })
const appealRules = {
  content: [{ required: true, message: '请输入申诉内容', trigger: 'blur' }]
}

function parseImages(images) {
  if (!images) return []
  if (Array.isArray(images)) return images
  try {
    return JSON.parse(images)
  } catch {
    return images.split(',').filter(s => s)
  }
}

function complaintStatusText(status) {
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回' }
  return map[status] || '未知'
}

function complaintStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] || 'info'
}

async function fetchComplaints() {
  loading.value = true
  try {
    const res = await getDriverComplaints(queryParams.value)
    const data = res.data || res
    complaintList.value = data.records || data.list || data || []
    total.value = data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openAppealDialog(row) {
  appealForm.value = { complaintId: row.id, content: '' }
  appealDialogVisible.value = true
}

async function handleSubmitAppeal() {
  try {
    await appealFormRef.value.validate()
  } catch { return }

  appealLoading.value = true
  try {
    // 敏感词检测
    const checkRes = await checkSensitive(appealForm.value.content)
    const checkData = checkRes.data || checkRes
    if (checkData.contains) {
      ElMessage.warning('申诉内容包含敏感词，请修改后重试')
      appealLoading.value = false
      return
    }

    await submitAppeal(appealForm.value)
    ElMessage.success('申诉提交成功')
    appealDialogVisible.value = false
    await fetchComplaints()
  } catch (e) {
    ElMessage.error(e.message || '申诉提交失败')
  } finally {
    appealLoading.value = false
  }
}

onMounted(() => {
  fetchComplaints()
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
.image-list {
  display: flex;
  gap: 4px;
}
.thumb-image {
  width: 40px;
  height: 40px;
  border-radius: 4px;
}
</style>
