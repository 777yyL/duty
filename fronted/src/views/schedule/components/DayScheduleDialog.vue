<template>
  <el-dialog
    :model-value="modelValue"
    title="日排班 - 批量设置所有班次"
    width="1100px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="dialog-content">
      <!-- 日期显示 -->
      <div class="date-display">
        <el-icon><Calendar /></el-icon>
        <span class="date-text">{{ formatDate(date) }}</span>
      </div>

      <!-- 双栏布局 -->
      <div class="two-column-layout">
        <!-- 左侧：班次列表 -->
        <div class="left-panel">
          <div class="panel-header">
            <el-icon><Clock /></el-icon>
            <span>班次列表</span>
          </div>
          <div class="shift-list">
            <div
              v-for="shift in shifts"
              :key="shift.id"
              class="shift-item"
              :class="{ active: currentShiftId === shift.id }"
              @click="selectShift(shift.id)"
            >
              <div class="shift-name">{{ shift.shiftName }}</div>
              <div class="shift-time">{{ shift.startTime }} - {{ shift.endTime }}</div>
              <div class="shift-count">
                <el-tag size="small" type="info">
                  {{ getShiftPersonCount(shift.id) }} 人
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：人员管理 -->
        <div class="right-panel">
          <div class="panel-header">
            <el-icon><User /></el-icon>
            <span>{{ getCurrentShiftName() }} - 人员管理</span>
          </div>

          <!-- 人员选择区 -->
          <div class="person-selector-section">
            <el-divider content-position="left">添加人员</el-divider>
            <div class="selector-layout">
              <!-- 部门树 -->
              <div class="dept-tree-box">
                <el-input
                  v-model="deptSearchKeyword"
                  placeholder="搜索部门"
                  clearable
                  prefix-icon="Search"
                  size="small"
                  class="search-input"
                />
                <el-tree
                  ref="deptTreeRef"
                  :data="departmentTree"
                  :props="{ label: 'deptName', children: 'children' }"
                  :filter-node-method="filterNode"
                  node-key="deptId"
                  highlight-current
                  default-expand-all
                  @node-click="handleDeptClick"
                  class="dept-tree"
                >
                  <template #default="{ node, data }">
                    <span class="custom-tree-node">
                      <el-icon><Folder /></el-icon>
                      <span class="node-label">{{ node.label }}</span>
                    </span>
                  </template>
                </el-tree>
              </div>

              <!-- 人员列表 -->
              <div class="person-list-box">
                <el-input
                  v-model="personSearchKeyword"
                  placeholder="搜索人员"
                  clearable
                  prefix-icon="Search"
                  size="small"
                  @input="handlePersonSearch"
                  class="search-input"
                />
                <div class="available-persons" v-loading="loadingPersons">
                  <div
                    v-for="person in filteredPersons"
                    :key="person.personId"
                    class="person-item"
                    @click="addPersonToCurrentShift(person)"
                  >
                    <div class="person-info">
                      <el-icon class="person-icon"><User /></el-icon>
                      <div class="person-details">
                        <div class="person-name">{{ person.personName }}</div>
                        <div class="person-dept">{{ person.deptName }}</div>
                      </div>
                    </div>
                    <el-icon class="add-icon"><Plus /></el-icon>
                  </div>
                  <el-empty
                    v-if="filteredPersons.length === 0"
                    description="暂无人员"
                    :image-size="60"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- 已选人员区 -->
          <div class="selected-persons-section">
            <el-divider content-position="left">
              已分配人员 ({{ getSelectedPersonsForCurrentShift().length }})
            </el-divider>
            <div class="selected-persons-list">
              <div
                v-for="person in getSelectedPersonsForCurrentShift()"
                :key="person.personId"
                class="selected-person-item"
              >
                <div class="person-info">
                  <el-icon class="person-icon"><User /></el-icon>
                  <div class="person-details">
                    <div class="person-name">{{ person.personName }}</div>
                    <div class="person-dept">{{ person.deptName }}</div>
                  </div>
                </div>
                <div class="person-actions">
                  <el-button
                    link
                    type="primary"
                    size="small"
                    @click="editPerson(person)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    link
                    type="danger"
                    size="small"
                    @click="removePersonFromCurrentShift(person.personId)"
                  >
                    移除
                  </el-button>
                </div>
              </div>
              <el-empty
                v-if="getSelectedPersonsForCurrentShift().length === 0"
                description="未分配人员"
                :image-size="60"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 备注 -->
      <div class="remark-section">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="2"
          placeholder="备注信息（选填）"
          maxlength="200"
          show-word-limit
        />
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        保存排班
      </el-button>
    </template>

    <!-- 编辑人员对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑人员信息"
      width="500px"
      append-to-body
    >
      <el-form :model="editFormData" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="editFormData.personName" disabled />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="editFormData.deptName" disabled />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="editFormData.personRemark"
            type="textarea"
            :rows="3"
            placeholder="该人员本次值班的备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="savePersonEdit">保存</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { scheduleApi, type Person, type DutySchedule, type Department } from '@/api/schedule'
