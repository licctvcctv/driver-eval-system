import request from '../utils/request'

export function getPunishList(params) {
  return request({ url: '/admin/punish/list', method: 'get', params })
}
