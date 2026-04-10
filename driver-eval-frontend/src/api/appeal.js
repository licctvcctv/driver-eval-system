import request from '../utils/request'

export function submitAppeal(data) {
  return request({ url: '/appeal/submit', method: 'post', data })
}

export function getDriverAppeals(params) {
  return request({ url: '/appeal/driver', method: 'get', params })
}

export function getAllAppeals(params) {
  return request({ url: '/appeal/all', method: 'get', params })
}

export function reviewAppeal(data) {
  return request({ url: '/appeal/review', method: 'post', data })
}
