import request from '../utils/request'

// Driver
export function submitAppeal(data) {
  return request({ url: '/driver/appeal/submit', method: 'post', data })
}
export function getDriverAppeals(params) {
  return request({ url: '/driver/appeal/list', method: 'get', params })
}

// Admin
export function getAllAppeals(params) {
  return request({ url: '/admin/appeal/list', method: 'get', params })
}
export function reviewAppeal(data) {
  return request({ url: '/admin/appeal/review', method: 'post', data })
}
