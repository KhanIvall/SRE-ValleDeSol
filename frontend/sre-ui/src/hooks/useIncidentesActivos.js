import { useCallback, useEffect, useRef, useState } from 'react';

const API = '/incidentes';
const INTERVAL_MS = 30_000;

const PRIORIDAD = { EN_PROGRESO: 0, REPORTADO: 1, CONTROLADO: 2, CERRADO: 3 };

export function useIncidentesActivos() {
  const [incidentes, setIncidentes] = useState([]);
  const [nuevoCritico, setNuevoCritico] = useState(null);
  const [online, setOnline] = useState(false);
  const prevIds = useRef(new Set());

  const fetchActivos = useCallback(async () => {
    try {
      const res = await fetch(API, { signal: AbortSignal.timeout(5000) });
      if (!res.ok) throw new Error('no ok');
      const data = await res.json();
      const activos = (Array.isArray(data) ? data : data.content ?? [])
        .filter((i) => i.estado !== 'CERRADO')
        .sort((a, b) => (PRIORIDAD[a.estado] ?? 9) - (PRIORIDAD[b.estado] ?? 9));

      // Detectar nuevos incendios críticos
      activos.forEach((inc) => {
        if (!prevIds.current.has(inc.id) && inc.estado === 'EN_PROGRESO') {
          setNuevoCritico(inc);
        }
      });
      prevIds.current = new Set(activos.map((i) => i.id));
      setIncidentes(activos);
      setOnline(true);
    } catch {
      setOnline(false);
    }
  }, []);

  useEffect(() => {
    fetchActivos();
    const t = setInterval(fetchActivos, INTERVAL_MS);
    return () => clearInterval(t);
  }, [fetchActivos]);

  const limpiarCritico = useCallback(() => setNuevoCritico(null), []);

  return { incidentes, nuevoCritico, limpiarCritico, online, refetch: fetchActivos };
}
