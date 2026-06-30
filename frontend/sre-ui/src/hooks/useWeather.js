import { useEffect, useState, useCallback } from 'react';

const LAT = -33.47;
const LON = -70.68;

const AQI_LABEL = (v) => {
  if (v == null) return { label: '—', cls: '' };
  if (v <= 20)  return { label: 'Buena', cls: 'aq-buena' };
  if (v <= 40)  return { label: 'Aceptable', cls: 'aq-aceptable' };
  if (v <= 60)  return { label: 'Moderada', cls: 'aq-moderada' };
  if (v <= 80)  return { label: 'Mala', cls: 'aq-mala' };
  return        { label: 'Muy mala', cls: 'aq-muymala' };
};

export function useWeather() {
  const [weather, setWeather] = useState(null);
  const [aqi, setAqi] = useState(null);
  const [hora, setHora] = useState('');
  const [error, setError] = useState(null);

  const fetchData = useCallback(async () => {
    try {
      const [wRes, aRes] = await Promise.all([
        fetch(
          `https://api.open-meteo.com/v1/forecast?latitude=${LAT}&longitude=${LON}` +
          `&current=temperature_2m,apparent_temperature,wind_speed_10m,wind_gusts_10m,wind_direction_10m,weather_code&wind_speed_unit=kmh`
        ),
        fetch(
          `https://air-quality-api.open-meteo.com/v1/air-quality?latitude=${LAT}&longitude=${LON}` +
          `&current=european_aqi,pm2_5`
        ),
      ]);
      const wData = await wRes.json();
      const aData = await aRes.json();
      setWeather(wData.current);
      const aqiVal = aData.current?.european_aqi;
      setAqi({ value: aqiVal, pm25: aData.current?.pm2_5, ...AQI_LABEL(aqiVal) });
    } catch (e) {
      setError('Sin datos meteorológicos');
    }
  }, []);

  useEffect(() => {
    fetchData();
    const tick = setInterval(() => {
      const now = new Date();
      setHora(now.toLocaleTimeString('es-CL', { hour: '2-digit', minute: '2-digit', second: '2-digit' }));
    }, 1000);
    const refresh = setInterval(fetchData, 10 * 60 * 1000);
    return () => { clearInterval(tick); clearInterval(refresh); };
  }, [fetchData]);

  return { weather, aqi, hora, error };
}
