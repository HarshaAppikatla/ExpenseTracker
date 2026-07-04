import React from 'react';
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip
} from 'recharts';

interface PieData {
  name: string;
  value: number;
  color: string;
  [key: string]: any;
}

interface ReusablePieChartProps {
  data: PieData[];
  height?: number;
  innerRadius?: number;
  outerRadius?: number;
  centerLabel?: string;
  centerValue?: string;
  currencySymbol?: string;
  onSliceClick?: (data: PieData) => void;
}

export const ReusablePieChart: React.FC<ReusablePieChartProps> = ({
  data,
  height = 200,
  innerRadius = 50,
  outerRadius = 75,
  centerLabel,
  centerValue,
  currencySymbol = '$',
  onSliceClick,
}) => {
  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      const pData = payload[0].payload;
      return (
        <div className="bg-white dark:bg-dark-surface p-4 rounded-xl border border-light-border dark:border-dark-border shadow-lg text-xs">
          <p className="font-bold text-slate-900 dark:text-slate-100">{pData.name}</p>
          <p className="font-semibold mt-1" style={{ color: pData.color }}>
            Amount: {currencySymbol}{Number(pData.value).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </p>
        </div>
      );
    }
    return null;
  };

  return (
    <div style={{ height, width: '100%' }} className="relative flex items-center justify-center">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Tooltip content={<CustomTooltip />} />
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            innerRadius={innerRadius}
            outerRadius={outerRadius}
            paddingAngle={3}
            dataKey="value"
            onClick={(e) => onSliceClick && onSliceClick(e.payload)}
            className="focus:outline-none cursor-pointer"
          >
            {data.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={entry.color} />
            ))}
          </Pie>
        </PieChart>
      </ResponsiveContainer>
      
      {/* Centered label inside donut chart */}
      {(centerLabel || centerValue) && (
        <div className="absolute flex flex-col items-center justify-center pointer-events-none">
          {centerLabel && <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">{centerLabel}</span>}
          {centerValue && <span className="text-sm font-extrabold text-slate-800 dark:text-slate-100">{centerValue}</span>}
        </div>
      )}
    </div>
  );
};
