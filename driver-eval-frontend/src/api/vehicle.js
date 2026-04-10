import request from '../utils/request'

// Driver
export function getMyVehicle() {
  return request({ url: '/driver/vehicle/info', method: 'get' })
}
export function saveVehicle(data) {
  return request({ url: '/driver/vehicle/save', method: 'post', data })
}

// Admin
export function getVehicleList(params) {
  return request({ url: '/admin/vehicle/list', method: 'get', params })
}
export function getVehicleTypes() {
  return request({ url: '/admin/vehicle/types', method: 'get' })
}
export function saveVehicleType(data) {
  return request({ url: '/admin/vehicle/type/save', method: 'post', data })
}
export function deleteVehicleType(id) {
  return request({ url: `/admin/vehicle/type/${id}`, method: 'delete' })
}
