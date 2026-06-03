import request from './index'

export function generateDietPlan() {
  return request.post('/diet-plans/generate')
}

export function getLatestDietPlan() {
  return request.get('/diet-plans/latest')
}

export function getDietPlan(id) {
  return request.get(`/diet-plans/${id}`)
}