import { type ShiftConfig } from '@/api/shift'

interface Props {
  modelValue: boolean
  date: string
  shifts: ShiftConfig[]
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const deptTreeRef = ref()
const submitting = ref(false)
const loadingPersons = ref(false)
const currentShiftId = ref<number>()
const showEditDialog = ref(false)

// 表单数据
const formData = ref({
  remark: ''
})

// 编辑人员数据
const editFormData = ref({
  personId: '',
  personName: '',
  deptName: '',
  personRemark: ''
})
const editingPerson = ref<Person | null>(null)

// 部门和人员数据
const departmentTree = ref<Department[]>([])
const currentDeptPersons = ref<Person[]>([])

// 搜索关键词
const deptSearchKeyword = ref('')
const personSearchKeyword = ref('')

// 已选人员（按班次分组）
const selectedPersonsByShift = reactive<Record<number, Person[]>>({})

// 人员备注（按人员ID）
const personRemarks = reactive<Record<string, string>>({})

// 初始化
watch(
  () => props.modelValue,
  async (val) => {
    if (val) {
      // 默认选择第一个班次
      if (props.shifts.length > 0) {
        currentShiftId.value = props.shifts[0].id
      }
      initializeShiftSelections()
      // 加载已有的排班数据
      await loadExistingSchedules()
      loadDepartmentTree()
    }
  }
)

// 初始化每个班次的选择列表
const initializeShiftSelections = () => {
  props.shifts.forEach(shift => {
    if (!selectedPersonsByShift[shift.id]) {
      selectedPersonsByShift[shift.id] = []
    }
  })
}

// 加载已有排班数据
const loadExistingSchedules = async () => {
  try {
    const data = await scheduleApi.listByDate(props.date)
    const existingSchedules = data as any[]

    // 清空现有数据
    props.shifts.forEach(shift => {
      selectedPersonsByShift[shift.id] = []
    })
    Object.keys(personRemarks).forEach(key => {
      delete personRemarks[key]
    })

    // 按班次分组填充数据
    existingSchedules.forEach(schedule => {
      const shiftId = schedule.shiftId
      if (!selectedPersonsByShift[shiftId]) {
        selectedPersonsByShift[shiftId] = []
      }

      // 构造人员对象
      const person: Person = {
        personId: schedule.personId,
        personName: schedule.personName,
        gender: schedule.personGender,
        deptId: schedule.deptId,
        deptName: schedule.deptName
      }

      // 添加到对应班次
      selectedPersonsByShift[shiftId].push(person)

      // 如果有备注，保存备注信息
      if (schedule.remark) {
        personRemarks[schedule.personId + '_' + shiftId] = schedule.remark
      }
    })

    console.log('已加载排班数据:', existingSchedules.length, '条')
  } catch (error) {
    console.error('加载已有排班数据失败:', error)
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  return dayjs(dateStr).format('YYYY年MM月DD日')
}

// 选择班次
const selectShift = (shiftId: number) => {
  currentShiftId.value = shiftId
}

// 获取当前班次名称
const getCurrentShiftName = () => {
  const shift = props.shifts.find(s => s.id === currentShiftId.value)
  return shift ? shift.shiftName : ''
}

// 获取班次人员数量
const getShiftPersonCount = (shiftId: number) => {
  return (selectedPersonsByShift[shiftId] || []).length
}

// 获取当前班次的已选人员
const getSelectedPersonsForCurrentShift = () => {
  return selectedPersonsByShift[currentShiftId.value!] || []
}

// 过滤部门树节点
const filterNode = (value: string, data: Department) => {
  if (!value) return true
  return data.deptName.includes(value)
}

// 监听部门搜索关键词
watch(deptSearchKeyword, (val) => {
  deptTreeRef.value?.filter(val)
})

// 处理部门点击
const handleDeptClick = (data: Department) => {
  loadDeptPersons(data.deptId)
}

// 加载部门人员
const loadDeptPersons = async (deptId: string) => {
  loadingPersons.value = true
  try {
    const data = await scheduleApi.getPersonsByDept(deptId)
    currentDeptPersons.value = (data as Person[]) || []
    personSearchKeyword.value = ''
  } catch (error) {
    console.error('加载部门人员失败:', error)
    ElMessage.error('加载部门人员失败')
    currentDeptPersons.value = []
  } finally {
    loadingPersons.value = false
  }
}

// 搜索人员
const handlePersonSearch = () => {
  // 过滤当前部门人员
}

// 过滤后的人员列表（排除已选）
const filteredPersons = computed(() => {
  let persons = currentDeptPersons.value

  if (personSearchKeyword.value) {
    const keyword = personSearchKeyword.value.toLowerCase()
    persons = persons.filter(person =>
      person.personName.toLowerCase().includes(keyword)
    )
  }

  // 排除当前班次已选的人员
  const currentSelected = selectedPersonsByShift[currentShiftId.value!] || []
  const selectedIds = new Set(currentSelected.map(p => p.personId))
  persons = persons.filter(p => !selectedIds.has(p.personId))

  return persons
})

// 添加人员到当前班次
const addPersonToCurrentShift = (person: Person) => {
  const shiftId = currentShiftId.value!
  if (!selectedPersonsByShift[shiftId]) {
    selectedPersonsByShift[shiftId] = []
  }

  // 检查是否已添加
  const exists = selectedPersonsByShift[shiftId].some(p => p.personId === person.personId)
  if (exists) {
    ElMessage.warning('该人员已在当前班次中')
    return
  }

  selectedPersonsByShift[shiftId].push(person)
  ElMessage.success(`已添加 ${person.personName} 到 ${getCurrentShiftName()}`)
}

// 从当前班次移除人员
const removePersonFromCurrentShift = (personId: string) => {
  const shiftId = currentShiftId.value!
  if (selectedPersonsByShift[shiftId]) {
    const index = selectedPersonsByShift[shiftId].findIndex(p => p.personId === personId)
    if (index > -1) {
      selectedPersonsByShift[shiftId].splice(index, 1)
      // 清除备注
      delete personRemarks[personId + '_' + shiftId]
      ElMessage.success('已移除')
    }
  }
}

// 编辑人员
const editPerson = (person: Person) => {
  const shiftId = currentShiftId.value!
  editingPerson.value = person
  editFormData.value = {
    personId: person.personId,
    personName: person.personName,
    deptName: person.deptName,
    personRemark: personRemarks[person.personId + '_' + shiftId] || ''
  }
  showEditDialog.value = true
}

// 保存人员编辑
const savePersonEdit = () => {
  if (editingPerson.value) {
    const shiftId = currentShiftId.value!
    personRemarks[editingPerson.value.personId + '_' + shiftId] = editFormData.value.personRemark
    ElMessage.success('保存成功')
    showEditDialog.value = false
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
  deptSearchKeyword.value = ''
  personSearchKeyword.value = ''
  currentDeptPersons.value = []
  props.shifts.forEach(shift => {
    selectedPersonsByShift[shift.id] = []
  })
  Object.keys(personRemarks).forEach(key => {
    delete personRemarks[key]
  })
}

// 提交表单
const handleSubmit = async () => {
  // 检查是否至少选择了一个班次的人员
  const totalPersons = Object.values(selectedPersonsByShift).reduce(
    (sum, persons) => sum + persons.length,
    0
  )

  if (totalPersons === 0) {
    ElMessage.warning('请至少为某个班次选择值班人员')
    return
  }

  submitting.value = true
  try {
    const schedules: DutySchedule[] = []

    // 遍历所有班次，生成排班数据
    props.shifts.forEach(shift => {
      const persons = selectedPersonsByShift[shift.id] || []
      persons.forEach(person => {
        schedules.push({
          dutyDate: props.date,
          shiftId: shift.id,
          personId: person.personId,
          personName: person.personName,
          personGender: person.gender,
          deptId: person.deptId,
          deptName: person.deptName,
          remark: personRemarks[person.personId + '_' + shift.id] || formData.value.remark
        })
      })
    })

    await scheduleApi.saveBatch(schedules)
    ElMessage.success(`成功添加 ${schedules.length} 条排班记录`)
    emit('success')
    handleClose()
  } catch (error: any) {
    console.error('提交失败:', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 加载部门树
const loadDepartmentTree = async () => {
  try {
    const data = await scheduleApi.getDepartmentTree()
    departmentTree.value = (data as Department[]) || []

    // 自动加载第一个部门的人员
    if (data && data.length > 0) {
      const firstDept = data[0]
      if (firstDept.children && firstDept.children.length > 0) {
        await loadDeptPersons(firstDept.children[0].deptId)
      } else {
        await loadDeptPersons(firstDept.deptId)
      }
    }
  } catch (error) {
    console.error('加载部门树失败:', error)
  }
}
</script>

<style scoped>
.dialog-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.date-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background-color: #ecf5ff;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

.date-text {
  font-size: 18px;
}

.two-column-layout {
  display: flex;
  gap: 16px;
  height: 600px;
}

.left-panel,
.right-panel {
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
}

.left-panel {
  width: 240px;
  flex-shrink: 0;
}

.right-panel {
  flex: 1;
}

.panel-header {
  padding: 12px 16px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.shift-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.shift-item {
  padding: 16px 12px;
  margin-bottom: 8px;
  border: 2px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fff;
}

.shift-item:hover {
  border-color: #c6e2ff;
  background-color: #f5f7fa;
}

.shift-item.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.shift-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  text-align: center;
  margin-bottom: 8px;
}

.shift-time {
  font-size: 13px;
  color: #606266;
  text-align: center;
  margin-bottom: 8px;
}

.shift-count {
  text-align: center;
}

.person-selector-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 12px;
}

.selector-layout {
  display: flex;
  gap: 12px;
  height: 100%;
}

.dept-tree-box {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 8px;
  background-color: #fafafa;
}

.person-list-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 8px;
  background-color: #fafafa;
}

.search-input {
  margin-bottom: 8px;
}

.dept-tree {
  flex: 1;
  overflow-y: auto;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.node-label {
  flex: 1;
  font-size: 14px;
}

.available-persons {
  flex: 1;
  overflow-y: auto;
}

.person-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.person-item:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.person-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.person-icon {
  font-size: 18px;
  color: #409eff;
}

.person-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.person-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.person-dept {
  font-size: 12px;
  color: #909399;
}

.add-icon {
  font-size: 18px;
  color: #67c23a;
}

.selected-persons-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 12px;
  min-height: 250px;
}

.selected-persons-list {
  flex: 1;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 8px;
  background-color: #fafafa;
}

.selected-person-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 6px;
}

.selected-person-item .person-info {
  flex: 1;
}

.person-actions {
  display: flex;
  gap: 8px;
}

.remark-section {
  padding: 12px 0;
}

:deep(.el-divider__text) {
  font-weight: bold;
  color: #409eff;
  font-size: 14px;
}

:deep(.el-tree-node__content) {
  height: 32px;
}

:deep(.el-tree-node__content:hover) {
  background-color: #f5f7fa;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #ecf5ff;
}
</style>
