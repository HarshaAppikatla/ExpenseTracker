import React from 'react';
import { Users, Search, Activity } from 'lucide-react';

interface EmptyGroupsStateProps {
  onCreateClick: () => void;
  onJoinClick: () => void;
}

export const EmptyGroupsState: React.FC<EmptyGroupsStateProps> = ({ onCreateClick, onJoinClick }) => {
  return (
    <div className="flex flex-col items-center justify-center text-center p-12 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl shadow-sm min-h-[300px]">
      <div className="w-16 h-16 bg-slate-50 dark:bg-slate-800 text-slate-400 rounded-full flex items-center justify-center mb-4">
        <Users className="w-8 h-8" />
      </div>
      <h3 className="text-lg font-bold text-slate-800 dark:text-slate-100 mb-2">No Groups Found</h3>
      <p className="text-slate-500 dark:text-slate-400 text-sm max-w-sm mb-6">
        Get started by creating a new shared finance group or joining an existing one with an invite room code.
      </p>
      <div className="flex gap-4">
        <button
          onClick={onCreateClick}
          className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-sm font-medium transition-colors"
        >
          Create Group
        </button>
        <button
          onClick={onJoinClick}
          className="px-4 py-2 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200 rounded-lg text-sm font-medium transition-colors"
        >
          Join Group
        </button>
      </div>
    </div>
  );
};

interface EmptySearchStateProps {
  onClearSearch: () => void;
}

export const EmptySearchState: React.FC<EmptySearchStateProps> = ({ onClearSearch }) => {
  return (
    <div className="flex flex-col items-center justify-center text-center p-12 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl shadow-sm min-h-[300px]">
      <div className="w-16 h-16 bg-slate-50 dark:bg-slate-800 text-slate-400 rounded-full flex items-center justify-center mb-4">
        <Search className="w-8 h-8" />
      </div>
      <h3 className="text-lg font-bold text-slate-800 dark:text-slate-100 mb-2">No Matching Groups</h3>
      <p className="text-slate-500 dark:text-slate-400 text-sm max-w-sm mb-6">
        We couldn't find any groups matching your search query. Try typing something else or clear filters.
      </p>
      <button
        onClick={onClearSearch}
        className="px-4 py-2 bg-slate-950 hover:bg-slate-800 text-white rounded-lg text-sm font-medium transition-colors"
      >
        Clear Search
      </button>
    </div>
  );
};

export const EmptyTimelineState: React.FC = () => {
  return (
    <div className="flex flex-col items-center justify-center text-center p-8 bg-slate-50 dark:bg-slate-950 border border-dashed border-slate-200 dark:border-slate-800 rounded-xl min-h-[200px]">
      <div className="w-12 h-12 bg-slate-100 dark:bg-slate-900 text-slate-400 rounded-full flex items-center justify-center mb-3">
        <Activity className="w-6 h-6" />
      </div>
      <h4 className="text-sm font-bold text-slate-700 dark:text-slate-300 mb-1">No Activity Logs</h4>
      <p className="text-slate-400 dark:text-slate-500 text-xs max-w-xs">
        Logs detailing membership joins, setting updates, or role changes will show up here.
      </p>
    </div>
  );
};
