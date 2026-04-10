import request from '../utils/request'

export function submitEval(data) {
  return request({ url: '/eval/submit', method: 'post', data })
}

export function getMyEvals(params) {
  return request({ url: '/eval/my', method: 'get', params })
}

export function getDriverEvals(params) {
  return request({ url: '/eval/driver', method: 'get', params })
}

export function replyEval(data) {
  return request({ url: '/eval/reply', method: 'post', data })
}

export function getTagStats(params) {
  return request({ url: '/eval/tag-stats', method: 'get', params })
}

export function getStarStats(params) {
  return request({ url: '/eval/star-stats', method: 'get', params })
}

export function getAllEvals(params) {
  return request({ url: '/eval/all', method: 'get', params })
}

export function getAllTagStats(params) {
  return request({ url: '/eval/all-tag-stats', method: 'get', params })
}
