import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
import { CategorySpend } from '../services/dashboardService';

interface CategoryListProps {
  categories: CategorySpend[];
  currencySymbol: string;
}

export const CategoryList: React.FC<CategoryListProps> = ({
  categories,
  currencySymbol,
}) => {
  // Calculate total amount to compute percentages if missing
  const totalAmount = categories.reduce((sum, cat) => sum + cat.amount, 0);

  return (
    <div className="bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 rounded-[20px] p-[24px] shadow-sm hover:shadow-md transition-all duration-200 hover:-translate-y-[2px] transform flex flex-col justify-between min-h-[440px]">
      <div>
        {/* Header */}
        <div className="flex justify-between items-center pb-[16px] border-b border-slate-100 dark:border-slate-800/80">
          <h2 className="text-[14px] font-bold text-slate-900 dark:text-slate-50">
            Top Categories
          </h2>
          <select className="text-[11px] font-bold bg-slate-50 border border-slate-200 dark:bg-slate-800 dark:border-slate-700 px-[8px] py-[4px] rounded-[8px] text-slate-650 dark:text-slate-350 focus:outline-none cursor-pointer">
            <option value="this-month">This Month</option>
            <option value="6-months">6 Months</option>
          </select>
        </div>

        {/* Categories list */}
        {!categories || categories.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-[48px] text-center">
            <span className="text-[12px] font-medium text-slate-400 dark:text-slate-500">No category spending recorded.</span>
          </div>
        ) : (
          <div className="space-y-[16px] mt-[16px]">
            {categories.slice(0, 6).map((cat) => {
              const percentage = totalAmount > 0 ? Math.round((cat.amount / totalAmount) * 100) : 0;
              
              return (
                <div key={cat.categoryId} className="flex items-center justify-between py-[4px]">
                  {/* Category Name & Color Dot */}
                  <div className="flex items-center gap-[10px] min-w-0">
                    <span 
                      className="w-[8px] h-[8px] rounded-full shrink-0" 
                      style={{ backgroundColor: cat.color || '#CBD5E1' }} 
                    />
                    <span className="text-[13px] font-semibold text-slate-700 dark:text-slate-300 truncate">
                      {cat.categoryName}
                    </span>
                  </div>

                  {/* Value and Percentage Pill */}
                  <div className="flex items-center gap-[12px] shrink-0">
                    <span className="text-[13px] font-bold text-slate-900 dark:text-slate-100">
                      {currencySymbol}{cat.amount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </span>
                    <span className="text-[11px] font-bold text-slate-500 bg-slate-50 border border-slate-100 dark:bg-slate-800 dark:text-slate-400 dark:border-slate-750 px-[8px] py-[2px] rounded-[8px] min-w-[36px] text-center">
                      {percentage}%
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Footer CTA */}
      <div className="border-t border-slate-100 dark:border-slate-800/80 pt-[12px] flex justify-end">
        <Link
          to="/categories"
          className="text-[12px] font-bold text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 flex items-center gap-[4px] transition-all hover:translate-x-[2px] duration-150"
        >
          View all categories
          <ArrowRight className="w-[14px] h-[14px]" />
        </Link>
      </div>
    </div>
  );
};

export default CategoryList;
