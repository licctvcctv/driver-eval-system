import request from '../utils/request'

export function getWordList(params) {
  return request({ url: '/admin/sensitive-word/list', method: 'get', params })
}
export function saveWord(data) {
  return request({ url: '/admin/sensitive-word/save', method: 'post', data })
}
export function deleteWord(id) {
  return request({ url: `/admin/sensitive-word/${id}`, method: 'delete' })
}
