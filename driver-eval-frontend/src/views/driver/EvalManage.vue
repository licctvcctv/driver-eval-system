<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <span>乘客评价管理</span>
      </template>

      <el-table :data="evalList" v-loading="loading" stripe border>
        <el-table-column label="乘客" width="120">
          <template #default="{ row }">
            {{ row.anonymous ? '匿名用户' : (row.passengerName || '-') }}
          </template>
        </el-table-column>
        <el-table-column label="评分" width="180">
          <template #default="{ row }">
            <el-rate v-model="row.starRating" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
        <el-table-column label="标签" width="200">
          <template #default="{ row }">
            <el-tag
              v-for="tag in (row.tags || '').split(',').filter(t => t)"
              :key="tag"
              size="small"
              style="margin: 2px"
            >
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="driverReply" label="司机回复" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.driverReply || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!row.driverReply"
              type="primary"
              size="small"
              @click="openReplyDialog(row)"
            >
              回复
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
          @size-change="fetchEvals"
          @current-change="fetchEvals"
        />
      </div>
    </el-card>

    <!-- 回复对话框 -->
    <el-dialog v-model="replyDialogVisible" title="回复评价" width="500px">
      <el-form :model="replyForm" :rules="replyRules" ref="replyFormRef" label-width="80px">
        <el-form-item label="回复内容" prop="content">
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReply" :loading="replyLoading">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDriverEvals, replyEval } from '@/api/evaluation'
import { checkSensitive } from '@/api/common'

const evalList = ref([])
const loading = ref(false)
const total = ref(0)
const queryParams = ref({ page: 1, size: 10 })

const replyDialogVisible = ref(false)
const replyLoading = ref(false)
const replyFormRef = ref(null)
const replyForm = ref({ evalId: null, content: '' })
const replyRules = {
  content: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
}

async function fetchEvals() {
  loading.value = true
  try {
    const res = await getDriverEvals(queryParams.value)
    const data = res.data || res
    evalList.value = data.records || data.list || data || []
    total.value = data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openReplyDialog(row) {
  replyForm.value = { evalId: row.id, content: '' }
  replyDialogVisible.value = true
}

async function handleReply() {
  try {
    await replyFormRef.value.validate()
  } catch { return }

  replyLoading.value = true
  try {
    // 敏感词检测
    const checkRes = await checkSensitive(replyForm.value.content)
    const checkData = checkRes.data || checkRes
    if (checkData.hasSensitive || checkData.found) {
      ElMessage.warning('回复内容包含敏感词，请修改后重试')
      replyLoading.value = false
      return
    }

    await replyEval(replyForm.value)
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    await fetchEvals()
  } catch (e) {
    ElMessage.error(e.message || '回复失败')
  } finally {
    replyLoading.value = false
  }
}

onMounted(() => {
  fetchEvals()
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
