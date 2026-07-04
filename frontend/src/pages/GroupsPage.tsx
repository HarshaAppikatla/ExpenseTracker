import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useMyGroups } from '../features/group/hooks/useGroups';
import { useCreateGroup, useJoinGroup } from '../features/group/hooks/useGroupMutations';
import { GroupCard } from '../features/group/components/GroupCard';
import { GroupListSkeleton } from '../features/group/components/GroupSkeletons';
import { EmptyGroupsState, EmptySearchState } from '../features/group/components/GroupEmptyStates';
import { CreateGroupDialog } from '../features/group/components/CreateGroupDialog';
import { JoinGroupDialog } from '../features/group/components/JoinGroupDialog';
import { Plus, UserPlus, Search, X, ChevronLeft, ChevronRight } from 'lucide-react';
import { DEFAULT_PAGE_SIZE } from '../features/group/constants/groupConstants';

export const GroupsPage: React.FC = () => {
  const navigate = useNavigate();
  const [search, setSearch] = React.useState('');
  const [searchInput, setSearchInput] = React.useState('');
  const [page, setPage] = React.useState(0);

  const [isCreateOpen, setIsCreateOpen] = React.useState(false);
  const [isJoinOpen, setIsJoinOpen] = React.useState(false);

  // Queries
  const { data, isLoading, isError, error, refetch } = useMyGroups(search, page, DEFAULT_PAGE_SIZE);

  // Mutations
  const createGroupMutation = useCreateGroup();
  const joinGroupMutation = useJoinGroup();

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setSearch(searchInput.trim());
    setPage(0); // Reset page to first
  };

  const handleClearSearch = () => {
    setSearchInput('');
    setSearch('');
    setPage(0);
  };

  const handleCreateSubmit = (formData: any) => {
    createGroupMutation.mutate(formData, {
      onSuccess: () => {
        setIsCreateOpen(false);
      },
    });
  };

  const handleJoinSubmit = (formData: any) => {
    joinGroupMutation.mutate(formData, {
      onSuccess: () => {
        setIsJoinOpen(false);
      },
    });
  };

  const handleCardClick = (id: string) => {
    navigate(`/dashboard/groups/${id}`);
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto px-4 sm:px-6">
      {/* Top dashboard action bar */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-800 dark:text-slate-100 tracking-tight">
            Groups & Collaboration
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Share expenses, split bills, and collaborate on budgets with friends.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={() => setIsJoinOpen(true)}
            className="flex items-center gap-2 px-4 py-2 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200 rounded-lg text-sm font-semibold transition-colors"
          >
            <UserPlus className="w-4 h-4" />
            Join Group
          </button>
          <button
            onClick={() => setIsCreateOpen(true)}
            className="flex items-center gap-2 px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-sm font-semibold transition-colors shadow-sm"
          >
            <Plus className="w-4 h-4" />
            Create Group
          </button>
        </div>
      </div>

      {/* Search Input Section */}
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-4 shadow-sm">
        <form onSubmit={handleSearchSubmit} className="flex gap-2 max-w-md">
          <div className="relative flex-1">
            <span className="absolute left-3 top-2.5 text-slate-400">
              <Search className="w-4 h-4" />
            </span>
            <input
              type="text"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              placeholder="Search groups by name..."
              className="w-full pl-9 pr-8 py-1.5 border border-slate-200 dark:border-slate-800 rounded-lg bg-white dark:bg-slate-950 text-slate-850 dark:text-slate-100 text-sm focus:outline-none focus:border-slate-400"
            />
            {searchInput && (
              <button
                type="button"
                onClick={handleClearSearch}
                className="absolute right-3 top-2.5 text-slate-400 hover:text-slate-600 dark:hover:text-slate-300"
              >
                <X className="w-4 h-4" />
              </button>
            )}
          </div>
          <button
            type="submit"
            className="px-4 py-1.5 bg-slate-950 hover:bg-slate-850 text-white text-sm font-medium rounded-lg transition-colors"
          >
            Search
          </button>
        </form>
      </div>

      {/* Main Content Grid */}
      {isLoading && <GroupListSkeleton />}

      {isError && (
        <div className="bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900/40 text-red-800 dark:text-red-300 p-4 rounded-xl text-center text-sm font-medium">
          Failed to load groups: {error?.message || 'Unknown error occurred.'}
          <button
            onClick={() => refetch()}
            className="block mx-auto mt-2 text-xs underline font-bold"
          >
            Retry
          </button>
        </div>
      )}

      {!isLoading && !isError && data && (
        <>
          {data.content.length === 0 ? (
            search ? (
              <EmptySearchState onClearSearch={handleClearSearch} />
            ) : (
              <EmptyGroupsState
                onCreateClick={() => setIsCreateOpen(true)}
                onJoinClick={() => setIsJoinOpen(true)}
              />
            )
          ) : (
            <div className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {data.content.map((group) => (
                  <GroupCard
                    key={group.id}
                    group={group}
                    onClick={handleCardClick}
                  />
                ))}
              </div>

              {/* Pagination controls */}
              {data.totalPages > 1 && (
                <div className="flex items-center justify-between border-t border-slate-100 dark:border-slate-850 pt-4 text-sm font-medium text-slate-500">
                  <span>
                    Page {page + 1} of {data.totalPages}
                  </span>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => setPage((p) => Math.max(0, p - 1))}
                      disabled={page === 0}
                      className="p-1.5 border border-slate-200 dark:border-slate-800 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-50 disabled:pointer-events-none transition-colors"
                    >
                      <ChevronLeft className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => setPage((p) => Math.min(data.totalPages - 1, p + 1))}
                      disabled={page >= data.totalPages - 1}
                      className="p-1.5 border border-slate-200 dark:border-slate-800 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-50 disabled:pointer-events-none transition-colors"
                    >
                      <ChevronRight className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}
        </>
      )}

      {/* Dialog Modals */}
      <CreateGroupDialog
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onSubmit={handleCreateSubmit}
        isLoading={createGroupMutation.isPending}
      />

      <JoinGroupDialog
        isOpen={isJoinOpen}
        onClose={() => setIsJoinOpen(false)}
        onSubmit={handleJoinSubmit}
        isLoading={joinGroupMutation.isPending}
      />
    </div>
  );
};
export default GroupsPage;
