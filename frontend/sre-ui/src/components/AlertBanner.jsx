import { useAlert } from '../context/AlertContext.jsx';

export default function AlertBanner() {
  const { alertas, descartar } = useAlert();

  if (alertas.length === 0) return null;

  return (
    <div className="alert-stack" aria-live="polite">
      {alertas.map((a) => (
        <div key={a.id} className={`alert-item alert-${a.tipo}`}>
          <span>{a.mensaje}</span>
          <button type="button" onClick={() => descartar(a.id)} aria-label="Cerrar">
            ×
          </button>
        </div>
      ))}
    </div>
  );
}
