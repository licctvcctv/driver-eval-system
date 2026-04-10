import request from '../utils/request'

export function createOrder(data) {
  return request({ url: '/order/create', method: 'post', data })
}

export function getOrders(params) {
  return request({ url: '/order/list', method: 'get', params })
}

export function cancelOrder(id) {
  return request({ url: `/order/cancel/${id}`, method: 'put' })
}

export function completeOrder(id) {
  return request({ url: `/order/complete/${id}`, method: 'put' })
}

export function getDispatchOrders(params) {
  return request({ url: '/order/dispatch', method: 'get', params })
}

export function getDriverCompletedOrders(params) {
  return request({ url: '/order/driver/completed', method: 'get', params })
}

export function getDriverCancelledOrders(params) {
  return request({ url: '/order/driver/cancelled', method: 'get', params })
}

export function getAllOrders(params) {
  return request({ url: '/order/all', method: 'get', params })
}

export function evaluatePassenger(data) {
  return request({ url: '/driver/order/evaluate-passenger', method: 'post', data })
}
