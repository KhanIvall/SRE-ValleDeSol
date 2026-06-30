import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { useIncidentesActivos } from './useIncidentesActivos.js';

describe('useIncidentesActivos', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
    vi.stubEnv('VITE_API_BASE_URL', 'http://localhost:8080');
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.unstubAllEnvs();
  });

  it('carga incidentes activos excluyendo CERRADO', async () => {
    fetch.mockResolvedValue({
      ok: true,
      json: () => Promise.resolve([
        { id: 1, estado: 'EN_PROGRESO', tipo: 'INCENDIO' },
        { id: 2, estado: 'CERRADO', tipo: 'INCENDIO' },
      ]),
    });

    const { result } = renderHook(() => useIncidentesActivos());

    await waitFor(() => expect(result.current.online).toBe(true));
    expect(result.current.incidentes).toHaveLength(1);
    expect(result.current.incidentes[0].id).toBe(1);
    expect(fetch).toHaveBeenCalledWith(
      'http://localhost:8080/incidentes',
      expect.objectContaining({ signal: expect.any(AbortSignal) }),
    );
  });

  it('marca offline cuando fetch falla', async () => {
    fetch.mockRejectedValue(new Error('network'));

    const { result } = renderHook(() => useIncidentesActivos());

    await waitFor(() => expect(result.current.online).toBe(false));
    expect(result.current.incidentes).toHaveLength(0);
  });

  it('refetch actualiza la lista', async () => {
    fetch.mockResolvedValue({
      ok: true,
      json: () => Promise.resolve([{ id: 3, estado: 'REPORTADO' }]),
    });

    const { result } = renderHook(() => useIncidentesActivos());
    await waitFor(() => expect(result.current.online).toBe(true));

    await act(async () => {
      await result.current.refetch();
    });

    expect(result.current.incidentes[0].id).toBe(3);
  });
});
