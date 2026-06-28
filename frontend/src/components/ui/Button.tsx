import React from 'react';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'filled' | 'outlined' | 'danger' | 'text';
  children: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'filled',
  children,
  className = '',
  ...props
}) => {
  const baseStyle =
    'px-16 py-8 rounded-btn font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 flex items-center justify-center gap-8 disabled:opacity-50 disabled:cursor-not-allowed';

  const variants = {
    filled: 'bg-primary text-white hover:bg-primary-hover focus:ring-primary',
    outlined:
      'border border-light-border dark:border-dark-border text-light-text dark:text-dark-text hover:bg-slate-100 dark:hover:bg-slate-800 focus:ring-primary',
    danger: 'bg-danger text-white hover:opacity-90 focus:ring-danger',
    text: 'text-primary hover:bg-blue-50 dark:hover:bg-slate-800 focus:ring-primary',
  };

  return (
    <button className={`${baseStyle} ${variants[variant]} ${className}`} {...props}>
      {children}
    </button>
  );
};
