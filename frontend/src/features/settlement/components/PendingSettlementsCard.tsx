import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '@/core/api/client';
import { useAuthContext } from '@/hooks/useAuthContext';
import { SettlementResponse } from '../types';
import { DollarSign, ArrowUpRight, ArrowDownLeft, Smile, Loader2 } from 'lucide-react';

export const PendingSettlementsCard: React.FC<{ currencySymbol?: string }> = ({ 
  currencySymbol = '$' 
}) => {
  const navigate = useNavigate();
  const { user } = useAuthContext();
  const [loading, setLoading] = useState(true);
  const [oweTotal, setOweTotal] = useState(0);
  const [owedTotal, setOwedTotal] = useState(0);
  const [activeDebts, setActiveDebts] = useState<SettlementResponse[]>([]);

  useEffect(() => {
    if (!user) return;

    const fetchAllSettlements = async () => {
      try {
        setLoading(true);
        // 1. Fetch user's active groups
        const groupsRes = await apiClient.get('/groups');
        const groups = groupsRes.data.data.content || [];

        // 2. Query personal settlements for each active group
        const settlementPromises = groups.map((g: any) => 
          apiClient.get(`/groups/${g.id}/settlements/me`)
            .then(res => res.data.data)
            .catch(() => [] as SettlementResponse[])
        );

        const results = await Promise.all(settlementPromises);
        const allSettlements = results.flat() as SettlementResponse[];

        // 3. Filter pending/disputed settlements and compute aggregates
        const pendingOrDisputed = allSettlements.filter(
          s => s.status === 'PENDING' || s.status === 'DISPUTED'
        );

        let oweSum = 0;
        let owedSum = 0;

        pendingOrDisputed.forEach(s => {
          if (s.fromUserId === user.id) {
            oweSum += s.amount;
          } else if (s.toUserId === user.id) {
            owedSum += s.amount;
          }
        });

        setOweTotal(oweSum);
        setOwedTotal(owedSum);
        setActiveDebts(pendingOrDisputed.slice(0, 3)); // show top 3
      } catch (err) {
        console.error('Failed to load global settlements summary:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchAllSettlements();
  }, [user]);

  if (loading) {
    return (
      <div className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-2xl p-6 shadow-sm flex items-center justify-center h-[320px]">
        <Loader2 className="w-6 h-6 text-blue-500 animate-spin" />
      </div>
    );
  }

  const hasOutstanding = oweTotal > 0 || owedTotal > 0;

  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-2xl p-6 shadow-sm flex flex-col h-[320px] transition-all duration-300 hover:shadow-md">
      <div className="flex items-center gap-2 mb-4">
        <div className="p-2 bg-teal-50 dark:bg-teal-950/20 rounded-lg text-teal-600 dark:text-teal-400">
          <DollarSign className="w-4 h-4" />
        </div>
        <h3 className="font-bold text-sm text-slate-850 dark:text-white">Settlement Summary</h3>
      </div>

      {/* Aggregate row cards */}
      <div className="grid grid-cols-2 gap-3 mb-4">
        <div className="bg-rose-50/50 dark:bg-rose-950/10 p-3 rounded-xl border border-rose-100/50 dark:border-rose-950/20 flex flex-col justify-between">
          <span className="text-[10px] font-semibold text-rose-600 dark:text-rose-400 flex items-center gap-1">
            <ArrowDownLeft className="w-3.5 h-3.5" />
            You Owe
          </span>
          <span className="text-sm font-bold text-rose-700 dark:text-rose-450 mt-1">
            {currencySymbol}{oweTotal.toLocaleString(undefined, { minimumFractionDigits: 2 })}
          </span>
        </div>
        <div className="bg-emerald-50/50 dark:bg-emerald-950/10 p-3 rounded-xl border border-emerald-100/50 dark:border-emerald-950/20 flex flex-col justify-between">
          <span className="text-[10px] font-semibold text-emerald-600 dark:text-emerald-400 flex items-center gap-1">
            <ArrowUpRight className="w-3.5 h-3.5" />
            You Are Owed
          </span>
          <span className="text-sm font-bold text-emerald-700 dark:text-emerald-450 mt-1">
            {currencySymbol}{owedTotal.toLocaleString(undefined, { minimumFractionDigits: 2 })}
          </span>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto space-y-2 pr-1">
        {hasOutstanding ? (
          activeDebts.map((s) => {
            const isOwed = s.toUserId === user?.id;
            return (
              <div 
                key={s.id} 
                onClick={() => navigate(`/groups/${s.groupId}`)}
                className="flex items-center justify-between p-2 hover:bg-slate-50 dark:hover:bg-slate-850/50 rounded-lg cursor-pointer transition-all duration-200 border border-slate-50 dark:border-slate-850/30"
              >
                <div className="min-w-0">
                  <p className="text-[10px] text-slate-500 dark:text-slate-400 leading-none mb-1 uppercase tracking-wider font-semibold">
                    {isOwed ? 'Receivable' : 'Payable'}
                  </p>
                  <p className="text-xs font-bold text-slate-800 dark:text-slate-200 truncate">
                    {isOwed ? `From ${s.fromUserName}` : `To ${s.toUserName}`}
                  </p>
                </div>
                <span className={`text-xs font-bold ${isOwed ? 'text-emerald-650' : 'text-rose-650'}`}>
                  {isOwed ? '+' : '-'}{s.currency}{s.amount}
                </span>
              </div>
            );
          })
        ) : (
          <div className="flex flex-col items-center justify-center h-full text-center py-4">
            <Smile className="w-8 h-8 text-teal-400 mb-1" />
            <p className="text-xs text-slate-500 font-semibold">No outstanding debts!</p>
            <p className="text-[9px] text-slate-400 mt-0.5">Everything is fully settled.</p>
          </div>
        )}
      </div>
    </div>
  );
};
