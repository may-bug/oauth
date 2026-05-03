export default defineNuxtRouteMiddleware((to) => {
  const authStore = useAuthStore()

  // Only /user and /admin require authentication
  const requiresAuth = to.path.startsWith('/user') || to.path.startsWith('/admin')

  if (!authStore.isAuthenticated && requiresAuth) {
    return navigateTo({ path: '/auth/login', query: { redirect: to.fullPath } })
  }

  // Authenticated user on login/register → redirect away
  if (authStore.isAuthenticated && (to.path === '/auth/login' || to.path === '/auth/register')) {
    const redirect = to.query.redirect
    if (typeof redirect === 'string' && redirect.startsWith('/') && !redirect.startsWith('//')) {
      return navigateTo(redirect)
    }
    return navigateTo('/')
  }
})
