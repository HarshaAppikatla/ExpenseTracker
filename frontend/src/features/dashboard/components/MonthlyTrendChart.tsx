import React, { useState } from 'react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
} from 'recharts';
import { Calendar } from 'lucide-react';
import { MonthlyTrend } from '../services/dashboardService';

interface MonthlyTrendChartProps {
  monthlyTrends: MonthlyTrend[];
  currencySymbol: string;
}

export const MonthlyTrendChart: React.FC<MonthlyTrendChartProps> = ({
  monthlyTrends,
  currencySymbol,
}) => {
  const [timeframe, setTimeframe] = useState('this-month');

  // Custom Recharts tooltip matching Image 2
  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 p-[12px] rounded-[12px] shadow-lg text-[11px] font-semibold space-y-[4px]">
          <p className="text-slate-500 dark:text-slate-400 mb-[4px]">{label}</p>
          {payload.map((p: any) => (
            <p key={p.name} style={{ color: p.color }} className="flex items-center gap-[6px]">
              <span className="w-1.5 h-1.5 rounded-full" style={{ backgroundColor: p.color }} />
              <span className="text-slate-600 dark:text-slate-350">{p.name}:</span>
              <span className="text-slate-900 dark:text-slate-50">
                {currencySymbol}{Number(p.value).toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 })}
              </span>
            </p>
          ))}
        </div>
      );
    }
    return null;
  };

  return (
    <div className="bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 rounded-[20px] p-[24px] shadow-sm hover:shadow-md transition-all duration-200 hover:-translate-y-[2px] transform flex flex-col justify-between h-[416px]">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-[12px] pb-[16px]">
        <h2 className="text-[14px] font-bold text-slate-900 dark:text-slate-50 flex items-center gap-[6px]">
          <Calendar className="w-[16px] h-[16px] text-slate-400" />
          Monthly Trends (Income vs. Expense)
        </h2>

        {/* Legend & Dropdown */}
        <div className="flex items-center gap-[16px] self-end sm:self-auto">
          {/* Custom Legends */}
          <div className="flex items-center gap-[12px] text-[11px] font-bold">
            <div className="flex items-center gap-[4px] text-slate-500 dark:text-slate-400">
              <span className="w-2 h-2 rounded-full bg-[#10b981]" />
              Income
            </div>
            <div className="flex items-center gap-[4px] text-slate-500 dark:text-slate-400">
              <span className="w-2 h-2 rounded-full bg-[#f43f5e]" />
              Expense
            </div>
          </div>

          {/* Timeframe Selector */}
          <select
            value={timeframe}
            onChange={(e) => setTimeframe(e.target.value)}
            className="text-[11px] font-bold bg-slate-50 hover:bg-slate-100 border border-slate-200 dark:bg-slate-800 dark:border-slate-700 dark:hover:bg-slate-700 px-[12px] py-[6px] rounded-[10px] text-slate-700 dark:text-slate-350 focus:outline-none transition-colors cursor-pointer"
          >
            <option value="this-month">This Month</option>
            <option value="6-months">6 Months</option>
            <option value="12-months">12 Months</option>
          </select>
        </div>
      </div>

      {/* Recharts Area */}
      <div className="flex-1 w-full h-full min-h-[250px] mt-[16px]">
        {monthlyTrends && monthlyTrends.length > 0 ? (
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={monthlyTrends} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#F1F5F9" vertical={false} className="dark:stroke-slate-800" opacity={0.6} />
              <XAxis
                dataKey="monthName"
                stroke="#94A3B8"
                fontSize={10}
                fontWeight={600}
                tickLine={false}
                axisLine={false}
                dy={10}
              />
              <YAxis
                stroke="#94A3B8"
                fontSize={10}
                fontWeight={600}
                tickLine={false}
                axisLine={false}
                dx={-10}
                tickFormatter={(value) => `${currencySymbol}${value}`}
              />
              <Tooltip content={<CustomTooltip />} cursor={{ stroke: '#F1F5F9', strokeWidth: 1.5 }} />
              <Line
                name="Income"
                type="monotone"
                dataKey="income"
                stroke="#10b981"
                strokeWidth={2.5}
                dot={{ r: 3, strokeWidth: 1.5, fill: '#FFFFFF', stroke: '#10b981' }}
                activeDot={{ r: 5, strokeWidth: 1.5, fill: '#10b981', stroke: '#FFFFFF' }}
              />
              <Line
                name="Expense"
                type="monotone"
                dataKey="expense"
                stroke="#f43f5e"
                strokeWidth={2.5}
                dot={{ r: 3, strokeWidth: 1.5, fill: '#FFFFFF', stroke: '#f43f5e' }}
                activeDot={{ r: 5, strokeWidth: 1.5, fill: '#f43f5e', stroke: '#FFFFFF' }}
              />
            </LineChart>
          </ResponsiveContainer>
        ) : (
          <div className="flex items-center justify-center h-full text-[12px] font-medium text-slate-400">
            No monthly trend data available.
          </div>
        )}
      </div>
    </div>
  );
};

export default MonthlyTrendChart;
