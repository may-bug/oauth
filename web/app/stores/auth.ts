// @ts-ignore
import { defineStore } from 'pinia'
import type {AuthStatus, PermissionsResponse} from '~/types/auth'
import type {User} from "~/types/user";

// @ts-ignore
export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const user = ref<User | null>(null)
  const permissions = ref<PermissionsResponse | null>(null)

  const isAuthenticated =ref<Boolean>(false)
  const userId = computed(() => user.value?.id)
  const username = computed(() => user.value?.username)
  const avatar = computed(() => user.value?.avatar)
  const menuPermissions = computed(() => permissions.value?.menus ?? [])
  const buttonPermissions = computed(() => permissions.value?.buttons ?? [])
  const apiPermissions = computed(() => permissions.value?.apis ?? [])

  function setTokens(at: string, rt: string) {
    accessToken.value = at
    refreshToken.value = rt
    isAuthenticated.value=true
  }

  function setUser(u: User) {
    user.value = u
  }

  function setStatus(status: AuthStatus) {
    isAuthenticated.value = status.authenticated
    console.log(status.authenticated)
    if (status.authenticated) {
      user.value = {
        username: status.username,
        nickname: status.nickname,
        id: status.userId,
      }
    }
  }

  function setPermissions(p: PermissionsResponse) {
    permissions.value = p
  }

  function clearAuth() {
    accessToken.value = null
    refreshToken.value = null
    user.value = null
    permissions.value = null
  }

  return {
    accessToken,
    refreshToken,
    user,
    permissions,
    isAuthenticated,
    userId,
    username,
    avatar,
    menuPermissions,
    buttonPermissions,
    apiPermissions,
    setTokens,
    setUser,
    setStatus,
    setPermissions,
    clearAuth,
  }
},{
  persist: true,
})
