import { PaletteOptions } from '@mui/material/styles';

export const lightPalette: PaletteOptions = {
  mode: 'light',
  primary: {
    main: '#2563EB',
    dark: '#1D4ED8',
    contrastText: '#FFFFFF',
  },
  secondary: {
    main: '#14B8A6',
    contrastText: '#FFFFFF',
  },
  background: {
    default: '#F8FAFC',
    paper: '#FFFFFF',
  },
  text: {
    primary: '#0F172A',
    secondary: '#64748B',
  },
  divider: '#E2E8F0',
};

export const darkPalette: PaletteOptions = {
  mode: 'dark',
  primary: {
    main: '#2563EB',
    dark: '#1D4ED8',
    contrastText: '#FFFFFF',
  },
  secondary: {
    main: '#14B8A6',
    contrastText: '#FFFFFF',
  },
  background: {
    default: '#0F172A',
    paper: '#1E293B',
  },
  text: {
    primary: '#F8FAFC',
    secondary: '#64748B',
  },
  divider: '#334155',
};
