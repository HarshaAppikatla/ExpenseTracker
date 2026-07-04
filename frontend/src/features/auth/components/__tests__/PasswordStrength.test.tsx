import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { PasswordStrength } from '../PasswordStrength';

describe('PasswordStrength Component', () => {
  it('renders nothing if password is empty', () => {
    render(<PasswordStrength password="" />);
    expect(screen.queryByText(/Password Strength:/i)).not.toBeInTheDocument();
  });

  it('renders Weak for short password', () => {
    render(<PasswordStrength password="abc" />);
    expect(screen.getByText(/Password Strength:/i)).toBeInTheDocument();
    expect(screen.getByText('Weak')).toBeInTheDocument();
  });

  it('renders Medium for intermediate passwords', () => {
    render(<PasswordStrength password="Password1" />);
    // Passes length (8), uppercase (P), lowercase, number (1) = 4 checks passed
    expect(screen.getByText('Medium')).toBeInTheDocument();
  });

  it('renders Strong for complete password matching all rules', () => {
    render(<PasswordStrength password="Password123!" />);
    expect(screen.getByText('Strong')).toBeInTheDocument();
  });
});
