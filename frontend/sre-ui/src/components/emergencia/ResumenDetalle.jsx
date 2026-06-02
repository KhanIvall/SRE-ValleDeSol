function nivelClass(nivel) {
  if (!nivel) return 'riesgo-medio';
  const key = nivel.toLowerCase();
  if (key.includes('critico')) return 'riesgo-critico';
  if (key.includes('alto')) return 'riesgo-alto';
  if (key.includes('bajo')) return 'riesgo-bajo';
  return 'riesgo-medio';
}

export default function ResumenDetalle({ resumen }) {
  if (!resumen) return null;

  const { incidente, recursosAsignados = [], zonaRiesgo, mensajeContingencia } = resumen;

  return (
    <div className="resumen-grid">
      <article className="card">
        <h3>Incidente #{resumen.incidenteId}</h3>
        <dl>
          <dt>Tipo</dt>
          <dd>{incidente?.tipo ?? '—'}</dd>
          <dt>Estado</dt>
          <dd>{incidente?.estado ?? '—'}</dd>
          <dt>Ubicacion</dt>
          <dd>
            {incidente?.latitud != null && incidente?.longitud != null
              ? `${incidente.latitud}, ${incidente.longitud}`
              : 'Sin coordenadas'}
          </dd>
          <dt>Descripcion</dt>
          <dd>{incidente?.descripcion || '—'}</dd>
        </dl>
      </article>

      <article className="card">
        <h3>Zona de riesgo</h3>
        {zonaRiesgo?.nivelRiesgo ? (
          <>
            <span className={`badge ${nivelClass(zonaRiesgo.nivelRiesgo)}`}>
              {zonaRiesgo.nivelRiesgo}
            </span>
            <dl>
              <dt>Clima</dt>
              <dd>{zonaRiesgo.condicionClimatica ?? '—'}</dd>
              <dt>Temperatura</dt>
              <dd>{zonaRiesgo.temperaturaCelsius ?? '—'} °C</dd>
              <dt>Humedad</dt>
              <dd>{zonaRiesgo.humedadPorcentaje ?? '—'} %</dd>
            </dl>
          </>
        ) : (
          <p className="muted">Sin datos de zona para estas coordenadas.</p>
        )}
      </article>

      <article className="card card-wide">
        <h3>Recursos asignados ({recursosAsignados.length})</h3>
        {recursosAsignados.length === 0 ? (
          <p className="muted">No hay recursos desplegados en este incidente.</p>
        ) : (
          <ul className="recurso-list">
            {recursosAsignados.map((r) => (
              <li key={r.id}>
                <strong>{r.nombre}</strong>
                <span>
                  {r.tipo} · {r.estado}
                </span>
              </li>
            ))}
          </ul>
        )}
      </article>

      {mensajeContingencia && (
        <p className="contingencia-msg" role="alert">
          {mensajeContingencia}
        </p>
      )}
    </div>
  );
}
