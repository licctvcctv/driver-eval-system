import request from '../utils/request'

// Admin
export function getUserList(params) {
  return request({ url: '/admin/user/list', method: 'get', params })
}
export function toggleStatus(data) {
  return request({ url: '/admin/user/toggle-status', method: 'post', data })
}
export function updateUser(data) {
  return request({ url: '/admin/user/update', method: 'post', data })
}
export function deleteUser(id) {
  return request({ url: `/admin/user/${id}`, method: 'delete' })
}

// Passenger
export function getPassengerProfile() {
  return request({ url: '/passenger/profile/info', method: 'get' })
}
export function updatePassengerProfile(data) {
  return request({ url: '/passenger/profile/update', method: 'post', data })
}

// Driver
export function getDriverProfile() {
  return request({ url: '/driver/profile/info', method: 'get' })
}
export function goOnline() {
  return request({ url: '/driver/profile/online', method: 'post' })
}
export function goOffline() {
  return request({ url: '/driver/profile/offline', method: 'post' })
}
export function updateProfile(data) {
  return request({ url: '/driver/profile/update', method: 'post', data })
}
