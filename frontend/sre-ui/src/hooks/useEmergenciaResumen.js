import { useCallback, useState } from 'react';
import { fetchEmergenciaResumen } from '../services/emergenciaApi.js';

/**
 * Patron Custom Hook: encapsula estado, carga y errores del agregado BFF.
 */
export function useEmergenciaResumen() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const cargar = useCallback(async (incidenteId) => {
    if (!incidenteId) {
      setError('Ingrese un ID de incidente valido');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const resumen = await fetchEmergenciaResumen(incidenteId);
      setData(resumen);
    } catch (err) {
      setData(null);
      setError(err.message || 'No fue posible obtener el resumen');
    } finally {
      setLoading(false);
    }
  }, []);

  const limpiar = useCallback(() => {
    setData(null);
    setError(null);
  }, []);

  return { data, loading, error, cargar, limpiar };
}
