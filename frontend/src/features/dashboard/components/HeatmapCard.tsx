import React from 'react';
import { Link } from 'react-router-dom';
import { BarChart2, ArrowRight } from 'lucide-react';
import { Transaction } from '../services/dashboardService';

interface HeatmapCardProps {
  transactions: Transaction[];
}

export const HeatmapCard: React.FC<HeatmapCardProps> = ({ transactions }) => {
  // Let's create a 7x7 grid representing 7 days of the week over the last 7 weeks
  // rows represent days of week (0 = Sunday, 1 = Monday, etc.)
  // columns represent weeks (0 = 6 weeks ago, ..., 6 = this week)
  const rows = [0, 1, 2, 3, 4, 5, 6];
  const columns = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]; // 11 weeks of history fits nicely in 1/4 width card

  // Map transactions to date buckets
  const getTransactionCountsByDay = () => {
    const counts: { [key: string]: number } = {};

    transactions.forEach((tx) => {
      const dateStr = new Date(tx.date).toDateString();
      counts[dateStr] = (counts[dateStr] || 0) + 1;
    });

    return counts;
  };

  const activityCounts = getTransactionCountsByDay();

  // Helper to get color shade based on activity count
  const getCellColor = (colIdx: number, rowIdx: number) => {
    const today = new Date();
    // Calculate the date corresponding to this cell in the grid
    // colIdx = weeks ago, rowIdx = day of week
    const currentDayOfWeek = today.getDay();
    const daysOffset = (10 - colIdx) * 7 + (currentDayOfWeek - rowIdx);
    const targetDate = new Date();
    targetDate.setDate(today.getDate() - daysOffset);

    // Don't render future cells
    if (targetDate > today) {
      return 'bg-slate-50 dark:bg-slate-800/40 opacity-20';
    }

    const count = activityCounts[targetDate.toDateString()] || 0;

    if (count === 0) return 'bg-slate-100 dark:bg-slate-800';
    if (count === 1) return 'bg-blue-200 dark:bg-blue-900/60';
    if (count === 2) return 'bg-blue-400 dark:bg-blue-700/80';
    return 'bg-blue-600 dark:bg-blue-500';
  };

  return (
    <div className="bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 rounded-[20px] p-[20px] shadow-sm hover:shadow-md transition-all duration-200 hover:-translate-y-[2px] transform flex flex-col justify-between h-[180px] w-full">
      <div>
        {/* Header */}
        <h2 className="text-[13px] font-bold text-slate-500 dark:text-slate-400 flex items-center gap-[6px]">
          <BarChart2 className="w-[15px] h-[15px] text-slate-400" />
          Activity Heatmap
        </h2>
        <p className="text-[11px] text-slate-400 mt-[2px] font-medium">Your expense activity</p>

        {/* Heatmap Grid */}
        <div className="flex items-center gap-[8px] mt-[16px]">
          {/* Day Labels */}
          <div className="flex flex-col justify-between h-[60px] text-[8px] font-bold text-slate-400 select-none pr-[2px]">
            <span>M</span>
            <span>W</span>
            <span>F</span>
          </div>

          {/* Grid Blocks */}
          <div className="flex gap-[4px]">
            {columns.map((col) => (
              <div key={col} className="flex flex-col gap-[4px]">
                {rows.map((row) => (
                  <div
                    key={row}
                    className={`w-[8px] h-[8px] rounded-[2px] ${getCellColor(col, row)} transition-colors duration-300`}
                  />
                ))}
              </div>
            ))}
          </div>
        </div>

        {/* Legend */}
        <div className="flex items-center justify-end gap-[4px] text-[8px] font-bold text-slate-400 mt-[8px] select-none">
          <span>Less</span>
          <div className="w-[6px] h-[6px] rounded-[1px] bg-slate-100 dark:bg-slate-800" />
          <div className="w-[6px] h-[6px] rounded-[1px] bg-blue-200 dark:bg-blue-900/60" />
          <div className="w-[6px] h-[6px] rounded-[1px] bg-blue-400 dark:bg-blue-700/80" />
          <div className="w-[6px] h-[6px] rounded-[1px] bg-blue-600 dark:bg-blue-500" />
          <span>More</span>
        </div>
      </div>

      {/* Footer link */}
      <div className="flex justify-end pt-[8px]">
        <Link
          to="/insights"
          className="text-[12px] font-bold text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 flex items-center gap-[4px] transition-all hover:translate-x-[2px] duration-150"
        >
          View full activity
          <ArrowRight className="w-[14px] h-[14px]" />
        </Link>
      </div>
    </div>
  );
};

export default HeatmapCard;
