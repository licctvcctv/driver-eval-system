<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">司机评分管理</h2>

    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="6">
        <el-select v-model="query.level" placeholder="等级筛选" clearable @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="金牌" value="金牌" />
          <el-option label="银牌" value="银牌" />
          <el-option label="普通" value="普通" />
        </el-select>
      </el-col>
      <el-col :span="4">
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-col>
    </el-row>

    <el-table :data="tableData" border stripe v-loading="loading" @row-click="showDetail" style="cursor: pointer">
      <el-table-column prop="driverName" label="司机姓名" />
      <el-table-column prop="score" label="评分" width="220">
        <template #default="{ row }">
          <span :style="{ color: scoreColor(row.score), fontWeight: 'bold', marginRight: '8px' }">{{ row.score }}</span>
          <el-progress
            :percentage="row.score"
            :stroke-width="10"
            :color="scoreColor(row.score)"
            :show-text="false"
            style="width: 100px; display: inline-flex; vertical-align: middle"
          />
        </template>
      </el-table-column>
      <el-table-column prop="level" label="等级" width="100">
        <template #default="{ row }">
          <el-tag :type="levelType(row.level)">{{ levelLabel(row.level) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalOrders" label="总订单数" width="100" />
      <el-table-column prop="totalComplaints" label="总投诉数" width="100" />
      <el-table-column prop="weekComplaints" label="周投诉数" width="100" />
      <el-table-column prop="onlineStatus" label="在线状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.onlineStatus === '在线' ? 'success' : 'info'" size="small">{{ row.onlineStatus }}</el-tag>
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

    <el-dialog v-model="detailVisible" title="评分详情" width="650px">
      <div v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="司机姓名">{{ detailData.driverName }}</el-descriptions-item>
          <el-descriptions-item label="当前评分">{{ detailData.score }}</el-descriptions-item>
          <el-descriptions-item label="等级">{{ detailData.level }}</el-descriptions-item>
          <el-descriptions-item label="总订单数">{{ detailData.totalOrders }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 16px 0 8px">评分变更记录</h4>
        <el-table :data="detailData.scoreLogs || []" border size="small" max-height="300">
          <el-table-column prop="changeTime" label="时间" width="180" />
          <el-table-column prop="changeType" label="变更类型" width="120" />
          <el-table-column prop="changeValue" label="变更值" width="100">
            <template #default="{ row }">
              <span :style="{ color: row.changeValue > 0 ? '#67C23A' : '#F56C6C' }">
                {{ row.changeValue > 0 ? '+' : '' }}{{ row.changeValue }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getDriverScoreList, getDriverScoreDetail } from '@/api/score'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ level: '', pageNum: 1, pageSize: 10 })
const detailVisible = ref(false)
const detailData = ref(null)

const scoreColor = (s) => {
  if (s >= 90) return '#67C23A'
  if (s >= 70) return '#E6A23C'
  return '#F56C6C'
}

const levelType = (l) => {
  if (l === 3 || l === '金牌') return 'warning'
  if (l === 2 || l === '银牌') return ''
  return 'info'
}

const levelLabel = (l) => {
  if (l === 3) return '金牌'
  if (l === 2) return '银牌'
  if (l === 1) return '普通'
  return l  // fallback for string values
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDriverScoreList(query)
    const d = res.data || res
    tableData.value = d.records || d.list || d
    total.value = d.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const showDetail = async (row) => {
  try {
    const res = await getDriverScoreDetail(row.driverId || row.id)
    detailData.value = res.data || res
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
