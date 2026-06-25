import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';

export default defineConfig(({ mode }) => {
  const isLib = mode === 'lib';

  if (isLib) {
    return {
      plugins: [react()],
      build: {
        lib: {
          entry: resolve(__dirname, 'src/index.js'),
          name: 'SreUi',
          fileName: 'sre-ui',
          formats: ['es', 'umd'],
        },
        rollupOptions: {
          external: ['react', 'react-dom', 'react/jsx-runtime'],
          output: {
            globals: {
              react: 'React',
              'react-dom': 'ReactDOM',
            },
          },
        },
      },
    };
  }

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        '/bff': {
          target: 'http://localhost:8080',
          changeOrigin: true,
        },
        '/incidentes': {
          target: 'http://localhost:8080',
          changeOrigin: true,
        },
        '/recursos': {
          target: 'http://localhost:8080',
          changeOrigin: true,
        },
        '/zonas-riesgo': {
          target: 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },
  };
});
