import { createContext, useContext } from 'react';

const PanelContext = createContext({ resumen: null });

/**
 * Patron Compound Components: composicion flexible del panel de emergencia.
 */
function EmergenciaPanel({ resumen, children, className = '' }) {
  return (
    <PanelContext.Provider value={{ resumen }}>
      <section className={`emergencia-panel ${className}`}>{children}</section>
    </PanelContext.Provider>
  );
}

function Header({ title, subtitle }) {
  const { resumen } = useContext(PanelContext);
  const contingencia = resumen?.datosContingencia;

  return (
    <header className="panel-header">
      <div>
        <h2>{title}</h2>
        {subtitle && <p className="panel-subtitle">{subtitle}</p>}
      </div>
      {contingencia && <span className="badge badge-warning">Contingencia</span>}
    </header>
  );
}

function Body({ children }) {
  return <div className="panel-body">{children}</div>;
}

function Footer({ children }) {
  return <footer className="panel-footer">{children}</footer>;
}

EmergenciaPanel.Header = Header;
EmergenciaPanel.Body = Body;
EmergenciaPanel.Footer = Footer;

export default EmergenciaPanel;
