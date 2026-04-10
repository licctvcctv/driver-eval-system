import request from '../utils/request'

export function getAnnouncementList(params) {
  return request({ url: '/admin/announcement/list', method: 'get', params })
}
export function saveAnnouncement(data) {
  return request({ url: '/admin/announcement/save', method: 'post', data })
}
export function deleteAnnouncement(id) {
  return request({ url: `/admin/announcement/${id}`, method: 'delete' })
}
