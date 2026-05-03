export interface Role {
  id: number
  code: string
  name: string
  description?: string
  orgId?: number
  isSystem?: boolean
  sortOrder?: number
  status: 0 | 1
  createdAt?: string
}

export interface PermissionNode {
  id: number
  code: string
  name: string
  description?: string
  type: 1 | 2 | 3
  parentId?: number
  path?: string
  icon?: string
  apiPattern?: string
  sortOrder?: number
  children?: PermissionNode[]
}
