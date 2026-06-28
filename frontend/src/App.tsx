import React from 'react';
import { BrowserRouter } from 'react-router-dom';
import { AppProviders } from './core/providers/AppProviders';
import { AppRoutes } from './routes';

const App: React.FC = () => {
  return (
    <BrowserRouter>
      <AppProviders>
        <AppRoutes />
      </AppProviders>
    </BrowserRouter>
  );
};

export default App;
export { App };
