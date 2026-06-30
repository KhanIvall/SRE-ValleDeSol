import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { useWeather } from './useWeather.js';

describe('useWeather', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('carga datos meteorológicos y calidad del aire', async () => {
    fetch
      .mockResolvedValueOnce({
        json: () => Promise.resolve({
          current: { temperature_2m: 22, wind_speed_10m: 15, wind_direction_10m: 180 },
        }),
      })
      .mockResolvedValueOnce({
        json: () => Promise.resolve({
          current: { european_aqi: 25, pm2_5: 12 },
        }),
      });

    const { result } = renderHook(() => useWeather());

    await waitFor(() => expect(result.current.weather).not.toBeNull());
    expect(result.current.weather.temperature_2m).toBe(22);
    expect(result.current.aqi.label).toBe('Aceptable');
    expect(result.current.error).toBeNull();
  });

  it('registra error cuando falla la API externa', async () => {
    fetch.mockRejectedValue(new Error('offline'));

    const { result } = renderHook(() => useWeather());

    await waitFor(() => expect(result.current.error).toBe('Sin datos meteorológicos'));
  });
});
