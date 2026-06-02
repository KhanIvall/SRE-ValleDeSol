import { useEffect, useState } from 'react';
import { useAlert } from '../context/AlertContext.jsx';
import { useEmergenciaResumen } from '../hooks/useEmergenciaResumen.js';
import EmergenciaPanel from './emergencia/EmergenciaPanel.jsx';
import ResumenDetalle from './emergencia/ResumenDetalle.jsx';
import AlertBanner from './AlertBanner.jsx';

export default function Dashboard() {
  const [incidenteId, setIncidenteId] = useState('1');
  const { data, loading, error, cargar, limpiar } = useEmergenciaResumen();
  const { publicar } = useAlert();

  const handleConsultar = async (e) => {
    e.preventDefault();
    await cargar(incidenteId);
  };

  useEffect(() => {
    if (error) {
      publicar(error, 'error');
    } else if (data?.datosContingencia) {
      publicar('Resumen obtenido en modo contingencia', 'warning');
    } else if (data) {
      publicar(`Resumen del incidente #${data.incidenteId} cargado`, 'success');
    }
  }, [data, error, publicar]);

  return (
    <div className="dashboard">
      <AlertBanner />

      <header className="app-header">
        <div>
          <p className="eyebrow">Municipalidad Valle del Sol</p>
          <h1>SRE — Panel de emergencias</h1>
        </div>
        <p className="tagline">Conectividad que salva vidas</p>
      </header>

      <form className="search-bar" onSubmit={handleConsultar}>
        <label htmlFor="incidenteId">ID de incidente</label>
        <input
          id="incidenteId"
          type="number"
          min="1"
          value={incidenteId}
          onChange={(e) => setIncidenteId(e.target.value)}
          placeholder="Ej: 1"
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Consultando BFF…' : 'Consultar resumen'}
        </button>
        <button
          type="button"
          className="btn-secondary"
          onClick={() => {
            limpiar();
          }}
        >
          Limpiar
        </button>
      </form>

      {error && <p className="inline-error">{error}</p>}

      {data && (
        <EmergenciaPanel resumen={data}>
          <EmergenciaPanel.Header
            title={`Operacion #${data.incidenteId}`}
            subtitle="Datos agregados por el BFF (incidentes · recursos · zonas)"
          />
          <EmergenciaPanel.Body>
            <ResumenDetalle resumen={data} />
          </EmergenciaPanel.Body>
          <EmergenciaPanel.Footer>
            <small>Fuente: GET /bff/emergencias/{data.incidenteId}/resumen</small>
          </EmergenciaPanel.Footer>
        </EmergenciaPanel>
      )}
    </div>
  );
}
