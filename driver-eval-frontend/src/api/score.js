import request from '../utils/request'

export function getDriverScoreList(params) {
  return request({ url: '/admin/score/list', method: 'get', params })
}
export function getDriverScoreDetail(id) {
  return request({ url: `/admin/score/detail/${id}`, method: 'get' })
}
