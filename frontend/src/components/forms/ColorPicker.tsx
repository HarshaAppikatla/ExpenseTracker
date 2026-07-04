import React from 'react';

interface ColorPickerProps {
  value: string;
  onChange: (val: string) => void;
  label?: string;
}

export const ColorPicker: React.FC<ColorPickerProps> = ({
  value,
  onChange,
  label = 'Select Accent Color',
}) => {
  const colors = [
    '#2563EB', // Blue
    '#14B8A6', // Teal
    '#10B981', // Emerald
    '#F59E0B', // Amber
    '#EF4444', // Red
    '#EC4899', // Pink
    '#8B5CF6', // Purple
    '#6366F1', // Indigo
    '#6B7280', // Slate
    '#B45309', // Brown
  ];

  return (
    <div className="flex flex-col gap-1.5">
      {label && (
        <label className="text-xs font-bold text-slate-700 dark:text-slate-300">{label}</label>
      )}
      <div className="flex flex-wrap gap-3">
        {colors.map((c) => (
          <button
            key={c}
            type="button"
            style={{ backgroundColor: c }}
            onClick={() => onChange(c)}
            className={`w-8 h-8 rounded-full border-2 transition-all duration-150 transform hover:scale-110 ${
              value.toLowerCase() === c.toLowerCase()
                ? 'border-slate-900 dark:border-white ring-2 ring-primary/20 scale-105'
                : 'border-transparent'
            }`}
          />
        ))}
      </div>
    </div>
  );
};
