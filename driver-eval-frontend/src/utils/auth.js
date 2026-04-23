const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getUserInfo() {
  const info = localStorage.getItem(USER_INFO_KEY)
  return info ? JSON.parse(info) : null
}

export function setUserInfo(info) {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
  window.dispatchEvent(new CustomEvent('user-info-updated', { detail: info }))
}

export function removeUserInfo() {
  localStorage.removeItem(USER_INFO_KEY)
  window.dispatchEvent(new CustomEvent('user-info-updated', { detail: null }))
}

export function isLoggedIn() {
  return !!getToken()
}
