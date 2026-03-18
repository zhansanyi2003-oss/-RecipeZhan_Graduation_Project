const decodeBase64Url = (value) => {
  if (!value) return null
  try {
    const base64 = value.replace(/-/g, '+').replace(/_/g, '/')
    const normalized = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')
    return decodeURIComponent(
      atob(normalized)
        .split('')
        .map((char) => `%${(`00${char.charCodeAt(0).toString(16)}`).slice(-2)}`)
        .join(''),
    )
  } catch (error) {
    return null
  }
}

export const getTokenFromAuthorization = (authorization) => {
  if (typeof authorization !== 'string') return null
  if (!authorization.startsWith('Bearer ')) return null
  return authorization.slice(7)
}

export const parseJwtPayload = (token) => {
  if (!token) return null
  const parts = token.split('.')
  if (parts.length !== 3) return null
  const decoded = decodeBase64Url(parts[1])
  if (!decoded) return null
  try {
    return JSON.parse(decoded)
  } catch (error) {
    return null
  }
}

export const getRoleFromLoginStorage = () => {
  const raw = localStorage.getItem('loginUser')
  if (!raw) return null
  try {
    const loginUser = JSON.parse(raw)
    if (typeof loginUser.role === 'string' && loginUser.role.trim()) {
      return loginUser.role.trim().toUpperCase()
    }

    const token = getTokenFromAuthorization(loginUser.Authorization)
    const payload = parseJwtPayload(token)
    return typeof payload?.role === 'string' ? payload.role.toUpperCase() : null
  } catch (error) {
    return null
  }
}

export const isAdminFromLoginStorage = () => getRoleFromLoginStorage() === 'ADMIN'
