import request from '../utils/request'

export function submitComplaint(data) {
  return request({ url: '/complaint/submit', method: 'post', data })
}

export function getMyComplaints(params) {
  return request({ url: '/complaint/my', method: 'get', params })
}

export function getDriverComplaints(params) {
  return request({ url: '/complaint/driver', method: 'get', params })
}

export function getAllComplaints(params) {
  return request({ url: '/complaint/all', method: 'get', params })
}

export function reviewComplaint(data) {
  return request({ url: '/complaint/review', method: 'post', data })
}
