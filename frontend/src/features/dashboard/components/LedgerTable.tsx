import React from 'react';
import { Link } from 'react-router-dom';
import { 
  Car, 
  Home, 
  ShoppingBag, 
  Utensils, 
  HelpCircle,
  Briefcase,
  DollarSign
} from 'lucide-react';
import { Transaction } from '../services/dashboardService';

interface LedgerTableProps {
  transactions: Transaction[];
  currencySymbol: string;
}

// Icon selection helper based on category/merchant names
const getTransactionIcon = (tx: Transaction) => {
  if (tx.type === 'INCOME') {
    return {
      icon: DollarSign,
      bg: 'bg-emerald-50 text-emerald-600 dark:bg-emerald-950/20 dark:text-emerald-400',
    };
  }

  const cat = (tx.categoryName || '').toLowerCase();
  const desc = (tx.sourceOrMerchant || '').toLowerCase();

  if (cat.includes('transport') || desc.includes('metro') || desc.includes('uber') || desc.includes('lyft') || cat.includes('travel')) {
    return {
      icon: Car,
      bg: 'bg-blue-50 text-blue-600 dark:bg-blue-950/20 dark:text-blue-400',
    };
  }
  if (cat.includes('lodging') || cat.includes('housing') || desc.includes('booking') || desc.includes('hotel') || desc.includes('rent')) {
    return {
      icon: Home,
      bg: 'bg-violet-50 text-violet-600 dark:bg-violet-950/20 dark:text-violet-400',
    };
  }
  if (cat.includes('shopping') || cat.includes('clothing') || desc.includes('store') || desc.includes('amazon')) {
    return {
      icon: ShoppingBag,
      bg: 'bg-pink-50 text-pink-600 dark:bg-pink-950/20 dark:text-pink-400',
    };
  }
  if (cat.includes('food') || cat.includes('dining') || desc.includes('restaurant') || desc.includes('cafe') || desc.includes('taj') || desc.includes('starbucks')) {
    return {
      icon: Utensils,
      bg: 'bg-amber-50 text-amber-600 dark:bg-amber-950/20 dark:text-amber-400',
    };
  }
  if (cat.includes('work') || desc.includes('freelance') || desc.includes('salary') || desc.includes('project')) {
    return {
      icon: Briefcase,
      bg: 'bg-indigo-50 text-indigo-600 dark:bg-indigo-950/20 dark:text-indigo-400',
    };
  }

  return {
    icon: HelpCircle,
    bg: 'bg-slate-50 text-slate-600 dark:bg-slate-800 dark:text-slate-400',
  };
};

export const LedgerTable: React.FC<LedgerTableProps> = ({
  transactions,
  currencySymbol,
}) => {
  return (
    <div className="bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 rounded-[20px] p-[24px] shadow-sm hover:shadow-md transition-all duration-200 hover:-translate-y-[2px] transform flex flex-col justify-between min-h-[440px]">
      <div>
        {/* Header */}
        <div className="flex justify-between items-center pb-[16px] border-b border-slate-100 dark:border-slate-800/80">
          <h2 className="text-[14px] font-bold text-slate-900 dark:text-slate-50">
            Recent Ledger Activity
          </h2>
          <Link
            to="/transactions"
            className="text-[12px] font-bold text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 transition-colors"
          >
            View All
          </Link>
        </div>

        {/* Transactions list */}
        {!transactions || transactions.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-[48px] text-center">
            <span className="text-[12px] font-medium text-slate-400 dark:text-slate-500">No transactions recorded.</span>
          </div>
        ) : (
          <div className="divide-y divide-slate-100 dark:divide-slate-800/60">
            {transactions.slice(0, 7).map((tx) => {
              const style = getTransactionIcon(tx);
              const TxIcon = style.icon;
              const isIncome = tx.type === 'INCOME';

              return (
                <div key={tx.id} className="flex items-center justify-between py-[18px] first:pt-[18px] last:pb-0 transition-all duration-150 hover:bg-slate-50/30 dark:hover:bg-slate-800/10 px-[4px] rounded-[8px]">
                  {/* Left content: Icon and Details */}
                  <div className="flex items-center gap-[12px] min-w-0">
                    <div className={`w-[36px] h-[36px] rounded-full flex items-center justify-center shrink-0 ${style.bg}`}>
                      <TxIcon className="w-[18px] h-[18px]" />
                    </div>
                    <div className="min-w-0">
                      <h4 className="text-[13px] font-bold text-slate-900 dark:text-slate-100 truncate">
                        {tx.sourceOrMerchant || 'Untitled'}
                      </h4>
                      <p className="text-[11px] text-slate-500 dark:text-slate-500 mt-[2px] truncate">
                        {tx.categoryName || (isIncome ? 'Income' : 'General')} &bull; {new Date(tx.date).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })}
                      </p>
                    </div>
                  </div>

                  {/* Right content: Status & Amount */}
                  <div className="flex items-center gap-[16px] shrink-0">
                    {/* Status Pill */}
                    <span className={`text-[10px] font-bold px-[8px] py-[2px] rounded-full border hidden sm:inline-block ${
                      isIncome 
                        ? 'bg-emerald-50/50 text-emerald-600 border-emerald-100 dark:bg-emerald-950/20 dark:text-emerald-400 dark:border-emerald-950/40' 
                        : 'bg-slate-50 text-slate-500 border-slate-100 dark:bg-slate-800/40 dark:text-slate-400 dark:border-slate-800'
                    }`}>
                      {isIncome ? 'Received' : 'Paid'}
                    </span>

                    {/* Amount */}
                    <span className={`text-[13px] font-bold ${
                      isIncome 
                        ? 'text-[#10b981]' 
                        : 'text-slate-900 dark:text-slate-50'
                    }`}>
                      {isIncome ? '+' : '-'} {currencySymbol}{tx.amount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default LedgerTable;
