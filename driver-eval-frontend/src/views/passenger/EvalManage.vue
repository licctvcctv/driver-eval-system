<template>
  <div class="page-container">
    <h2>评价管理</h2>

    <el-card shadow="hover">
      <el-table :data="evalList" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column label="星级评分" width="200">
          <template #default="{ row }">
            <el-rate :model-value="row.starRating" disabled show-score score-template="{value}" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="匿名" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isAnonymous ? 'warning' : 'info'" size="small">
              {{ row.isAnonymous ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="司机回复" min-width="200">
          <template #default="{ row }">
            <div v-if="row.driverReply" style="background: #f5f7fa; padding: 8px 12px; border-radius: 4px; font-size: 13px">
              <div style="color: #409eff; font-size: 12px; margin-bottom: 4px">司机回复：</div>
              <span>{{ row.driverReply }}</span>
            </div>
            <span v-else style="color: #c0c4cc">暂无回复</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="170" />
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 16px">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadEvals"
          @current-change="loadEvals"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyEvals } from '@/api/evaluation'

const loading = ref(false)
const evalList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadEvals = async () => {
  loading.value = true
  try {
    const res = await getMyEvals({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    const data = res.data || res
    evalList.value = data.records || data.list || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载评价列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadEvals()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
