import { useWeather } from '../hooks/useWeather.js';

function gradosACardinal(deg) {
  const dirs = ['N','NE','E','SE','S','SO','O','NO'];
  return dirs[Math.round((deg ?? 0) / 45) % 8];
}

const COMPANIAS = [
  { numero: 1, nombre: 'Primera Compañía — Bomba España' },
  { numero: 2, nombre: 'Segunda Compañía — Bomba Santiago' },
  { numero: 3, nombre: 'Tercera Compañía — Bomba Yungay' },
  { numero: 4, nombre: 'Cuarta Compañía — Bomba Atacama' },
  { numero: 5, nombre: 'Quinta Compañía — Bomba Mapocho' },
  { numero: 6, nombre: 'Sexta Compañía — Bomba Damas' },
];

export default function WeatherBar({ onWeatherUpdate }) {
  const { weather, aqi, hora } = useWeather();

  const temp     = weather?.temperature_2m != null ? `${Math.round(weather.temperature_2m)}°C` : '—';
  const sens     = weather?.apparent_temperature != null ? `${Math.round(weather.apparent_temperature)}°C` : '';
  const viento   = weather?.wind_speed_10m != null ? `${Math.round(weather.wind_speed_10m)} km/h` : '—';
  const racha    = weather?.wind_gusts_10m != null ? `Racha ${Math.round(weather.wind_gusts_10m)} km/h` : '';
  const windDeg  = weather?.wind_direction_10m;
  const cardinal = windDeg != null ? gradosACardinal(windDeg) : '';

  // Notifica al padre los datos de viento para el mapa
  if (onWeatherUpdate && weather) {
    onWeatherUpdate({ windDir: windDeg, windSpeed: weather.wind_speed_10m });
  }

  return (
    <div className="weather-bar">
      {/* Reloj */}
      <div className="wb-item wb-reloj">
        <span className="wb-icon">🕐</span>
        <div>
          <div className="wb-val">{hora || '—'}</div>
          <div className="wb-label">Hora local</div>
        </div>
      </div>

      {/* Temperatura */}
      <div className="wb-item">
        <span className="wb-icon">🌡️</span>
        <div>
          <div className="wb-val">{temp}</div>
          <div className="wb-label">Sensación {sens}</div>
        </div>
      </div>

      {/* Viento */}
      <div className="wb-item">
        <span className="wb-icon">💨</span>
        <div>
          <div className="wb-val">
            {viento}
            {cardinal && (
              <span style={{ marginLeft: 6, fontSize: '0.8rem', color: 'var(--accent-soft)' }}>
                {cardinal} <span style={{ display:'inline-block', transform:`rotate(${windDeg}deg)` }}>➤</span>
              </span>
            )}
          </div>
          <div className="wb-label">{racha || 'Viento'}</div>
        </div>
      </div>

      {/* Calidad del aire */}
      <div className="wb-item">
        <span className="wb-icon">🌫️</span>
        <div>
          <div className={`wb-val ${aqi?.cls ?? ''}`}>{aqi?.label ?? '—'}</div>
          <div className="wb-label">Calidad aire{aqi?.pm25 != null ? ` · PM2.5 ${Math.round(aqi.pm25)}` : ''}</div>
        </div>
      </div>

      {/* Compañías */}
      <div className="wb-item wb-companias">
        <span className="wb-icon">🚒</span>
        <div>
          <div className="wb-val">{COMPANIAS.length}</div>
          <div className="wb-label">Compañías activas</div>
        </div>
        <div className="companias-tooltip">
          {COMPANIAS.map((c) => (
            <div key={c.numero} className="compania-item">
              <span className="compania-num">{c.numero}ª</span> {c.nombre}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
