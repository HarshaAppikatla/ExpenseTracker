import React, { useState } from 'react';
import { useTransactions } from '@/features/transaction/hooks/useTransactions';
import { useCategories } from '@/features/category/hooks/useCategories';
import { useProfile } from '@/features/profile/hooks/useProfile';
import { Filter } from 'lucide-react';

export const TransactionsPage: React.FC = () => {
  const { data: profile } = useProfile();
  const currencySymbol = profile?.preferredCurrency || '$';

  const [page, setPage] = useState(0);
  
  // Filter States
  const [type, setType] = useState('');
  const [category, setCategory] = useState('');
  const [sourceOrMerchant, setSourceOrMerchant] = useState('');
  const [minAmount, setMinAmount] = useState('');
  const [maxAmount, setMaxAmount] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [description, setDescription] = useState('');
  const [tag, setTag] = useState('');

  const { data: transactionsData, isLoading } = useTransactions({
    type,
    category,
    sourceOrMerchant,
    minAmount: minAmount ? parseFloat(minAmount) : undefined,
    maxAmount: maxAmount ? parseFloat(maxAmount) : undefined,
    fromDate: fromDate ? new Date(fromDate).toISOString() : undefined,
    toDate: toDate ? new Date(toDate).toISOString() : undefined,
    description,
    tag,
    page,
    size: 15,
  });

  const { data: categories = [] } = useCategories();

  const handleClearFilters = () => {
    setType('');
    setCategory('');
    setSourceOrMerchant('');
    setMinAmount('');
    setMaxAmount('');
    setFromDate('');
    setToDate('');
    setDescription('');
    setTag('');
    setPage(0);
  };

  return (
    <div className="flex flex-col gap-6 w-full">
      {/* Title */}
      <div>
        <h1 className="text-xl font-bold text-slate-900 dark:text-slate-50">Transactions Ledger</h1>
        <p className="text-xs text-slate-400">Search and filter across your combined income and expense history</p>
      </div>

      <div className="flex flex-col lg:flex-row gap-6 w-full">
        {/* Filters Panel */}
        <div className="w-full lg:w-1/4 bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border h-fit flex flex-col gap-4">
          <h2 className="text-sm font-bold text-slate-900 dark:text-slate-50 flex items-center gap-2">
            <Filter size={16} className="text-indigo-600" />
            Filters
          </h2>

          {/* Type Filter */}
          <div className="flex flex-col gap-1">
            <label className="text-[10px] font-bold text-slate-500 uppercase">Transaction Type</label>
            <select
              value={type}
              onChange={(e) => { setType(e.target.value); setPage(0); }}
              className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 outline-none text-xs bg-transparent text-slate-900 dark:text-slate-50"
            >
              <option value="" className="dark:bg-slate-900">All Transactions</option>
              <option value="EXPENSE" className="dark:bg-slate-900">Expenses</option>
              <option value="INCOME" className="dark:bg-slate-900">Income</option>
            </select>
          </div>

          {/* Category Filter */}
          <div className="flex flex-col gap-1">
            <label className="text-[10px] font-bold text-slate-500 uppercase">Category</label>
            <select
              value={category}
              onChange={(e) => { setCategory(e.target.value); setPage(0); }}
              className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 outline-none text-xs bg-transparent text-slate-900 dark:text-slate-50"
            >
              <option value="" className="dark:bg-slate-900">All Categories</option>
              {categories.map((c) => (
                <option key={c.id} value={c.name} className="dark:bg-slate-900">
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          {/* Search Source / Merchant */}
          <div className="flex flex-col gap-1">
            <label className="text-[10px] font-bold text-slate-500 uppercase">Merchant / Source</label>
            <input
              type="text"
              placeholder="e.g. Starbucks"
              value={sourceOrMerchant}
              onChange={(e) => { setSourceOrMerchant(e.target.value); setPage(0); }}
              className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 outline-none text-xs bg-transparent text-slate-900 dark:text-slate-50"
            />
          </div>

          {/* Amount range */}
          <div className="flex flex-col gap-1">
            <label className="text-[10px] font-bold text-slate-500 uppercase">Amount Range</label>
            <div className="flex gap-2">
              <input
                type="number"
                placeholder="Min"
                value={minAmount}
                onChange={(e) => { setMinAmount(e.target.value); setPage(0); }}
                className="w-1/2 px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 outline-none text-xs bg-transparent text-slate-900 dark:text-slate-50"
              />
              <input
                type="number"
                placeholder="Max"
                value={maxAmount}
                onChange={(e) => { setMaxAmount(e.target.value); setPage(0); }}
                className="w-1/2 px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 outline-none text-xs bg-transparent text-slate-900 dark:text-slate-50"
              />
            </div>
          </div>

          {/* Dates */}
          <div className="flex flex-col gap-1">
            <label className="text-[10px] font-bold text-slate-500 uppercase">Date Range</label>
            <div className="flex flex-col gap-2">
              <input
                type="date"
                value={fromDate}
                onChange={(e) => { setFromDate(e.target.value); setPage(0); }}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 outline-none text-xs bg-transparent text-slate-900 dark:text-slate-50"
              />
              <input
                type="date"
                value={toDate}
                onChange={(e) => { setToDate(e.target.value); setPage(0); }}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 outline-none text-xs bg-transparent text-slate-900 dark:text-slate-50"
              />
            </div>
          </div>

          {/* Search description */}
          <div className="flex flex-col gap-1">
            <label className="text-[10px] font-bold text-slate-500 uppercase">Description</label>
            <input
              type="text"
              placeholder="Search keyword..."
              value={description}
              onChange={(e) => { setDescription(e.target.value); setPage(0); }}
              className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 outline-none text-xs bg-transparent text-slate-900 dark:text-slate-50"
            />
          </div>

          {/* Search tag */}
          <div className="flex flex-col gap-1">
            <label className="text-[10px] font-bold text-slate-500 uppercase">Tag</label>
            <input
              type="text"
              placeholder="e.g. Business"
              value={tag}
              onChange={(e) => { setTag(e.target.value); setPage(0); }}
              className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 outline-none text-xs bg-transparent text-slate-900 dark:text-slate-50"
            />
          </div>

          <button
            onClick={handleClearFilters}
            className="w-full py-2.5 rounded-xl border border-slate-200 dark:border-slate-850 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-300 font-semibold text-xs transition-all mt-2"
          >
            Clear Filters
          </button>
        </div>

        {/* Ledger Table */}
        <div className="flex-1 bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border overflow-x-auto">
          {isLoading ? (
            <div className="text-center py-12 text-slate-500">Loading ledger logs...</div>
          ) : !transactionsData?.content?.length ? (
            <div className="text-center py-12 text-slate-500">No matching transactions found.</div>
          ) : (
            <div className="w-full flex flex-col justify-between h-full min-h-[500px]">
              <table className="w-full text-left text-sm text-slate-500 dark:text-slate-400">
                <thead className="text-xs uppercase bg-slate-50 dark:bg-slate-900/50 text-slate-700 dark:text-slate-300">
                  <tr>
                    <th className="px-6 py-4">Date</th>
                    <th className="px-6 py-4">Type</th>
                    <th className="px-6 py-4">Merchant / Source</th>
                    <th className="px-6 py-4">Category</th>
                    <th className="px-6 py-4 text-right">Amount</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                  {transactionsData.content.map((tx) => (
                    <tr key={tx.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-900/10">
                      <td className="px-6 py-4 whitespace-nowrap font-medium text-slate-900 dark:text-slate-50">
                        {new Date(tx.date).toLocaleDateString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span
                          className={`px-2.5 py-1 rounded-lg text-[10px] font-bold ${
                            tx.type === 'INCOME'
                              ? 'bg-emerald-50 dark:bg-emerald-950/20 text-emerald-600'
                              : 'bg-indigo-50 dark:bg-indigo-950/20 text-indigo-600'
                          }`}
                        >
                          {tx.type}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-slate-950 dark:text-slate-50 font-semibold">
                        {tx.sourceOrMerchant || '—'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span
                          style={{
                            backgroundColor: `${tx.type === 'INCOME' ? '#2ECC71' : tx.categoryColor}15`,
                            color: tx.type === 'INCOME' ? '#2ECC71' : tx.categoryColor,
                          }}
                          className="px-2.5 py-1 rounded-lg text-xs font-semibold"
                        >
                          {tx.categoryName || 'Income'}
                        </span>
                      </td>
                      <td
                        className={`px-6 py-4 whitespace-nowrap text-right font-bold ${
                          tx.type === 'INCOME' ? 'text-green-600 dark:text-green-400' : 'text-slate-900 dark:text-slate-50'
                        }`}
                      >
                        {tx.type === 'INCOME' ? '+' : '-'} {currencySymbol} {tx.amount.toFixed(2)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {/* Pagination Controls */}
              {transactionsData.totalPages > 1 && (
                <div className="flex justify-between items-center mt-6 pt-4 border-t border-slate-100 dark:border-slate-800">
                  <button
                    disabled={page === 0}
                    onClick={() => setPage(page - 1)}
                    className="px-4 py-2 border border-slate-200 dark:border-slate-800 rounded-xl text-xs font-semibold disabled:opacity-50"
                  >
                    Previous
                  </button>
                  <span className="text-xs text-slate-500">
                    Page {page + 1} of {transactionsData.totalPages}
                  </span>
                  <button
                    disabled={page >= transactionsData.totalPages - 1}
                    onClick={() => setPage(page + 1)}
                    className="px-4 py-2 border border-slate-200 dark:border-slate-800 rounded-xl text-xs font-semibold disabled:opacity-50"
                  >
                    Next
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
export default TransactionsPage;
