import request from '@/utils/request'

export interface ShiftConfig {
  id?: number
  shiftName: string
  shiftCode: string
  startTime: string
  endTime: string
  sortOrder?: number
  status?: number
  remark?: string
}

export const shiftApi = {
  // 获取所有启用的班次
  listEnabled() {
    return request({
      url: '/shift/list/enabled',
      method: 'get'
    })
  },

  // 分页查询班次列表
  page(params: any) {
    return request({
      url: '/shift/page',
      method: 'get',
      params
    })
  },

  // 根据ID查询班次
  getById(id: number) {
    return request({
      url: `/shift/${id}`,
      method: 'get'
    })
  },

  // 新增班次
  save(data: ShiftConfig) {
    return request({
      url: '/shift',
      method: 'post',
      data
    })
  },

  // 更新班次
  update(data: ShiftConfig) {
    return request({
      url: '/shift',
      method: 'put',
      data
    })
  },

  // 删除班次
  delete(id: number) {
    return request({
      url: `/shift/${id}`,
      method: 'delete'
    })
  }
}
