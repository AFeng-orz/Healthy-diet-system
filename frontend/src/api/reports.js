import request from './index'

export function getWeeklyReport() {
  return request.get('/reports/weekly')
}
