<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">乘客评价管理</h2>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column label="订单号" width="180">
        <template #default="{ row }">
          {{ row.orderNo || row.orderId || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="乘客" width="100">
        <template #default="{ row }">
          {{ isAnonymous(row) ? '匿名' : (row.passengerName || row.passengerId || '-') }}
        </template>
      </el-table-column>
      <el-table-column label="司机" width="100">
        <template #default="{ row }">
          {{ row.driverName || row.driverId || '-' }}
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
            v-for="tag in normalizeTags(row.tags)"
            :key="tag"
            size="small"
            style="margin: 2px"
          >{{ tag }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="driverReply" label="司机回复" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="180" />
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
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getAllEvals } from '@/api/evaluation'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

const isAnonymous = (row) => Boolean(row.anonymous ?? row.isAnonymous)

const normalizeTags = (tags) => {
  if (!tags) return []
  if (Array.isArray(tags)) return tags.filter(Boolean)
  return String(tags).split(',').map(t => t.trim()).filter(Boolean)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAllEvals(query)
    const d = res.data || res
    tableData.value = d.records || d.list || d
    total.value = d.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
