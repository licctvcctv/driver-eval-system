import request from '../utils/request'

export function getTagList(params) {
  return request({ url: '/tag/list', method: 'get', params })
}

export function saveTag(data) {
  return request({ url: '/tag/save', method: 'post', data })
}

export function deleteTag(id) {
  return request({ url: `/tag/delete/${id}`, method: 'delete' })
}
