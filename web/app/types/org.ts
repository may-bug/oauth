export interface Organization {
  id: number
  name: string
  code: string
  parentId?: number
  status: 0 | 1
  sortOrder?: number
  children?: Organization[]
}
