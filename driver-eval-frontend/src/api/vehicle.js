import request from '../utils/request'

export function getMyVehicle() {
  return request({ url: '/vehicle/my', method: 'get' })
}

export function saveVehicle(data) {
  return request({ url: '/vehicle/save', method: 'post', data })
}

export function getVehicleList(params) {
  return request({ url: '/vehicle/list', method: 'get', params })
}

export function getVehicleTypes(params) {
  return request({ url: '/vehicle/types', method: 'get', params })
}

export function saveVehicleType(data) {
  return request({ url: '/vehicle/type/save', method: 'post', data })
}

export function deleteVehicleType(id) {
  return request({ url: `/vehicle/type/delete/${id}`, method: 'delete' })
}
