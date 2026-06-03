import request from './index'

export function getHealthQuestions() {
  return request.get('/health/questions')
}

export function submitHealthAssessment(data) {
  return request.post('/health/assessment', data)
}

export function getLatestHealthAssessment() {
  return request.get('/health/assessment/latest')
}