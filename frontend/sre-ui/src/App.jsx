import Dashboard from './components/Dashboard.jsx';
import { AlertProvider } from './context/AlertContext.jsx';

export default function App() {
  return (
    <AlertProvider>
      <Dashboard />
    </AlertProvider>
  );
}
