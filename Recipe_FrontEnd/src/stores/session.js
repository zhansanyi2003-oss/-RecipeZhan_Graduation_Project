import { defineStore } from 'pinia'
import { ref } from 'vue'

const PROFILE_STORAGE_KEY = 'session_profile'

const cloneProfile = (profile) => {
  if (!profile || typeof profile !== 'object') return null
  return { ...profile }
}

const normalizeRole = (role) => {
  return typeof role === 'string' && role.trim() ? role.trim().toUpperCase() : undefined
}

const normalizeAvatarUrl = (avatarUrl) => {
  if (typeof avatarUrl !== 'string' || !avatarUrl.trim()) return avatarUrl

  return avatarUrl.replace(/^https?:\/\/localhost:8888\/images\//, '/images/')
}

const normalizeProfile = (profile) => {
  const cloned = cloneProfile(profile)
  if (!cloned) return null

  const role = normalizeRole(cloned.role)
  const avatarUrl = normalizeAvatarUrl(cloned.avatarUrl)
  const normalized = avatarUrl === cloned.avatarUrl ? cloned : { ...cloned, avatarUrl }

  return role ? { ...normalized, role } : normalized
}

const getStorage = () => {
  try {
    return globalThis.localStorage || null
  } catch {
    return null
  }
}

const readStoredProfile = () => {
  const storage = getStorage()
  if (!storage) return null

  try {
    const rawProfile = storage.getItem(PROFILE_STORAGE_KEY)
    if (!rawProfile) return null
    return normalizeProfile(JSON.parse(rawProfile))
  } catch {
    storage.removeItem(PROFILE_STORAGE_KEY)
    return null
  }
}

const writeStoredProfile = (profile) => {
  const storage = getStorage()
  if (!storage) return

  if (!profile) {
    storage.removeItem(PROFILE_STORAGE_KEY)
    return
  }

  storage.setItem(PROFILE_STORAGE_KEY, JSON.stringify(profile))
}

export const useSessionStore = defineStore('session', () => {
  const profile = ref(readStoredProfile())
  let profilePromise = null

  const clearProfile = () => {
    profile.value = null
    profilePromise = null
    writeStoredProfile(null)
  }

  const setProfile = (nextProfile) => {
    profile.value = normalizeProfile(nextProfile)
    writeStoredProfile(profile.value)
    return cloneProfile(profile.value)
  }

  const patchProfile = (updates = {}) => {
    const merged = {
      ...(profile.value || {}),
      ...(cloneProfile(updates) || {}),
    }

    profile.value = normalizeProfile(merged)
    writeStoredProfile(profile.value)
    return cloneProfile(profile.value)
  }

  const loadProfile = async (loader, { force = false } = {}) => {
    if (profile.value && !force) {
      return cloneProfile(profile.value)
    }

    if (!profilePromise) {
      profilePromise = Promise.resolve(loader())
        .then((result) => {
          if (result?.code && result.data) {
            return setProfile(result.data)
          }
          return null
        })
        .finally(() => {
          profilePromise = null
        })
    }

    const resolvedProfile = await profilePromise
    return cloneProfile(resolvedProfile)
  }

  return {
    profile,
    clearProfile,
    setProfile,
    patchProfile,
    loadProfile,
  }
})
