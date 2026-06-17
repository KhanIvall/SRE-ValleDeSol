import { useEffect } from 'react';
import { MapContainer, TileLayer, Circle, Polygon, useMap } from 'react-leaflet';
import L from 'leaflet';
import { MarkerIncidente } from './MarkerIncidente.jsx';
import 'leaflet/dist/leaflet.css';

const NIVEL_COLOR = {
  CRITICO: '#e24b4a',
  ALTO:    '#e76f51',
  MEDIO:   '#e9c46a',
  BAJO:    '#2a9d8f',
};

function colorPorNivel(nivel) {
  return NIVEL_COLOR[(nivel || '').toUpperCase()] ?? '#888';
}

function gradosACardinal(deg) {
  const dirs = ['N','NE','E','SE','S','SO','O','NO'];
  return dirs[Math.round((deg ?? 0) / 45) % 8];
}

function desplazar(lat, lon, distanciaKm, rumbo) {
  const R = 6371;
  const d = distanciaKm / R;
  const r = (rumbo * Math.PI) / 180;
  const lat1 = (lat * Math.PI) / 180;
  const lon1 = (lon * Math.PI) / 180;
  const lat2 = Math.asin(Math.sin(lat1) * Math.cos(d) + Math.cos(lat1) * Math.sin(d) * Math.cos(r));
  const lon2 = lon1 + Math.atan2(Math.sin(r) * Math.sin(d) * Math.cos(lat1), Math.cos(d) - Math.sin(lat1) * Math.sin(lat2));
  return [(lat2 * 180) / Math.PI, (lon2 * 180) / Math.PI];
}

function conePropagacion(lat, lon, windDir, windSpeed, nivelRiesgo) {
  const largo = Math.max(1, Math.min(windSpeed / 15, 4));
  const angApertura = nivelRiesgo === 'CRITICO' ? 45 : nivelRiesgo === 'ALTO' ? 35 : 25;
  const puntos = [[lat, lon]];
  for (let a = -angApertura / 2; a <= angApertura / 2; a += 5) {
    puntos.push(desplazar(lat, lon, largo, windDir + a));
  }
  return puntos;
}

function VolarA({ lat, lon, zoom = 14 }) {
  const mapa = useMap();
  useEffect(() => {
    if (lat != null && lon != null) mapa.flyTo([lat, lon], zoom, { duration: 1.2 });
  }, [lat, lon, zoom, mapa]);
  return null;
}

function FlechaViento({ lat, lon, windDir, windSpeed }) {
  const mapa = useMap();
  useEffect(() => {
    if (windDir == null) return;
    const cardinal = gradosACardinal(windDir);
    const icon = L.divIcon({
      className: '',
      html: `<div style="
        background:rgba(26,35,50,0.85);
        border:1px solid #3a5068;
        border-radius:8px;
        padding:6px 10px;
        font-size:12px;
        color:#e8eef4;
        white-space:nowrap;
        display:flex;align-items:center;gap:6px;
      ">
        <span style="font-size:18px;transform:rotate(${windDir}deg);display:inline-block">➤</span>
        <span><b>${Math.round(windSpeed)} km/h</b> ${cardinal}</span>
      </div>`,
      iconAnchor: [0, 0],
    });
    const marker = L.marker([lat + 0.12, lon - 0.28], { icon, interactive: false }).addTo(mapa);
    return () => mapa.removeLayer(marker);
  }, [mapa, lat, lon, windDir, windSpeed]);
  return null;
}

export default function MapaIncidentes({ incidentes = [], seleccionado, onSeleccionar, windDir, windSpeed }) {
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

        {windDir != null && windSpeed != null && (
          <FlechaViento lat={-33.47} lon={-70.68} windDir={windDir} windSpeed={windSpeed} />
        )}

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
              key={inc.incidenteId}
              incidente={inc}
              lat={lat}
              lon={lon}
              color={color}
              activo={activo}
              onSeleccionar={onSeleccionar}
            />
          );
        })}

        {/* Círculo de radio de afectación */}
        {incidentes.map((inc) => {
          const lat = inc.incidente?.latitud;
          const lon = inc.incidente?.longitud;
          if (lat == null || lon == null) return null;
          const color = colorPorNivel(inc.zonaRiesgo?.nivelRiesgo);
          return (
            <Circle
              key={`radio-${inc.incidenteId}`}
              center={[lat, lon]}
              radius={800}
              pathOptions={{ color, fillColor: color, fillOpacity: 0.07, weight: 1 }}
            />
          );
        })}

        {/* Cono de propagación por viento */}
        {windDir != null && incidentes
          .filter((i) => i.incidente?.estado === 'EN_PROGRESO' && i.incidente?.latitud != null)
          .map((inc) => {
            const lat = inc.incidente.latitud;
            const lon = inc.incidente.longitud;
            const nivel = inc.zonaRiesgo?.nivelRiesgo ?? 'MEDIO';
            const puntos = conePropagacion(lat, lon, windDir, windSpeed ?? 20, nivel);
            return (
              <Polygon
                key={`cono-${inc.incidenteId}`}
                positions={puntos}
                pathOptions={{
                  color: '#e24b4a',
                  fillColor: '#e24b4a',
                  fillOpacity: 0.18,
                  weight: 1.5,
                  dashArray: '5,4',
                }}
              />
            );
          })}
      </MapContainer>
    </div>
  );
}
