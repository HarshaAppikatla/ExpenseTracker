import React from 'react';
import { UseFormRegisterReturn } from 'react-hook-form';

interface TextInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  register?: UseFormRegisterReturn;
}

export const TextInput: React.FC<TextInputProps> = ({
  label,
  error,
  register,
  className = '',
  ...props
}) => {
  return (
    <div className="flex flex-col gap-6 w-full">
      <label className="text-xs font-semibold text-light-textSecondary dark:text-slate-400">
        {label}
      </label>
      <input
        className={`px-16 py-12 rounded-input border transition-all outline-none text-sm bg-transparent ${
          error
            ? 'border-danger focus:border-danger focus:ring-1 focus:ring-danger'
            : 'border-light-border dark:border-dark-border focus:border-primary focus:ring-1 focus:ring-primary text-light-text dark:text-dark-text'
        } ${className}`}
        {...register}
        {...props}
      />
      {error && <span className="text-xs font-medium text-danger">{error}</span>}
    </div>
  );
};
