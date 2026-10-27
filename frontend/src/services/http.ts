const base = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export async function api(path: string, options: RequestInit = {}) {
  const response = await fetch(base + '/api' + path, { ...options, credentials: 'include', cache: 'no-store' })
  if (!response.ok) {
    const detail = await response.json().catch(() => null)
    if ([400, 404, 409].includes(response.status) && typeof detail?.error === 'string') throw new Error(detail.error)
    throw new Error(response.status === 401 ? 'Acceso inválido o sesión vencida.' :
    response.status === 403 ? 'La operación no está permitida. Volvé a iniciar sesión.' :
    response.status === 409 ? 'Los datos coinciden con un registro existente.' : 'No se pudo completar la operación.')
  }
  return response.status === 204 ? null : response.json()
}
export async function write(path: string, body?: BodyInit, json = false, method = 'POST') {
  const csrf = await api('/auth/csrf')
  return api(path, { method, headers: { [csrf.headerName]: csrf.token,
    'Content-Type': json ? 'application/json' : 'application/x-www-form-urlencoded' }, body })
}
