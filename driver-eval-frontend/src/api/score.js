import request from '../utils/request'

export function getDriverScoreList(params) {
  return request({ url: '/score/list', method: 'get', params })
}

export function getDriverScoreDetail(id) {
  return request({ url: `/score/detail/${id}`, method: 'get' })
}
