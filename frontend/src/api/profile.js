import request from './index'

export function getProfile() {
  return request.get('/profile')
}

export function saveProfile(data) {
  return request.put('/profile', data)
}

export function getProfileMetrics() {
  return request.get('/profile/metrics')
}