import request from '../utils/request'

export function getPunishList(params) {
  return request({ url: '/admin/punish/list', method: 'get', params })
}

export function manualPunish(data) {
  return request({ url: '/admin/punish/manual', method: 'post', data })
}

export function liftPunish(data) {
  return request({ url: '/admin/punish/lift', method: 'post', data })
}
