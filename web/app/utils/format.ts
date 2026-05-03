import { UserStatusLabel, GenderLabel, ClientTypeLabel, PermissionTypeLabel, OrgMemberStatusLabel } from './constants'

export function formatDate(date?: string | Date): string {
  if (!date) return '-'
  const d = typeof date === 'string' ? new Date(date) : date
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

export function formatDateTime(date?: string | Date): string {
  if (!date) return '-'
  const d = typeof date === 'string' ? new Date(date) : date
  return d.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
  })
}

export function formatUserStatus(status: number): string {
  return UserStatusLabel[status] ?? '未知'
}

export function formatGender(gender: number): string {
  return GenderLabel[gender] ?? '未知'
}

export function formatClientType(type: number): string {
  return ClientTypeLabel[type] ?? '未知'
}

export function formatPermissionType(type: number): string {
  return PermissionTypeLabel[type] ?? '未知'
}

export function formatOrgMemberStatus(status: number): string {
  return OrgMemberStatusLabel[status] ?? '未知'
}

export function truncate(str: string, length: number): string {
  if (str.length <= length) return str
  return str.slice(0, length) + '...'
}
