/**
 * Facade de acceso HTTP: oculta la URL del BFF y headers al resto de la UI.
 */
const baseUrl = () => import.meta.env.VITE_API_BASE_URL || '';

function buildHeaders() {
  const headers = { Accept: 'application/json' };
  const token = import.meta.env.VITE_AUTH_TOKEN;
  if (token) {
    headers.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
  }
  return headers;
}

export async function fetchEmergenciaResumen(incidenteId) {
  const url = `${baseUrl()}/bff/emergencias/${incidenteId}/resumen`;
  const response = await fetch(url, { headers: buildHeaders() });

  if (!response.ok) {
    const detail = await response.text();
    throw new Error(detail || `Error HTTP ${response.status}`);
  }

  return response.json();
}
