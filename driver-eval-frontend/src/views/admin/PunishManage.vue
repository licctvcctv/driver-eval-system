<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">投诉处罚管理</h2>

    <!-- Overview cards -->
    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">当前生效处罚数</div>
            <div class="stat-value" style="color: #f56c6c">{{ stats.active }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">本周新增处罚</div>
            <div class="stat-value" style="color: #e6a23c">{{ stats.weekNew }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">累计处罚次数</div>
            <div class="stat-value" style="color: #909399">{{ stats.totalCount }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-alert
      type="info"
      show-icon
      :closable="false"
      style="margin-bottom: 16px"
      title="系统自动处罚规则：司机每周被通过的投诉达到5次及以上，系统自动处罚下线3天。每周一00:05自动检查，00:10重置计数。每天00:01自动解除到期处罚。"
    />

    <!-- Action bar -->
    <el-row :gutter="16" style="margin-bottom: 16px" justify="space-between">
      <el-col :span="6">
        <el-select v-model="query.status" placeholder="状态筛选" clearable @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="生效中" :value="1" />
          <el-option label="已过期" :value="2" />
        </el-select>
      </el-col>
      <el-col :span="4" :offset="14" style="text-align: right">
        <el-button type="warning" @click="openManualDialog">手动处罚司机</el-button>
      </el-col>
    </el-row>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column label="司机姓名">
        <template #default="{ row }">
          {{ row.driverName || row.driverId || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="punishReason" label="处罚原因" show-overflow-tooltip />
      <el-table-column prop="punishDays" label="处罚天数" width="100" />
      <el-table-column prop="punishStart" label="开始时间" width="180" />
      <el-table-column prop="punishEnd" label="结束时间" width="180" />
      <el-table-column label="剩余时间" width="120">
        <template #default="{ row }">
          {{ remainingTime(row.punishEnd, row.statusCode) }}
        </template>
      </el-table-column>
      <el-table-column prop="weekComplaints" label="周投诉数" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.statusCode === 1 ? 'danger' : 'info'">
            {{ row.statusCode === 1 ? '生效中' : '已过期' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-popconfirm
            v-if="row.statusCode === 1"
            title="确认解除该司机的处罚？"
            confirm-button-text="确认"
            cancel-button-text="取消"
            @confirm="handleLift(row)"
          >
            <template #reference>
              <el-button size="small" type="success">解除处罚</el-button>
            </template>
          </el-popconfirm>
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

    <!-- Manual Punish Dialog -->
    <el-dialog v-model="manualVisible" title="手动处罚司机" width="500px">
      <el-form :model="manualForm" label-width="90px">
        <el-form-item label="司机选择">
          <el-select
            v-model="manualForm.driverId"
            filterable
            placeholder="请搜索选择司机"
            style="width: 100%"
            :loading="driverLoading"
          >
            <el-option
              v-for="d in driverList"
              :key="d.id"
              :label="(d.realName || d.username) + ' (ID: ' + d.id + ')'"
              :value="d.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="处罚原因">
          <el-input
            v-model="manualForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入处罚原因"
          />
        </el-form-item>
        <el-form-item label="处罚天数">
          <el-input-number v-model="manualForm.days" :min="1" :max="30" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="manualVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleManualPunish">确认处罚</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { getPunishList, manualPunish, liftPunish } from '@/api/punish'
import { getUserList } from '@/api/user'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ status: '', pageNum: 1, pageSize: 10 })

// Stats
const stats = reactive({ active: 0, weekNew: 0, totalCount: 0 })

// Manual punish dialog
const manualVisible = ref(false)
const submitLoading = ref(false)
const driverLoading = ref(false)
const driverList = ref([])
const manualForm = reactive({ driverId: null, reason: '', days: 3 })

const remainingTime = (punishEnd, statusCode) => {
  if (!punishEnd) return '-'
  if (statusCode !== 1) return '已结束'
  const end = new Date(punishEnd).getTime()
  const now = Date.now()
  const diff = end - now
  if (diff <= 0) return '已结束'
  const days = Math.floor(diff / 86400000)
  const hours = Math.floor((diff % 86400000) / 3600000)
  if (days > 0) return `${days}天${hours}小时`
  return `${hours}小时`
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPunishList(query)
    const d = res.data || res
    tableData.value = d.records || d.list || d
    total.value = d.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const [resActive, resAll] = await Promise.all([
      getPunishList({ status: 1, pageNum: 1, pageSize: 1 }),
      getPunishList({ status: '', pageNum: 1, pageSize: 1 })
    ])
    const dActive = resActive.data || resActive
    const dAll = resAll.data || resAll
    stats.active = dActive.total || 0
    stats.totalCount = dAll.total || 0
    // 本周新增从总列表中统计
    const allRecords = (dAll.records || dAll.list || [])
    const weekAgo = new Date(Date.now() - 7 * 86400000)
    stats.weekNew = allRecords.filter(r => r.createTime && new Date(r.createTime) >= weekAgo).length
  } catch (e) {
    console.error(e)
  }
}

const loadDrivers = async () => {
  driverLoading.value = true
  try {
    const res = await getUserList({ role: 2, pageNum: 1, pageSize: 500 })
    const d = res.data || res
    driverList.value = d.records || d.list || d || []
  } catch (e) {
    console.error(e)
  } finally {
    driverLoading.value = false
  }
}

const openManualDialog = () => {
  Object.assign(manualForm, { driverId: null, reason: '', days: 3 })
  manualVisible.value = true
  if (driverList.value.length === 0) {
    loadDrivers()
  }
}

const handleManualPunish = async () => {
  if (!manualForm.driverId) {
    ElMessage.warning('请选择司机')
    return
  }
  submitLoading.value = true
  try {
    await manualPunish({
      driverId: manualForm.driverId,
      reason: manualForm.reason || '管理员手动处罚',
      days: manualForm.days
    })
    ElMessage.success('处罚成功')
    manualVisible.value = false
    loadData()
    loadStats()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '处罚失败')
  } finally {
    submitLoading.value = false
  }
}

const handleLift = async (row) => {
  try {
    await liftPunish({ punishId: row.id })
    ElMessage.success('已解除处罚')
    loadData()
    loadStats()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '操作失败')
  }
}

onMounted(() => {
  loadData()
  loadStats()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.stat-card {
  text-align: center;
  padding: 10px 0;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
}
</style>
