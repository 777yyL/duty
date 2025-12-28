import request from '@/utils/request'

export interface Person {
  personId: string
  personName: string
  gender: string
  deptId: string
  deptName: string
  phone?: string
  position?: string
}

export interface Department {
  deptId: string
  deptName: string
  parentId?: string
  children?: Department[]
}

export interface DutySchedule {
  id?: number
  dutyDate: string
  shiftId: number
  shiftName?: string
  shiftStartTime?: string
  shiftEndTime?: string
  personId: string
  personName: string
  personGender?: string
  deptId?: string
  deptName?: string
  status?: number
  remark?: string
  hasRecord?: boolean
}

export const scheduleApi = {
  // 分页查询排班列表
  page(params: any) {
    return request({
      url: '/schedule/page',
      method: 'get',
      params
    })
  },

  // 获取月度排班表
  getMonthSchedule(year: number, month: number) {
    return request({
      url: `/schedule/month/${year}/${month}`,
      method: 'get'
    })
  },

  // 获取指定日期的排班列表
  listByDate(date: string) {
    return request({
      url: `/schedule/date/${date}`,
      method: 'get'
    })
  },

  // 根据ID查询排班
  getById(id: number) {
    return request({
      url: `/schedule/${id}`,
      method: 'get'
    })
  },

  // 批量添加排班
  saveBatch(data: DutySchedule[]) {
    return request({
      url: '/schedule/batch',
      method: 'post',
      data
    })
  },

  // 更新排班
  update(data: DutySchedule) {
    return request({
      url: '/schedule',
      method: 'put',
      data
    })
  },

  // 删除排班
  delete(id: number) {
    return request({
      url: `/schedule/${id}`,
      method: 'delete'
    })
  },

  // 取消排班
  cancel(id: number) {
    return request({
      url: `/schedule/cancel/${id}`,
      method: 'put'
    })
  },

  // 搜索人员
  searchPersons(keyword?: string) {
    return request({
      url: '/schedule/person/search',
      method: 'get',
      params: { keyword }
    })
  },

  // 获取部门树
  getDepartmentTree() {
    return request({
      url: '/schedule/departments',
      method: 'get'
    })
  },

  // 根据部门ID获取人员列表
  getPersonsByDept(deptId: string, keyword?: string) {
    return request({
      url: '/schedule/person/department',
      method: 'get',
      params: { deptId, keyword }
    })
  }
}
