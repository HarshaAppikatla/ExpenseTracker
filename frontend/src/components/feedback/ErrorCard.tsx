import React from 'react';
import { AlertCircle, RotateCcw } from 'lucide-react';
import { Button } from '../ui/Button';

interface ErrorCardProps {
  onRetry: () => void;
  message?: string;
}

export const ErrorCard: React.FC<ErrorCardProps> = ({ 
  onRetry, 
  message = 'Unable to establish a connection with the server. Please verify the backend is online.' 
}) => {
  return (
    <div className="flex flex-col items-center justify-center p-32 text-center max-w-md mx-auto bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900/50 rounded-card gap-16">
      <div className="p-12 bg-red-100 dark:bg-red-900/30 rounded-full text-danger">
        <AlertCircle className="w-8 h-8" />
      </div>
      
      <div className="space-y-4">
        <h3 className="font-bold text-base text-light-text dark:text-dark-text">Connection Error</h3>
        <p className="text-sm text-light-textSecondary dark:text-slate-400 leading-relaxed">
          {message}
        </p>
      </div>

      <Button 
        variant="outlined" 
        onClick={onRetry} 
        className="mt-8 border-red-300 dark:border-red-900 text-red-700 dark:text-red-400 hover:bg-red-100 dark:hover:bg-red-900/20"
      >
        <RotateCcw className="w-4 h-4" />
        <span>Retry Connection</span>
      </Button>
    </div>
  );
};
