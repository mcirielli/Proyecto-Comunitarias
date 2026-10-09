export interface HealthResponse { application: string; status: 'UP' }

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export async function getHealth(signal: AbortSignal): Promise<HealthResponse> {
  const response = await fetch(apiBaseUrl + '/api/health', { signal, cache: 'no-store' })
  if (!response.ok) throw new Error('La API respondió HTTP ' + response.status)
  const body: unknown = await response.json()
  if (typeof body !== 'object' || body === null ||
      !('application' in body) || body.application !== 'Comunitarias' ||
      !('status' in body) || body.status !== 'UP') {
    throw new Error('La respuesta de la API no tiene el formato esperado')
  }
  return { application: body.application, status: body.status }
}
