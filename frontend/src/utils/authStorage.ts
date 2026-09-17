const TOKEN_KEY = 'token'
const USER_KEY = 'moodboard_current_user'
const USER_ID_KEY = 'moodboard_current_user_id'
const MIGRATION_KEY = 'moodboard_auth_storage_v2'

function read(key: string) {
  return sessionStorage.getItem(key) || localStorage.getItem(key)
}

export function migrateLegacyAuth() {
  if (localStorage.getItem(MIGRATION_KEY) === '1') return
  // Legacy builds persisted auth without a remember-me choice. Treat it as a
  // session login and clear it on upgrade; business/demo data is untouched.
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  localStorage.setItem(MIGRATION_KEY, '1')
}

export function getToken() { return read(TOKEN_KEY) }
export function getCurrentUser() { return read(USER_KEY) }
export function getCurrentUserId() { return read(USER_ID_KEY) }
export function setToken(token: string, remember = false) {
  removeToken()
  ;(remember ? localStorage : sessionStorage).setItem(TOKEN_KEY, token)
}
export function setCurrentUser(user: string, remember = false) {
  sessionStorage.removeItem(USER_KEY)
  localStorage.removeItem(USER_KEY)
  ;(remember ? localStorage : sessionStorage).setItem(USER_KEY, user || 'user')
}
export function setCurrentUserId(userId: string | number, remember = false) {
  sessionStorage.removeItem(USER_ID_KEY)
  localStorage.removeItem(USER_ID_KEY)
  ;(remember ? localStorage : sessionStorage).setItem(USER_ID_KEY, String(userId))
}
export function removeToken() { sessionStorage.removeItem(TOKEN_KEY); localStorage.removeItem(TOKEN_KEY) }
export function removeCurrentUser() {
  sessionStorage.removeItem(USER_KEY); localStorage.removeItem(USER_KEY)
  sessionStorage.removeItem(USER_ID_KEY); localStorage.removeItem(USER_ID_KEY)
}
export function clearAuth() { removeToken(); removeCurrentUser() }
export function isRemembered() { return !!localStorage.getItem(TOKEN_KEY) }
