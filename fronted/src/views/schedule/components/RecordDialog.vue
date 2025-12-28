<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="900px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div v-loading="loading" class="record-content">
      <div v-if="scheduleInfo" class="schedule-info">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="值班日期">
            {{ scheduleInfo.dutyDate }}
          </el-descriptions-item>
          <el-descriptions-item label="班次">
            {{ scheduleInfo.shiftName }} ({{ scheduleInfo.shiftStartTime }}-{{ scheduleInfo.shiftEndTime }})
          </el-descriptions-item>
          <el-descriptions-item label="值班人员">
            {{ scheduleInfo.personName }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <el-form :model="formData" ref="formRef" label-width="120px" class="record-form">
        <div v-for="template in templates" :key="template.id" class="form-section">
          <div class="section-title">
            <span class="required-mark" v-if="template.isRequired === 1">*</span>
            {{ template.categoryName }}
          </div>

          <el-form-item
            :prop="`details.${template.id}`"
            :rules="{
              required: template.isRequired === 1,
              message: `请填写${template.categoryName}`,
              trigger: 'blur'
            }"
          >
            <el-input
              v-if="template.inputType === 'TEXT'"
              v-model="formData.details[template.id!]"
              :placeholder="template.fieldDescription || '请输入'"
              clearable
            />

            <el-input
              v-else-if="template.inputType === 'TEXTAREA'"
              v-model="formData.details[template.id!]"
              type="textarea"
              :rows="4"
              :placeholder="template.fieldDescription || '请输入'"
              clearable
            />

            <el-select
              v-else-if="template.inputType === 'SELECT'"
              v-model="formData.details[template.id!]"
              :placeholder="template.fieldDescription || '请选择'"
              style="width: 100%"
              clearable
            >
              <el-option
                v-for="option in getSelectOptions(template.options)"
                :key="option"
                :label="option"
                :value="option"
              />
            </el-select>

            <div v-if="template.defaultValue" class="default-value">
              默认值：{{ template.defaultValue }}
            </div>
          </el-form-item>
        </div>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { scheduleApi, type DutySchedule } from '@/api/schedule'
import { recordApi, type RecordTemplate } from '@/api/record'

interface Props {
  modelValue: boolean
  scheduleId?: number
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const scheduleInfo = ref<DutySchedule | null>(null)
const templates = ref<RecordTemplate[]>([])
const existingRecord = ref<any>(null)

const formData = ref<{
  details: Record<number, string>
}>({
  details: {}
})

const dialogTitle = ref('填写值班记录')

// 获取下拉选项
const getSelectOptions = (options: string | undefined) => {
  if (!options) return []
  try {
    return JSON.parse(options)
  } catch {
    return []
  }
}

// 加载数据
const loadData = async () => {
  if (!props.scheduleId) return

  loading.value = true
  try {
    // 加载模板
    const templateData = await recordApi.getTemplateList()
    templates.value = (templateData as RecordTemplate[]) || []

    // 加载排班信息
    const scheduleData = await scheduleApi.getById(props.scheduleId)
    scheduleInfo.value = scheduleData as DutySchedule

    // 尝试加载已有记录
    try {
      const recordData = await recordApi.getByScheduleId(props.scheduleId)
      existingRecord.value = recordData

      // 填充已有数据
      formData.value.details = {}
      if (recordData.detailList) {
        recordData.detailList.forEach((detail: any) => {
          formData.value.details[detail.templateId] = detail.recordContent || ''
        })
      }

      dialogTitle.value = '编辑值班记录'
    } catch (error) {
      // 没有已有记录，填充默认值
      formData.value.details = {}
      templates.value.forEach(template => {
        if (template.defaultValue) {
          formData.value.details[template.id!] = template.defaultValue
        } else {
          formData.value.details[template.id!] = ''
        }
      })

      dialogTitle.value = '填写值班记录'
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false)
  resetForm()
}

// 重置表单
const resetForm = () => {
  formData.value = {
    details: {}
  }
  scheduleInfo.value = null
  templates.value = []
  existingRecord.value = null
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const details = Object.entries(formData.value.details).map(
          ([templateId, recordContent]) => ({
            templateId: Number(templateId),
            recordContent
          })
        )

        const submitData = {
          scheduleId: props.scheduleId!,
          details
        }

        if (existingRecord.value) {
          await recordApi.update(existingRecord.value.id, submitData)
          ElMessage.success('更新成功')
        } else {
          await recordApi.save(submitData)
          ElMessage.success('保存成功')
        }

        emit('success')
        handleClose()
      } catch (error: any) {
        console.error('提交失败:', error)
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      loadData()
    }
  }
)
</script>

<style scoped>
.record-content {
  max-height: 60vh;
  overflow-y: auto;
}

.schedule-info {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.record-form {
  margin-top: 20px;
}

.form-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.form-section:last-child {
  border-bottom: none;
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
}

.required-mark {
  color: #f56c6c;
  margin-right: 4px;
}

.default-value {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

:deep(.el-form-item__label) {
  font-weight: normal;
}
</style>
