import { useCallback, useEffect, useState, lazy, Suspense } from 'react';
import { useAlert } from '../context/AlertContext.jsx';
import { useEmergenciaResumen } from '../hooks/useEmergenciaResumen.js';
import EmergenciaPanel from './emergencia/EmergenciaPanel.jsx';
import ResumenDetalle from './emergencia/ResumenDetalle.jsx';
import AlertBanner from './AlertBanner.jsx';
import WeatherBar from './WeatherBar.jsx';
import FocosActivos from './FocosActivos.jsx';
import logo from '../assets/logo.svg';

const MapaIncidentes = lazy(() => import('./mapa/MapaIncidentes.jsx'));

const ESTADO_COLOR = {
  EN_PROGRESO: 'estado-progreso',
  REPORTADO:   'estado-reportado',
  CONTROLADO:  'estado-controlado',
  CERRADO:     'estado-cerrado',
};

export default function Dashboard() {
  const [incidenteId, setIncidenteId]   = useState('');
  const [historial, setHistorial]       = useState([]);
  const [seleccionado, setSeleccionado] = useState(null);
  const [windData, setWindData]         = useState({ windDir: null, windSpeed: null });
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
      publicar(`Incidente #${data.incidenteId} cargado`, 'success');
      setSeleccionado(data.incidenteId);
      setHistorial((prev) => {
        const filtrado = prev.filter((i) => i.incidenteId !== data.incidenteId);
        return [data, ...filtrado].slice(0, 10);
      });
    }
  }, [data, error, publicar]);

  const seleccionarDesdeHistorial = useCallback(async (id) => {
    const local = historial.find((i) => i.incidenteId === id);
    if (local) {
      setSeleccionado(id);
    } else {
      setIncidenteId(String(id));
      await cargar(id);
    }
  }, [historial, cargar]);

  const handleWeatherUpdate = useCallback((wd) => {
    setWindData((prev) =>
      prev.windDir === wd.windDir && prev.windSpeed === wd.windSpeed ? prev : wd
    );
  }, []);

  const incidenteSeleccionado = historial.find((i) => i.incidenteId === seleccionado) ?? data;

  const contadorEstado = (estado) =>
    historial.filter((i) => i.incidente?.estado === estado).length;

  return (
    <div className="dashboard">
      <AlertBanner />

      <header className="app-header">
        <div className="header-brand">
          <img src={logo} alt="Logo SRE" className="header-icono" style={{ width: 52, height: 52 }} />
          <div>
            <p className="eyebrow">Cuerpo de Bomberos · Municipalidad Valle del Sol</p>
            <h1>SRE — Centro de operaciones</h1>
          </div>
        </div>
        <div className="header-estado">
          <span className="dot-activo" aria-hidden="true"></span>
          <span className="estado-texto">Sistema activo</span>
        </div>
      </header>

      <WeatherBar onWeatherUpdate={handleWeatherUpdate} />

      <div className="stats-row">
        <div className="stat-card">
          <span className="stat-label">En progreso</span>
          <span className="stat-val stat-rojo">{contadorEstado('EN_PROGRESO')}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Reportados</span>
          <span className="stat-val stat-amarillo">{contadorEstado('REPORTADO')}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Controlados</span>
          <span className="stat-val stat-verde">{contadorEstado('CONTROLADO')}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Recursos activos</span>
          <span className="stat-val">
            {historial.reduce((acc, i) => acc + (i.recursosAsignados?.length ?? 0), 0)}
          </span>
        </div>
      </div>

      {/* Panel de focos activos en tiempo real */}
      <FocosActivos onSeleccionar={seleccionarDesdeHistorial} windDir={windData.windDir} />

      <form className="search-bar" onSubmit={handleConsultar}>
        <label htmlFor="incidenteId">Buscar incidente por N°</label>
        <input
          id="incidenteId"
          type="number"
          min="1"
          value={incidenteId}
          onChange={(e) => setIncidenteId(e.target.value)}
          placeholder="Ej: 1"
        />
        <button type="submit" className="btn-buscar" disabled={loading}>
          {loading ? 'Consultando…' : '🔍 Buscar'}
        </button>
        <button
          type="button"
          className="btn-secondary"
          onClick={() => { limpiar(); setHistorial([]); setSeleccionado(null); }}
        >
          Limpiar
        </button>
      </form>

      {error && <p className="inline-error">{error}</p>}

      <div className="layout-principal">

        <section className="columna-mapa">
          <div className="seccion-titulo">
            <span aria-hidden="true">🗺️</span> Mapa de incidentes
            {windData.windDir != null && (
              <span className="leyenda-viento">
                · Cono rojo = propagación estimada por viento
              </span>
            )}
          </div>
          <Suspense fallback={<div className="mapa-cargando">Cargando mapa…</div>}>
            <MapaIncidentes
              incidentes={historial}
              seleccionado={seleccionado}
              onSeleccionar={seleccionarDesdeHistorial}
              windDir={windData.windDir}
              windSpeed={windData.windSpeed}
            />
          </Suspense>

          <div className="leyenda-mapa">
            <span className="leyenda-item"><span className="dot-leyenda dot-rojo"></span> En progreso</span>
            <span className="leyenda-item"><span className="dot-leyenda dot-amarillo"></span> Reportado</span>
            <span className="leyenda-item"><span className="dot-leyenda dot-verde"></span> Controlado</span>
            <span className="leyenda-item"><span className="dot-leyenda" style={{background:'rgba(226,75,74,0.4)',border:'1px dashed #e24b4a'}}></span> Propagación</span>
          </div>
        </section>

        <section className="columna-detalle">
          {historial.length > 1 && (
            <div className="historial-tabs">
              {historial.map((inc) => (
                <button
                  key={inc.incidenteId}
                  className={`tab-incidente ${inc.incidenteId === seleccionado ? 'tab-activo' : ''}`}
                  onClick={() => setSeleccionado(inc.incidenteId)}
                >
                  #{inc.incidenteId}
                  <span className={`tab-badge ${ESTADO_COLOR[inc.incidente?.estado] ?? ''}`}>
                    {(inc.incidente?.estado ?? '').replace('_', ' ')}
                  </span>
                </button>
              ))}
            </div>
          )}

          {incidenteSeleccionado ? (
            <EmergenciaPanel resumen={incidenteSeleccionado}>
              <EmergenciaPanel.Header
                title={`Incidente #${incidenteSeleccionado.incidenteId}`}
                subtitle={`${incidenteSeleccionado.incidente?.tipo ?? ''} · ${(incidenteSeleccionado.incidente?.estado ?? '').replace('_', ' ')}`}
              />
              <EmergenciaPanel.Body>
                <ResumenDetalle resumen={incidenteSeleccionado} />
              </EmergenciaPanel.Body>
              <EmergenciaPanel.Footer>
                <small>GET /bff/emergencias/{incidenteSeleccionado.incidenteId}/resumen</small>
              </EmergenciaPanel.Footer>
            </EmergenciaPanel>
          ) : (
            <div className="detalle-vacio">
              <span aria-hidden="true">🔎</span>
              <p>Busca un incidente o haz clic en un foco activo para ver su detalle</p>
            </div>
          )}
        </section>

      </div>
    </div>
  );
}
