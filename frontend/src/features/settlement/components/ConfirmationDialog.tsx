import React from 'react';

interface ConfirmationDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  description: string;
  confirmLabel?: string;
  cancelLabel?: string;
  confirmVariant?: 'danger' | 'primary' | 'success';
}

export const ConfirmationDialog: React.FC<ConfirmationDialogProps> = ({
  isOpen,
  onClose,
  onConfirm,
  title,
  description,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  confirmVariant = 'primary',
}) => {
  React.useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleEscape);
    return () => window.removeEventListener('keydown', handleEscape);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const getConfirmButtonClasses = () => {
    const base =
      'px-4 py-2 text-sm font-semibold rounded-xl transition-all shadow-md active:scale-98 focus:outline-none focus:ring-2 focus:ring-offset-2';
    switch (confirmVariant) {
      case 'danger':
        return `${base} bg-red-600 hover:bg-red-700 text-white focus:ring-red-500`;
      case 'success':
        return `${base} bg-emerald-600 hover:bg-emerald-700 text-white focus:ring-emerald-500`;
      default:
        return `${base} bg-indigo-600 hover:bg-indigo-700 text-white focus:ring-indigo-500`;
    }
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirm-dialog-title"
      aria-describedby="confirm-dialog-description"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 dark:bg-slate-950/80 backdrop-blur-sm animate-fade-in"
    >
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl w-full max-w-md overflow-hidden shadow-2xl transition-all scale-100 transform duration-300">
        <div className="p-6">
          <h3
            id="confirm-dialog-title"
            className="text-lg font-bold text-slate-900 dark:text-white mb-2"
          >
            {title}
          </h3>
          <p
            id="confirm-dialog-description"
            className="text-sm text-slate-550 dark:text-slate-400 mb-6"
          >
            {description}
          </p>
          <div className="flex gap-3 justify-end">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-750 text-slate-700 dark:text-slate-350 rounded-xl text-sm font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-slate-500"
            >
              {cancelLabel}
            </button>
            <button
              type="button"
              onClick={() => {
                onConfirm();
                onClose();
              }}
              className={getConfirmButtonClasses()}
            >
              {confirmLabel}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
export default React.memo(ConfirmationDialog);
