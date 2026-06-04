import request from './index'

export function getDailyDietRecords(params) {
  return request.get('/diet-records/daily', { params })
}

export function createDietRecord(data) {
  return request.post('/diet-records', data)
}

export function deleteDietRecord(id) {
  return request.delete(`/diet-records/${id}`)
}
