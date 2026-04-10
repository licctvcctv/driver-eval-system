<template>
  <div class="page-container">
    <!-- 柱状图 -->
    <el-card shadow="hover" style="margin-bottom: 20px">
      <template #header>
        <span>评价标签统计 - 图表</span>
      </template>
      <div ref="chartRef" style="width: 100%; height: 400px"></div>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="hover">
      <template #header>
        <span>评价标签统计 - 列表</span>
      </template>
      <el-table :data="tagList" v-loading="loading" stripe border>
        <el-table-column prop="tagName" label="标签名称" />
        <el-table-column label="标签类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.tagType === 1 ? 'success' : 'danger'">
              {{ row.tagType === 1 ? '好评' : '差评' }}
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
import { getTagStats } from '@/api/evaluation'

const tagList = ref([])
const loading = ref(false)
const chartRef = ref(null)
let chartInstance = null

async function fetchTagStats() {
  loading.value = true
  try {
    const res = await getTagStats()
    tagList.value = res.data || res || []
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

  const names = tagList.value.map(item => item.tagName)
  const counts = tagList.value.map(item => item.count)
  const colors = tagList.value.map(item =>
    item.tagType === 1 ? '#67c23a' : '#f56c6c'
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
      axisLabel: {
        rotate: 30,
        interval: 0
      }
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

function handleResize() {
  chartInstance?.resize()
}

onMounted(() => {
  fetchTagStats()
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
