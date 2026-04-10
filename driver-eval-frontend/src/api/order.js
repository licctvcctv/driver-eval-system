import request from '../utils/request'

// Passenger
export function createOrder(data) {
  return request({ url: '/passenger/order/create', method: 'post', data })
}
export function getOrders(params) {
  return request({ url: '/passenger/order/list', method: 'get', params })
}
export function cancelOrder(id, data) {
  return request({ url: `/passenger/order/cancel/${id}`, method: 'post', data })
}

// Driver
export function getDispatchOrders(params) {
  return request({ url: '/driver/order/dispatch', method: 'get', params })
}
export function getDriverCompletedOrders(params) {
  return request({ url: '/driver/order/completed', method: 'get', params })
}
export function getDriverCancelledOrders(params) {
  return request({ url: '/driver/order/cancelled', method: 'get', params })
}
export function completeOrder(id) {
  return request({ url: `/driver/order/complete/${id}`, method: 'post' })
}
export function cancelDriverOrder(id, data) {
  return request({ url: `/driver/order/cancel/${id}`, method: 'post', data })
}
export function evaluatePassenger(data) {
  return request({ url: '/driver/order/evaluate-passenger', method: 'post', data })
}

// Admin
export function getAllOrders(params) {
  return request({ url: '/admin/order/list', method: 'get', params })
}
