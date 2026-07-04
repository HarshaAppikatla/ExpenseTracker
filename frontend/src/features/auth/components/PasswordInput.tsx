import React, { useState } from 'react';
import { UseFormRegisterReturn } from 'react-hook-form';
import { Eye, EyeOff } from 'lucide-react';

interface PasswordInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  register?: UseFormRegisterReturn;
}

export const PasswordInput: React.FC<PasswordInputProps> = ({
  label,
  error,
  register,
  className = '',
  ...props
}) => {
  const [showPassword, setShowPassword] = useState(false);

  const inputId = label.toLowerCase().replace(/\s+/g, '-');

  return (
    <div className="flex flex-col gap-1.5 w-full">
      <label htmlFor={inputId} className="text-xs font-semibold text-slate-500 dark:text-slate-400">
        {label}
      </label>
      <div className="relative w-full">
        <input
          id={inputId}
          type={showPassword ? 'text' : 'password'}
          className={`w-full px-4 py-3 rounded-xl border transition-all outline-none text-sm bg-transparent pr-10 ${
            error
              ? 'border-red-500 focus:border-red-500 focus:ring-1 focus:ring-red-500'
              : 'border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 focus:ring-indigo-600 text-slate-900 dark:text-slate-50'
          } ${className}`}
          {...register}
          {...props}
        />
        <button
          type="button"
          onClick={() => setShowPassword((prev) => !prev)}
          className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors"
        >
          {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
        </button>
      </div>
      {error && <span className="text-xs font-medium text-red-500">{error}</span>}
    </div>
  );
};
export type PasswordInputPropsType = PasswordInputProps;
