<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px">评价标签管理</h2>

    <el-button type="primary" style="margin-bottom: 16px" @click="openDialog()">新增标签</el-button>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="tagName" label="标签名称" />
      <el-table-column prop="tagType" label="标签类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.tagType === '好评' ? 'success' : 'danger'">{{ row.tagType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑标签' : '新增标签'" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标签名称">
          <el-input v-model="form.tagName" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="标签类型">
          <el-radio-group v-model="form.tagType">
            <el-radio label="好评">好评</el-radio>
            <el-radio label="差评">差评</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
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
import { getTagList, saveTag, deleteTag } from '@/api/tag'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, tagName: '', tagType: '好评', sortOrder: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTagList()
    tableData.value = res.data || res
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, { id: row.id, tagName: row.tagName, tagType: row.tagType, sortOrder: row.sortOrder })
  } else {
    Object.assign(form, { id: null, tagName: '', tagType: '好评', sortOrder: 0 })
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    await saveTag({ ...form })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该标签？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteTag(row.id)
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
