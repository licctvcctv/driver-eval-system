<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">敏感词管理</h2>

    <el-button type="primary" style="margin-bottom: 16px" @click="openDialog()">新增敏感词</el-button>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="word" label="敏感词" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" title="新增敏感词" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="敏感词">
          <el-input v-model="form.word" placeholder="请输入敏感词" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getWordList, saveWord, deleteWord } from '@/api/sensitiveWord'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })
const dialogVisible = ref(false)
const form = reactive({ word: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getWordList(query)
    const d = res.data || res
    tableData.value = d.records || d.list || d
    total.value = d.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openDialog = () => {
  form.word = ''
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    await saveWord({ word: form.word })
    ElMessage.success('添加成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该敏感词？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteWord(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
