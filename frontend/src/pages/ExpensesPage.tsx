import React, { useState } from 'react';
import { useExpenses } from '@/features/expense/hooks/useExpenses';
import { useProfile } from '@/features/profile/hooks/useProfile';
import { WorkspaceLayout } from '@/layouts/WorkspaceLayout';
import { Card } from '@/components/ui/Card';
import { 
  Search, 
  Calendar, 
  Filter, 
  ChevronDown, 
  ChevronUp, 
  Users, 
  ShieldAlert,
  Info
} from 'lucide-react';
import { matchCategory } from '@/features/expense/utils/categoryNormalizer';

export const ExpensesPage: React.FC = () => {
  const { data: profile } = useProfile();
  
  const [page, setPage] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [selectedDateRange, setSelectedDateRange] = useState('ALL');
  const [expandedRow, setExpandedRow] = useState<string | null>(null);

  const { data: expensesData, isLoading, isError } = useExpenses(page, 10);

  const toggleRow = (id: string) => {
    setExpandedRow(expandedRow === id ? null : id);
  };

  const getFilteredExpenses = () => {
    if (!expensesData?.content) return [];
    let list = [...expensesData.content];

    // Filter by search query (description)
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase().trim();
      list = list.filter(exp => exp.description?.toLowerCase().includes(q));
    }

    // Filter by category
    if (selectedCategory !== 'ALL') {
      list = list.filter(exp => matchCategory(exp.category, selectedCategory));
    }

    // Filter by date range
    if (selectedDateRange !== 'ALL') {
      const now = new Date();
      list = list.filter(exp => {
        const expDate = new Date(exp.expenseDate);
        if (selectedDateRange === 'this-month') {
          return expDate.getMonth() === now.getMonth() && expDate.getFullYear() === now.getFullYear();
        } else if (selectedDateRange === 'last-7-days') {
          const diffTime = Math.abs(now.getTime() - expDate.getTime());
          const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
          return diffDays <= 7;
        }
        return true;
      });
    }

    return list;
  };

  const filteredExpenses = getFilteredExpenses();

  if (isLoading) {
    return (
      <WorkspaceLayout title="Expenses Ledger" subtitle="Loading your transactions...">
        <div className="flex flex-col items-center justify-center py-24 space-y-4">
          <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
          <p className="text-sm text-slate-500">Retrieving shared transactions...</p>
        </div>
      </WorkspaceLayout>
    );
  }

  if (isError) {
    return (
      <WorkspaceLayout title="Expenses Ledger" subtitle="Error loading transactions">
        <div className="bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900/40 p-6 rounded-2xl max-w-lg mx-auto my-12 text-center space-y-4">
          <ShieldAlert className="w-8 h-8 text-red-500 mx-auto" />
          <h3 className="font-bold text-red-800 dark:text-red-400">Failed to Load Ledger</h3>
          <p className="text-sm text-red-700 dark:text-red-500">
            An unexpected error occurred while communicating with the server. Please try again.
          </p>
        </div>
      </WorkspaceLayout>
    );
  }

  const CATEGORIES = ['ALL', 'FOOD', 'LODGING', 'TRANSPORT', 'ENTERTAINMENT', 'SHOPPING', 'OTHER'];

  return (
    <WorkspaceLayout 
      title="Expenses Ledger" 
      subtitle="View all shared and group transactions involving you"
    >
      <div className="space-y-6">
        {/* Filters Panel */}
        <Card className="p-4 bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border shadow-sm">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {/* Search */}
            <div className="relative">
              <Search className="absolute left-3 top-2.5 w-4 h-4 text-slate-400" />
              <input
                type="text"
                placeholder="Search description..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-9 pr-4 py-2 text-sm bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary text-slate-900 dark:text-slate-100"
              />
            </div>

            {/* Category */}
            <div className="relative">
              <Filter className="absolute left-3 top-2.5 w-4 h-4 text-slate-400" />
              <select
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                className="w-full pl-9 pr-4 py-2 text-sm bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary text-slate-900 dark:text-slate-100 appearance-none"
              >
                {CATEGORIES.map((cat) => (
                  <option key={cat} value={cat}>
                    {cat === 'ALL' ? 'All Categories' : cat}
                  </option>
                ))}
              </select>
            </div>

            {/* Date Range */}
            <div className="relative">
              <Calendar className="absolute left-3 top-2.5 w-4 h-4 text-slate-400" />
              <select
                value={selectedDateRange}
                onChange={(e) => setSelectedDateRange(e.target.value)}
                className="w-full pl-9 pr-4 py-2 text-sm bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary text-slate-900 dark:text-slate-100 appearance-none"
              >
                <option value="ALL">All Time</option>
                <option value="this-month">This Month</option>
                <option value="last-7-days">Last 7 Days</option>
              </select>
            </div>
          </div>
        </Card>

        {/* Ledger Table */}
        <Card className="overflow-hidden border border-light-border dark:border-dark-border shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 dark:bg-slate-900 border-b border-light-border dark:border-dark-border text-slate-500 text-xs font-bold uppercase">
                  <th className="py-4 px-6">Expense</th>
                  <th className="py-4 px-6">Category</th>
                  <th className="py-4 px-6">Payer</th>
                  <th className="py-4 px-6">Status</th>
                  <th className="py-4 px-6 text-right">Total Amount</th>
                  <th className="py-4 px-6 text-right">Your Share</th>
                  <th className="py-4 px-6"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-light-border dark:divide-dark-border text-sm">
                {filteredExpenses.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="py-12 text-center text-slate-400 text-sm">
                      No shared expenses found matching these criteria.
                    </td>
                  </tr>
                ) : (
                  filteredExpenses.map((exp) => {
                    const userSplit = exp.splits?.find(s => s.userId === profile?.id);
                    const userOwed = userSplit ? userSplit.owedAmount : 0;
                    const isPayer = exp.paidByUserId === profile?.id;
                    const isExpanded = expandedRow === exp.id;

                    return (
                      <React.Fragment key={exp.id}>
                        <tr className="hover:bg-slate-50/50 dark:hover:bg-slate-900/20 transition-colors duration-150">
                          <td className="py-4 px-6">
                            <div>
                              <div className="font-semibold text-slate-900 dark:text-white">{exp.description}</div>
                              <div className="text-xs text-slate-400 mt-0.5">
                                {new Date(exp.expenseDate).toLocaleDateString(undefined, {
                                  year: 'numeric', month: 'short', day: 'numeric'
                                })}
                              </div>
                            </div>
                          </td>
                          <td className="py-4 px-6">
                            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300">
                              <span className="w-1.5 h-1.5 rounded-full bg-primary" />
                              {exp.category}
                            </span>
                          </td>
                          <td className="py-4 px-6">
                            <span className="text-slate-600 dark:text-slate-400 font-medium">
                              {isPayer ? 'You' : (exp.paidByUserName || 'Unknown')}
                            </span>
                          </td>
                          <td className="py-4 px-6">
                            <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold ${
                              exp.status === 'POSTED' ? 'bg-green-50 dark:bg-green-950/20 text-green-700 dark:text-green-400' :
                              exp.status === 'DRAFT' ? 'bg-amber-50 dark:bg-amber-950/20 text-amber-700 dark:text-amber-400' :
                              'bg-rose-50 dark:bg-rose-950/20 text-rose-700 dark:text-rose-400'
                            }`}>
                              {exp.status}
                            </span>
                          </td>
                          <td className="py-4 px-6 text-right font-semibold text-slate-900 dark:text-white">
                            {exp.currency} {exp.amount.toFixed(2)}
                          </td>
                          <td className="py-4 px-6 text-right font-bold text-primary">
                            {exp.currency} {userOwed.toFixed(2)}
                          </td>
                          <td className="py-4 px-6 text-right">
                            <button
                              onClick={() => toggleRow(exp.id)}
                              className="p-1 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors"
                              title="Toggle details"
                            >
                              {isExpanded ? <ChevronUp className="w-5 h-5" /> : <ChevronDown className="w-5 h-5" />}
                            </button>
                          </td>
                        </tr>

                        {isExpanded && (
                          <tr className="bg-slate-50/30 dark:bg-slate-900/10">
                            <td colSpan={7} className="p-0">
                              <div className="px-6 py-4 border-t border-b border-light-border/50 dark:border-dark-border/50 bg-slate-50/20 dark:bg-slate-900/5 space-y-4">
                                <div className="flex items-center gap-2 text-xs font-bold text-slate-400 uppercase tracking-wider">
                                  <Users className="w-4 h-4 text-slate-400" />
                                  <span>Split Details ({exp.splitType} Split)</span>
                                </div>
                                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                                  {exp.splits?.map((split) => (
                                    <div 
                                      key={split.id} 
                                      className="flex justify-between items-center px-4 py-2.5 bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-xl shadow-xs"
                                    >
                                      <div>
                                        <div className="text-xs font-semibold text-slate-800 dark:text-slate-200">
                                          {split.userId === profile?.id ? 'You' : (split.userName || 'Unknown')}
                                        </div>
                                        <div className="text-[10px] text-slate-400">{split.userEmail}</div>
                                      </div>
                                      <div className="text-right">
                                        <div className="text-xs font-bold text-slate-900 dark:text-white">
                                          {exp.currency} {split.owedAmount.toFixed(2)}
                                        </div>
                                        {exp.splitType !== 'EQUAL' && (
                                          <div className="text-[10px] text-slate-400">
                                            Val: {split.allocationValue}
                                          </div>
                                        )}
                                      </div>
                                    </div>
                                  ))}
                                </div>
                                {exp.tripId && (
                                  <div className="flex items-center gap-1.5 text-xs text-slate-400 mt-2 bg-slate-50 dark:bg-slate-900/50 p-2.5 rounded-xl border border-light-border dark:border-dark-border max-w-max">
                                    <Info className="w-4 h-4 text-slate-400 shrink-0" />
                                    <span>Associated with Trip ID: <strong>{exp.tripId}</strong></span>
                                  </div>
                                )}
                              </div>
                            </td>
                          </tr>
                        )}
                      </React.Fragment>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {expensesData && expensesData.totalPages > 1 && (
            <div className="flex items-center justify-between px-6 py-4 bg-slate-50 dark:bg-slate-900 border-t border-light-border dark:border-dark-border text-xs text-slate-500">
              <div>
                Showing {page * 10 + 1}–{Math.min((page + 1) * 10, expensesData.totalElements)} of {expensesData.totalElements} records
              </div>
              <div className="flex gap-2">
                <button
                  disabled={page === 0}
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  className="px-3 py-1.5 bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg font-medium hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Previous
                </button>
                <button
                  disabled={page >= expensesData.totalPages - 1}
                  onClick={() => setPage(p => Math.min(expensesData.totalPages - 1, p + 1))}
                  className="px-3 py-1.5 bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg font-medium hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </Card>
      </div>
    </WorkspaceLayout>
  );
};

export default ExpensesPage;
