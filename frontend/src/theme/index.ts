import { createTheme, Theme } from '@mui/material/styles';
import { lightPalette, darkPalette } from './palette';
import { typography } from './typography';
import { shadows } from './shadows';
import { spacing } from './spacing';

export const createAppTheme = (mode: 'light' | 'dark'): Theme => {
  const palette = mode === 'light' ? lightPalette : darkPalette;

  return createTheme({
    palette,
    typography,
    shadows,
    spacing,
    shape: {
      borderRadius: 8,
    },
    components: {
      MuiButton: {
        styleOverrides: {
          root: {
            borderRadius: '12px',
            padding: '8px 16px',
            fontWeight: 500,
            boxShadow: 'none',
            '&:hover': {
              boxShadow: 'none',
            },
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            borderRadius: '16px',
            boxShadow: shadows[1],
            backgroundImage: 'none',
          },
        },
      },
      MuiDialog: {
        styleOverrides: {
          paper: {
            borderRadius: '20px',
            boxShadow: shadows[24],
            padding: '16px',
          },
        },
      },
      MuiOutlinedInput: {
        styleOverrides: {
          root: {
            borderRadius: '12px',
            '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
              borderWidth: '1.5px',
            },
          },
        },
      },
    },
  });
};
