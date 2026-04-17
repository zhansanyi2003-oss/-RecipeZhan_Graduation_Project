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

const getStorage = (storage) => storage || globalThis.localStorage

export const clearStoredLogin = (storage) => {
  const target = getStorage(storage)
  target.removeItem('loginUser')
  target.removeItem('token_exp')
}

export const isStoredLoginActive = (storage) => {
  const target = getStorage(storage)
  const rawLoginUser = target.getItem('loginUser')
  const rawExp = target.getItem('token_exp')

  if (!rawLoginUser || !rawExp) return false

  const exp = Number(rawExp)
  if (!Number.isFinite(exp) || Date.now() >= exp) {
    clearStoredLogin(target)
    return false
  }

  try {
    const loginUser = JSON.parse(rawLoginUser)
    return Boolean(loginUser?.username)
  } catch (error) {
    clearStoredLogin(target)
    return false
  }
}

export const getStoredLoginSnapshot = (storage) => {
  if (!isStoredLoginActive(storage)) {
    return {
      isLoggedIn: false,
      username: null,
      role: null,
    }
  }

  const target = getStorage(storage)
  try {
    const loginUser = JSON.parse(target.getItem('loginUser'))
    return {
      isLoggedIn: true,
      username: loginUser.username,
      role: typeof loginUser.role === 'string' ? loginUser.role.toUpperCase() : null,
    }
  } catch (error) {
    clearStoredLogin(target)
    return {
      isLoggedIn: false,
      username: null,
      role: null,
    }
  }
}

export const getTokenFromAuthorization = (authorization) => {
  if (typeof authorization !== 'string') return null
  if (!authorization.startsWith('Bearer ')) return null
  return authorization.slice(7)
}

export const getTokenExpiryTimestamp = (authorization) => {
  const token = getTokenFromAuthorization(authorization)
  const payload = parseJwtPayload(token)
  const exp = Number(payload?.exp)

  if (!Number.isFinite(exp) || exp <= 0) return null
  return exp * 1000
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
