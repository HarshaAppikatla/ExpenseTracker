import React from 'react';
import { Link } from 'react-router-dom';
import { Calendar, ArrowRight } from 'lucide-react';
import { RecurringTransaction } from '@/features/recurring/services/recurringService';

interface UpcomingBillsCardProps {
  bills: RecurringTransaction[];
  currencySymbol: string;
}

export const UpcomingBillsCard: React.FC<UpcomingBillsCardProps> = ({
  bills,
  currencySymbol,
}) => {
  // Filter active recurring bills (EXPENSE type)
  const activeBills = bills.filter(b => b.transactionType === 'EXPENSE' && b.status === 'ACTIVE');
  const dueCount = activeBills.length;

  // Sort by nextExecution date to find the soonest
  const sortedBills = [...activeBills].sort((a, b) => {
    return new Date(a.nextExecution).getTime() - new Date(b.nextExecution).getTime();
  });

  const nextBill = sortedBills[0];

  return (
    <div className="bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 rounded-[20px] p-[20px] shadow-sm hover:shadow-md transition-all duration-200 hover:-translate-y-[2px] transform flex flex-col justify-between h-[180px] w-full">
      <div>
        {/* Header */}
        <h2 className="text-[13px] font-bold text-slate-500 dark:text-slate-400 flex items-center gap-[6px]">
          <Calendar className="w-[15px] h-[15px] text-slate-400" />
          Upcoming Bills
        </h2>
        <p className="text-[11px] text-slate-400 mt-[2px] font-medium">
          {dueCount > 0 ? `${dueCount} bills due this month` : 'No bills due this month'}
        </p>

        {/* Content details */}
        {nextBill ? (
          <div className="flex justify-between items-center mt-[20px]">
            <div>
              <h4 className="text-[13px] font-bold text-slate-900 dark:text-slate-100">
                {nextBill.merchant || 'Subscription Bill'}
              </h4>
              <p className="text-[11px] text-slate-450 dark:text-slate-500 mt-[2px]">
                {new Date(nextBill.nextExecution).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })}
              </p>
            </div>
            <span className="text-[13px] font-bold text-slate-900 dark:text-slate-50 shrink-0">
              {currencySymbol}{nextBill.amount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </span>
          </div>
        ) : (
          <div className="mt-[20px] text-[11px] font-medium text-slate-400 text-center py-[4px]">
            All bills are fully paid.
          </div>
        )}
      </div>

      {/* Footer link */}
      <div className="flex justify-end pt-[8px]">
        <Link
          to="/recurring"
          className="text-[12px] font-bold text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 flex items-center gap-[4px] transition-all hover:translate-x-[2px] duration-150"
        >
          View all bills
          <ArrowRight className="w-[14px] h-[14px]" />
        </Link>
      </div>
    </div>
  );
};

export default UpcomingBillsCard;
