import request from '../utils/request'

export function getTagList() {
  return request({ url: '/admin/tag/list', method: 'get' })
}
export function saveTag(data) {
  return request({ url: '/admin/tag/save', method: 'post', data })
}
export function deleteTag(id) {
  return request({ url: `/admin/tag/${id}`, method: 'delete' })
}
