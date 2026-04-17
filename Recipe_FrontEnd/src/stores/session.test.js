import assert from 'node:assert/strict'
import { createPinia, setActivePinia } from 'pinia'
import { useSessionStore } from './session.js'

const createStorage = (seed = {}) => {
  const state = new Map(Object.entries(seed))

  return {
    getItem(key) {
      return state.has(key) ? state.get(key) : null
    },
    setItem(key, value) {
      state.set(key, String(value))
    },
    removeItem(key) {
      state.delete(key)
    },
    clear() {
      state.clear()
    },
  }
}

const runHydrateFromStorage = () => {
  globalThis.localStorage = createStorage({
    session_profile: JSON.stringify({
      username: 'peipei',
      avatarUrl: 'cached-avatar.png',
      role: 'user',
    }),
  })

  setActivePinia(createPinia())
  const store = useSessionStore()

  assert.equal(store.profile?.username, 'peipei')
  assert.equal(store.profile?.avatarUrl, 'cached-avatar.png')
  assert.equal(store.profile?.role, 'USER')
}

const runLegacyAvatarNormalization = () => {
  globalThis.localStorage = createStorage({
    session_profile: JSON.stringify({
      username: 'peipei',
      avatarUrl: 'http://localhost:8888/images/legacy-avatar.png',
      role: 'user',
    }),
  })

  setActivePinia(createPinia())
  const store = useSessionStore()

  assert.equal(store.profile?.avatarUrl, '/images/legacy-avatar.png')
}

const runPersistenceUpdates = () => {
  const storage = createStorage()
  globalThis.localStorage = storage

  setActivePinia(createPinia())
  const store = useSessionStore()

  store.setProfile({
    username: 'peipei',
    avatarUrl: 'fresh-avatar.png',
    role: 'admin',
  })

  assert.equal(
    storage.getItem('session_profile'),
    JSON.stringify({
      username: 'peipei',
      avatarUrl: 'fresh-avatar.png',
      role: 'ADMIN',
    }),
  )

  store.clearProfile()
  assert.equal(storage.getItem('session_profile'), null)
}

const runForcedRefresh = async () => {
  const storage = createStorage({
    session_profile: JSON.stringify({
      username: 'peipei',
      avatarUrl: 'cached-avatar.png',
      role: 'user',
    }),
  })
  globalThis.localStorage = storage

  setActivePinia(createPinia())
  const store = useSessionStore()

  let loadCount = 0
  const loader = async () => {
    loadCount += 1
    return {
      code: 1,
      data: {
        username: 'peipei',
        avatarUrl: 'server-avatar.png',
        role: 'admin',
      },
    }
  }

  const refreshedProfile = await store.loadProfile(loader, { force: true })

  assert.equal(loadCount, 1)
  assert.equal(refreshedProfile?.avatarUrl, 'server-avatar.png')
  assert.equal(store.profile?.avatarUrl, 'server-avatar.png')
  assert.equal(store.profile?.role, 'ADMIN')
  assert.equal(
    storage.getItem('session_profile'),
    JSON.stringify({
      username: 'peipei',
      avatarUrl: 'server-avatar.png',
      role: 'ADMIN',
    }),
  )
}

runHydrateFromStorage()
runLegacyAvatarNormalization()
runPersistenceUpdates()
await runForcedRefresh()

console.log('session store tests passed')
