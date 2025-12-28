<template>
  <div class="template-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">记录模板配置</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增模板
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="categoryName" label="类别名称" min-width="150" />
        <el-table-column prop="categoryCode" label="类别代码" min-width="120" />
        <el-table-column prop="fieldDescription" label="字段描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="输入类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getInputTypeTag(row.inputType)">
              {{ getInputTypeLabel(row.inputType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="必填" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isRequired === 1 ? 'danger' : 'info'" size="small">
              {{ row.isRequired === 1 ? '是' : '否' }}
            </el-tag>
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
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类别名称" prop="categoryName">
              <el-input
                v-model="formData.categoryName"
                placeholder="如：留置对象"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类别代码" prop="categoryCode">
              <el-input
                v-model="formData.categoryCode"
                placeholder="如：DETENTION"
                clearable
                :disabled="!!formData.id"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="字段描述" prop="fieldDescription">
          <el-input
            v-model="formData.fieldDescription"
            placeholder="对该字段的说明，如：当前留置对象人数及基本情况"
            clearable
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="输入类型" prop="inputType">
              <el-select v-model="formData.inputType" placeholder="请选择" style="width: 100%">
                <el-option label="单行文本" value="TEXT" />
                <el-option label="多行文本" value="TEXTAREA" />
                <el-option label="下拉选择" value="SELECT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否必填" prop="isRequired">
              <el-radio-group v-model="formData.isRequired">
                <el-radio :label="1">必填</el-radio>
                <el-radio :label="0">非必填</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="默认值" prop="defaultValue">
          <el-input
            v-model="formData.defaultValue"
            type="textarea"
            :rows="2"
            placeholder="填写记录时自动填充的默认内容"
          />
        </el-form-item>

        <el-form-item
          v-if="formData.inputType === 'SELECT'"
          label="选项配置"
          prop="options"
        >
          <el-input
            v-model="formData.options"
            type="textarea"
            :rows="3"
            placeholder='JSON格式，如：["选项1", "选项2", "选项3"]'
          />
          <div class="form-tip">
            仅当输入类型为"下拉选择"时需要配置，格式为JSON数组
          </div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number
                v-model="formData.sortOrder"
                :min="0"
                :max="999"
                :step="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="2"
            placeholder="备注信息（选填）"
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { templateApi, type RecordTemplateConfig } from '@/api/template'

const tableData = ref<RecordTemplateConfig[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const formRef = ref<FormInstance>()

const formData = ref<RecordTemplateConfig>({
  categoryName: '',
  categoryCode: '',
  fieldDescription: '',
  defaultValue: '',
  inputType: 'TEXT',
  options: '',
  isRequired: 0,
  sortOrder: 0,
  status: 1,
  remark: ''
})

const rules: FormRules = {
  categoryName: [
    { required: true, message: '请输入类别名称', trigger: 'blur' },
    { min: 2, max: 100, message: '类别名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  categoryCode: [
    { required: true, message: '请输入类别代码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '类别代码只能包含大写字母和下划线', trigger: 'blur' }
  ],
  fieldDescription: [
    { required: true, message: '请输入字段描述', trigger: 'blur' }
  ],
  inputType: [{ required: true, message: '请选择输入类型', trigger: 'change' }]
}

// 获取输入类型标签
const getInputTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    TEXT: '单行文本',
    TEXTAREA: '多行文本',
    SELECT: '下拉选择'
  }
  return map[type] || type
}

// 获取输入类型标签颜色
const getInputTypeTag = (type: string) => {
  const map: Record<string, string> = {
    TEXT: '',
    TEXTAREA: 'success',
    SELECT: 'warning'
  }
  return map[type] || ''
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const data = await templateApi.listEnabled()
    tableData.value = (data as RecordTemplateConfig[]) || []
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增模板'
  formData.value = {
    categoryName: '',
    categoryCode: '',
    fieldDescription: '',
    defaultValue: '',
    inputType: 'TEXT',
    options: '',
    isRequired: 0,
    sortOrder: tableData.value.length,
    status: 1,
    remark: ''
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: RecordTemplateConfig) => {
  dialogTitle.value = '编辑模板'
  formData.value = { ...row }
  dialogVisible.value = true
}

// 删除
const handleDelete = (row: RecordTemplateConfig) => {
  ElMessageBox.confirm(
    `确定要删除模板"${row.categoryName}"吗？删除后不可恢复！`,
    '删除确认',
    {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }
  )
    .then(async () => {
      try {
        await templateApi.delete(row.id!)
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
          await templateApi.update(formData.value)
          ElMessage.success('更新成功')
        } else {
          await templateApi.save(formData.value)
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
.template-container {
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
