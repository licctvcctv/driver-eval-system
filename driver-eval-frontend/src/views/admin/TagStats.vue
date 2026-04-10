<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">评价标签统计</h2>

    <el-card shadow="hover" style="margin-bottom: 20px">
      <div ref="chartRef" style="width: 100%; height: 400px"></div>
    </el-card>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="tagName" label="标签名称" />
      <el-table-column prop="tagType" label="标签类型" width="120">
        <template #default="{ row }">
          <el-tag :type="tagTypeStyle(row.tagType)">{{ tagTypeLabel(row.tagType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="count" label="使用次数" width="120" sortable />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getAllTagStats } from '@/api/evaluation'

const chartRef = ref(null)
const tableData = ref([])
let chartInstance = null

const tagTypeLabel = (tagType) => {
  const value = Number(tagType)
  if (value === 1 || tagType === '好评') return '好评'
  if (value === 2 || tagType === '差评') return '差评'
  return tagType || '-'
}

const tagTypeStyle = (tagType) => {
  const value = Number(tagType)
  if (value === 1 || tagType === '好评') return 'success'
  return 'danger'
}

const normalizeTagType = (tagType) => {
  const value = Number(tagType)
  if (value === 1 || tagType === '好评') return 1
  if (value === 2 || tagType === '差评') return 2
  return 2
}

const loadData = async () => {
  try {
    const res = await getAllTagStats()
    const data = res.data || res
    tableData.value = data

    await nextTick()
    renderChart(data)
  } catch (e) {
    console.error(e)
  }
}

const renderChart = (data) => {
  if (!chartRef.value) return
  if (chartInstance) {
    chartInstance.dispose()
  }
  chartInstance = echarts.init(chartRef.value)

  const names = data.map(d => d.tagName)
  const counts = data.map(d => d.count)
  const colors = data.map(d => normalizeTagType(d.tagType) === 1 ? '#67C23A' : '#F56C6C')

  chartInstance.setOption({
    title: { text: '标签使用统计', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { rotate: 30 }
    },
    yAxis: { type: 'value', name: '使用次数' },
    series: [{
      type: 'bar',
      data: counts.map((v, i) => ({ value: v, itemStyle: { color: colors[i] } })),
      barWidth: '50%'
    }]
  })
}

const handleResize = () => {
  chartInstance && chartInstance.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
