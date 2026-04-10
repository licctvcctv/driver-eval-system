import request from '../utils/request'

export function upload(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/common/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getAnnouncements(params) {
  return request({ url: '/common/announcements', method: 'get', params })
}

export function getCommonVehicleTypes() {
  return request({ url: '/common/vehicle-types', method: 'get' })
}

export function getCommonTagList() {
  return request({ url: '/common/tags', method: 'get' })
}

export function checkSensitive(text) {
  return request({ url: '/common/check-sensitive', method: 'post', data: { text } })
}
