import React from 'react';

interface MonthPickerProps {
  month: number; // 1-12
  year: number;
  onMonthChange: (m: number) => void;
  onYearChange: (y: number) => void;
  label?: string;
}

export const MonthPicker: React.FC<MonthPickerProps> = ({
  month,
  year,
  onMonthChange,
  onYearChange,
  label,
}) => {
  const months = [
    { value: 1, label: 'January' },
    { value: 2, label: 'February' },
    { value: 3, label: 'March' },
    { value: 4, label: 'April' },
    { value: 5, label: 'May' },
    { value: 6, label: 'June' },
    { value: 7, label: 'July' },
    { value: 8, label: 'August' },
    { value: 9, label: 'September' },
    { value: 10, label: 'October' },
    { value: 11, label: 'November' },
    { value: 12, label: 'December' },
  ];

  // Generate range of years (+/- 5 years around current year)
  const currentYear = new Date().getFullYear();
  const years = Array.from({ length: 11 }, (_, i) => currentYear - 5 + i);

  return (
    <div className="flex flex-col gap-1.5">
      {label && (
        <label className="text-xs font-bold text-slate-700 dark:text-slate-300">{label}</label>
      )}
      <div className="flex gap-2">
        <select
          value={month}
          onChange={(e) => onMonthChange(parseInt(e.target.value))}
          className="h-10 px-8 text-xs bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-input text-slate-900 dark:text-slate-50 focus:outline-none focus:border-primary transition-colors"
        >
          {months.map((m) => (
            <option key={m.value} value={m.value}>
              {m.label}
            </option>
          ))}
        </select>

        <select
          value={year}
          onChange={(e) => onYearChange(parseInt(e.target.value))}
          className="h-10 px-8 text-xs bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-input text-slate-900 dark:text-slate-50 focus:outline-none focus:border-primary transition-colors"
        >
          {years.map((y) => (
            <option key={y} value={y}>
              {y}
            </option>
          ))}
        </select>
      </div>
    </div>
  );
};
