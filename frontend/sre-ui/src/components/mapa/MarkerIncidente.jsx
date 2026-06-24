import { useRef, useEffect } from 'react';
import { Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';

function crearIcono(color, activo) {
  const size = activo ? 28 : 22;
  const border = activo ? 4 : 3;
  return L.divIcon({
    className: '',
    html: `<div style="
      width:${size}px;height:${size}px;
      border-radius:50%;
      background:${color};
      border:${border}px solid white;
      box-shadow:0 2px 8px rgba(0,0,0,0.35);
      transition:all .2s;
    "></div>`,
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
  });
}

export function MarkerIncidente({ incidente, lat, lon, color, activo, onSeleccionar }) {
  const markerRef = useRef(null);

  useEffect(() => {
    if (activo && markerRef.current) {
      markerRef.current.openPopup();
    }
  }, [activo]);

  const tipo = incidente.incidente?.tipo ?? 'Incidente';
  const estado = (incidente.incidente?.estado ?? '').replace('_', ' ');
  const nivel = incidente.zonaRiesgo?.nivelRiesgo ?? '—';
  const recursos = incidente.recursosAsignados?.length ?? 0;

  return (
    <Marker
      ref={markerRef}
      position={[lat, lon]}
      icon={crearIcono(color, activo)}
      eventHandlers={{ click: () => onSeleccionar(incidente.incidenteId) }}
    >
      <Popup>
        <div style={{ minWidth: 160, fontFamily: 'system-ui', fontSize: 13 }}>
          <div style={{ fontWeight: 600, marginBottom: 4 }}>
            Incidente #{incidente.incidenteId ?? incidente.id}
          </div>
          <div style={{ color: '#555', marginBottom: 2 }}>{tipo}</div>
          <div style={{ marginBottom: 2 }}>Estado: <strong>{estado}</strong></div>
          <div style={{ marginBottom: 2 }}>Riesgo: <strong style={{ color }}>{nivel}</strong></div>
          <div>Recursos: <strong>{recursos}</strong></div>
        </div>
      </Popup>
    </Marker>
  );
}
