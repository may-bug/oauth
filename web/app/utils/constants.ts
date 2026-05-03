export const UserStatus = {
  DISABLED: 0,
  ACTIVE: 1,
  LOCKED: 2,
} as const

export const UserStatusLabel: Record<number, string> = {
  [UserStatus.DISABLED]: '禁用',
  [UserStatus.ACTIVE]: '正常',
  [UserStatus.LOCKED]: '锁定',
}

export const Gender = {
  UNKNOWN: 0,
  MALE: 1,
  FEMALE: 2,
} as const

export const GenderLabel: Record<number, string> = {
  [Gender.UNKNOWN]: '未知',
  [Gender.MALE]: '男',
  [Gender.FEMALE]: '女',
}

export const ClientType = {
  PUBLIC: 1,
  CONFIDENTIAL: 2,
  BEARER_ONLY: 3,
} as const

export const ClientTypeLabel: Record<number, string> = {
  [ClientType.PUBLIC]: '公开',
  [ClientType.CONFIDENTIAL]: '机密',
  [ClientType.BEARER_ONLY]: '仅Bearer',
}

export const PermissionType = {
  MENU: 1,
  BUTTON: 2,
  API: 3,
} as const

export const PermissionTypeLabel: Record<number, string> = {
  [PermissionType.MENU]: '菜单',
  [PermissionType.BUTTON]: '按钮',
  [PermissionType.API]: 'API',
}

export const OrgMemberStatus = {
  PENDING: 0,
  ACTIVE: 1,
  REJECTED: 2,
} as const

export const OrgMemberStatusLabel: Record<number, string> = {
  [OrgMemberStatus.PENDING]: '待审核',
  [OrgMemberStatus.ACTIVE]: '已通过',
  [OrgMemberStatus.REJECTED]: '已拒绝',
}

export const SocialProviders = ['github', 'gitee', 'qq'] as const

export const SocialProviderLabel: Record<string, string> = {
  github: 'GitHub',
  gitee: 'Gitee',
  qq: 'QQ',
}

// Icon paths — place actual SVG/PNG files in public/icons/
export const SocialProviderIcon: Record<string, string> = {
  github: 'mdi:github',
  gitee: 'simple-icons:gitee',
  qq: 'mdi:qqchat',
}

export const SocialProviderIconPath: Record<string, string> = {
  github: '/icons/github.svg',
  gitee: '/icons/gitee.svg',
  wechat: '/icons/wechat.svg',
  qq: '/icons/qq.png',
}
