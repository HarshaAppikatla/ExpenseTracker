import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { PasswordInput } from '../PasswordInput';

describe('PasswordInput Component', () => {
  it('renders input with type password initially', () => {
    render(<PasswordInput label="Test Password" placeholder="placeholder" />);
    
    const input = screen.getByPlaceholderText('placeholder') as HTMLInputElement;
    expect(input.type).toBe('password');
  });

  it('toggles password visibility when eye icon is clicked', () => {
    render(<PasswordInput label="Test Password" placeholder="placeholder" />);
    
    const input = screen.getByPlaceholderText('placeholder') as HTMLInputElement;
    const button = screen.getByRole('button');

    // Click to show password
    fireEvent.click(button);
    expect(input.type).toBe('text');

    // Click again to hide
    fireEvent.click(button);
    expect(input.type).toBe('password');
  });

  it('renders error message if provided', () => {
    render(<PasswordInput label="Test Password" error="Invalid format" />);
    expect(screen.getByText('Invalid format')).toBeInTheDocument();
  });
});
