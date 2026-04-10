<template>
  <div class="page-container">
    <h2>投诉管理</h2>

    <el-card shadow="hover">
      <el-table :data="complaints" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="content" label="投诉内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="图片" width="200">
          <template #default="{ row }">
            <div v-if="row.images" style="display: flex; gap: 6px; flex-wrap: wrap">
              <el-image
                v-for="(img, idx) in parseImages(row.images)"
                :key="idx"
                :src="img"
                :preview-src-list="parseImages(row.images)"
                :initial-index="idx"
                fit="cover"
                style="width: 50px; height: 50px; border-radius: 4px"
                preview-teleported
              />
            </div>
            <span v-else style="color: #c0c4cc">无</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="管理员备注" min-width="160">
          <template #default="{ row }">
            <span v-if="row.adminRemark">{{ row.adminRemark }}</span>
            <span v-else style="color: #c0c4cc">暂无</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="投诉时间" width="170" />
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 16px">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadComplaints"
          @current-change="loadComplaints"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyComplaints } from '@/api/complaint'

const loading = ref(false)
const complaints = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回' }
  return map[status] || '未知'
}

const statusTagType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const parseImages = (images) => {
  if (!images) return []
  if (Array.isArray(images)) return images
  return images.split(',').filter(Boolean)
}

const loadComplaints = async () => {
  loading.value = true
  try {
    const res = await getMyComplaints({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    const data = res.data || res
    complaints.value = data.records || data.list || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载投诉列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadComplaints()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
