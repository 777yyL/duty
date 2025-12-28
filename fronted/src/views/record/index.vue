<template>
  <div class="record-container">
    <el-card>
      <!-- 搜索表单 -->
      <template #header>
        <el-form :inline="true" :model="queryForm" class="search-form">
          <el-form-item label="人员姓名">
            <el-input
              v-model="queryForm.personName"
              placeholder="请输入人员姓名"
              clearable
              style="width: 180px"
            />
          </el-form-item>
          <el-form-item label="值班日期">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 280px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><RefreshLeft /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </template>

      <!-- 数据表格 -->
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="dutyDate" label="值班日期" width="120" />
        <el-table-column prop="personName" label="姓名" width="100" />
        <el-table-column label="值班时间" width="180">
          <template #default="{ row }">
            {{ row.shiftStartTime }} - {{ row.shiftEndTime }}
          </template>
        </el-table-column>
        <el-table-column prop="workLogSummary" label="工作日志" min-width="300" show-overflow-tooltip />
        <el-table-column prop="recordTime" label="填写时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button link type="success" size="small" @click="handleExport(row)">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="值班记录详情" width="900px">
      <div v-loading="detailLoading">
        <div v-if="detailData">
          <el-descriptions :column="2" border class="info-section">
            <el-descriptions-item label="值班日期">
              {{ detailData.dutyDate }}
            </el-descriptions-item>
            <el-descriptions-item label="值班人员">
              {{ detailData.personName }}
            </el-descriptions-item>
            <el-descriptions-item label="值班时段">
              {{ detailData.shiftStartTime }} - {{ detailData.shiftEndTime }}
            </el-descriptions-item>
            <el-descriptions-item label="填写时间">
              {{ detailData.recordTime }}
            </el-descriptions-item>
          </el-descriptions>

          <el-divider content-position="left">工作日志</el-divider>

          <div class="detail-list">
            <div
              v-for="item in detailData.detailList"
              :key="item.id"
              class="detail-item"
            >
              <div class="detail-label">
                <el-tag type="primary" size="small">{{ item.categoryName }}</el-tag>
              </div>
              <div class="detail-content">
                {{ item.recordContent || '（未填写）' }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          type="primary"
          @click="handleExport(detailData)"
          :disabled="!detailData"
        >
          <el-icon><Download /></el-icon>
          导出Word
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { recordApi, type DutyRecord } from '@/api/record'

const loading = ref(false)
const tableData = ref<DutyRecord[]>([])

const queryForm = ref({
  personName: '',
  startDate: '',
  endDate: ''
})

const dateRange = ref<string[]>([])

const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<DutyRecord | null>(null)

// 计算查询参数
const queryParams = computed(() => {
  const params: any = {
    current: pagination.value.current,
    size: pagination.value.size
  }

  if (queryForm.value.personName) {
    params.personName = queryForm.value.personName
  }

  if (dateRange.value && dateRange.value.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }

  return params
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const result = await recordApi.page(queryParams.value)
    tableData.value = result.records || []
    pagination.value.total = result.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  queryForm.value = {
    personName: '',
    startDate: '',
    endDate: ''
  }
  dateRange.value = []
  pagination.value.current = 1
  loadData()
}

// 查看详情
const handleView = async (row: DutyRecord) => {
  detailVisible.value = true
  detailLoading.value = true

  try {
    const data = await recordApi.getDetailById(row.id!)
    detailData.value = data as DutyRecord
  } catch (error) {
    console.error('加载详情失败:', error)
    ElMessage.error('加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 导出Word
const handleExport = (row: DutyRecord | null) => {
  if (!row || !row.id) {
    ElMessage.warning('请选择要导出的记录')
    return
  }

  const url = recordApi.exportSingle(row.id)
  window.open(url, '_blank')
  ElMessage.success('正在导出，请稍候...')
}

// 删除
const handleDelete = (row: DutyRecord) => {
  ElMessageBox.confirm(
    `确定要删除${row.dutyDate} ${row.personName}的值班记录吗？`,
    '删除确认',
    {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }
  ).then(async () => {
    try {
      await recordApi.delete(row.id!)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.record-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.info-section {
  margin-bottom: 20px;
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.detail-label {
  flex-shrink: 0;
  min-width: 120px;
}

.detail-content {
  flex: 1;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-descriptions__label) {
  font-weight: bold;
}
</style>
