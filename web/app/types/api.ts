export interface ApiResponse<T = unknown> {
  code?: number
  message?: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export interface ApiError {
  code: number
  message: string
  status: number
}
