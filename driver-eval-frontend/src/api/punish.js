import request from '../utils/request'

export function getPunishList(params) {
  return request({ url: '/punish/list', method: 'get', params })
}
