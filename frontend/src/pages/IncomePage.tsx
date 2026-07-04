import React, { useState } from 'react';
import { useIncomeList, useCreateIncome, useDeleteIncome, useUpdateIncome } from '@/features/income/hooks/useIncome';
import toast from 'react-hot-toast';
import { Plus, Trash2, Calendar, DollarSign, X, Edit2, Copy, Search, HelpCircle, TrendingUp, TrendingDown, Wallet, ListFilter } from 'lucide-react';
import { useProfile } from '@/features/profile/hooks/useProfile';
import { Income } from '@/features/income/services/incomeService';
import { useDashboardSummary } from '@/features/dashboard/hooks/useDashboard';

export const IncomePage: React.FC = () => {
  const { data: profile } = useProfile();
  const currencySymbol = profile?.preferredCurrency || '$';

  const [page, setPage] = useState(0);
  const { data: incomeData, isLoading: isIncomeLoading } = useIncomeList(page, 10);
  const { data: summary } = useDashboardSummary();
  const createMutation = useCreateIncome();
  const updateMutation = useUpdateIncome();
  const deleteMutation = useDeleteIncome();

  // Local filters and sort states
  const [searchQuery, setSearchQuery] = useState('');
  const [sortOrder, setSortOrder] = useState('date-desc');
  const [selectedDateRange, setSelectedDateRange] = useState('all');
  const [quickFilter, setQuickFilter] = useState('all');

  // Modal & Form States
  const [isOpen, setIsOpen] = useState(false);
  const [editingIncome, setEditingIncome] = useState<Income | null>(null);
  const [amount, setAmount] = useState('');
  const [source, setSource] = useState('');
  const [incomeDate, setIncomeDate] = useState(new Date().toISOString().substring(0, 16));
  const [description, setDescription] = useState('');
  const [notes, setNotes] = useState('');

  const handleEditClick = (inc: Income) => {
    setEditingIncome(inc);
    setAmount(inc.amount.toString());
    setSource(inc.source);
    const dateObj = new Date(inc.incomeDate);
    const tzOffset = dateObj.getTimezoneOffset() * 60000;
    const localISOTime = (new Date(dateObj.getTime() - tzOffset)).toISOString().substring(0, 16);
    setIncomeDate(localISOTime);
    setDescription(inc.description || '');
    setNotes(inc.notes || '');
    setIsOpen(true);
  };

  const handleDuplicateClick = (inc: Income) => {
    setEditingIncome(null);
    setAmount(inc.amount.toString());
    setSource(inc.source ? `${inc.source} (Copy)` : '');
    setIncomeDate(new Date().toISOString().substring(0, 16));
    setDescription(inc.description || '');
    setNotes(inc.notes || '');
    setIsOpen(true);
    toast.success('Income details duplicated into form!');
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const numAmt = parseFloat(amount);
    if (isNaN(numAmt) || numAmt <= 0) {
      toast.error('Amount must be a positive number');
      return;
    }
    if (!source.trim()) {
      toast.error('Source is required');
      return;
    }

    const payload = {
      amount: numAmt,
      source: source.trim(),
      incomeDate: new Date(incomeDate).toISOString(),
      description: description.trim() || undefined,
      notes: notes.trim() || undefined,
    };

    if (editingIncome) {
      updateMutation.mutate(
        { id: editingIncome.id, data: payload },
        {
          onSuccess: () => {
            toast.success('Income updated successfully!');
            resetForm();
            setIsOpen(false);
          },
          onError: (err: any) => {
            toast.error(err.message || 'Failed to update income');
          },
        }
      );
    } else {
      createMutation.mutate(
        payload,
        {
          onSuccess: () => {
            toast.success('Income recorded successfully!');
            resetForm();
            setIsOpen(false);
          },
          onError: (err: any) => {
            toast.error(err.message || 'Failed to record income');
          },
        }
      );
    }
  };

  const resetForm = () => {
    setAmount('');
    setSource('');
    setIncomeDate(new Date().toISOString().substring(0, 16));
    setDescription('');
    setNotes('');
    setEditingIncome(null);
  };

  const handleDelete = (id: string) => {
    if (!window.confirm('Are you sure you want to delete this income record?')) return;
    deleteMutation.mutate(id, {
      onSuccess: () => {
        toast.success('Income deleted successfully!');
      },
      onError: (err: any) => {
        toast.error(err.message || 'Failed to delete income');
      },
    });
  };

  const getFilteredAndSortedIncome = () => {
    if (!incomeData?.content) return [];

    let list = [...incomeData.content];

    // Filter by search query
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase().trim();
      list = list.filter(inc => 
        (inc.source && inc.source.toLowerCase().includes(q)) ||
        (inc.description && inc.description.toLowerCase().includes(q))
      );
    }

    // Filter by Date Range Picker dropdown
    if (selectedDateRange !== 'all') {
      const now = new Date();
      list = list.filter(inc => {
        const incDate = new Date(inc.incomeDate);
        if (selectedDateRange === 'this-month') {
          return incDate.getMonth() === now.getMonth() && incDate.getFullYear() === now.getFullYear();
        } else if (selectedDateRange === 'last-30-days') {
          const diffTime = Math.abs(now.getTime() - incDate.getTime());
          const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
          return diffDays <= 30;
        } else if (selectedDateRange === 'last-7-days') {
          const diffTime = Math.abs(now.getTime() - incDate.getTime());
          const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
          return diffDays <= 7;
        }
        return true;
      });
    }

    // Quick filter chips (Today, Week, Month)
    if (quickFilter !== 'all') {
      const now = new Date();
      list = list.filter(inc => {
        const incDate = new Date(inc.incomeDate);
        if (quickFilter === 'today') {
          return incDate.getDate() === now.getDate() &&
                 incDate.getMonth() === now.getMonth() &&
                 incDate.getFullYear() === now.getFullYear();
        } else if (quickFilter === 'week') {
          const oneWeekAgo = new Date();
          oneWeekAgo.setDate(now.getDate() - 7);
          return incDate >= oneWeekAgo;
        } else if (quickFilter === 'month') {
          return incDate.getMonth() === now.getMonth() &&
                 incDate.getFullYear() === now.getFullYear();
        }
        return true;
      });
    }

    // Sort order
    list.sort((a, b) => {
      if (sortOrder === 'date-desc') {
        return new Date(b.incomeDate).getTime() - new Date(a.incomeDate).getTime();
      } else if (sortOrder === 'date-asc') {
        return new Date(a.incomeDate).getTime() - new Date(b.incomeDate).getTime();
      } else if (sortOrder === 'amount-desc') {
        return b.amount - a.amount;
      } else if (sortOrder === 'amount-asc') {
        return a.amount - b.amount;
      }
      return 0;
    });

    return list;
  };

  const displayedIncome = getFilteredAndSortedIncome();
  const totalFilteredAmount = displayedIncome.reduce((sum, inc) => sum + inc.amount, 0);

  // Source icon mappings
  const getSourceIconSymbol = (sourceName: string) => {
    const s = sourceName.toLowerCase();
    if (s.includes('salary')) return { emoji: '💼', bg: 'bg-emerald-50 text-emerald-600 border border-emerald-100' };
    if (s.includes('freelance') || s.includes('project') || s.includes('work')) return { emoji: '💻', bg: 'bg-indigo-50 text-indigo-650 border border-indigo-100' };
    if (s.includes('invest') || s.includes('stock') || s.includes('dividend')) return { emoji: '📈', bg: 'bg-teal-50 text-teal-650 border border-teal-100' };
    return { emoji: '💰', bg: 'bg-amber-50 text-amber-600 border border-amber-100' };
  };

  // Payment method badge helper
  const getPaymentBadge = (description: string = '', notes: string = '') => {
    const text = (description + ' ' + notes).toLowerCase();
    if (text.includes('cash')) return { label: 'Cash', icon: '💵', bg: 'bg-amber-50 text-amber-700 border-amber-100 dark:bg-amber-955/20' };
    if (text.includes('upi') || text.includes('gpay') || text.includes('phonepe')) return { label: 'UPI', icon: '📱', bg: 'bg-emerald-50 text-emerald-700 border-emerald-100 dark:bg-emerald-955/20' };
    return { label: 'Bank', icon: '🏦', bg: 'bg-slate-50 text-slate-700 border-slate-200 dark:bg-slate-800/40 dark:text-slate-400 dark:border-slate-750/50' };
  };

  // Loader template
  if (isIncomeLoading) {
    return (
      <div className="flex flex-col gap-4 w-full animate-pulse">
        {/* Header Skeleton */}
        <div className="flex justify-between items-center">
          <div className="flex flex-col gap-1.5">
            <div className="h-6 w-40 bg-slate-200 dark:bg-slate-800 rounded-lg" />
            <div className="h-3 w-56 bg-slate-100 dark:bg-slate-800/60 rounded-lg" />
          </div>
          <div className="h-9 w-28 bg-slate-200 dark:bg-slate-800 rounded-lg" />
        </div>

        {/* Cards Grid Skeleton */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="bg-white dark:bg-dark-surface p-4 rounded-xl border border-light-border dark:border-dark-border h-20" />
          ))}
        </div>

        {/* Toolbar Skeleton */}
        <div className="bg-white dark:bg-dark-surface p-3 rounded-xl border border-light-border dark:border-dark-border h-12" />

        {/* Table Skeleton */}
        <div className="bg-white dark:bg-dark-surface p-5 rounded-xl border border-light-border dark:border-dark-border flex flex-col gap-3">
          <div className="h-7 bg-slate-100 dark:bg-slate-800 rounded w-full" />
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-12 bg-slate-50 dark:bg-slate-900/50 rounded w-full" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-4 w-full relative">
      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3">
        <div className="flex flex-col">
          <h1 className="text-xl font-bold text-slate-900 dark:text-slate-50">Income Ledger</h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">Track, search and manage all your income streams in one place.</p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => { resetForm(); setIsOpen(true); }}
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-xs shadow-md transition-all active:scale-[0.98]"
          >
            <Plus size={16} />
            Add Income
          </button>
        </div>
      </div>

      {/* KPI Bento summary grid */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
        {/* Total Income Card */}
        <div className="bg-white dark:bg-dark-surface p-4 rounded-xl border border-light-border dark:border-dark-border flex justify-between items-center shadow-sm relative overflow-hidden group hover:shadow hover:border-slate-300 dark:hover:border-slate-700 transition-all duration-200">
          <div className="flex flex-col gap-0.5 z-10">
            <div className="flex items-center gap-1">
              <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Total Income</span>
              <div className="relative group/tooltip">
                <HelpCircle size={11} className="text-slate-350 cursor-pointer" />
                <div className="absolute bottom-full mb-1.5 hidden group-hover/tooltip:block bg-slate-950 text-white text-[9px] p-2 rounded shadow-lg z-50 w-40">
                  Total cumulative income recorded across all deposits.
                </div>
              </div>
            </div>
            <span className="text-lg font-bold text-slate-800 dark:text-slate-100 mt-0.5">
              {currencySymbol} {summary?.totalIncome?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}
            </span>
            <div className="flex items-center gap-1 mt-1.5">
              <span className="flex items-center gap-0.5 text-[9px] font-bold text-emerald-500 bg-emerald-50 dark:bg-emerald-950/30 px-1 py-0.5 rounded-full">
                <TrendingUp size={9} /> 8.4%
              </span>
              <span className="text-[9px] text-slate-400 font-medium">This Month</span>
            </div>
          </div>
          {/* Sparkline SVG */}
          <div className="absolute bottom-1 right-12 w-12 h-6 opacity-20 group-hover:opacity-50 transition-opacity duration-200">
            <svg viewBox="0 0 100 30" className="w-full h-full text-emerald-500" fill="none">
              <path d="M0,20 Q20,5 40,25 T80,8 T100,15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            </svg>
          </div>
          <div className="p-2.5 bg-gradient-to-tr from-emerald-500/10 to-emerald-500/5 dark:from-emerald-500/20 rounded-lg text-emerald-500 z-10 shadow-inner group-hover:scale-102 transition-transform duration-200">
            <DollarSign size={18} />
          </div>
        </div>

        {/* Transactions Card */}
        <div className="bg-white dark:bg-dark-surface p-4 rounded-xl border border-light-border dark:border-dark-border flex justify-between items-center shadow-sm relative overflow-hidden group hover:shadow hover:border-slate-300 dark:hover:border-slate-700 transition-all duration-200">
          <div className="flex flex-col gap-0.5 z-10">
            <div className="flex items-center gap-1">
              <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Deposits</span>
              <div className="relative group/tooltip">
                <HelpCircle size={11} className="text-slate-350 cursor-pointer" />
                <div className="absolute bottom-full mb-1.5 hidden group-hover/tooltip:block bg-slate-950 text-white text-[9px] p-2 rounded shadow-lg z-50 w-40">
                  Count of income streams logged.
                </div>
              </div>
            </div>
            <span className="text-lg font-bold text-slate-800 dark:text-slate-100 mt-0.5">
              {incomeData?.totalElements || 0}
            </span>
            <div className="flex items-center gap-1 mt-1.5">
              <span className="flex items-center gap-0.5 text-[9px] font-bold text-indigo-500 bg-indigo-50 dark:bg-indigo-950/30 px-1 py-0.5 rounded-full">
                <TrendingUp size={9} /> +1
              </span>
              <span className="text-[9px] text-slate-400 font-medium">This Month</span>
            </div>
          </div>
          {/* Sparkline SVG */}
          <div className="absolute bottom-1 right-12 w-12 h-6 opacity-20 group-hover:opacity-50 transition-opacity duration-200">
            <svg viewBox="0 0 100 30" className="w-full h-full text-indigo-500" fill="none">
              <path d="M0,10 Q25,25 50,5 T100,20" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            </svg>
          </div>
          <div className="p-2.5 bg-gradient-to-tr from-indigo-500/10 to-indigo-500/5 dark:from-indigo-500/20 rounded-lg text-indigo-500 z-10 shadow-inner group-hover:scale-102 transition-transform duration-200">
            <ListFilter size={18} />
          </div>
        </div>

        {/* Average Income Card */}
        <div className="bg-white dark:bg-dark-surface p-4 rounded-xl border border-light-border dark:border-dark-border flex justify-between items-center shadow-sm relative overflow-hidden group hover:shadow hover:border-slate-300 dark:hover:border-slate-700 transition-all duration-200">
          <div className="flex flex-col gap-0.5 z-10">
            <div className="flex items-center gap-1">
              <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Average Income</span>
              <div className="relative group/tooltip">
                <HelpCircle size={11} className="text-slate-350 cursor-pointer" />
                <div className="absolute bottom-full mb-1.5 hidden group-hover/tooltip:block bg-slate-950 text-white text-[9px] p-2 rounded shadow-lg z-50 w-40">
                  Average amount recorded per deposit log.
                </div>
              </div>
            </div>
            <span className="text-lg font-bold text-slate-800 dark:text-slate-100 mt-0.5">
              {currencySymbol} {((incomeData?.totalElements ? (summary?.totalIncome || 0) / incomeData.totalElements : 0)).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </span>
            <div className="flex items-center gap-1 mt-1.5">
              <span className="flex items-center gap-0.5 text-[9px] font-bold text-emerald-500 bg-emerald-50 dark:bg-emerald-950/30 px-1 py-0.5 rounded-full">
                <TrendingUp size={9} /> 4.2%
              </span>
              <span className="text-[9px] text-slate-400 font-medium">Per Deposit</span>
            </div>
          </div>
          <div className="p-2.5 bg-gradient-to-tr from-teal-500/10 to-teal-500/5 dark:from-teal-500/20 rounded-lg text-teal-650 z-10 shadow-inner group-hover:scale-102 transition-transform duration-200">
            <TrendingUp size={18} />
          </div>
        </div>

        {/* Net Balance Card */}
        <div className="bg-white dark:bg-dark-surface p-4 rounded-xl border border-light-border dark:border-dark-border flex justify-between items-center shadow-sm relative overflow-hidden group hover:shadow hover:border-slate-300 dark:hover:border-slate-700 transition-all duration-200">
          <div className="flex flex-col gap-0.5 z-10">
            <div className="flex items-center gap-1">
              <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Net Balance</span>
              <div className="relative group/tooltip">
                <HelpCircle size={11} className="text-slate-350 cursor-pointer" />
                <div className="absolute bottom-full mb-1.5 hidden group-hover/tooltip:block bg-slate-950 text-white text-[9px] p-2 rounded shadow-lg z-50 w-40">
                  Current net liquid balance (Opening + Income - Expenses).
                </div>
              </div>
            </div>
            <span className="text-lg font-bold text-slate-800 dark:text-slate-100 mt-0.5">
              {currencySymbol} {summary?.netBalance?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}
            </span>
            <div className="flex items-center gap-1 mt-1.5">
              <span className="text-[9px] text-indigo-600 dark:text-indigo-400 font-bold bg-indigo-50 dark:bg-indigo-950/20 px-1.5 py-0.5 rounded-full">
                Active Wallet
              </span>
            </div>
          </div>
          <div className="p-2.5 bg-gradient-to-tr from-purple-500/10 to-purple-500/5 dark:from-purple-500/20 rounded-lg text-purple-500 z-10 shadow-inner group-hover:scale-102 transition-transform duration-200">
            <Wallet size={18} />
          </div>
        </div>
      </div>

      {/* Search & Filters Toolbar */}
      <div className="bg-white dark:bg-dark-surface p-3.5 rounded-xl border border-light-border dark:border-dark-border flex flex-col gap-3.5 shadow-sm">
        <div className="flex flex-col md:flex-row gap-3 items-center justify-between">
          {/* Search box */}
          <div className="relative w-full md:w-80">
            <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              placeholder="Search income sources..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-8 py-1.5 text-xs rounded-lg border border-slate-205 dark:border-slate-800 bg-transparent text-slate-805 dark:text-slate-200 focus:ring-1 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all"
            />
            {searchQuery && (
              <button 
                onClick={() => setSearchQuery('')}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-650"
              >
                <X size={12} />
              </button>
            )}
          </div>

          {/* Quick Date Range Filter Chips */}
          <div className="flex flex-wrap gap-1.5 w-full md:w-auto">
            <button
              onClick={() => setQuickFilter('all')}
              className={`px-3 py-1 rounded-full text-[10px] font-bold tracking-wider uppercase transition-all ${
                quickFilter === 'all'
                  ? 'bg-indigo-655 text-white shadow-sm'
                  : 'bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-405'
              }`}
            >
              All Time
            </button>
            <button
              onClick={() => setQuickFilter('today')}
              className={`px-3 py-1 rounded-full text-[10px] font-bold tracking-wider uppercase transition-all ${
                quickFilter === 'today'
                  ? 'bg-indigo-655 text-white shadow-sm'
                  : 'bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-405'
              }`}
            >
              Today
            </button>
            <button
              onClick={() => setQuickFilter('week')}
              className={`px-3 py-1 rounded-full text-[10px] font-bold tracking-wider uppercase transition-all ${
                quickFilter === 'week'
                  ? 'bg-indigo-655 text-white shadow-sm'
                  : 'bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-405'
              }`}
            >
              This Week
            </button>
            <button
              onClick={() => setQuickFilter('month')}
              className={`px-3 py-1 rounded-full text-[10px] font-bold tracking-wider uppercase transition-all ${
                quickFilter === 'month'
                  ? 'bg-indigo-655 text-white shadow-sm'
                  : 'bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-405'
              }`}
            >
              This Month
            </button>
          </div>
        </div>

        {/* Dropdowns Filters Bar */}
        <div className="flex flex-wrap items-center gap-3 border-t border-slate-100 dark:border-slate-800/80 pt-3 justify-end">
          {/* Active filters badge */}
          {(selectedDateRange !== 'all' || searchQuery || quickFilter !== 'all') && (
            <span className="text-[9px] font-bold text-indigo-600 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-950/20 px-2 py-0.5 rounded-full mr-auto flex items-center gap-1 animate-fade-in">
              Active Filters
              <button 
                onClick={() => {
                  setSelectedDateRange('all');
                  setSearchQuery('');
                  setQuickFilter('all');
                }}
                className="hover:text-indigo-850"
                title="Clear all filters"
              >
                <X size={9} />
              </button>
            </span>
          )}

          {/* Date Picker select range */}
          <div className="relative">
            <select
              value={selectedDateRange}
              onChange={(e) => setSelectedDateRange(e.target.value)}
              style={{ paddingLeft: '34px' }}
              className="pr-7 py-1.5 text-xs font-semibold rounded-lg border border-slate-200 dark:border-slate-800 bg-transparent text-slate-705 dark:text-slate-300 outline-none appearance-none cursor-pointer hover:bg-slate-50 dark:hover:bg-slate-900 transition-all"
            >
              <option value="all">Custom Range</option>
              <option value="this-month">This Month</option>
              <option value="last-30-days">Last 30 Days</option>
              <option value="last-7-days">Last 7 Days</option>
            </select>
            <Calendar size={13} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
          </div>

          {/* Sort Order */}
          <div className="relative">
            <select
              value={sortOrder}
              onChange={(e) => setSortOrder(e.target.value)}
              style={{ paddingLeft: '34px' }}
              className="pr-7 py-1.5 text-xs font-semibold rounded-lg border border-slate-200 dark:border-slate-800 bg-transparent text-slate-750 dark:text-slate-300 outline-none appearance-none cursor-pointer hover:bg-slate-50 dark:hover:bg-slate-900 transition-all"
            >
              <option value="date-desc">Newest First</option>
              <option value="date-asc">Oldest First</option>
              <option value="amount-desc">Highest Amount</option>
              <option value="amount-asc">Lowest Amount</option>
            </select>
            <Calendar size={13} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
          </div>
        </div>
      </div>

      {/* Modern table ledger */}
      <div className="bg-white dark:bg-dark-surface p-5 rounded-xl border border-light-border dark:border-dark-border overflow-hidden shadow-sm">
        {!displayedIncome.length ? (
          <div className="bg-white dark:bg-dark-surface p-10 rounded-2xl text-center flex flex-col items-center justify-center gap-3.5 min-h-[300px]">
            <div className="w-14 h-14 rounded-xl bg-indigo-50 dark:bg-indigo-950/30 text-indigo-600 dark:text-indigo-400 flex items-center justify-center">
              <DollarSign size={28} />
            </div>
            <div className="flex flex-col gap-0.5 max-w-sm">
              <h3 className="text-sm font-bold text-slate-850 dark:text-slate-100">No income records yet</h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">Start logging your deposits.</p>
            </div>
            <button
              onClick={() => { resetForm(); setIsOpen(true); }}
              className="mt-1 flex items-center gap-1.5 px-4.5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-xs shadow-md transition-all active:scale-[0.98]"
            >
              <Plus size={14} />
              Add Income
            </button>
          </div>
        ) : (
          <div className="w-full">
            {/* Desktop Table View */}
            <div className="hidden sm:block overflow-x-auto max-h-[60vh] scrollbar-thin">
              <table className="w-full text-left text-xs text-slate-500 dark:text-slate-400 border-collapse table-fixed">
                <thead className="text-[10px] tracking-wider uppercase bg-slate-50 dark:bg-slate-900/50 text-slate-650 dark:text-slate-350 sticky top-0 z-20 border-b border-slate-150 dark:border-slate-800">
                  <tr>
                    <th className="px-5 py-3 w-[85px]">Date</th>
                    <th className="px-5 py-3 w-[180px]">Source</th>
                    <th className="px-5 py-3 w-[260px]">Description</th>
                    <th className="px-5 py-3 text-right w-[130px]">Amount</th>
                    <th className="px-5 py-3 text-center w-[120px]">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-850">
                  {displayedIncome.map((inc, idx) => {
                    const dateObj = new Date(inc.incomeDate);
                    const day = dateObj.getDate();
                    const month = dateObj.toLocaleString('en-US', { month: 'short' }).toUpperCase();
                    const year = dateObj.getFullYear();
                    
                    const sourceDetails = getSourceIconSymbol(inc.source);
                    const paymentBadge = getPaymentBadge(inc.description, inc.notes);

                    return (
                      <tr 
                        key={inc.id} 
                        className={`group transition-colors duration-150 ${
                          idx % 2 === 0 ? 'bg-transparent' : 'bg-slate-50/20 dark:bg-slate-900/5'
                        } hover:bg-slate-50/70 dark:hover:bg-slate-900/15`}
                      >
                        {/* Prominent Date Cell */}
                        <td className="px-5 py-2.5 whitespace-nowrap">
                          <div className="flex flex-col items-center justify-center bg-slate-50 dark:bg-slate-900/60 w-11 h-13 rounded-lg border border-slate-100 dark:border-slate-800/80 group-hover:scale-102 transition-transform duration-200">
                            <span className="text-[9px] font-extrabold text-slate-400 uppercase tracking-wider">{month}</span>
                            <span className="text-lg font-black text-slate-900 dark:text-slate-55 leading-none mt-0.5">{day}</span>
                            <span className="text-[8px] text-slate-400 font-bold mt-0.5">{year}</span>
                          </div>
                        </td>

                        {/* Source Details with emoji and type */}
                        <td className="px-5 py-2.5 whitespace-nowrap overflow-hidden text-ellipsis">
                          <div className="flex items-center gap-2.5">
                            <div className={`w-8 h-8 rounded-full flex items-center justify-center font-bold text-sm shadow-sm ${sourceDetails.bg}`}>
                              {sourceDetails.emoji}
                            </div>
                            <div className="flex flex-col overflow-hidden text-ellipsis">
                              <span 
                                onClick={() => setSearchQuery(inc.source)}
                                className="font-semibold text-slate-855 dark:text-slate-100 text-xs hover:text-indigo-650 dark:hover:text-indigo-400 cursor-pointer truncate"
                                title="Filter history by source"
                              >
                                {inc.source}
                              </span>
                              <span className="text-[9px] text-slate-400 truncate">
                                Deposit Stream
                              </span>
                            </div>
                          </div>
                        </td>

                        {/* Description */}
                        <td className="px-5 py-2.5 whitespace-nowrap overflow-hidden text-ellipsis text-slate-600 dark:text-slate-350">
                          {inc.description || inc.notes || 'No description provided'}
                        </td>

                        {/* Amount & payment method */}
                        <td className="px-5 py-2.5 whitespace-nowrap text-right">
                          <div className="flex flex-col text-right pr-2">
                            <span className="font-bold text-emerald-600 dark:text-emerald-400 text-xs">
                              + {currencySymbol} {inc.amount.toFixed(2)}
                            </span>
                            <span className={`inline-flex items-center justify-center gap-0.5 self-end px-1.5 py-0.5 rounded text-[8px] font-black mt-0.5 border uppercase tracking-wider ${paymentBadge.bg}`}>
                              <span>{paymentBadge.icon}</span>
                              <span>{paymentBadge.label}</span>
                            </span>
                          </div>
                        </td>

                        {/* Actions */}
                        <td className="px-5 py-2.5 whitespace-nowrap text-center">
                          <div className="flex items-center justify-center gap-1.5">
                            <button
                              onClick={() => handleEditClick(inc)}
                              className="p-1 rounded bg-indigo-55 bg-indigo-50 hover:bg-indigo-100 dark:bg-indigo-950/40 dark:hover:bg-indigo-900/60 text-indigo-600 dark:text-indigo-400 transition-colors"
                              title="Edit details"
                            >
                              <Edit2 size={12} />
                            </button>
                            <button
                              onClick={() => handleDuplicateClick(inc)}
                              className="p-1 rounded bg-emerald-50 hover:bg-emerald-100 dark:bg-emerald-950/40 dark:hover:bg-emerald-900/60 text-emerald-600 dark:text-emerald-400 transition-colors"
                              title="Duplicate entry"
                            >
                              <Copy size={12} />
                            </button>
                            <button
                              onClick={() => handleDelete(inc.id)}
                              className="p-1 rounded bg-rose-50 hover:bg-rose-100 dark:bg-rose-950/40 dark:hover:bg-rose-900/60 text-rose-600 dark:text-rose-450 transition-colors"
                              title="Delete log"
                            >
                              <Trash2 size={12} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            {/* Mobile Stacked Card View */}
            <div className="block sm:hidden flex flex-col gap-3">
              {displayedIncome.map((inc) => {
                const sourceDetails = getSourceIconSymbol(inc.source);
                return (
                  <div key={inc.id} className="bg-slate-50 dark:bg-slate-900/40 p-3.5 rounded-xl border border-slate-100 dark:border-slate-800 flex flex-col gap-2.5">
                    <div className="flex justify-between items-start">
                      <span className="text-[9px] text-slate-400 font-bold">
                        {new Date(inc.incomeDate).toLocaleDateString()}
                      </span>
                      <span className="bg-emerald-50 text-emerald-600 px-2 py-0.5 rounded-full text-[9px] font-bold">
                        Deposit
                      </span>
                    </div>

                    <div className="flex justify-between items-center">
                      <div className="flex items-center gap-2">
                        <div className="w-7 h-7 rounded-full bg-slate-100 dark:bg-slate-850 flex items-center justify-center text-xs">
                          {sourceDetails.emoji}
                        </div>
                        <span className="font-bold text-slate-805 dark:text-slate-100 text-xs">
                          {inc.source}
                        </span>
                      </div>
                      <span className="font-black text-emerald-650 dark:text-emerald-400 text-xs">
                        + {currencySymbol} {inc.amount.toFixed(2)}
                      </span>
                    </div>

                    <div className="flex justify-between items-center text-[10px] border-t border-slate-100 dark:border-slate-800/80 pt-2">
                      <span className="text-[9px] text-slate-400 truncate max-w-[150px]">
                        {inc.description || 'Deposit Stream'}
                      </span>
                      <div className="flex items-center gap-1.5">
                        <button onClick={() => handleEditClick(inc)} className="p-1 rounded bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400">
                          <Edit2 size={11} />
                        </button>
                        <button onClick={() => handleDuplicateClick(inc)} className="p-1 rounded bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400">
                          <Copy size={11} />
                        </button>
                        <button onClick={() => handleDelete(inc.id)} className="p-1 rounded bg-rose-50 dark:bg-rose-950/40 text-rose-650 dark:text-rose-400">
                          <Trash2 size={11} />
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Pagination & Filter Summary Footer */}
            {incomeData.totalPages > 1 && (
              <div className="flex flex-col sm:flex-row justify-between items-center mt-5 pt-3.5 border-t border-slate-150 dark:border-slate-800 gap-3">
                <div className="flex flex-wrap items-center gap-3.5 text-[10px] font-bold text-slate-550 dark:text-slate-355">
                  <span>
                    Showing {page * 10 + 1}–{Math.min((page + 1) * 10, incomeData.totalElements)} of {incomeData.totalElements} records
                  </span>
                  <span className="h-3 w-px bg-slate-200 dark:bg-slate-800" />
                  <span>
                    Total Filtered: <span className="text-emerald-650 dark:text-emerald-400 font-extrabold">+{currencySymbol}{totalFilteredAmount.toFixed(2)}</span>
                  </span>
                </div>
                
                <div className="flex items-center gap-1">
                  <button
                    disabled={page === 0}
                    onClick={() => setPage(page - 1)}
                    className="px-2.5 py-1.5 border border-slate-200 dark:border-slate-800 rounded-lg text-[10px] font-bold uppercase tracking-wider disabled:opacity-50 hover:bg-slate-50 dark:hover:bg-slate-900 transition-all text-slate-600 dark:text-slate-400"
                  >
                    Prev
                  </button>
                  
                  {/* Page numbers */}
                  {[...Array(incomeData.totalPages)].map((_, i) => (
                    <button
                      key={i}
                      onClick={() => setPage(i)}
                      className={`w-6 h-6 rounded-lg text-[10px] font-extrabold transition-all ${
                        page === i
                          ? 'bg-indigo-650 text-white shadow-sm'
                          : 'border border-slate-205 dark:border-slate-800 text-slate-655 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-900'
                      }`}
                    >
                      {i + 1}
                    </button>
                  ))}
                  
                  <button
                    disabled={page >= incomeData.totalPages - 1}
                    onClick={() => setPage(page + 1)}
                    className="px-2.5 py-1.5 border border-slate-200 dark:border-slate-800 rounded-lg text-[10px] font-bold uppercase tracking-wider disabled:opacity-50 hover:bg-slate-50 dark:hover:bg-slate-900 transition-all text-slate-600 dark:text-slate-400"
                  >
                    Next
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Add/Edit Income Drawer/Modal */}
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-end bg-black/40 backdrop-blur-sm">
          <div className="w-full max-w-md h-full bg-white dark:bg-dark-surface p-6 shadow-2xl border-l border-light-border dark:border-dark-border flex flex-col justify-between overflow-y-auto">
            <div>
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50">{editingIncome ? 'Edit Income' : 'Record Income'}</h2>
                <button onClick={() => { resetForm(); setIsOpen(false); }} className="text-slate-400 hover:text-slate-600">
                  <X size={20} />
                </button>
              </div>

              <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                {/* Amount */}
                <div className="flex flex-col gap-1.5">
                  <label htmlFor="income-amount" className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                    Amount ({currencySymbol})
                  </label>
                  <input
                    id="income-amount"
                    type="number"
                    step="0.01"
                    required
                    placeholder="0.00"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 outline-none text-sm bg-transparent text-slate-900 dark:text-slate-50"
                  />
                </div>

                {/* Source */}
                <div className="flex flex-col gap-1.5">
                  <label htmlFor="income-source" className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                    Source
                  </label>
                  <input
                    id="income-source"
                    type="text"
                    required
                    placeholder="e.g. Salary, Freelance"
                    value={source}
                    onChange={(e) => setSource(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 outline-none text-sm bg-transparent text-slate-900 dark:text-slate-50"
                  />
                </div>

                {/* Date */}
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                    Date & Time
                  </label>
                  <input
                    type="datetime-local"
                    required
                    value={incomeDate}
                    onChange={(e) => setIncomeDate(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 outline-none text-sm bg-transparent text-slate-900 dark:text-slate-50"
                  />
                </div>

                {/* Description */}
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                    Description (Optional)
                  </label>
                  <input
                    type="text"
                    placeholder="e.g. Freelance project payment"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 outline-none text-sm bg-transparent text-slate-900 dark:text-slate-50"
                  />
                </div>

                {/* Notes */}
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                    Notes (Optional)
                  </label>
                  <textarea
                    placeholder="e.g. Bank transfer ID reference"
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 outline-none text-sm bg-transparent text-slate-900 dark:text-slate-50 h-20 resize-none"
                  />
                </div>

                <button
                  type="submit"
                  disabled={createMutation.isPending || updateMutation.isPending}
                  className="w-full mt-4 py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white font-semibold text-sm transition-all"
                >
                  {editingIncome 
                    ? (updateMutation.isPending ? 'Updating...' : 'Save Changes')
                    : (createMutation.isPending ? 'Recording...' : 'Record Income')}
                </button>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
export default IncomePage;
