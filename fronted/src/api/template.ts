import request from '@/utils/request'

export interface RecordTemplateConfig {
  id?: number
  categoryName: string
  categoryCode: string
  fieldDescription?: string
  defaultValue?: string
  inputType: string
  options?: string
  isRequired?: number
  sortOrder?: number
  status?: number
  remark?: string
}

export const templateApi = {
  // 获取所有启用的模板配置
  listEnabled() {
    return request({
      url: '/template/list/enabled',
      method: 'get'
    })
  },

  // 根据ID查询模板配置
  getById(id: number) {
    return request({
      url: `/template/${id}`,
      method: 'get'
    })
  },

  // 新增模板配置
  save(data: RecordTemplateConfig) {
    return request({
      url: '/template',
      method: 'post',
      data
    })
  },

  // 更新模板配置
  update(data: RecordTemplateConfig) {
    return request({
      url: '/template',
      method: 'put',
      data
    })
  },

  // 删除模板配置
  delete(id: number) {
    return request({
      url: `/template/${id}`,
      method: 'delete'
    })
  }
}
