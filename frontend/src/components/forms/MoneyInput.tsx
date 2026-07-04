import React from 'react';

interface MoneyInputProps {
  amountValue: string;
  onAmountChange: (val: string) => void;
  currencyValue: string;
  onCurrencyChange: (val: string) => void;
  label?: string;
  amountError?: string;
  required?: boolean;
}

export const MoneyInput: React.FC<MoneyInputProps> = ({
  amountValue,
  onAmountChange,
  currencyValue,
  onCurrencyChange,
  label = 'Amount',
  amountError,
  required = false,
}) => {
  const currencies = ['USD', 'EUR', 'GBP', 'INR', 'JPY', 'CAD', 'AUD'];

  const inputId = label.toLowerCase().replace(/\s+/g, '-');

  return (
    <div className="flex flex-col gap-1.5">
      {label && (
        <label htmlFor={inputId} className="text-xs font-bold text-slate-700 dark:text-slate-300">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
      )}
      <div className="flex gap-2">
        {/* Currency Select */}
        <select
          value={currencyValue}
          onChange={(e) => onCurrencyChange(e.target.value)}
          className="h-11 px-8 text-xs bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-input text-slate-900 dark:text-slate-50 focus:outline-none focus:border-primary transition-colors shrink-0"
        >
          {currencies.map((curr) => (
            <option key={curr} value={curr}>
              {curr}
            </option>
          ))}
        </select>

        {/* Amount Input */}
        <div className="relative flex-1">
          <input
            id={inputId}
            type="number"
            step="0.01"
            min="0"
            placeholder="0.00"
            value={amountValue}
            onChange={(e) => onAmountChange(e.target.value)}
            className={`w-full h-11 px-12 text-xs bg-white dark:bg-dark-surface border rounded-input text-slate-900 dark:text-slate-50 focus:outline-none focus:border-primary transition-colors ${
              amountError ? 'border-red-500 focus:border-red-500' : 'border-light-border dark:border-dark-border'
            }`}
          />
        </div>
      </div>
      {amountError && <span className="text-[10px] text-red-500 font-semibold">{amountError}</span>}
    </div>
  );
};
