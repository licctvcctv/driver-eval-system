<template>
  <div class="page-container">
    <!-- Bar Chart -->
    <el-card shadow="hover" style="margin-bottom: 20px">
      <template #header>
        <div class="card-header">
          <el-icon :size="18" color="#4A90D9"><DataAnalysis /></el-icon>
          <span>评价标签统计 - 柱状图</span>
        </div>
      </template>
      <div ref="chartRef" style="width: 100%; height: 400px"></div>
    </el-card>

    <!-- Word Cloud -->
    <el-card shadow="hover" style="margin-bottom: 20px">
      <template #header>
        <div class="card-header">
          <el-icon :size="18" color="#E6A23C"><Collection /></el-icon>
          <span>评价标签统计 - 词云</span>
        </div>
      </template>
      <div ref="wordCloudRef" style="width: 100%; height: 400px"></div>
    </el-card>

    <!-- Table -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon :size="18" color="#52c41a"><List /></el-icon>
          <span>评价标签统计 - 列表</span>
        </div>
      </template>
      <el-table :data="tagList" v-loading="loading" stripe border>
        <el-table-column prop="tagName" label="标签名称" />
        <el-table-column label="标签类型" width="120">
          <template #default="{ row }">
            <el-tag :type="tagTypeStyle(row.tagType)">
              {{ tagTypeLabel(row.tagType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="数量" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import 'echarts-wordcloud'
import { getTagStats } from '@/api/evaluation'
import { DataAnalysis, Collection, List } from '@element-plus/icons-vue'

const tagList = ref([])
const loading = ref(false)
const chartRef = ref(null)
const wordCloudRef = ref(null)
let chartInstance = null
let wordCloudInstance = null

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

async function fetchTagStats() {
  loading.value = true
  try {
    const res = await getTagStats()
    tagList.value = res.data || res || []
    await nextTick()
    renderChart()
    renderWordCloud()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function renderChart() {
  if (!chartRef.value) return
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(chartRef.value)

  const names = tagList.value.map(item => item.tagName)
  const counts = tagList.value.map(item => item.count)
  const colors = tagList.value.map(item =>
    normalizeTagType(item.tagType) === 1 ? '#67c23a' : '#f56c6c'
  )

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { rotate: 30, interval: 0 }
    },
    yAxis: {
      type: 'value',
      name: '数量'
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        data: counts.map((val, idx) => ({
          value: val,
          itemStyle: { color: colors[idx] }
        })),
        barMaxWidth: 50
      }
    ]
  }

  chartInstance.setOption(option)
}

const cloudColors = [
  '#4A90D9', '#52c41a', '#E6A23C', '#F56C6C', '#9b59b6',
  '#1abc9c', '#e74c3c', '#3498db', '#f39c12', '#2ecc71',
  '#e67e22', '#1d1e2c', '#16a085', '#c0392b', '#8e44ad'
]

function renderWordCloud() {
  if (!wordCloudRef.value) return
  if (wordCloudInstance) wordCloudInstance.dispose()
  wordCloudInstance = echarts.init(wordCloudRef.value)

  const wordData = tagList.value.map(item => ({
    name: item.tagName,
    value: item.count,
    textStyle: {
      color: cloudColors[Math.floor(Math.random() * cloudColors.length)]
    }
  }))

  const option = {
    tooltip: {
      show: true,
      formatter: (params) => `${params.name}: ${params.value} 次`
    },
    series: [{
      type: 'wordCloud',
      shape: 'circle',
      left: 'center',
      top: 'center',
      width: '90%',
      height: '90%',
      sizeRange: [16, 60],
      rotationRange: [-45, 45],
      rotationStep: 15,
      gridSize: 10,
      drawOutOfBound: false,
      textStyle: {
        fontFamily: 'sans-serif',
        fontWeight: 'bold'
      },
      emphasis: {
        textStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0,0,0,0.25)'
        }
      },
      data: wordData
    }]
  }

  wordCloudInstance.setOption(option)
}

function handleResize() {
  chartInstance?.resize()
  wordCloudInstance?.resize()
}

onMounted(() => {
  fetchTagStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  wordCloudInstance?.dispose()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 15px;
}
</style>
