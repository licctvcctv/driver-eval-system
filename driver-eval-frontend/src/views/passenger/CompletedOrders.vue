<template>
  <div class="page-container">
    <h2>已完成订单</h2>

    <el-card shadow="hover">
      <el-table :data="orders" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="departure" label="出发地" min-width="120" show-overflow-tooltip />
        <el-table-column prop="destination" label="目的地" min-width="120" show-overflow-tooltip />
        <el-table-column label="距离" width="90" align="center">
          <template #default="{ row }">
            {{ row.distance ? Number(row.distance).toFixed(1) + ' km' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="费用" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.price" style="color: #F56C6C; font-weight: 600">¥{{ Number(row.price).toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="司机信息" min-width="160">
          <template #default="{ row }">
            <div v-if="row.driverName">
              <span>{{ row.driverName }}</span>
              <el-tag
                v-if="row.driverLevel"
                :type="levelTagType(row.driverLevel)"
                size="small"
                style="margin-left: 6px"
              >{{ levelText(row.driverLevel) }}</el-tag>
            </div>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="completeTime" label="完成时间" width="170" />
        <el-table-column label="司机评价" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.driverEvalContent">{{ row.driverEvalContent }}</span>
            <span v-else style="color: #909399">暂无评价</span>
          </template>
        </el-table-column>
        <el-table-column label="已评价" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isEvaluated ? 'success' : 'info'" size="small">
              {{ row.isEvaluated ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="已投诉" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isComplained ? 'danger' : 'info'" size="small">
              {{ row.isComplained ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              :disabled="!!row.isEvaluated"
              @click="openEvalDialog(row)"
            >评价</el-button>
            <el-button
              type="warning"
              size="small"
              link
              :disabled="!!row.isComplained"
              @click="openComplaintDialog(row)"
            >投诉</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 16px">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadOrders"
          @current-change="loadOrders"
        />
      </div>
    </el-card>

    <!-- 评价对话框 -->
    <el-dialog v-model="evalDialogVisible" title="评价司机" width="520px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="星级评分">
          <el-rate v-model="evalForm.starRating" show-text :texts="['很差', '较差', '一般', '较好', '很好']" />
        </el-form-item>
        <el-form-item label="评价标签">
          <el-checkbox-group v-model="evalForm.tagIds">
            <el-checkbox v-for="tag in tagList" :key="tag.id" :label="tag.id">
              {{ tag.tagName || tag.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="evalForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入评价内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="匿名评价">
          <el-switch v-model="evalForm.isAnonymous" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evalDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="evalSubmitting" @click="handleSubmitEval">提交评价</el-button>
      </template>
    </el-dialog>

    <!-- 投诉对话框 -->
    <el-dialog v-model="complaintDialogVisible" title="投诉司机" width="520px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="投诉内容">
          <el-input
            v-model="complaintForm.content"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您的投诉内容"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="上传图片">
          <el-upload
            v-model:file-list="complaintForm.fileList"
            :http-request="handleUpload"
            list-type="picture-card"
            :limit="3"
            accept="image/*"
            :on-exceed="handleExceed"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">最多上传3张图片</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="匿名投诉">
          <el-switch v-model="complaintForm.isAnonymous" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="complaintDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="complaintSubmitting" @click="handleSubmitComplaint">提交投诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getOrders } from '@/api/order'
import { submitEval } from '@/api/evaluation'
import { submitComplaint } from '@/api/complaint'
import { upload, getCommonTagList } from '@/api/common'

const loading = ref(false)
const orders = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 评价相关
const evalDialogVisible = ref(false)
const evalSubmitting = ref(false)
const currentOrder = ref(null)
const tagList = ref([])
const evalForm = reactive({
  starRating: 5,
  tagIds: [],
  content: '',
  isAnonymous: false
})

// 投诉相关
const complaintDialogVisible = ref(false)
const complaintSubmitting = ref(false)
const complaintForm = reactive({
  content: '',
  fileList: [],
  uploadedUrls: [],
  isAnonymous: false
})

const levelTagType = (level) => {
  if (level === 3 || level === '金牌') return 'warning'
  if (level === 2 || level === '银牌') return 'primary'
  return 'info'
}

const levelText = (level) => {
  if (level === '金牌') return '金牌司机'
  if (level === '银牌') return '银牌司机'
  if (level === '普通') return '普通司机'
  if (level === 3) return '金牌司机'
  if (level === 2) return '银牌司机'
  if (level === 1) return '普通司机'
  return level  // fallback for string values
}

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getOrders({
      status: 4,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    const data = res.data || res
    orders.value = data.records || data.list || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const loadTags = async () => {
  try {
    const res = await getCommonTagList()
    const data = res.data || res
    tagList.value = data.records || data.list || data || []
  } catch (e) {
    console.error('加载标签失败', e)
  }
}

let tagsLoaded = false

const openEvalDialog = async (row) => {
  currentOrder.value = row
  evalForm.starRating = 5
  evalForm.tagIds = []
  evalForm.content = ''
  evalForm.isAnonymous = false
  evalDialogVisible.value = true
  if (!tagsLoaded) {
    await loadTags()
    tagsLoaded = true
  }
}

const handleSubmitEval = async () => {
  if (!evalForm.starRating) {
    ElMessage.warning('请选择星级评分')
    return
  }
  evalSubmitting.value = true
  try {
    await submitEval({
      orderId: currentOrder.value.id,
      driverId: currentOrder.value.driverId,
      starRating: evalForm.starRating,
      tagIds: evalForm.tagIds,
      content: evalForm.content,
      isAnonymous: evalForm.isAnonymous ? 1 : 0
    })
    ElMessage.success('评价提交成功')
    evalDialogVisible.value = false
    loadOrders()
  } catch (e) {
    ElMessage.error(e.message || '评价提交失败')
  } finally {
    evalSubmitting.value = false
  }
}

const openComplaintDialog = (row) => {
  currentOrder.value = row
  complaintForm.content = ''
  complaintForm.fileList = []
  complaintForm.uploadedUrls = []
  complaintForm.isAnonymous = false
  complaintDialogVisible.value = true
}

const handleUpload = async (options) => {
  try {
    const res = await upload(options.file)
    const url = res.data?.url || res.data || res.url
    complaintForm.uploadedUrls.push(url)
    options.onSuccess(res)
  } catch (e) {
    options.onError(e)
    ElMessage.error('图片上传失败')
  }
}

const handleExceed = () => {
  ElMessage.warning('最多上传3张图片')
}

const handleSubmitComplaint = async () => {
  if (!complaintForm.content.trim()) {
    ElMessage.warning('请输入投诉内容')
    return
  }
  complaintSubmitting.value = true
  try {
    await submitComplaint({
      orderId: currentOrder.value.id,
      driverId: currentOrder.value.driverId,
      content: complaintForm.content,
      images: complaintForm.uploadedUrls.join(','),
      isAnonymous: complaintForm.isAnonymous ? 1 : 0
    })
    ElMessage.success('投诉提交成功')
    complaintDialogVisible.value = false
    loadOrders()
  } catch (e) {
    ElMessage.error(e.message || '投诉提交失败')
  } finally {
    complaintSubmitting.value = false
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
