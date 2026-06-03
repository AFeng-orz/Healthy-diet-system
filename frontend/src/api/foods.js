import request from './index'

export function getFoods(params) {
  return request.get('/foods', { params })
}

export function createFood(data) {
  return request.post('/foods', data)
}

export function updateFood(id, data) {
  return request.put(`/foods/${id}`, data)
}

export function deleteFood(id) {
  return request.delete(`/foods/${id}`)
}

export function importFoods(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/foods/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 60000
  })
}
