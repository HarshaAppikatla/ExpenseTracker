import React from 'react';

interface DatePickerProps {
  value: string;
  onChange: (val: string) => void;
  label?: string;
  error?: string;
  required?: boolean;
  minDate?: string;
  maxDate?: string;
}

export const DatePicker: React.FC<DatePickerProps> = ({
  value,
  onChange,
  label,
  error,
  required = false,
  minDate,
  maxDate,
}) => {
  return (
    <div className="flex flex-col gap-1.5">
      {label && (
        <label className="text-xs font-bold text-slate-700 dark:text-slate-300">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
      )}
      <input
        type="date"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        min={minDate}
        max={maxDate}
        className={`w-full h-11 px-12 text-xs bg-white dark:bg-dark-surface border rounded-input text-slate-900 dark:text-slate-50 focus:outline-none focus:border-primary transition-colors ${
          error ? 'border-red-500 focus:border-red-500' : 'border-light-border dark:border-dark-border'
        }`}
      />
      {error && <span className="text-[10px] text-red-500 font-semibold">{error}</span>}
    </div>
  );
};
