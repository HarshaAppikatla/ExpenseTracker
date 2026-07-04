import React from 'react';
import { useCategories } from '@/features/category/hooks/useCategories';

interface CategorySelectorProps {
  value: string;
  onChange: (val: string) => void;
  label?: string;
  error?: string;
  required?: boolean;
}

export const CategorySelector: React.FC<CategorySelectorProps> = ({
  value,
  onChange,
  label = 'Category',
  error,
  required = false,
}) => {
  const { data: categories = [], isLoading } = useCategories();

  return (
    <div className="flex flex-col gap-1.5">
      {label && (
        <label className="text-xs font-bold text-slate-700 dark:text-slate-300">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
      )}
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        disabled={isLoading}
        className={`w-full h-11 px-12 text-xs bg-white dark:bg-dark-surface border rounded-input text-slate-900 dark:text-slate-50 focus:outline-none focus:border-primary transition-colors ${
          error ? 'border-red-500 focus:border-red-500' : 'border-light-border dark:border-dark-border'
        }`}
      >
        <option value="">{isLoading ? 'Loading categories...' : 'Select Category'}</option>
        {categories.map((c) => (
          <option key={c.id} value={c.id}>
            {c.name}
          </option>
        ))}
      </select>
      {error && <span className="text-[10px] text-red-500 font-semibold">{error}</span>}
    </div>
  );
};
