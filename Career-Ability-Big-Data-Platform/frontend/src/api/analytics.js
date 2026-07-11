import request from '../utils/request.js'

const dataOf = (response) => response.data

export const getOverview = () => request.get('/stats/overview').then(dataOf)
export const getDashboard = () => request.get('/dashboard/all').then(dataOf)
export const getPositionStats = () => request.get('/stats/positions').then(dataOf)
export const getSalaryStats = () => request.get('/stats/salary').then(dataOf)
export const getSkillStats = () => request.get('/stats/skills').then(dataOf)
export const getEducationStats = () => request.get('/stats/education').then(dataOf)
export const getCityStats = () => request.get('/stats/city').then(dataOf)
export const getCompanyStats = () => request.get('/stats/company').then(dataOf)
export const getTrendStats = () => request.get('/stats/trends').then(dataOf)
