import { describe, expect, it, vi, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useEmergenciaResumen } from './useEmergenciaResumen.js';

vi.mock('../services/emergenciaApi.js', () => ({
  fetchEmergenciaResumen: vi.fn(),
}));

import { fetchEmergenciaResumen } from '../services/emergenciaApi.js';

describe('useEmergenciaResumen', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('carga resumen exitosamente', async () => {
    const mock = { incidenteId: 1, incidente: { tipo: 'INCENDIO' } };
    fetchEmergenciaResumen.mockResolvedValue(mock);

    const { result } = renderHook(() => useEmergenciaResumen());

    await act(async () => {
      await result.current.cargar(1);
    });

    expect(result.current.data).toEqual(mock);
    expect(result.current.error).toBeNull();
    expect(result.current.loading).toBe(false);
  });

  it('registra error cuando falla la API', async () => {
    fetchEmergenciaResumen.mockRejectedValue(new Error('401 Unauthorized'));

    const { result } = renderHook(() => useEmergenciaResumen());

    await act(async () => {
      await result.current.cargar(1);
    });

    expect(result.current.data).toBeNull();
    expect(result.current.error).toContain('401');
  });
});
