import request from '@/utils/request'

export interface RecordTemplate {
  id: number
  categoryName: string
  categoryCode: string
  fieldDescription?: string
  defaultValue?: string
  inputType: string
  options?: string
  isRequired: number
  sortOrder: number
}

export interface RecordDetail {
  templateId: number
  recordContent?: string
}

export interface DutyRecord {
  id?: number
  scheduleId?: number
  dutyDate: string
  shiftId?: number
  shiftName?: string
  shiftStartTime?: string
  shiftEndTime?: string
  personId?: string
  personName?: string
  recordTime?: string
  workLogSummary?: string
  detailList?: RecordDetail[]
  status?: number
}

export const recordApi = {
  // 获取所有启用的记录模板
  getTemplateList() {
    return request({
      url: '/template/list/enabled',
      method: 'get'
    })
  },

  // 分页查询值班记录列表
  page(params: any) {
    return request({
      url: '/record/page',
      method: 'get',
      params
    })
  },

  // 根据ID查询值班记录详情
  getDetailById(id: number) {
    return request({
      url: `/record/${id}`,
      method: 'get'
    })
  },

  // 根据排班ID查询值班记录
  getByScheduleId(scheduleId: number) {
    return request({
      url: `/record/schedule/${scheduleId}`,
      method: 'get'
    })
  },

  // 保存值班记录
  save(data: { scheduleId: number; details: RecordDetail[] }) {
    return request({
      url: '/record',
      method: 'post',
      data
    })
  },

  // 更新值班记录
  update(id: number, data: { scheduleId: number; details: RecordDetail[] }) {
    return request({
      url: `/record/${id}`,
      method: 'put',
      data
    })
  },

  // 删除值班记录
  delete(id: number) {
    return request({
      url: `/record/${id}`,
      method: 'delete'
    })
  },

  // 导出单条记录
  exportSingle(recordId: number) {
    return `/api/export/record/${recordId}`
  },

  // 导出个人记录
  exportPerson(personId: string, startDate: string, endDate: string) {
    return `/api/export/person/${personId}?startDate=${startDate}&endDate=${endDate}`
  }
}
