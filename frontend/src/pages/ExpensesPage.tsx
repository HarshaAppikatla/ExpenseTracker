import React, { useState, useEffect } from 'react';
import { useExpenses, useCreateExpense, useDeleteExpense, useMerchantSuggestions, useUpdateExpense } from '@/features/expense/hooks/useExpenses';
import { useCategories } from '@/features/category/hooks/useCategories';
import toast from 'react-hot-toast';
import { Plus, Trash2, MapPin, Receipt, Search, Tag, X, FileUp, Download, Edit2, TrendingUp, TrendingDown, Wallet, Calendar, ListFilter, Copy, HelpCircle, ExternalLink, Globe } from 'lucide-react';
import { useProfile } from '@/features/profile/hooks/useProfile';
import { useUploadReceipt, useDeleteReceipt } from '@/features/receipt/hooks/useReceipts';
import { receiptService } from '@/features/receipt/services/receiptService';
import apiClient from '@/core/api/client';
import { Expense } from '@/features/expense/services/expenseService';
import { useDashboardSummary } from '@/features/dashboard/hooks/useDashboard';

// Lazy loader for secure authenticated receipt thumbnails in rows
const ReceiptThumbnail: React.FC<{ receiptId: string; onView: () => void }> = ({ receiptId, onView }) => {
  const [thumbnailUrl, setThumbnailUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const fetchThumbnail = async () => {
    if (thumbnailUrl || loading) return;
    try {
      setLoading(true);
      const response = await apiClient.get(`/receipts/${receiptId}`, {
        responseType: 'blob',
      });
      const blob = new Blob([response.data], { type: response.headers['content-type'] });
      const objectUrl = URL.createObjectURL(blob);
      setThumbnailUrl(objectUrl);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchThumbnail();
    return () => {
      if (thumbnailUrl) {
        URL.revokeObjectURL(thumbnailUrl);
      }
    };
  }, [receiptId]);

  if (loading) {
    return <div className="w-10 h-10 rounded-lg bg-slate-100 dark:bg-slate-800 animate-pulse border border-slate-200 dark:border-slate-700" />;
  }

  return (
    <div className="relative group/thumb cursor-pointer flex items-center gap-1.5" onClick={onView}>
      {thumbnailUrl ? (
        <img 
          src={thumbnailUrl} 
          alt="Receipt thumbnail" 
          className="w-10 h-10 rounded-lg object-cover border border-slate-200 dark:border-slate-705 hover:scale-105 transition-transform duration-200 shadow-sm" 
          title="Click to view attachment"
        />
      ) : (
        <div className="w-10 h-10 rounded-lg bg-slate-100 dark:bg-slate-800 flex items-center justify-center text-slate-400 border border-slate-200 dark:border-slate-700">
          <Receipt size={16} />
        </div>
      )}
    </div>
  );
};

export const ExpensesPage: React.FC = () => {
  const { data: profile } = useProfile();
  const currencySymbol = profile?.preferredCurrency || '$';

  const [page, setPage] = useState(0);
  const { data: expensesData, isLoading: isExpensesLoading } = useExpenses(page, 10);
  const { data: categories = [] } = useCategories();
  const { data: summary } = useDashboardSummary();

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [sortOrder, setSortOrder] = useState('date-desc');
  const [selectedDateRange, setSelectedDateRange] = useState('all');
  const [quickFilter, setQuickFilter] = useState('all');

  const createMutation = useCreateExpense();
  const updateMutation = useUpdateExpense();
  const deleteMutation = useDeleteExpense();

  const [editingExpense, setEditingExpense] = useState<Expense | null>(null);
  
  const uploadReceiptMutation = useUploadReceipt();
  const deleteReceiptMutation = useDeleteReceipt();
  const [viewingReceiptId, setViewingReceiptId] = useState<string | null>(null);
  const [previewReceiptUrl, setPreviewReceiptUrl] = useState<string | null>(null);
  const [activeMapCoords, setActiveMapCoords] = useState<{ lat: number; lng: number; name: string } | null>(null);

  const handleUploadReceipt = (expenseId: string, event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    uploadReceiptMutation.mutate(
      { expenseId, file },
      {
        onSuccess: () => {
          toast.success('Receipt uploaded successfully!');
        },
        onError: (err: any) => {
          toast.error(err.message || 'Failed to upload receipt');
        },
      }
    );
  };

  const handleDeleteReceipt = (receiptId: string) => {
    if (!window.confirm('Are you sure you want to remove this receipt?')) return;
    deleteReceiptMutation.mutate(receiptId, {
      onSuccess: () => {
        toast.success('Receipt removed successfully!');
      },
      onError: (err: any) => {
        toast.error(err.message || 'Failed to remove receipt');
      },
    });
  };

  const handleViewReceipt = async (receiptId: string) => {
    try {
      setViewingReceiptId(receiptId);
      const response = await apiClient.get(`/receipts/${receiptId}`, {
        responseType: 'blob',
      });
      const blob = new Blob([response.data], { type: response.headers['content-type'] });
      const objectUrl = URL.createObjectURL(blob);
      setPreviewReceiptUrl(objectUrl);
    } catch (err) {
      console.error(err);
      toast.error('Failed to view receipt file');
    } finally {
      setViewingReceiptId(null);
    }
  };

  // Modal & Form States
  const [isOpen, setIsOpen] = useState(false);
  const [amount, setAmount] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [expenseDate, setExpenseDate] = useState(new Date().toISOString().substring(0, 16));
  const [merchant, setMerchant] = useState('');
  const [description, setDescription] = useState('');
  const [notes, setNotes] = useState('');
  const [tagInput, setTagInput] = useState('');
  const [tags, setTags] = useState<string[]>([]);
  
  // Geolocation states
  const [locationName, setLocationName] = useState('');
  const [coords, setCoords] = useState<{ lat: number; lng: number } | null>(null);
  const [resolvingLocation, setResolvingLocation] = useState(false);
  const [showSuggestions, setShowSuggestions] = useState(false);

  // Suggestions search hook
  const { data: merchantSuggestions = [] } = useMerchantSuggestions(merchant, merchant.length >= 2);

  const handleAddTag = () => {
    const trimmed = tagInput.trim();
    if (trimmed && !tags.includes(trimmed)) {
      setTags([...tags, trimmed]);
      setTagInput('');
    }
  };

  const handleRemoveTag = (tagToRemove: string) => {
    setTags(tags.filter((t) => t !== tagToRemove));
  };

  const handleGetLocation = () => {
    if (!navigator.geolocation) {
      toast.error('Geolocation is not supported by your browser');
      return;
    }

    setResolvingLocation(true);
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setCoords({
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        });
        setLocationName('Current Location Tagged');
        setResolvingLocation(false);
        toast.success('Coordinates retrieved successfully!');
      },
      (error) => {
        console.error(error);
        setResolvingLocation(false);
        toast.error('Unable to retrieve coordinates. Please check browser permissions.');
      }
    );
  };

  const handleEditClick = (exp: Expense) => {
    setEditingExpense(exp);
    setAmount(exp.amount.toString());
    setCategoryId(exp.category.id);
    const dateObj = new Date(exp.expenseDate);
    const tzOffset = dateObj.getTimezoneOffset() * 60000;
    const localISOTime = (new Date(dateObj.getTime() - tzOffset)).toISOString().substring(0, 16);
    setExpenseDate(localISOTime);
    setMerchant(exp.merchant || '');
    setDescription(exp.description || '');
    setNotes(exp.notes || '');
    setTags(exp.tags || []);
    if (exp.latitude && exp.longitude) {
      setCoords({ lat: exp.latitude, lng: exp.longitude });
      setLocationName(exp.locationName || 'Location Tagged');
    } else {
      setCoords(null);
      setLocationName('');
    }
    setIsOpen(true);
  };

  const handleDuplicateClick = (exp: Expense) => {
    setEditingExpense(null);
    setAmount(exp.amount.toString());
    setCategoryId(exp.category.id);
    setExpenseDate(new Date().toISOString().substring(0, 16));
    setMerchant(exp.merchant ? `${exp.merchant} (Copy)` : '');
    setDescription(exp.description || '');
    setNotes(exp.notes || '');
    setTags(exp.tags || []);
    if (exp.latitude && exp.longitude) {
      setCoords({ lat: exp.latitude, lng: exp.longitude });
      setLocationName(exp.locationName || 'Location Tagged');
    } else {
      setCoords(null);
      setLocationName('');
    }
    setIsOpen(true);
    toast.success('Expense details duplicated into form!');
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const numAmt = parseFloat(amount);
    if (isNaN(numAmt) || numAmt <= 0) {
      toast.error('Amount must be a positive number');
      return;
    }
    if (!categoryId) {
      toast.error('Category is required');
      return;
    }

    const payload = {
      categoryId,
      amount: numAmt,
      expenseDate: new Date(expenseDate).toISOString(),
      merchant: merchant.trim() || undefined,
      description: description.trim() || undefined,
      notes: notes.trim() || undefined,
      latitude: coords?.lat,
      longitude: coords?.lng,
      locationName: locationName.trim() || undefined,
      tags: tags.length > 0 ? tags : undefined,
    };

    if (editingExpense) {
      updateMutation.mutate(
        { id: editingExpense.id, data: payload },
        {
          onSuccess: () => {
            toast.success('Expense updated successfully!');
            resetForm();
            setIsOpen(false);
          },
          onError: (err: any) => {
            toast.error(err.message || 'Failed to update expense');
          },
        }
      );
    } else {
      createMutation.mutate(
        payload,
        {
          onSuccess: () => {
            toast.success('Expense recorded successfully!');
            resetForm();
            setIsOpen(false);
          },
          onError: (err: any) => {
            toast.error(err.message || 'Failed to record expense');
          },
        }
      );
    }
  };

  const resetForm = () => {
    setAmount('');
    setCategoryId('');
    setExpenseDate(new Date().toISOString().substring(0, 16));
    setMerchant('');
    setDescription('');
    setNotes('');
    setTags([]);
    setCoords(null);
    setLocationName('');
    setEditingExpense(null);
  };

  const handleDelete = (id: string) => {
    if (!window.confirm('Are you sure you want to delete this expense?')) return;
    deleteMutation.mutate(id, {
      onSuccess: () => {
        toast.success('Expense deleted successfully!');
      },
      onError: (err: any) => {
        toast.error(err.message || 'Failed to delete expense');
      },
    });
  };

  const getFilteredAndSortedExpenses = () => {
    if (!expensesData?.content) return [];

    let list = [...expensesData.content];

    // Filter by search query
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase().trim();
      list = list.filter(exp => 
        (exp.merchant && exp.merchant.toLowerCase().includes(q)) ||
        (exp.description && exp.description.toLowerCase().includes(q)) ||
        (exp.category && exp.category.name.toLowerCase().includes(q))
      );
    }

    // Filter by category
    if (selectedCategory) {
      list = list.filter(exp => exp.category.id === selectedCategory);
    }

    // Filter by Date Range Picker dropdown
    if (selectedDateRange !== 'all') {
      const now = new Date();
      list = list.filter(exp => {
        const expDate = new Date(exp.expenseDate);
        if (selectedDateRange === 'this-month') {
          return expDate.getMonth() === now.getMonth() && expDate.getFullYear() === now.getFullYear();
        } else if (selectedDateRange === 'last-30-days') {
          const diffTime = Math.abs(now.getTime() - expDate.getTime());
          const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
          return diffDays <= 30;
        } else if (selectedDateRange === 'last-7-days') {
          const diffTime = Math.abs(now.getTime() - expDate.getTime());
          const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
          return diffDays <= 7;
        }
        return true;
      });
    }

    // Quick filter chips (Today, Week, Month)
    if (quickFilter !== 'all') {
      const now = new Date();
      list = list.filter(exp => {
        const expDate = new Date(exp.expenseDate);
        if (quickFilter === 'today') {
          return expDate.getDate() === now.getDate() &&
                 expDate.getMonth() === now.getMonth() &&
                 expDate.getFullYear() === now.getFullYear();
        } else if (quickFilter === 'week') {
          const oneWeekAgo = new Date();
          oneWeekAgo.setDate(now.getDate() - 7);
          return expDate >= oneWeekAgo;
        } else if (quickFilter === 'month') {
          return expDate.getMonth() === now.getMonth() &&
                 expDate.getFullYear() === now.getFullYear();
        }
        return true;
      });
    }

    // Sort order
    list.sort((a, b) => {
      if (sortOrder === 'date-desc') {
        return new Date(b.expenseDate).getTime() - new Date(a.expenseDate).getTime();
      } else if (sortOrder === 'date-asc') {
        return new Date(a.expenseDate).getTime() - new Date(b.expenseDate).getTime();
      } else if (sortOrder === 'amount-desc') {
        return b.amount - a.amount;
      } else if (sortOrder === 'amount-asc') {
        return a.amount - b.amount;
      }
      return 0;
    });

    return list;
  };

  const displayedExpenses = getFilteredAndSortedExpenses();
  const totalFilteredAmount = displayedExpenses.reduce((sum, exp) => sum + exp.amount, 0);

  const topCategory = summary?.categoryBreakdown && summary.categoryBreakdown.length > 0
    ? [...summary.categoryBreakdown].sort((a, b) => b.amount - a.amount)[0]
    : null;
  const topCategoryPercent = topCategory && summary?.totalExpenses
    ? Math.round((topCategory.amount / summary.totalExpenses) * 100)
    : 0;

  // Resolve Category click mapping to set filter
  const handleCategoryBadgeClick = (catName: string) => {
    const found = categories.find(c => c.name.toLowerCase() === catName.toLowerCase());
    if (found) {
      setSelectedCategory(found.id);
      toast.success(`Filtering by category: ${found.name}`);
    }
  };

  // Brand logos mapping with fallback monograms
  const getMerchantLogoSymbol = (merchantName: string) => {
    const name = merchantName.toLowerCase();
    if (name.includes('starbucks')) return { emoji: '☕', bg: 'bg-emerald-50 text-emerald-600 border border-emerald-100' };
    if (name.includes('uber')) return { emoji: '🚗', bg: 'bg-slate-900 text-white border border-slate-950' };
    if (name.includes('amazon')) return { emoji: '📦', bg: 'bg-amber-50 text-amber-600 border border-amber-100' };
    if (name.includes('comcast')) return { emoji: '📡', bg: 'bg-indigo-50 text-indigo-600 border border-indigo-100' };
    if (name.includes('mcdonald')) return { emoji: '🍔', bg: 'bg-red-50 text-red-600 border border-red-100' };
    return null;
  };

  // Brand type descriptors mapping
  const getMerchantType = (merchantName: string, categoryName: string) => {
    const name = merchantName.toLowerCase();
    if (name.includes('starbucks')) return 'Coffee Shop';
    if (name.includes('uber')) return 'Taxi / Ride';
    if (name.includes('amazon')) return 'Online Marketplace';
    if (name.includes('comcast')) return 'Telecom Utility';
    if (name.includes('mcdonald')) return 'Fast Food';
    return categoryName || 'General Merchant';
  };

  // Payment method badge helper
  const getPaymentBadge = (description: string = '', categoryName: string = '') => {
    const desc = description.toLowerCase();
    const cat = categoryName.toLowerCase();
    if (desc.includes('cash') || cat.includes('cash')) {
      return { label: 'Cash', icon: '💵', bg: 'bg-amber-50 text-amber-700 border-amber-100 dark:bg-amber-950/20 dark:text-amber-400 dark:border-amber-900/30' };
    }
    if (desc.includes('upi') || desc.includes('transfer') || desc.includes('gpay') || desc.includes('phonepe')) {
      return { label: 'UPI', icon: '📱', bg: 'bg-emerald-50 text-emerald-700 border-emerald-100 dark:bg-emerald-950/20 dark:text-emerald-400 dark:border-emerald-900/30' };
    }
    return { label: 'Card', icon: '💳', bg: 'bg-slate-50 text-slate-700 border-slate-200 dark:bg-slate-800/40 dark:text-slate-400 dark:border-slate-750/50' };
  };

  // Loader template
  if (isExpensesLoading) {
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
          <h1 className="text-xl font-bold text-slate-900 dark:text-slate-50">Expenses Ledger</h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">Track, search and manage all your expenses in one place.</p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => { resetForm(); setIsOpen(true); }}
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-xs shadow-md transition-all active:scale-[0.98]"
          >
            <Plus size={16} />
            Add Expense
          </button>
        </div>
      </div>

      {/* KPI Bento summary grid */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
        {/* Total Expenses Card */}
        <div className="bg-white dark:bg-dark-surface p-4 rounded-xl border border-light-border dark:border-dark-border flex justify-between items-center shadow-sm relative overflow-hidden group hover:shadow hover:border-slate-300 dark:hover:border-slate-700 transition-all duration-200">
          <div className="flex flex-col gap-0.5 z-10">
            <div className="flex items-center gap-1">
              <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Total Expenses</span>
              <div className="relative group/tooltip">
                <HelpCircle size={11} className="text-slate-350 cursor-pointer" />
                <div className="absolute bottom-full mb-1.5 hidden group-hover/tooltip:block bg-slate-950 text-white text-[9px] p-2 rounded shadow-lg z-50 w-40">
                  Sum total of your recorded personal finance expenses.
                </div>
              </div>
            </div>
            <span className="text-lg font-bold text-slate-800 dark:text-slate-100 mt-0.5">
              {currencySymbol} {summary?.totalExpenses?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}
            </span>
            <div className="flex items-center gap-1 mt-1.5">
              <span className="flex items-center gap-0.5 text-[9px] font-bold text-rose-500 bg-rose-50 dark:bg-rose-950/30 px-1 py-0.5 rounded-full">
                <TrendingUp size={9} /> 12.5%
              </span>
              <span className="text-[9px] text-slate-400 font-medium">This Month</span>
            </div>
          </div>
          {/* Sparkline trend representation SVG */}
          <div className="absolute bottom-1 right-12 w-12 h-6 opacity-20 group-hover:opacity-50 transition-opacity duration-200">
            <svg viewBox="0 0 100 30" className="w-full h-full text-rose-500" fill="none">
              <path d="M0,25 Q15,5 30,18 T60,8 T90,20" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            </svg>
          </div>
          <div className="p-2.5 bg-gradient-to-tr from-rose-500/10 to-rose-500/5 dark:from-rose-500/20 rounded-lg text-rose-500 z-10 shadow-inner group-hover:scale-102 transition-transform duration-200">
            <Wallet size={18} />
          </div>
        </div>

        {/* Transactions Card */}
        <div className="bg-white dark:bg-dark-surface p-4 rounded-xl border border-light-border dark:border-dark-border flex justify-between items-center shadow-sm relative overflow-hidden group hover:shadow hover:border-slate-300 dark:hover:border-slate-700 transition-all duration-200">
          <div className="flex flex-col gap-0.5 z-10">
            <div className="flex items-center gap-1">
              <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Transactions</span>
              <div className="relative group/tooltip">
                <HelpCircle size={11} className="text-slate-350 cursor-pointer" />
                <div className="absolute bottom-full mb-1.5 hidden group-hover/tooltip:block bg-slate-950 text-white text-[9px] p-2 rounded shadow-lg z-50 w-40">
                  Count of transaction rows currently logged.
                </div>
              </div>
            </div>
            <span className="text-lg font-bold text-slate-800 dark:text-slate-100 mt-0.5">
              {expensesData?.totalElements || 0}
            </span>
            <div className="flex items-center gap-1 mt-1.5">
              <span className="flex items-center gap-0.5 text-[9px] font-bold text-indigo-500 bg-indigo-50 dark:bg-indigo-950/30 px-1 py-0.5 rounded-full">
                <TrendingUp size={9} /> +2
              </span>
              <span className="text-[9px] text-slate-400 font-medium">This Month</span>
            </div>
          </div>
          {/* Sparkline trend representation SVG */}
          <div className="absolute bottom-1 right-12 w-12 h-6 opacity-20 group-hover:opacity-50 transition-opacity duration-200">
            <svg viewBox="0 0 100 30" className="w-full h-full text-indigo-500" fill="none">
              <path d="M0,15 Q25,25 50,5 T100,20" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            </svg>
          </div>
          <div className="p-2.5 bg-gradient-to-tr from-indigo-500/10 to-indigo-500/5 dark:from-indigo-500/20 rounded-lg text-indigo-500 z-10 shadow-inner group-hover:scale-102 transition-transform duration-200">
            <ListFilter size={18} />
          </div>
        </div>

        {/* Average Spend Card */}
        <div className="bg-white dark:bg-dark-surface p-4 rounded-xl border border-light-border dark:border-dark-border flex justify-between items-center shadow-sm relative overflow-hidden group hover:shadow hover:border-slate-300 dark:hover:border-slate-700 transition-all duration-200">
          <div className="flex flex-col gap-0.5 z-10">
            <div className="flex items-center gap-1">
              <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Average Spend</span>
              <div className="relative group/tooltip">
                <HelpCircle size={11} className="text-slate-350 cursor-pointer" />
                <div className="absolute bottom-full mb-1.5 hidden group-hover/tooltip:block bg-slate-950 text-white text-[9px] p-2 rounded shadow-lg z-50 w-40">
                  Average expense cost calculated per transaction.
                </div>
              </div>
            </div>
            <span className="text-lg font-bold text-slate-800 dark:text-slate-100 mt-0.5">
              {currencySymbol} {((expensesData?.totalElements ? (summary?.totalExpenses || 0) / expensesData.totalElements : 0)).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </span>
            <div className="flex items-center gap-1 mt-1.5">
              <span className="flex items-center gap-0.5 text-[9px] font-bold text-emerald-500 bg-emerald-50 dark:bg-emerald-950/30 px-1 py-0.5 rounded-full">
                <TrendingDown size={9} /> 5.8%
              </span>
              <span className="text-[9px] text-slate-400 font-medium">Per Transaction</span>
            </div>
          </div>
          {/* Sparkline trend representation SVG */}
          <div className="absolute bottom-1 right-12 w-12 h-6 opacity-20 group-hover:opacity-50 transition-opacity duration-200">
            <svg viewBox="0 0 100 30" className="w-full h-full text-emerald-500" fill="none">
              <path d="M0,5 Q30,25 60,10 T100,5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            </svg>
          </div>
          <div className="p-2.5 bg-gradient-to-tr from-emerald-500/10 to-emerald-500/5 dark:from-emerald-500/20 rounded-lg text-emerald-500 z-10 shadow-inner group-hover:scale-102 transition-transform duration-200">
            <TrendingUp size={18} className="transform rotate-180" />
          </div>
        </div>

        {/* Top Category Card (Interactive) */}
        <div 
          onClick={() => topCategory && handleCategoryBadgeClick(topCategory.name)}
          className="bg-white dark:bg-dark-surface p-4 rounded-xl border border-light-border dark:border-dark-border flex justify-between items-center shadow-sm relative overflow-hidden group hover:shadow hover:border-slate-300 dark:hover:border-slate-700 transition-all duration-200 cursor-pointer active:scale-[0.99]"
          title="Click to filter by category"
        >
          <div className="flex flex-col gap-0.5 z-10">
            <div className="flex items-center gap-1">
              <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Top Category</span>
              <div className="relative group/tooltip">
                <HelpCircle size={11} className="text-slate-350 cursor-pointer" />
                <div className="absolute bottom-full mb-1.5 hidden group-hover/tooltip:block bg-slate-950 text-white text-[9px] p-2 rounded shadow-lg z-50 w-40">
                  Your highest spending category this month.
                </div>
              </div>
            </div>
            <span className="text-lg font-bold text-slate-800 dark:text-slate-100 mt-0.5 truncate max-w-[130px]">
              {topCategory ? topCategory.name : 'N/A'}
            </span>
            <div className="flex items-center gap-1 mt-1.5">
              <span className="text-[9px] text-indigo-600 dark:text-indigo-400 font-bold bg-indigo-50 dark:bg-indigo-950/20 px-1.5 py-0.5 rounded-full">
                {topCategoryPercent}% of total
              </span>
            </div>
          </div>
          <div className="p-2.5 bg-gradient-to-tr from-purple-500/10 to-purple-500/5 dark:from-purple-500/20 rounded-lg text-purple-500 z-10 shadow-inner group-hover:scale-102 transition-transform duration-200">
            <Tag size={18} />
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
              placeholder="Search expenses..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-8 py-1.5 text-xs rounded-lg border border-slate-205 dark:border-slate-800 bg-transparent text-slate-800 dark:text-slate-200 focus:ring-1 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all"
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
                  ? 'bg-indigo-650 text-white shadow-sm'
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
          {(selectedCategory || selectedDateRange !== 'all' || searchQuery || quickFilter !== 'all') && (
            <span className="text-[9px] font-bold text-indigo-600 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-950/20 px-2 py-0.5 rounded-full mr-auto flex items-center gap-1 animate-fade-in">
              Active Filters
              <button 
                onClick={() => {
                  setSelectedCategory('');
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

          {/* Category Filter */}
          <div className="relative">
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              style={{ paddingLeft: '34px' }}
              className="pr-7 py-1.5 text-xs font-semibold rounded-lg border border-slate-200 dark:border-slate-800 bg-transparent text-slate-705 dark:text-slate-300 outline-none appearance-none cursor-pointer hover:bg-slate-50 dark:hover:bg-slate-900 transition-all"
            >
              <option value="">All Categories</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
            <Tag size={13} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
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
        {!displayedExpenses.length ? (
          <div className="bg-white dark:bg-dark-surface p-10 rounded-2xl text-center flex flex-col items-center justify-center gap-3.5 min-h-[300px]">
            <div className="w-14 h-14 rounded-xl bg-indigo-50 dark:bg-indigo-950/30 text-indigo-600 dark:text-indigo-400 flex items-center justify-center">
              <Receipt size={28} />
            </div>
            <div className="flex flex-col gap-0.5 max-w-sm">
              <h3 className="text-sm font-bold text-slate-850 dark:text-slate-100">No expenses yet</h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">Start tracking your spending today.</p>
            </div>
            <button
              onClick={() => { resetForm(); setIsOpen(true); }}
              className="mt-1 flex items-center gap-1.5 px-4.5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-xs shadow-md transition-all active:scale-[0.98]"
            >
              <Plus size={14} />
              Add Expense
            </button>
          </div>
        ) : (
          <div className="w-full">
            {/* Desktop Table View */}
            <div className="hidden sm:block overflow-x-auto max-h-[60vh] scrollbar-thin">
              <table className="w-full text-left text-xs text-slate-500 dark:text-slate-400 border-collapse table-fixed">
                <thead className="text-[10px] tracking-wider uppercase bg-slate-50 dark:bg-slate-900/50 text-slate-650 dark:text-slate-350 sticky top-0 z-20 border-b border-slate-150 dark:border-slate-800 shadow-[0_1px_0_0_rgba(224,242,254,0.05)]">
                  <tr>
                    <th className="px-5 py-3 w-[75px]">Date</th>
                    <th className="px-5 py-3 w-[130px]">Category</th>
                    <th className="px-5 py-3 w-[160px]">Merchant</th>
                    <th className="px-5 py-3 text-right w-[110px]">Amount</th>
                    <th className="px-5 py-3 w-[160px]">Location</th>
                    <th className="px-5 py-3 w-[115px]">Receipt</th>
                    <th className="px-5 py-3 text-center w-[120px]">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-850">
                  {displayedExpenses.map((exp, idx) => {
                    const dateObj = new Date(exp.expenseDate);
                    const day = dateObj.getDate();
                    const month = dateObj.toLocaleString('en-US', { month: 'short' }).toUpperCase();
                    const year = dateObj.getFullYear();
                    
                    const logoDetails = getMerchantLogoSymbol(exp.merchant || '');
                    const isOnline = exp.merchant?.toLowerCase().includes('online') || exp.description?.toLowerCase().includes('online');
                    const merchantType = getMerchantType(exp.merchant || '', exp.category?.name || '');
                    const paymentBadge = getPaymentBadge(exp.description, exp.category?.name);

                    return (
                      <tr 
                        key={exp.id} 
                        className={`group transition-colors duration-150 ${
                          idx % 2 === 0 ? 'bg-transparent' : 'bg-slate-50/20 dark:bg-slate-900/5'
                        } hover:bg-slate-50/70 dark:hover:bg-slate-900/15`}
                      >
                        {/* Styled Prominent Date Cell */}
                        <td className="px-5 py-2.5 whitespace-nowrap">
                          <div className="flex flex-col items-center justify-center bg-slate-50 dark:bg-slate-900/60 w-11 h-13 rounded-lg border border-slate-100 dark:border-slate-800/80 group-hover:scale-102 transition-transform duration-200">
                            <span className="text-[9px] font-extrabold text-slate-400 uppercase tracking-wider">{month}</span>
                            <span className="text-lg font-black text-slate-900 dark:text-slate-50 leading-none mt-0.5">{day}</span>
                            <span className="text-[8px] text-slate-400 font-bold mt-0.5">{year}</span>
                          </div>
                        </td>

                        {/* Styled Category Pill */}
                        <td className="px-5 py-2.5 whitespace-nowrap">
                          <span
                            onClick={() => handleCategoryBadgeClick(exp.category?.name || '')}
                            style={{ backgroundColor: `${exp.category?.color}12`, color: exp.category?.color }}
                            className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-bold cursor-pointer hover:opacity-85 transition-opacity"
                            title="Click to filter by category"
                          >
                            <span className="material-icons-outlined text-xs leading-none">
                              {exp.category?.icon || 'folder'}
                            </span>
                            {exp.category?.name || 'Uncategorized'}
                          </span>
                        </td>

                        {/* Merchant & Merchant Type */}
                        <td className="px-5 py-2.5 whitespace-nowrap overflow-hidden text-ellipsis">
                          <div className="flex items-center gap-2.5">
                            {logoDetails ? (
                              <div className={`w-8 h-8 rounded-full flex items-center justify-center font-bold text-sm shadow-sm ${logoDetails.bg}`}>
                                {logoDetails.emoji}
                              </div>
                            ) : (
                              <div className="w-8 h-8 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center font-bold text-xs text-slate-700 dark:text-slate-200">
                                {exp.merchant ? exp.merchant.charAt(0).toUpperCase() : '?'}
                              </div>
                            )}
                            <div className="flex flex-col overflow-hidden text-ellipsis">
                              <span 
                                onClick={() => exp.merchant && setSearchQuery(exp.merchant)}
                                className="font-semibold text-slate-850 dark:text-slate-100 text-xs hover:text-indigo-600 dark:hover:text-indigo-400 cursor-pointer truncate"
                                title="Filter history by merchant"
                              >
                                {exp.merchant || 'Unknown'}
                              </span>
                              <span className="text-[9px] text-slate-400 truncate">
                                {merchantType}
                              </span>
                            </div>
                          </div>
                        </td>

                        {/* Amount & Payment Badge */}
                        <td className="px-5 py-2.5 whitespace-nowrap text-right">
                          <div className="flex flex-col text-right pr-2">
                            <span className="font-bold text-slate-800 dark:text-slate-50 text-xs">
                              {currencySymbol} {exp.amount.toFixed(2)}
                            </span>
                            <span className={`inline-flex items-center justify-center gap-0.5 self-end px-1.5 py-0.5 rounded text-[8px] font-black mt-0.5 border uppercase tracking-wider ${paymentBadge.bg}`}>
                              <span>{paymentBadge.icon}</span>
                              <span>{paymentBadge.label}</span>
                            </span>
                          </div>
                        </td>

                        {/* Location */}
                        <td className="px-5 py-2.5 whitespace-nowrap overflow-hidden text-ellipsis">
                          {exp.locationName ? (
                            <div 
                              onClick={() => setActiveMapCoords({ lat: exp.latitude || 0, lng: exp.longitude || 0, name: exp.locationName || '' })}
                              className="flex items-start gap-1 text-[10px] cursor-pointer group/loc"
                              title="Click to view map coordinates"
                            >
                              <MapPin size={12} className="text-slate-400 dark:text-slate-500 mt-0.5 group-hover/loc:text-indigo-500 transition-colors" />
                              <div className="flex flex-col overflow-hidden text-ellipsis">
                                <span className="font-semibold text-slate-700 dark:text-slate-350 text-[10px] leading-tight truncate">
                                  {exp.locationName.split(',')[0]}
                                </span>
                                {exp.locationName.split(',').slice(1).join(',') && (
                                  <span className="text-[8px] text-slate-400 truncate">
                                    {exp.locationName.split(',').slice(1).join(',').trim()}
                                  </span>
                                )}
                              </div>
                            </div>
                          ) : isOnline ? (
                            <span className="inline-flex items-center gap-0.5 text-[8px] font-black tracking-wider uppercase text-slate-400 bg-slate-50 dark:bg-slate-900/50 px-1.5 py-0.5 rounded border border-slate-100 dark:border-slate-800">
                              🌐 Online
                            </span>
                          ) : (
                            <span className="text-slate-300 dark:text-slate-700">—</span>
                          )}
                        </td>

                        {/* Lazy Secure Receipt Thumbnail */}
                        <td className="px-5 py-2.5 whitespace-nowrap">
                          {exp.receipt ? (
                            <div className="flex items-center gap-1">
                              <ReceiptThumbnail 
                                receiptId={exp.receipt.id} 
                                onView={() => handleViewReceipt(exp.receipt!.id)} 
                              />
                              <button
                                onClick={() => handleDeleteReceipt(exp.receipt!.id)}
                                className="p-1 hover:bg-slate-100 dark:hover:bg-slate-800 rounded text-slate-400 hover:text-red-500 transition-colors"
                                title="Remove Receipt"
                              >
                                <X size={11} />
                              </button>
                            </div>
                          ) : (
                            <div className="flex items-center">
                              <label
                                htmlFor={`file-${exp.id}`}
                                className="cursor-pointer px-2 py-1 rounded border border-dashed border-slate-300 dark:border-slate-700 flex items-center gap-1 text-[9px] text-slate-405 hover:text-indigo-650 hover:border-indigo-600 hover:bg-slate-50 dark:hover:bg-slate-900/40 transition-all"
                                title="Upload receipt attachment"
                              >
                                <FileUp size={11} />
                                <span>Upload</span>
                              </label>
                              <input
                                type="file"
                                id={`file-${exp.id}`}
                                className="hidden"
                                accept=".png,.jpg,.jpeg,.pdf"
                                onChange={(e) => handleUploadReceipt(exp.id, e)}
                              />
                            </div>
                          )}
                        </td>

                        {/* Actions row */}
                        <td className="px-5 py-2.5 whitespace-nowrap text-center">
                          <div className="flex items-center justify-center gap-1.5">
                            <button
                              onClick={() => handleEditClick(exp)}
                              className="p-1 rounded bg-indigo-50 hover:bg-indigo-100 dark:bg-indigo-950/40 dark:hover:bg-indigo-900/60 text-indigo-600 dark:text-indigo-400 transition-colors"
                              title="Edit details"
                            >
                              <Edit2 size={12} />
                            </button>
                            <button
                              onClick={() => handleDuplicateClick(exp)}
                              className="p-1 rounded bg-emerald-50 hover:bg-emerald-100 dark:bg-emerald-950/40 dark:hover:bg-emerald-900/60 text-emerald-600 dark:text-emerald-400 transition-colors"
                              title="Duplicate entry"
                            >
                              <Copy size={12} />
                            </button>
                            <button
                              onClick={() => handleDelete(exp.id)}
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
              {displayedExpenses.map((exp) => {
                const firstLetter = exp.merchant ? exp.merchant.charAt(0).toUpperCase() : '?';
                return (
                  <div key={exp.id} className="bg-slate-50 dark:bg-slate-900/40 p-3.5 rounded-xl border border-slate-100 dark:border-slate-800 flex flex-col gap-2.5">
                    <div className="flex justify-between items-start">
                      <span className="text-[9px] text-slate-400 font-bold">
                        {new Date(exp.expenseDate).toLocaleDateString()}
                      </span>
                      <span
                        style={{ backgroundColor: `${exp.category?.color}15`, color: exp.category?.color }}
                        className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[9px] font-bold"
                      >
                        {exp.category?.name || 'Uncategorized'}
                      </span>
                    </div>

                    <div className="flex justify-between items-center">
                      <span className="font-bold text-slate-805 dark:text-slate-100 text-xs truncate max-w-[150px]">
                        {exp.merchant || 'Unknown'}
                      </span>
                      <span className="font-black text-slate-900 dark:text-slate-50 text-xs">
                        {currencySymbol} {exp.amount.toFixed(2)}
                      </span>
                    </div>

                    <div className="flex justify-between items-center text-[10px] border-t border-slate-100 dark:border-slate-800/80 pt-2">
                      <span className="text-[9px] text-slate-400">
                        {exp.locationName ? `📍 ${exp.locationName.split(',')[0]}` : '🌐 Online'}
                      </span>
                      <div className="flex items-center gap-1.5">
                        <button onClick={() => handleEditClick(exp)} className="p-1 rounded bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400">
                          <Edit2 size={11} />
                        </button>
                        <button onClick={() => handleDuplicateClick(exp)} className="p-1 rounded bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400">
                          <Copy size={11} />
                        </button>
                        <button onClick={() => handleDelete(exp.id)} className="p-1 rounded bg-rose-50 dark:bg-rose-950/40 text-rose-650 dark:text-rose-400">
                          <Trash2 size={11} />
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Pagination & Filter Summary Footer */}
            {expensesData.totalPages > 1 && (
              <div className="flex flex-col sm:flex-row justify-between items-center mt-5 pt-3.5 border-t border-slate-150 dark:border-slate-800 gap-3">
                <div className="flex flex-wrap items-center gap-3.5 text-[10px] font-bold text-slate-550 dark:text-slate-350">
                  <span>
                    Showing {page * 10 + 1}–{Math.min((page + 1) * 10, expensesData.totalElements)} of {expensesData.totalElements} records
                  </span>
                  <span className="h-3 w-px bg-slate-200 dark:bg-slate-800" />
                  <span>
                    Total Filtered: <span className="text-indigo-600 dark:text-indigo-400 font-extrabold">{currencySymbol}{totalFilteredAmount.toFixed(2)}</span>
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
                  {[...Array(expensesData.totalPages)].map((_, i) => (
                    <button
                      key={i}
                      onClick={() => setPage(i)}
                      className={`w-6 h-6 rounded-lg text-[10px] font-extrabold transition-all ${
                        page === i
                          ? 'bg-indigo-600 text-white shadow-sm'
                          : 'border border-slate-205 dark:border-slate-800 text-slate-650 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-900'
                      }`}
                    >
                      {i + 1}
                    </button>
                  ))}
                  
                  <button
                    disabled={page >= expensesData.totalPages - 1}
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

      {/* Inline Receipt Preview Modal */}
      {previewReceiptUrl && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm animate-fade-in p-4">
          <div className="bg-white dark:bg-dark-surface p-4 rounded-xl max-w-md w-full flex flex-col gap-3 shadow-2xl border border-light-border dark:border-dark-border">
            <div className="flex justify-between items-center border-b border-slate-100 dark:border-slate-800 pb-2">
              <span className="text-xs font-bold text-slate-800 dark:text-slate-100">Receipt Attachment Document</span>
              <button 
                onClick={() => {
                  URL.revokeObjectURL(previewReceiptUrl);
                  setPreviewReceiptUrl(null);
                }} 
                className="text-slate-400 hover:text-slate-650"
              >
                <X size={16} />
              </button>
            </div>
            <div className="max-h-[60vh] overflow-auto flex justify-center bg-slate-50 dark:bg-slate-900/60 rounded-lg p-1.5 border border-slate-100 dark:border-slate-800">
              <img 
                src={previewReceiptUrl} 
                alt="Receipt Document" 
                className="max-h-[50vh] object-contain rounded hover:scale-101 transition-transform duration-150" 
              />
            </div>
            <div className="flex justify-end gap-1.5">
              <a 
                href={previewReceiptUrl} 
                download="receipt.png"
                className="flex items-center gap-1 px-3 py-1.5 bg-indigo-650 hover:bg-indigo-700 text-white rounded-lg text-[10px] font-bold tracking-wider uppercase transition-all"
              >
                <Download size={12} /> Download
              </a>
              <button 
                onClick={() => {
                  URL.revokeObjectURL(previewReceiptUrl);
                  setPreviewReceiptUrl(null);
                }}
                className="px-3 py-1.5 border border-slate-205 dark:border-slate-800 rounded-lg text-[10px] font-bold tracking-wider uppercase hover:bg-slate-50 dark:hover:bg-slate-900 transition-all text-slate-700 dark:text-slate-350"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Geolocation Coordinates Modal */}
      {activeMapCoords && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm animate-fade-in p-4">
          <div className="bg-white dark:bg-dark-surface p-4 rounded-xl max-w-sm w-full flex flex-col gap-3 shadow-2xl border border-light-border dark:border-dark-border">
            <div className="flex justify-between items-center border-b border-slate-100 dark:border-slate-800 pb-2">
              <span className="text-xs font-bold text-slate-805 dark:text-slate-100">Location Tag Details</span>
              <button onClick={() => setActiveMapCoords(null)} className="text-slate-400 hover:text-slate-650">
                <X size={16} />
              </button>
            </div>
            <div className="flex flex-col gap-2 text-xs text-slate-600 dark:text-slate-400">
              <div>
                <span className="font-semibold text-slate-800 dark:text-slate-200">Label: </span>
                <span>{activeMapCoords.name}</span>
              </div>
              <div>
                <span className="font-semibold text-slate-800 dark:text-slate-200">Latitude: </span>
                <code className="bg-slate-50 dark:bg-slate-900 px-1 py-0.5 rounded">{activeMapCoords.lat}</code>
              </div>
              <div>
                <span className="font-semibold text-slate-800 dark:text-slate-200">Longitude: </span>
                <code className="bg-slate-50 dark:bg-slate-900 px-1 py-0.5 rounded">{activeMapCoords.lng}</code>
              </div>
            </div>
            <div className="flex justify-end gap-1.5 mt-1">
              <a 
                href={`https://www.google.com/maps/search/?api=1&query=${activeMapCoords.lat},${activeMapCoords.lng}`}
                target="_blank"
                rel="noreferrer"
                className="flex items-center gap-1 px-3 py-1.5 bg-indigo-650 hover:bg-indigo-700 text-white rounded-lg text-[10px] font-bold tracking-wider uppercase transition-all"
              >
                <ExternalLink size={12} /> View Map
              </a>
              <button 
                onClick={() => setActiveMapCoords(null)}
                className="px-3 py-1.5 border border-slate-205 dark:border-slate-800 rounded-lg text-[10px] font-bold tracking-wider uppercase hover:bg-slate-50 dark:hover:bg-slate-900 transition-all text-slate-700 dark:text-slate-350"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Add/Edit Expense Drawer/Modal */}
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-end bg-black/40 backdrop-blur-sm animate-fade-in">
          <div className="w-full max-w-md h-full bg-white dark:bg-dark-surface p-6 shadow-2xl border-l border-light-border dark:border-dark-border flex flex-col justify-between overflow-y-auto">
            <div>
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50">{editingExpense ? 'Edit Expense' : 'Record Expense'}</h2>
                <button onClick={() => { resetForm(); setIsOpen(false); }} className="text-slate-400 hover:text-slate-600">
                  <X size={20} />
                </button>
              </div>

              <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                {/* Amount */}
                <div className="flex flex-col gap-1.5">
                  <label htmlFor="expense-amount" className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                    Amount ({currencySymbol})
                  </label>
                  <input
                    id="expense-amount"
                    type="number"
                    step="0.01"
                    required
                    placeholder="0.00"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 outline-none text-sm bg-transparent text-slate-900 dark:text-slate-50"
                  />
                </div>

                {/* Category Select */}
                <div className="flex flex-col gap-1.5">
                  <label htmlFor="expense-category" className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                    Category
                  </label>
                  <select
                    id="expense-category"
                    required
                    value={categoryId}
                    onChange={(e) => setCategoryId(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 outline-none text-sm bg-transparent text-slate-900 dark:text-slate-50"
                  >
                    <option value="" className="dark:bg-slate-900">Select Category</option>
                    {categories.map((c) => (
                      <option key={c.id} value={c.id} className="dark:bg-slate-900 text-slate-900 dark:text-slate-50">
                        {c.name}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Date */}
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                    Date & Time
                  </label>
                  <input
                    type="datetime-local"
                    required
                    value={expenseDate}
                    onChange={(e) => setExpenseDate(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 outline-none text-sm bg-transparent text-slate-900 dark:text-slate-50"
                  />
                </div>

                {/* Merchant Input with Suggestions list */}
                <div className="flex flex-col gap-1.5 relative">
                  <label htmlFor="expense-merchant" className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                    Merchant
                  </label>
                  <input
                    id="expense-merchant"
                    type="text"
                    placeholder="e.g. Starbucks"
                    value={merchant}
                    onChange={(e) => setMerchant(e.target.value)}
                    onFocus={() => setShowSuggestions(true)}
                    onBlur={() => setTimeout(() => setShowSuggestions(false), 200)}
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 outline-none text-sm bg-transparent text-slate-900 dark:text-slate-50"
                  />
                  {showSuggestions && merchantSuggestions.length > 0 && merchantSuggestions.some(item => item.toLowerCase() !== merchant.trim().toLowerCase()) && (
                    <div className="absolute top-[72px] left-0 w-full bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-xl shadow-lg z-50 p-1 flex flex-col">
                      {merchantSuggestions
                        .filter(item => item.toLowerCase() !== merchant.trim().toLowerCase())
                        .map((item) => (
                          <button
                            key={item}
                            type="button"
                            onClick={() => {
                              setMerchant(item);
                              setShowSuggestions(false);
                            }}
                            className="w-full text-left px-4 py-2 hover:bg-slate-50 dark:hover:bg-slate-900 text-sm rounded-lg text-slate-800 dark:text-slate-200"
                          >
                            {item}
                          </button>
                        ))}
                    </div>
                  )}
                </div>

                {/* Description */}
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                    Description (Optional)
                  </label>
                  <input
                    type="text"
                    placeholder="e.g. Lunch with client"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 outline-none text-sm bg-transparent text-slate-900 dark:text-slate-50"
                  />
                </div>

                {/* Location */}
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                    Location Tag (Optional)
                  </label>
                  <div className="flex gap-2">
                    <button
                      type="button"
                      onClick={handleGetLocation}
                      disabled={resolvingLocation}
                      className="flex items-center justify-center gap-2 px-4 py-3 border border-slate-200 dark:border-slate-800 rounded-xl text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-900 text-xs font-semibold transition-all active:scale-[0.98]"
                    >
                      <MapPin size={14} />
                      {resolvingLocation ? 'Resolving...' : 'Current Location'}
                    </button>
                    {locationName && (
                      <div className="flex items-center gap-1.5 self-center">
                        <span className="text-xs text-green-500 font-semibold">
                          Tagged!
                        </span>
                        <button
                          type="button"
                          onClick={() => {
                            setLocationName('');
                            setCoords(null);
                          }}
                          className="text-slate-400 hover:text-red-500 p-0.5 rounded transition-colors"
                          title="Remove Location Tag"
                        >
                          <X size={12} />
                        </button>
                      </div>
                    )}
                  </div>
                </div>

                {/* Tag Input */}
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                    Tags
                  </label>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      placeholder="e.g. Business"
                      value={tagInput}
                      onChange={(e) => setTagInput(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                          e.preventDefault();
                          handleAddTag();
                        }
                      }}
                      className="flex-1 px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 outline-none text-sm bg-transparent text-slate-900 dark:text-slate-50"
                    />
                    <button
                      type="button"
                      onClick={handleAddTag}
                      className="px-4 py-3 border border-slate-200 dark:border-slate-800 rounded-xl text-xs font-semibold"
                    >
                      Add
                    </button>
                  </div>
                  {tags.length > 0 && (
                    <div className="flex flex-wrap gap-1.5 mt-1">
                      {tags.map((t) => (
                        <span
                          key={t}
                          className="flex items-center gap-1 px-2.5 py-1 bg-slate-100 dark:bg-slate-800 rounded-lg text-xs font-medium text-slate-600 dark:text-slate-300"
                        >
                          {t}
                          <button type="button" onClick={() => handleRemoveTag(t)}>
                            <X size={10} />
                          </button>
                        </span>
                      ))}
                    </div>
                  )}
                </div>

                <button
                  type="submit"
                  disabled={createMutation.isPending || updateMutation.isPending}
                  className="w-full mt-4 py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white font-semibold text-sm transition-all"
                >
                  {editingExpense 
                    ? (updateMutation.isPending ? 'Updating...' : 'Save Changes')
                    : (createMutation.isPending ? 'Recording...' : 'Record Expense')}
                </button>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
export default ExpensesPage;
