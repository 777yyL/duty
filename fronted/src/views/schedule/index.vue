<template>
  <div class="schedule-container">
    <el-card>
      <!-- 头部工具栏 -->
      <template #header>
        <div class="toolbar">
          <div class="left">
            <el-date-picker
              v-model="currentMonth"
              type="month"
              placeholder="选择月份"
              format="YYYY年MM月"
              value-format="YYYY-MM"
              @change="loadMonthSchedule"
              style="width: 150px"
            />
            <el-button-group class="ml-2">
              <el-button size="small" @click="changeMonth(-1)">
                <el-icon><ArrowLeft /></el-icon>
                上月
              </el-button>
              <el-button size="small" @click="changeMonth(0)">本月</el-button>
              <el-button size="small" @click="changeMonth(1)">
                下月
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </el-button-group>
          </div>
          <div class="right">
            <el-button type="primary" @click="openBatchScheduleDialog()">
              <el-icon><Plus /></el-icon>
              批量添加排班
            </el-button>
          </div>
        </div>
      </template>

      <!-- 日历视图 -->
      <div v-loading="loading" class="calendar-wrapper">
        <!-- 星期标题 -->
        <div class="calendar-header">
          <div v-for="week in weeks" :key="week" class="calendar-header-item">
            {{ week }}
          </div>
        </div>

        <!-- 日期网格 -->
        <div class="calendar-grid">
          <div
            v-for="(day, index) in calendarDays"
            :key="index"
            :class="[
              'calendar-day',
              { 'other-month': !day.isCurrentMonth },
              { today: day.isToday }
            ]"
          >
            <div class="day-header">
              <span class="day-number">{{ day.day }}</span>
              <span v-if="day.isToday" class="today-badge">今天</span>
              <el-button
                v-if="day.isCurrentMonth"
                type="primary"
                size="small"
                class="day-schedule-btn"
                @click="openDayScheduleDialog(day)"
              >
                <el-icon><Plus /></el-icon>
                日排班
              </el-button>
            </div>

            <div class="day-content">
              <div
                v-for="shift in shifts"
                :key="shift.id"
                class="shift-section"
              >
                <div class="shift-title">
                  {{ shift.shiftName }}
                </div>
                <div class="schedule-list">
                  <div
                    v-for="schedule in getSchedulesForDayAndShift(
                      day.date,
                      shift.id
                    )"
                    :key="schedule.id"
                    class="schedule-item"
                    :class="{
                      cancelled: schedule.status === 0,
                      'has-record': schedule.hasRecord
                    }"
                    @click="handlePersonClick(schedule)"
                  >
                    <div class="person-info">
                      <el-icon class="person-icon"><User /></el-icon>
                      <span class="person-name-large">{{ schedule.personName }}</span>
                      <el-tag
                        v-if="schedule.hasRecord"
                        size="small"
                        type="success"
                        class="record-tag"
                      >
                        已填写
                      </el-tag>
                    </div>
                    <div class="click-hint">
                      <el-icon><ArrowRight /></el-icon>
                    </div>
                  </div>
                  <div v-if="!hasSchedule(day.date, shift.id)" class="no-schedule">
                    <span class="no-data-tip">暂无排班</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 日排班对话框（新增） -->
    <DayScheduleDialog
      v-model="showDayScheduleDialog"
      :date="selectedDate"
      :shifts="shifts"
      @success="loadMonthSchedule"
    />

    <!-- 批量添加排班对话框 -->
    <ScheduleDialog
      v-model="showScheduleDialog"
      :shifts="shifts"
      @success="loadMonthSchedule"
    />


    <!-- 值班记录填写对话框 -->
    <RecordDialog
      v-model="showRecordDialog"
      :schedule-id="currentScheduleId"
      @success="loadMonthSchedule"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import weekday from 'dayjs/plugin/weekday'
import { scheduleApi, type DutySchedule } from '@/api/schedule'
import { shiftApi, type ShiftConfig } from '@/api/shift'
import ScheduleDialog from './components/ScheduleDialog.vue'
import DayScheduleDialog from './components/DayScheduleDialog.vue'
import RecordDialog from './components/RecordDialog.vue'

// 扩展dayjs支持星期
dayjs.extend(weekday)

// 修改为从周一开始：一、二、三、四、五、六、日
const weeks = ['一', '二', '三', '四', '五', '六', '日']
const currentMonth = ref(dayjs().format('YYYY-MM'))
const loading = ref(false)
const scheduleData = ref<any>({})
const shifts = ref<ShiftConfig[]>([])
const showScheduleDialog = ref(false)
const showDayScheduleDialog = ref(false)
const showRecordDialog = ref(false)
const currentScheduleId = ref<number>()

// 日排班相关
const selectedDate = ref('')

