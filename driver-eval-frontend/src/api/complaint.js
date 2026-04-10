import request from '../utils/request'

// Passenger
export function submitComplaint(data) {
  return request({ url: '/passenger/complaint/submit', method: 'post', data })
}
export function getMyComplaints(params) {
  return request({ url: '/passenger/complaint/list', method: 'get', params })
}

// Driver
export function getDriverComplaints(params) {
  return request({ url: '/driver/complaint/list', method: 'get', params })
}

// Admin
export function getAllComplaints(params) {
  return request({ url: '/admin/complaint/list', method: 'get', params })
}
export function reviewComplaint(data) {
  return request({ url: '/admin/complaint/review', method: 'post', data })
}
