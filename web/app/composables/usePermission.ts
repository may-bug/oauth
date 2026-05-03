import { useAuthStore } from '~/stores/auth'

export function usePermission() {
  const authStore = useAuthStore()

  function hasPermission(code: string): boolean {
    return authStore.buttonPermissions.some((p) => p.code === code) ||
           authStore.apiPermissions.includes(code)
  }

  function hasAnyPermission(codes: string[]): boolean {
    return codes.some((code) => hasPermission(code))
  }

  function hasAllPermissions(codes: string[]): boolean {
    return codes.every((code) => hasPermission(code))
  }

  function hasRole(code: string): boolean {
    // Roles are checked server-side; this checks menu permissions as proxy
    return authStore.menuPermissions.some((p) => p.code === code)
  }

  return {
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    hasRole,
  }
}
