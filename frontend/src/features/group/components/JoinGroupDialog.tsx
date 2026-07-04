import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { joinGroupSchema, JoinGroupInput } from '../schemas/groupSchemas';
import { X, Key } from 'lucide-react';

interface JoinGroupDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: JoinGroupInput) => void;
  isLoading: boolean;
}

export const JoinGroupDialog: React.FC<JoinGroupDialogProps> = ({
  isOpen,
  onClose,
  onSubmit,
  isLoading,
}) => {
  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors },
  } = useForm<JoinGroupInput>({
    resolver: zodResolver(joinGroupSchema),
    defaultValues: {
      roomCode: '',
    },
  });

  React.useEffect(() => {
    if (isOpen) {
      reset();
    }
  }, [isOpen, reset]);

  if (!isOpen) return null;

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const rawVal = e.target.value.toUpperCase();
    setValue('roomCode', rawVal, { shouldValidate: true });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop */}
      <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={onClose}></div>

      {/* Modal Dialog */}
      <div className="relative bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl w-full max-w-md p-6 shadow-xl animate-in fade-in zoom-in-95 duration-250">
        <button
          onClick={onClose}
          className="absolute right-4 top-4 p-1.5 hover:bg-slate-50 dark:hover:bg-slate-800 rounded-lg text-slate-400 dark:text-slate-500 hover:text-slate-600 dark:hover:text-slate-355 transition-colors"
        >
          <X className="w-5 h-5" />
        </button>

        <h3 className="text-xl font-bold text-slate-800 dark:text-slate-100 mb-6">
          Join Shared Group
        </h3>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Room Invitation Code
            </label>
            <div className="relative flex items-center">
              <span className="absolute left-3 text-slate-400 dark:text-slate-500">
                <Key className="w-5 h-5" />
              </span>
              <input
                type="text"
                {...register('roomCode')}
                onChange={handleInputChange}
                className={`w-full pl-10 pr-3 py-2 border rounded-lg bg-white dark:bg-slate-950 text-slate-850 dark:text-slate-100 text-sm focus:outline-none focus:ring-2 font-mono tracking-widest uppercase ${
                  errors.roomCode
                    ? 'border-red-500 focus:ring-red-200'
                    : 'border-slate-200 dark:border-slate-800 focus:border-slate-400 focus:ring-slate-100'
                }`}
                placeholder="e.g. ABCD2345"
                maxLength={8}
                disabled={isLoading}
              />
            </div>
            {errors.roomCode && (
              <span className="text-xs text-red-500 mt-1 block font-medium">
                {errors.roomCode.message}
              </span>
            )}
          </div>

          <div className="flex gap-3 justify-end pt-4">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200 rounded-lg text-sm font-semibold transition-colors"
              disabled={isLoading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-sm font-semibold transition-colors"
              disabled={isLoading}
            >
              {isLoading ? 'Joining...' : 'Join Group'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
export default JoinGroupDialog;
