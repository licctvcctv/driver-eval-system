import request from '../utils/request'

export function getUserList(params) {
  return request({ url: '/user/list', method: 'get', params })
}

export function toggleStatus(id) {
  return request({ url: `/user/toggle-status/${id}`, method: 'put' })
}

export function getDriverProfile() {
  return request({ url: '/user/driver/profile', method: 'get' })
}

export function goOnline() {
  return request({ url: '/user/driver/online', method: 'put' })
}

export function goOffline() {
  return request({ url: '/user/driver/offline', method: 'put' })
}

export function updateProfile(data) {
  return request({ url: '/user/update', method: 'put', data })
}
