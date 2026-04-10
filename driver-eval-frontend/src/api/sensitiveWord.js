import request from '../utils/request'

export function getWordList(params) {
  return request({ url: '/sensitive-word/list', method: 'get', params })
}

export function saveWord(data) {
  return request({ url: '/sensitive-word/save', method: 'post', data })
}

export function deleteWord(id) {
  return request({ url: `/sensitive-word/delete/${id}`, method: 'delete' })
}
