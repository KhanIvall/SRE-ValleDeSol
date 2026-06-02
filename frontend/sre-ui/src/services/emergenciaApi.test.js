import { describe, expect, it, vi, beforeEach } from 'vitest';
import { fetchEmergenciaResumen } from './emergenciaApi.js';

describe('emergenciaApi (Facade HTTP)', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  it('fetchEmergenciaResumen retorna JSON cuando la respuesta es OK', async () => {
    const mockResumen = { incidenteId: 1, incidente: { tipo: 'INCENDIO' } };
    fetch.mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockResumen),
    });

    const result = await fetchEmergenciaResumen(1);

    expect(result).toEqual(mockResumen);
    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining('/bff/emergencias/1/resumen'),
      expect.objectContaining({ headers: expect.any(Object) }),
    );
  });

  it('fetchEmergenciaResumen lanza error en respuesta no OK', async () => {
    fetch.mockResolvedValue({
      ok: false,
      status: 401,
      text: () => Promise.resolve('Unauthorized'),
    });

    await expect(fetchEmergenciaResumen(1)).rejects.toThrow('Unauthorized');
  });
});
