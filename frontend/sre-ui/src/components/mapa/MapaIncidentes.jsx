import { useEffect } from 'react';
import { MapContainer, TileLayer, Circle, Popup, useMap } from 'react-leaflet';
import { MarkerIncidente } from './MarkerIncidente.jsx';
import 'leaflet/dist/leaflet.css';

const NIVEL_COLOR = {
  CRITICO: '#e24b4a',
  ALTO: '#e76f51',
  MEDIO: '#e9c46a',
  BAJO: '#2a9d8f',
};

function colorPorNivel(nivel) {
  return NIVEL_COLOR[(nivel || '').toUpperCase()] ?? '#888';
}

function VolarA({ lat, lon, zoom = 14 }) {
  const mapa = useMap();
  useEffect(() => {
    if (lat != null && lon != null) {
      mapa.flyTo([lat, lon], zoom, { duration: 1.2 });
    }
  }, [lat, lon, zoom, mapa]);
  return null;
}

export default function MapaIncidentes({ incidentes = [], seleccionado, onSeleccionar }) {
  const incidenteActivo = incidentes.find((i) => i.incidenteId === seleccionado);

  return (
    <div className="mapa-contenedor">
      <MapContainer
        center={[-33.47, -70.68]}
        zoom={11}
        style={{ height: '100%', width: '100%' }}
        zoomControl={true}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution="© OpenStreetMap contributors"
        />

        {incidenteActivo && (
          <VolarA lat={incidenteActivo.incidente?.latitud} lon={incidenteActivo.incidente?.longitud} />
        )}

        {incidentes.map((inc) => {
          const lat = inc.incidente?.latitud;
          const lon = inc.incidente?.longitud;
          if (lat == null || lon == null) return null;

          const nivel = inc.zonaRiesgo?.nivelRiesgo;
          const color = colorPorNivel(nivel);
          const activo = inc.incidenteId === seleccionado;

          return (
            <MarkerIncidente
              key={inc.id}
              incidente={inc}
              lat={lat}
              lon={lon}
              color={color}
              activo={activo}
              onSeleccionar={onSeleccionar}
            />
          );
        })}

        {incidentes.map((inc) => {
          const lat = inc.incidente?.latitud;
          const lon = inc.incidente?.longitud;
          if (lat == null || lon == null) return null;
          const color = colorPorNivel(inc.zonaRiesgo?.nivelRiesgo);
          return (
            <Circle
              key={`radio-${inc.incidenteId}`}
              center={[lat, lon]}
              radius={1000}
              pathOptions={{ color, fillColor: color, fillOpacity: 0.08, weight: 1 }}
            />
          );
        })}
      </MapContainer>
    </div>
  );
}
