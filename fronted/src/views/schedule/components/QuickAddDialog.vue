<template>
  <el-dialog
    :model-value="modelValue"
    title="快速添加值班人员"
    width="600px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-form :model="formData" ref="formRef" label-width="100px">
      <el-form-item label="值班日期">
        <el-input :value="date" disabled />
      </el-form-item>

      <el-form-item label="班次">
        <el-input :value="shiftName" disabled />
      </el-form-item>

      <el-form-item label="值班人员" prop="persons">
        <div class="person-selector">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索人员（姓名或部门）"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <div class="person-list" v-loading="searching">
            <div
              v-for="person in personList"
              :key="person.personId"
              class="person-item"
              :class="{ selected: isPersonSelected(person.personId) }"
              @click="togglePerson(person)"
            >
              <div class="person-info">
                <el-icon><User /></el-icon>
                <div class="person-details">
                  <div class="person-name">{{ person.personName }}</div>
                  <div class="person-dept">{{ person.deptName }}</div>
                </div>
              </div>
              <el-icon v-if="isPersonSelected(person.personId)" color="#409eff">
                <Check />
              </el-icon>
            </div>
          </div>

          <div class="selected-persons">
            <el-tag
              v-for="person in selectedPersons"
              :key="person.personId"
              closable
              @close="removePerson(person.personId)"
            >
              {{ person.personName }}
            </el-tag>
            <span v-if="selectedPersons.length === 0" class="empty-tip">
              未选择人员
            </span>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="备注">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="2"
          placeholder="备注信息（选填）"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { scheduleApi, type Person, type DutySchedule } from '@/api/schedule'

interface Props {
  modelValue: boolean
  date: string
  shiftId: number
  shiftName: string
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref()
const submitting = ref(false)
const searchKeyword = ref('')
const searching = ref(false)
const personList = ref<Person[]>([])
const selectedPersons = ref<Person[]>([])

const formData = ref({
  remark: ''
})

// 搜索人员
const handleSearch = async () => {
  searching.value = true
  try {
    const data = await scheduleApi.searchPersons(searchKeyword.value)
    personList.value = (data as Person[]) || []
  } catch (error) {
    console.error('搜索人员失败:', error)
  } finally {
    searching.value = false
  }
}

// 判断人员是否已选择
const isPersonSelected = (personId: string) => {
  return selectedPersons.value.some(p => p.personId === personId)
}

// 切换人员选择状态
const togglePerson = (person: Person) => {
  const index = selectedPersons.value.findIndex(p => p.personId === person.personId)
  if (index > -1) {
    selectedPersons.value.splice(index, 1)
  } else {
    selectedPersons.value.push(person)
  }
}

// 移除人员
const removePerson = (personId: string) => {
  const index = selectedPersons.value.findIndex(p => p.personId === personId)
  if (index > -1) {
    selectedPersons.value.splice(index, 1)
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
    remark: ''
  }
  searchKeyword.value = ''
  personList.value = []
  selectedPersons.value = []
}

// 提交表单
const handleSubmit = async () => {
  if (selectedPersons.value.length === 0) {
    ElMessage.warning('请至少选择一名值班人员')
    return
  }

  submitting.value = true
  try {
    const schedules: DutySchedule[] = selectedPersons.value.map(person => ({
      dutyDate: props.date,
      shiftId: props.shiftId,
      personId: person.personId,
      personName: person.personName,
      personGender: person.gender,
      deptId: person.deptId,
      deptName: person.deptName,
      remark: formData.value.remark
    }))

    await scheduleApi.saveBatch(schedules)
    ElMessage.success('添加成功')
    emit('success')
    handleClose()
  } catch (error: any) {
    console.error('提交失败:', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 初始化加载人员列表
watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      handleSearch()
    }
  }
)
</script>

<style scoped>
.person-selector {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.person-list {
  max-height: 250px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 8px;
}

.person-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.person-item:hover {
  background-color: #f5f7fa;
}

.person-item.selected {
  background-color: #ecf5ff;
}

.person-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.person-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.person-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.person-dept {
  font-size: 12px;
  color: #909399;
}

.selected-persons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.empty-tip {
  color: #909399;
  font-size: 14px;
  line-height: 16px;
}
</style>
