import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { pageVariants } from '@/animations/variants';
import { useRecurringById, usePauseRecurring, useResumeRecurring, useDeleteRecurring } from '@/features/recurring/hooks/useRecurring';
import { useProfile } from '@/features/profile/hooks/useProfile';
import { WorkspaceLayout } from '@/layouts/WorkspaceLayout';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import {
  ArrowLeft,
  AlertTriangle,
  Play,
  Pause,
  Trash2,
  Clock,
  CheckCircle2
} from 'lucide-react';
import toast from 'react-hot-toast';

export const RecurringDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { data: template, isLoading, isError } = useRecurringById(id || '');
  const { data: profile } = useProfile();

  const pauseMutation = usePauseRecurring();
  const resumeMutation = useResumeRecurring();
  const deleteMutation = useDeleteRecurring();

  const [isActioning, setIsActioning] = useState(false);

  const currencySymbol = profile?.preferredCurrency || '$';

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center py-24 space-y-4">
        <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
        <p className="text-sm text-slate-500">Loading subscription details...</p>
      </div>
    );
  }

  if (isError || !template) {
    return (
      <div className="bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900/40 p-6 rounded-2xl max-w-lg mx-auto my-12 text-center space-y-4">
        <AlertTriangle className="w-8 h-8 text-red-500 mx-auto" />
        <h3 className="font-bold text-red-800 dark:text-red-400">Recurring Rule Not Found</h3>
        <p className="text-sm text-red-700 dark:text-red-500">
          The requested recurring template could not be resolved.
        </p>
        <Button onClick={() => navigate('/recurring')} variant="outlined" className="mx-auto">
          Back to list
        </Button>
      </div>
    );
  }

  const handlePause = () => {
    setIsActioning(true);
    pauseMutation.mutate(template.id, {
      onSuccess: () => {
        toast.success('Subscription paused');
        setIsActioning(false);
      },
      onError: (err: any) => {
        toast.error(err.message || 'Failed to pause');
        setIsActioning(false);
      }
    });
  };

  const handleResume = () => {
    setIsActioning(true);
    resumeMutation.mutate(template.id, {
      onSuccess: () => {
        toast.success('Subscription resumed');
        setIsActioning(false);
      },
      onError: (err: any) => {
        toast.error(err.message || 'Failed to resume');
        setIsActioning(false);
      }
    });
  };

  const handleDelete = () => {
    if (window.confirm('Are you sure you want to delete this recurring template?')) {
      setIsActioning(true);
      deleteMutation.mutate(template.id, {
        onSuccess: () => {
          toast.success('Recurring template deleted');
          navigate('/recurring');
        },
        onError: (err: any) => {
          toast.error(err.message || 'Failed to delete template');
          setIsActioning(false);
        }
      });
    }
  };

  // Generate a list of executions from start date up to today for illustration
  const getCalculatedExecutions = () => {
    const list = [];
    const start = new Date(template.startDate);
    const today = new Date();
    let current = new Date(start);

    // Limit to max 6 to avoid clutter
    while (current <= today && list.length < 6) {
      list.push(new Date(current));
      if (template.recurrenceType === 'DAILY') {
        current.setDate(current.getDate() + template.recurrenceInterval);
      } else if (template.recurrenceType === 'WEEKLY') {
        current.setDate(current.getDate() + 7 * template.recurrenceInterval);
      } else if (template.recurrenceType === 'MONTHLY') {
        current.setMonth(current.getMonth() + template.recurrenceInterval);
      } else {
        current.setFullYear(current.getFullYear() + template.recurrenceInterval);
      }
    }
    return list.reverse();
  };

  const executions = getCalculatedExecutions();

  return (
    <motion.div variants={pageVariants} initial="hidden" animate="visible" exit="exit" className="w-full">
      <WorkspaceLayout
        title={template.merchant || 'Scheduled Transaction'}
        subtitle={`${template.transactionType} subscription detail workspace`}
        action={
          <div className="flex gap-3">
            <Button onClick={() => navigate('/recurring')} variant="outlined" className="flex items-center gap-2">
              <ArrowLeft className="w-4 h-4" />
              <span>All Subscriptions</span>
            </Button>
            {template.status === 'ACTIVE' ? (
              <Button onClick={handlePause} disabled={isActioning} className="bg-amber-500 hover:bg-amber-600 text-white flex items-center gap-2">
                <Pause className="w-4 h-4" />
                <span>Pause</span>
              </Button>
            ) : (
              <Button onClick={handleResume} disabled={isActioning} className="bg-emerald-500 hover:bg-emerald-600 text-white flex items-center gap-2">
                <Play className="w-4 h-4" />
                <span>Resume</span>
              </Button>
            )}
            <Button onClick={handleDelete} disabled={isActioning} className="bg-red-500 hover:bg-red-600 text-white flex items-center gap-2">
              <Trash2 className="w-4 h-4" />
              <span>Delete</span>
            </Button>
          </div>
        }
      >
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Detail Panel */}
          <div className="lg:col-span-1 space-y-6">
            <Card className="p-24 space-y-6">
              <h3 className="font-bold text-sm text-slate-800 dark:text-slate-200">Rule Parameters</h3>
              <div className="space-y-4">
                <div className="flex justify-between border-b border-light-border dark:border-dark-border pb-3">
                  <span className="text-xs text-slate-400">Status</span>
                  <span className={`text-xs font-bold px-2 py-0.5 rounded-full ${
                    template.status === 'ACTIVE' ? 'bg-emerald-50 dark:bg-emerald-950/20 text-emerald-600' :
                    template.status === 'PAUSED' ? 'bg-amber-50 dark:bg-amber-950/20 text-amber-600' :
                    'bg-slate-50 dark:bg-slate-900 text-slate-600'
                  }`}>
                    {template.status}
                  </span>
                </div>
                <div className="flex justify-between border-b border-light-border dark:border-dark-border pb-3">
                  <span className="text-xs text-slate-400">Billing Type</span>
                  <span className="text-xs font-bold text-slate-800 dark:text-slate-100">
                    {template.transactionType}
                  </span>
                </div>
                <div className="flex justify-between border-b border-light-border dark:border-dark-border pb-3">
                  <span className="text-xs text-slate-400">Amount</span>
                  <span className="text-xs font-extrabold text-slate-800 dark:text-slate-100">
                    {currencySymbol} {template.amount.toFixed(2)} {template.currencyCode}
                  </span>
                </div>
                <div className="flex justify-between border-b border-light-border dark:border-dark-border pb-3">
                  <span className="text-xs text-slate-400">Frequency</span>
                  <span className="text-xs font-semibold text-slate-800 dark:text-slate-100">
                    Every {template.recurrenceInterval > 1 ? template.recurrenceInterval : ''} {template.recurrenceType.toLowerCase()}
                  </span>
                </div>
                <div className="flex justify-between border-b border-light-border dark:border-dark-border pb-3">
                  <span className="text-xs text-slate-400">Start Date</span>
                  <span className="text-xs font-semibold text-slate-800 dark:text-slate-100">
                    {new Date(template.startDate).toLocaleDateString()}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-xs text-slate-400">Next Billing Run</span>
                  <span className="text-xs font-bold text-primary flex items-center gap-1">
                    <Clock className="w-3.5 h-3.5" />
                    {new Date(template.nextExecution).toLocaleDateString()}
                  </span>
                </div>
              </div>
            </Card>
          </div>

          {/* History Logs */}
          <div className="lg:col-span-2 space-y-6">
            <Card className="p-24 space-y-4">
              <h3 className="font-bold text-sm text-slate-800 dark:text-slate-200">Execution History Logs</h3>
              {!executions.length ? (
                <div className="text-center py-12 text-xs text-slate-400">
                  No past billing executions resolved yet. Next execution triggers automatically.
                </div>
              ) : (
                <div className="space-y-6 pt-2">
                  {executions.map((exeDate, idx) => (
                    <div key={idx} className="flex items-start gap-4">
                      <div className="w-8 h-8 rounded-full bg-emerald-50 dark:bg-emerald-950/20 text-emerald-600 flex items-center justify-center shrink-0 mt-0.5 border border-emerald-100 dark:border-emerald-950/40">
                        <CheckCircle2 size={16} />
                      </div>
                      <div className="flex-1 pb-4 border-b border-light-border dark:border-dark-border last:border-b-0">
                        <h4 className="text-xs font-bold text-slate-900 dark:text-white">
                          Billing run generated successfully
                        </h4>
                        <p className="text-[10px] text-slate-400 mt-1">
                          Run Date: {exeDate.toLocaleDateString()} &bull; Amount recorded: {currencySymbol}{template.amount.toFixed(2)}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </Card>
          </div>
        </div>
      </WorkspaceLayout>
    </motion.div>
  );
};
