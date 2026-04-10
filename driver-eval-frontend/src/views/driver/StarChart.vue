<template>
  <div class="page-container">
    <!-- 折线图 -->
    <el-card shadow="hover" style="margin-bottom: 20px">
      <template #header>
        <span>星级评价趋势</span>
      </template>
      <div ref="chartRef" style="width: 100%; height: 400px"></div>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="hover">
      <template #header>
        <span>星级评价数据</span>
      </template>
      <el-table :data="starList" v-loading="loading" stripe border>
        <el-table-column prop="month" label="月份" />
        <el-table-column label="平均评分">
          <template #default="{ row }">
            {{ row.avgStar ? Number(row.avgStar).toFixed(2) : '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getStarStats } from '@/api/evaluation'

const starList = ref([])
const loading = ref(false)
const chartRef = ref(null)
let chartInstance = null

async function fetchStarStats() {
  loading.value = true
  try {
    const res = await getStarStats()
    starList.value = res.data || res || []
    await nextTick()
    renderChart()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function renderChart() {
  if (!chartRef.value) return
  if (chartInstance) {
    chartInstance.dispose()
  }
  chartInstance = echarts.init(chartRef.value)

  const months = starList.value.map(item => item.month)
  const avgStars = starList.value.map(item => Number(item.avgStar) || 0)

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>平均评分: {c}'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months,
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      name: '平均评分',
      min: 1,
      max: 5,
      interval: 1
    },
    series: [
      {
        name: '平均评分',
        type: 'line',
        data: avgStars,
        smooth: true,
        itemStyle: { color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        markLine: {
          data: [{ type: 'average', name: '平均值' }],
          label: { formatter: '平均: {c}' }
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

function handleResize() {
  chartInstance?.resize()
}

onMounted(() => {
  fetchStarStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
