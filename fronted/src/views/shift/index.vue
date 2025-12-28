<template>
  <div class="shift-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">班次配置管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增班次
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="shiftName" label="班次名称" min-width="120" />
        <el-table-column prop="shiftCode" label="班次代码" min-width="120" />
        <el-table-column label="时间段" min-width="180">
          <template #default="{ row }">
            <el-tag type="info">{{ row.startTime }} - {{ row.endTime }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="班次名称" prop="shiftName">
          <el-input
            v-model="formData.shiftName"
            placeholder="请输入班次名称，如：早班、晚班"
            clearable
          />
        </el-form-item>
        <el-form-item label="班次代码" prop="shiftCode">
          <el-input
            v-model="formData.shiftCode"
            placeholder="请输入班次代码，如：MORNING、EVENING"
            clearable
            :disabled="!!formData.id"
          />
          <div class="form-tip">班次代码创建后不可修改，建议使用大写英文</div>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-time-picker
                v-model="formData.startTime"
                format="HH:mm"
                value-format="HH:mm"
                placeholder="请选择开始时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-time-picker
                v-model="formData.endTime"
                format="HH:mm"
                value-format="HH:mm"
                placeholder="请选择结束时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number
            v-model="formData.sortOrder"
            :min="0"
            :max="999"
            :step="1"
            style="width: 100%"
          />
          <div class="form-tip">数字越小排序越靠前</div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { shiftApi, type ShiftConfig } from '@/api/shift'

const tableData = ref<ShiftConfig[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const formRef = ref<FormInstance>()

const formData = ref<ShiftConfig>({
  shiftName: '',
  shiftCode: '',
  startTime: '',
  endTime: '',
  sortOrder: 0,
  status: 1,
  remark: ''
})

const rules: FormRules = {
  shiftName: [
    { required: true, message: '请输入班次名称', trigger: 'blur' },
    { min: 2, max: 50, message: '班次名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  shiftCode: [
    { required: true, message: '请输入班次代码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '班次代码只能包含大写字母和下划线', trigger: 'blur' }
  ],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const data = await shiftApi.listEnabled()
    tableData.value = (data as ShiftConfig[]) || []
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增班次'
  formData.value = {
    shiftName: '',
    shiftCode: '',
    startTime: '',
    endTime: '',
    sortOrder: tableData.value.length,
    status: 1,
    remark: ''
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: ShiftConfig) => {
  dialogTitle.value = '编辑班次'
  formData.value = { ...row }
  dialogVisible.value = true
}

// 删除
const handleDelete = (row: ShiftConfig) => {
  ElMessageBox.confirm(
    `确定要删除班次"${row.shiftName}"吗？删除后不可恢复！`,
    '删除确认',
    {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }
  )
    .then(async () => {
      try {
        await shiftApi.delete(row.id!)
        ElMessage.success('删除成功')
        loadData()
      } catch (error) {
        console.error('删除失败:', error)
      }
    })
    .catch(() => {
      // 用户取消
    })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (formData.value.id) {
          await shiftApi.update(formData.value)
          ElMessage.success('更新成功')
        } else {
          await shiftApi.save(formData.value)
          ElMessage.success('新增成功')
        }

        dialogVisible.value = false
        loadData()
      } catch (error: any) {
        console.error('提交失败:', error)
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.shift-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-dialog__body) {
  padding-top: 20px;
}
</style>
