import React, { useState, useEffect } from 'react';
import {
  GroupExpense,
  GroupExpenseCategory,
  SplitType,
  CreateGroupExpenseRequest,
  UpdateGroupExpenseRequest,
} from '../services/expenseService';
import { GroupMemberDto } from '@/features/group/types/group';
import { X, DollarSign, Info } from 'lucide-react';

interface CreateExpenseDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: any) => void;
  isLoading: boolean;
  members: GroupMemberDto[];
  expense?: GroupExpense | null; // If editing
  tripId?: string | null;
}

export const CreateExpenseDialog: React.FC<CreateExpenseDialogProps> = ({
  isOpen,
  onClose,
  onSubmit,
  isLoading,
  members,
  expense,
  tripId,
}) => {
  const [description, setDescription] = useState('');
  const [amount, setAmount] = useState('');
  const [currency, setCurrency] = useState('USD');
  const [category, setCategory] = useState<GroupExpenseCategory>('FOOD');
  const [paidByUserId, setPaidByUserId] = useState('');
  const [expenseDate, setExpenseDate] = useState(new Date().toISOString().split('T')[0]);
  const [splitType, setSplitType] = useState<SplitType>('EQUAL');
  
  // Track allocation inputs for each member: userId -> input string
  const [allocations, setAllocations] = useState<Record<string, string>>({});
  // For EQUAL split, track checkboxes: userId -> boolean
  const [equalSelections, setEqualSelections] = useState<Record<string, boolean>>({});

  const [validationError, setValidationError] = useState<string | null>(null);

  // Initialize form when opening/editing
  useEffect(() => {
    if (isOpen) {
      if (expense) {
        setDescription(expense.description || '');
        setAmount(expense.amount.toString());
        setCurrency(expense.currency);
        setCategory(expense.category);
        setPaidByUserId(expense.paidByUserId);
        setExpenseDate(expense.expenseDate);
        setSplitType(expense.splitType || 'EQUAL');
        
        // Populate splits if editing
        const initialAllocations: Record<string, string> = {};
        const initialEquals: Record<string, boolean> = {};
        
        // Try to identify split type
        // If we have splits, map them
        expense.splits.forEach((s) => {
          initialAllocations[s.userId] = s.allocationValue !== undefined && s.allocationValue !== null
            ? s.allocationValue.toString()
            : s.owedAmount.toString();
          initialEquals[s.userId] = true;
        });
        
        // Also ensure any member not in splits gets default
        members.forEach((m) => {
          if (!initialAllocations[m.userId]) {
            initialAllocations[m.userId] = '';
          }
          if (initialEquals[m.userId] === undefined) {
            initialEquals[m.userId] = false;
          }
        });

        setAllocations(initialAllocations);
        setEqualSelections(initialEquals);
      } else {
        // Defaults for create
        setDescription('');
        setAmount('');
        setCurrency('USD');
        setCategory('FOOD');
        setPaidByUserId(members[0]?.userId || '');
        setExpenseDate(new Date().toISOString().split('T')[0]);
        setSplitType('EQUAL');

        const initialAllocations: Record<string, string> = {};
        const initialEquals: Record<string, boolean> = {};
        members.forEach((m) => {
          initialAllocations[m.userId] = '';
          initialEquals[m.userId] = true; // Default EQUAL split includes everyone
        });
        setAllocations(initialAllocations);
        setEqualSelections(initialEquals);
      }
      setValidationError(null);
    }
  }, [isOpen, expense, members]);

  if (!isOpen) return null;

  const handleCheckboxChange = (userId: string, checked: boolean) => {
    setEqualSelections((prev) => ({ ...prev, [userId]: checked }));
  };

  const handleAllocationChange = (userId: string, val: string) => {
    setAllocations((prev) => ({ ...prev, [userId]: val }));
  };

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setValidationError(null);

    const parsedAmount = parseFloat(amount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      setValidationError('Please enter a valid amount greater than 0');
      return;
    }

    if (!description.trim()) {
      setValidationError('Please enter a description');
      return;
    }

    // Build allocation values object for request
    const allocationValues: Record<string, number> = {};

    if (splitType === 'EQUAL') {
      const selectedMembers = Object.keys(equalSelections).filter((id) => equalSelections[id]);
      if (selectedMembers.length === 0) {
        setValidationError('At least one group member must be included in the split');
        return;
      }
      // EQUAL split strategy expects value of 0.0000 or similar (ignored by strategy anyway, but mapped)
      selectedMembers.forEach((id) => {
        allocationValues[id] = 0;
      });
    } else {
      // EXACT, PERCENTAGE, SHARES
      const activeMembers = members.filter((m) => {
        const val = parseFloat(allocations[m.userId] || '');
        return !isNaN(val) && val > 0;
      });

      if (activeMembers.length === 0) {
        setValidationError('At least one group member must have an allocation value greater than 0');
        return;
      }

      let sum = 0;
      activeMembers.forEach((m) => {
        const val = parseFloat(allocations[m.userId]);
        allocationValues[m.userId] = val;
        sum += val;
      });

      if (splitType === 'EXACT') {
        if (Math.abs(sum - parsedAmount) > 0.01) {
          setValidationError(`The sum of exact splits (${sum.toFixed(2)}) must equal the total amount (${parsedAmount.toFixed(2)})`);
          return;
        }
      } else if (splitType === 'PERCENTAGE') {
        if (Math.abs(sum - 100) > 0.01) {
          setValidationError(`The sum of percentages must equal 100% (currently ${sum.toFixed(2)}%)`);
          return;
        }
      }
    }

    const payload: CreateGroupExpenseRequest | UpdateGroupExpenseRequest = {
      description,
      category,
      amount: parsedAmount,
      currency,
      paidByUserId,
      expenseDate,
      splitType,
      allocationValues,
      ...(expense ? {} : { tripId }), // tripId only on create request
    };

    onSubmit(payload);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 dark:bg-black/80 backdrop-blur-sm">
      <div className="bg-white dark:bg-slate-900 w-full max-w-lg rounded-2xl border border-slate-200 dark:border-slate-800 shadow-2xl flex flex-col max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="p-5 border-b border-slate-100 dark:border-slate-850/80 flex items-center justify-between shrink-0">
          <h3 className="text-lg font-black text-slate-900 dark:text-white">
            {expense ? 'Edit shared expense' : 'Add shared expense'}
          </h3>
          <button
            onClick={onClose}
            className="p-1.5 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-300 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <form onSubmit={handleFormSubmit} className="flex-1 overflow-y-auto p-6 space-y-5">
          {validationError && (
            <div className="bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900/40 text-red-800 dark:text-red-300 p-3.5 rounded-xl text-xs flex items-start gap-2">
              <Info className="w-4 h-4 shrink-0 mt-0.5" />
              <span>{validationError}</span>
            </div>
          )}

          {/* Amount & Currency */}
          <div className="grid grid-cols-3 gap-4">
            <div className="col-span-2">
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 dark:text-slate-500 mb-1.5">
                Amount
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400 dark:text-slate-500">
                  <DollarSign className="w-4 h-4" />
                </div>
                <input
                  type="number"
                  step="0.01"
                  required
                  placeholder="0.00"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="pl-9 w-full bg-slate-50 dark:bg-slate-950/40 border border-slate-200 dark:border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-850 dark:text-white focus:outline-none focus:border-slate-900 dark:focus:border-slate-700 font-semibold"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 dark:text-slate-500 mb-1.5">
                Currency
              </label>
              <select
                value={currency}
                onChange={(e) => setCurrency(e.target.value)}
                className="w-full bg-slate-50 dark:bg-slate-950/40 border border-slate-200 dark:border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-850 dark:text-white focus:outline-none focus:border-slate-900 dark:focus:border-slate-700 font-semibold"
              >
                <option value="USD">USD</option>
                <option value="INR">INR</option>
                <option value="EUR">EUR</option>
                <option value="GBP">GBP</option>
              </select>
            </div>
          </div>

          {/* Description */}
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 dark:text-slate-500 mb-1.5">
              Description
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Flight, Dinner, Cab..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full bg-slate-50 dark:bg-slate-950/40 border border-slate-200 dark:border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-850 dark:text-white focus:outline-none focus:border-slate-900 dark:focus:border-slate-700 font-semibold"
            />
          </div>

          {/* Category & Date */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 dark:text-slate-500 mb-1.5">
                Category
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value as GroupExpenseCategory)}
                className="w-full bg-slate-50 dark:bg-slate-950/40 border border-slate-200 dark:border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-850 dark:text-white focus:outline-none focus:border-slate-900 dark:focus:border-slate-700 font-semibold"
              >
                <option value="FOOD">Food</option>
                <option value="LODGING">Lodging</option>
                <option value="TRANSPORT">Transport</option>
                <option value="ENTERTAINMENT">Entertainment</option>
                <option value="SHOPPING">Shopping</option>
                <option value="OTHER">Other</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 dark:text-slate-500 mb-1.5">
                Date
              </label>
              <div className="relative">
                <input
                  type="date"
                  required
                  value={expenseDate}
                  onChange={(e) => setExpenseDate(e.target.value)}
                  className="w-full bg-slate-50 dark:bg-slate-950/40 border border-slate-200 dark:border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-850 dark:text-white focus:outline-none focus:border-slate-900 dark:focus:border-slate-700 font-semibold"
                />
              </div>
            </div>
          </div>

          {/* Paid By */}
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 dark:text-slate-500 mb-1.5">
              Paid By
            </label>
            <select
              value={paidByUserId}
              onChange={(e) => setPaidByUserId(e.target.value)}
              className="w-full bg-slate-50 dark:bg-slate-950/40 border border-slate-200 dark:border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-850 dark:text-white focus:outline-none focus:border-slate-900 dark:focus:border-slate-700 font-semibold"
            >
              {members.map((m) => (
                <option key={m.userId} value={m.userId}>
                  {m.userName} ({m.userEmail})
                </option>
              ))}
            </select>
          </div>

          <div className="border-t border-slate-100 dark:border-slate-850/60 pt-4">
            <div className="flex items-center justify-between mb-3">
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 dark:text-slate-500">
                Split Distribution
              </label>
              <select
                value={splitType}
                onChange={(e) => setSplitType(e.target.value as SplitType)}
                className="bg-slate-50 dark:bg-slate-950/40 border border-slate-200 dark:border-slate-800 rounded-lg px-2.5 py-1 text-xs text-slate-850 dark:text-white focus:outline-none font-bold"
              >
                <option value="EQUAL">Equally</option>
                <option value="EXACT">Exact Amounts</option>
                <option value="PERCENTAGE">Percentages</option>
                <option value="SHARES">Shares</option>
              </select>
            </div>

            {/* Split Members Workspace List */}
            <div className="bg-slate-50 dark:bg-slate-950/20 border border-slate-150 dark:border-slate-850 rounded-xl p-4 space-y-3 max-h-56 overflow-y-auto">
              {members.map((m) => (
                <div key={m.userId} className="flex items-center justify-between gap-4 py-0.5">
                  <div className="flex items-center gap-3">
                    {splitType === 'EQUAL' ? (
                      <input
                        type="checkbox"
                        checked={equalSelections[m.userId] ?? false}
                        onChange={(e) => handleCheckboxChange(m.userId, e.target.checked)}
                        className="rounded border-slate-300 dark:border-slate-750 text-slate-900 focus:ring-slate-900 w-4 h-4"
                      />
                    ) : (
                      <span className="w-1.5 h-1.5 rounded-full bg-slate-350 dark:bg-slate-650" />
                    )}
                    <div className="text-xs">
                      <div className="font-bold text-slate-700 dark:text-slate-200">{m.userName}</div>
                      <div className="text-[10px] text-slate-400 dark:text-slate-550">{m.userEmail}</div>
                    </div>
                  </div>

                  {splitType !== 'EQUAL' && (
                    <div className="flex items-center gap-1.5 w-24">
                      <input
                        type="number"
                        step={splitType === 'PERCENTAGE' ? '0.01' : '1'}
                        placeholder={
                          splitType === 'EXACT' ? '0.00' : splitType === 'PERCENTAGE' ? '0%' : '1 share'
                        }
                        value={allocations[m.userId] ?? ''}
                        onChange={(e) => handleAllocationChange(m.userId, e.target.value)}
                        className="w-full text-right bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-lg px-2 py-1 text-xs text-slate-850 dark:text-white font-bold"
                      />
                      <span className="text-[10px] font-bold text-slate-400">
                        {splitType === 'EXACT' ? currency : splitType === 'PERCENTAGE' ? '%' : 'sh'}
                      </span>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        </form>

        {/* Footer */}
        <div className="p-5 border-t border-slate-100 dark:border-slate-850/80 bg-slate-50 dark:bg-slate-900/60 flex items-center justify-end gap-3 shrink-0">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl text-xs font-bold text-slate-650 hover:bg-slate-50 dark:text-slate-200 dark:hover:bg-slate-750 transition-colors"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleFormSubmit}
            disabled={isLoading}
            className="px-4 py-2 bg-slate-900 dark:bg-white text-white dark:text-slate-900 rounded-xl text-xs font-bold hover:bg-slate-800 dark:hover:bg-slate-100 disabled:opacity-50 transition-colors"
          >
            {isLoading ? 'Saving...' : 'Save Expense'}
          </button>
        </div>
      </div>
    </div>
  );
};
