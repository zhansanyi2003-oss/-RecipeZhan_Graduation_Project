export const resolvePrimaryMenuIndex = (path) => {
  if (path === '/' || path === '') {
    return '/index'
  }

  return path
}
