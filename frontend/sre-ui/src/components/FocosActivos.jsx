import { useEffect } from 'react';
import { useIncidentesActivos } from '../hooks/useIncidentesActivos.js';

const ESTADO_CLS = {
  EN_PROGRESO: 'foco-progreso',
  REPORTADO:   'foco-reportado',
  CONTROLADO:  'foco-controlado',
};

function playAlerta() {
  try {
    const ctx = new AudioContext();
    [880, 1100, 880].forEach((freq, i) => {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.frequency.value = freq;
      gain.gain.setValueAtTime(0.3, ctx.currentTime + i * 0.25);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + i * 0.25 + 0.22);
      osc.start(ctx.currentTime + i * 0.25);
      osc.stop(ctx.currentTime + i * 0.25 + 0.25);
    });
  } catch {}
}

export default function FocosActivos({ onSeleccionar, windDir }) {
  const { incidentes, nuevoCritico, limpiarCritico, online, refetch } = useIncidentesActivos();

  useEffect(() => {
    if (nuevoCritico) {
      playAlerta();
      limpiarCritico();
    }
  }, [nuevoCritico, limpiarCritico]);

  const enProgreso = incidentes.filter((i) => i.estado === 'EN_PROGRESO');
  const reportados = incidentes.filter((i) => i.estado === 'REPORTADO');

  return (
    <div className="focos-panel">
      <div className="focos-header">
        <span className="focos-titulo">🔥 Focos activos</span>
        <div className="focos-meta">
          <span className={`dot-estado ${online ? 'dot-online' : 'dot-offline'}`}></span>
          <span className="focos-badge foco-progreso">{enProgreso.length} en progreso</span>
          <span className="focos-badge foco-reportado">{reportados.length} reportados</span>
          <button className="btn-refresh" onClick={refetch} title="Actualizar">↻</button>
        </div>
      </div>

      {!online && (
        <p className="focos-offline">Backend sin conexión — datos no disponibles en tiempo real</p>
      )}

      {incidentes.length === 0 && online && (
        <p className="focos-vacio">✅ Sin focos activos en este momento</p>
      )}

      <div className="focos-lista">
        {incidentes.map((inc) => (
          <div
            key={inc.id}
            className={`foco-item ${ESTADO_CLS[inc.estado] ?? ''}`}
            onClick={() => onSeleccionar?.(inc.id)}
            role="button"
            tabIndex={0}
          >
            <div className="foco-icono">
              {inc.estado === 'EN_PROGRESO' ? '🔥' : inc.estado === 'REPORTADO' ? '⚠️' : '✅'}
            </div>
            <div className="foco-info">
              <div className="foco-nombre">
                #{inc.id} — {inc.tipo ?? 'Incendio'}
              </div>
              <div className="foco-detalle">
                {inc.descripcion ?? ''}
                {inc.latitud && inc.longitud
                  ? ` · ${inc.latitud.toFixed(4)}, ${inc.longitud.toFixed(4)}`
                  : ''}
              </div>
            </div>
            {inc.latitud && inc.longitud && windDir != null && (
              <div className="foco-viento" title="Dirección de propagación estimada">
                <span style={{ display: 'inline-block', transform: `rotate(${windDir}deg)` }}>➤</span>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
