import request from '../utils/request'

// Passenger
export function submitEval(data) {
  return request({ url: '/passenger/eval/submit', method: 'post', data })
}
export function getMyEvals(params) {
  return request({ url: '/passenger/eval/list', method: 'get', params })
}

// Driver
export function getDriverEvals(params) {
  return request({ url: '/driver/eval/list', method: 'get', params })
}
export function replyEval(data) {
  return request({ url: '/driver/eval/reply', method: 'post', data })
}
export function getTagStats() {
  return request({ url: '/driver/eval/tag-stats', method: 'get' })
}
export function getStarStats() {
  return request({ url: '/driver/eval/star-stats', method: 'get' })
}

// Admin
export function getAllEvals(params) {
  return request({ url: '/admin/eval/list', method: 'get', params })
}
export function getAllTagStats() {
  return request({ url: '/admin/eval/tag-stats', method: 'get' })
}
