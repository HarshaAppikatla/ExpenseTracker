import React from 'react';

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
}

export const Card: React.FC<CardProps> = ({ children, className = '', ...props }) => {
  return (
    <div
      className={`rounded-card p-16 bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border shadow-sm dark:shadow-lg transition-all duration-300 ${className}`}
      {...props}
    >
      {children}
    </div>
  );
};
