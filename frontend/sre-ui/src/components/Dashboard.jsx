import { useCallback, useEffect, useState } from 'react';
import { useAlert } from '../context/AlertContext.jsx';
import { useEmergenciaResumen } from '../hooks/useEmergenciaResumen.js';
import { useIncidentesActivos } from '../hooks/useIncidentesActivos.js';
import AlertBanner from './AlertBanner.jsx';
import EmergenciaPanel from './emergencia/EmergenciaPanel.jsx';
import FocosActivos from './FocosActivos.jsx';
import MapaIncidentes from './mapa/MapaIncidentes.jsx';
import ResumenDetalle from './emergencia/ResumenDetalle.jsx';
import WeatherBar from './WeatherBar.jsx';
import logo from '../assets/logo.svg';

export default function Dashboard() {
  const [incidenteId, setIncidenteId]     = useState('');
  const [seleccionado, setSeleccionado]   = useState(null);
  const [windData, setWindData]           = useState({ windDir: 0, windSpeed: 0 });

  const { data, loading, error, cargar, limpiar } = useEmergenciaResumen();
  const { incidentes, online }                     = useIncidentesActivos();
  const { publicar }                               = useAlert();

  const handleWeatherUpdate = useCallback((wd) => setWindData(wd), []);

  const handleConsultar = async (e) => {
    e.preventDefault();
    await cargar(incidenteId);
  };

  const handleSeleccionar = useCallback((id) => {
    setSeleccionado(id);
    setIncidenteId(String(id));
  }, []);

  useEffect(() => {
    if (error) {
      publicar(error, 'error');
    } else if (data?.datosContingencia) {
      publicar('Resumen obtenido en modo contingencia', 'warning');
    } else if (data) {
      publicar(`Resumen del incidente #${data.incidenteId} cargado`, 'success');
    }
  }, [data, error, publicar]);

  const enProgreso  = incidentes.filter(i => i.estado === 'EN_PROGRESO').length;
  const reportados  = incidentes.filter(i => i.estado === 'REPORTADO').length;
  const controlados = incidentes.filter(i => i.estado === 'CONTROLADO').length;

  return (
    <div className="app-container">
      <header className="app-header">
        <div className="header-izq">
          <img src={logo} alt="Logo SRE" className="header-logo" />
          <div>
            <span className="eyebrow">Municipalidad Valle del Sol</span>
            <h1>SRE — Panel de Emergencias</h1>
          </div>
        </div>
        <span className={`badge-online ${online ? 'online' : 'offline'}`}>
          {online ? '● En línea' : '○ Sin conexión'}
        </span>
      </header>

      <WeatherBar onWeatherUpdate={handleWeatherUpdate} />

      <AlertBanner />

      <div className="stats-row">
        <div className="stat-card rojo">
          <span className="stat-num">{enProgreso}</span>
          <span className="stat-label">En Progreso</span>
        </div>
        <div className="stat-card naranja">
          <span className="stat-num">{reportados}</span>
          <span className="stat-label">Reportados</span>
        </div>
        <div className="stat-card verde">
          <span className="stat-num">{controlados}</span>
          <span className="stat-label">Controlados</span>
        </div>
        <div className="stat-card azul">
          <span className="stat-num">{incidentes.length}</span>
          <span className="stat-label">Total</span>
        </div>
      </div>

      <div className="layout-principal">
        <div className="columna-mapa">
          <FocosActivos
            incidentes={incidentes}
            online={online}
            onSeleccionar={handleSeleccionar}
          />
          <div className="mapa-contenedor">
            <MapaIncidentes
              incidentes={incidentes}
              seleccionado={seleccionado}
              onSeleccionar={handleSeleccionar}
              windDir={windData.windDir}
              windSpeed={windData.windSpeed}
            />
          </div>
        </div>

        <div className="columna-detalle">
          <form className="search-bar" onSubmit={handleConsultar}>
            <label htmlFor="incidenteId">Consultar resumen BFF</label>
            <input
              id="incidenteId"
              type="number"
              min="1"
              value={incidenteId}
              onChange={(e) => setIncidenteId(e.target.value)}
              placeholder="ID de incidente"
            />
            <button type="submit" disabled={loading}>
              {loading ? 'Consultando…' : 'Consultar'}
            </button>
            <button type="button" className="btn-secondary" onClick={limpiar}>
              Limpiar
            </button>
          </form>

          {error && <p className="inline-error">{error}</p>}

          {data && (
            <EmergenciaPanel resumen={data}>
              <EmergenciaPanel.Header
                title={`Operación #${data.incidenteId}`}
                subtitle="Datos agregados por el BFF"
              />
              <EmergenciaPanel.Body>
                <ResumenDetalle resumen={data} />
              </EmergenciaPanel.Body>
              <EmergenciaPanel.Footer>
                <small>GET /bff/emergencias/{data.incidenteId}/resumen</small>
              </EmergenciaPanel.Footer>
            </EmergenciaPanel>
          )}

          {!data && (
            <div className="detalle-vacio">
              <p>Haz clic en un incidente del mapa<br/>o ingresa un ID para ver el resumen BFF</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
