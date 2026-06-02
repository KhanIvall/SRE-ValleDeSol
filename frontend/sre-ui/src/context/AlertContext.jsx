import { createContext, useCallback, useContext, useMemo, useState } from 'react';

const AlertContext = createContext(null);

/**
 * Patron Provider (Context): estado global de alertas operativas para toda la app.
 */
export function AlertProvider({ children }) {
  const [alertas, setAlertas] = useState([]);

  const publicar = useCallback((mensaje, tipo = 'info') => {
    const id = crypto.randomUUID();
    setAlertas((prev) => [...prev, { id, mensaje, tipo, ts: Date.now() }]);
    return id;
  }, []);

  const descartar = useCallback((id) => {
    setAlertas((prev) => prev.filter((a) => a.id !== id));
  }, []);

  const value = useMemo(
    () => ({ alertas, publicar, descartar }),
    [alertas, publicar, descartar],
  );

  return <AlertContext.Provider value={value}>{children}</AlertContext.Provider>;
}

export function useAlert() {
  const ctx = useContext(AlertContext);
  if (!ctx) {
    throw new Error('useAlert debe usarse dentro de AlertProvider');
  }
  return ctx;
}