// 日历天数 - 从周一开始
const calendarDays = computed(() => {
  const date = dayjs(currentMonth.value)
  const year = date.year()
  const month = date.month()
  const today = dayjs()

  // 当月第一天
  const firstDay = dayjs().year(year).month(month).date(1)
  // 当月最后一天
  const lastDay = dayjs().year(year).month(month).date(firstDay.daysInMonth())

  // 获取当月第一天是星期几（1=周一，7=周日）
  const firstDayOfWeek = firstDay.day() || 7

  // 日历开始日期：从当月第一天往前推到周一
  const startDate = firstDay.subtract(firstDayOfWeek - 1, 'day')

  // 计算需要显示多少天（6周，保证完整显示）
  const totalDays = 6 * 7 // 6周，每周7天
  const endDate = startDate.add(totalDays - 1, 'day')

  const days = []
  let current = startDate

  for (let i = 0; i < totalDays; i++) {
    days.push({
      date: current.format('YYYY-MM-DD'),
      day: current.date(),
      isCurrentMonth: current.month() === month,
      isToday: current.isSame(today, 'day')
    })
    current = current.add(1, 'day')
  }

  return days
})

// 加载月度排班数据
const loadMonthSchedule = async () => {
  loading.value = true
  try {
    const [year, month] = currentMonth.value.split('-').map(Number)
    const data = await scheduleApi.getMonthSchedule(year, month)
    scheduleData.value = data.schedules || {}
    shifts.value = data.shifts || []
  } catch (error) {
    console.error('加载排班数据失败:', error)
    ElMessage.error('加载排班数据失败')
  } finally {
    loading.value = false
  }
}

// 切换月份
const changeMonth = (offset: number) => {
  const date = dayjs(currentMonth.value).add(offset, 'month')
  currentMonth.value = date.format('YYYY-MM')
  loadMonthSchedule()
}

// 获取指定日期和班次的排班列表
const getSchedulesForDayAndShift = (date: string, shiftId: number) => {
  return scheduleData.value[date]?.filter((s: DutySchedule) => s.shiftId === shiftId) || []
}

// 判断是否有排班
const hasSchedule = (date: string, shiftId: number) => {
  const schedules = scheduleData.value[date] || []
  return schedules.some((s: DutySchedule) => s.shiftId === shiftId && s.status === 1)
}

// 打开日排班对话框（新功能）
const openDayScheduleDialog = (day: any) => {
  selectedDate.value = day.date
  showDayScheduleDialog.value = true
}

// 打开批量排班对话框
const openBatchScheduleDialog = () => {
  showScheduleDialog.value = true
}

// 点击人员时的操作
const handlePersonClick = (schedule: DutySchedule) => {
  // 直接打开记录对话框（填写或查看）
  openRecordDialog(schedule)
}

// 打开记录填写对话框
const openRecordDialog = (schedule: DutySchedule) => {
  currentScheduleId.value = schedule.id
  showRecordDialog.value = true
}

onMounted(() => {
  loadMonthSchedule()
})
</script>

<style scoped>
.schedule-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar .left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ml-2 {
  margin-left: 8px;
}

.ml-1 {
  margin-left: 4px;
}

/* 日历样式 */
.calendar-wrapper {
  min-height: 600px;
}

.calendar-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background-color: #dcdfe6;
  border: 1px solid #dcdfe6;
}

.calendar-header-item {
  padding: 12px;
  text-align: center;
  font-weight: bold;
  background-color: #f5f7fa;
  color: #303133;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background-color: #dcdfe6;
  border: 1px solid #dcdfe6;
  border-top: none;
}

.calendar-day {
  background-color: #fff;
  min-height: 120px;
  display: flex;
  flex-direction: column;
}

.calendar-day.other-month {
  background-color: #fafafa;
}

.calendar-day.today {
  background-color: #ecf5ff;
}

.day-header {
  padding: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
}

.day-number {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.today-badge {
  font-size: 12px;
  padding: 2px 6px;
  background-color: #409eff;
  color: #fff;
  border-radius: 4px;
}

.day-schedule-btn {
  font-size: 12px;
  padding: 2px 8px;
}

.day-content {
  flex: 1;
  padding: 8px;
  overflow-y: auto;
}

.shift-section {
  margin-bottom: 8px;
}

.shift-title {
  font-size: 12px;
  font-weight: bold;
  color: #606266;
  margin-bottom: 4px;
  padding-left: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.add-person-btn {
  font-size: 14px;
  padding: 2px;
  opacity: 0.6;
  transition: opacity 0.3s;
}

.add-person-btn:hover {
  opacity: 1;
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.schedule-item {
  padding: 10px 12px;
  background-color: #f0f9ff;
  border-left: 4px solid #409eff;
  border-radius: 6px;
  font-size: 12px;
  transition: all 0.3s;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 48px;
}

.schedule-item:hover {
  background-color: #e1f3ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
  transform: translateY(-1px);
}

.schedule-item.cancelled {
  background-color: #fef0f0;
  border-left-color: #f56c6c;
  opacity: 0.7;
}

.schedule-item.has-record {
  background-color: #f0f9ff;
  border-left-color: #67c23a;
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

/* 人员名称增大显示 */
.person-name-large {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  letter-spacing: 0.5px;
}

.record-tag {
  margin-left: 8px;
}

.click-hint {
  color: #909399;
  font-size: 16px;
  opacity: 0;
  transition: opacity 0.3s;
}

.schedule-item:hover .click-hint {
  opacity: 1;
}

.no-schedule {
  padding: 12px 0;
  text-align: center;
  min-height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-data-tip {
  font-size: 13px;
  color: #c0c4cc;
  font-style: italic;
}

:deep(.el-button--small) {
  padding: 2px 8px;
}
</style>
