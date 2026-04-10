import request from '../utils/request'

export function getAnnouncementList(params) {
  return request({ url: '/announcement/list', method: 'get', params })
}

export function saveAnnouncement(data) {
  return request({ url: '/announcement/save', method: 'post', data })
}

export function deleteAnnouncement(id) {
  return request({ url: `/announcement/delete/${id}`, method: 'delete' })
}
