import React from 'react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Cell
} from 'recharts';

interface BarData {
  name: string;
  value: number;
  color?: string;
}

interface ReusableBarChartProps {
  data: BarData[];
  height?: number;
  currencySymbol?: string;
  defaultColor?: string;
}

export const ReusableBarChart: React.FC<ReusableBarChartProps> = ({
  data,
  height = 300,
  currencySymbol = '$',
  defaultColor = '#6366F1',
}) => {
  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white dark:bg-dark-surface p-4 rounded-xl border border-light-border dark:border-dark-border shadow-lg text-xs">
          <p className="font-bold text-slate-900 dark:text-slate-100">{label}</p>
          <p className="font-semibold mt-1 text-primary">
            Value: {currencySymbol}{Number(payload[0].value).toLocaleString(undefined, { minimumFractionDigits: 2 })}
          </p>
        </div>
      );
    }
    return null;
  };

  return (
    <div style={{ height, width: '100%' }}>
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
          <XAxis dataKey="name" stroke="#94A3B8" fontSize={11} tickLine={false} axisLine={false} />
          <YAxis stroke="#94A3B8" fontSize={11} tickLine={false} axisLine={false} />
          <Tooltip content={<CustomTooltip />} />
          <Bar dataKey="value" radius={[4, 4, 0, 0]}>
            {data.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={entry.color || defaultColor} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};
