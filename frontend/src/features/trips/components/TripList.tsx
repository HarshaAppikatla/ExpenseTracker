import React, { useState } from 'react';
import { useGroupTrips } from '../hooks/useTripQueries';
import { useCreateTrip } from '../hooks/useTripMutations';
import { TripCard } from './TripCard';
import { CreateTripDialog } from './CreateTripDialog';
import { CreateTripInput } from '../schemas/tripSchemas';
import { Compass, Plus } from 'lucide-react';

interface TripListProps {
  groupId: string;
}

export const TripList: React.FC<TripListProps> = ({ groupId }) => {
  const page = 0;
  const [isCreateOpen, setIsCreateOpen] = useState(false);

  // Queries
  const { data, isLoading, isError, error } = useGroupTrips(groupId, page, 12);

  // Mutations
  const createMutation = useCreateTrip();

  const handleCreateSubmit = (input: CreateTripInput) => {
    createMutation.mutate(
      {
        groupId,
        title: input.title,
        description: input.description,
        destination: {
          city: input.city,
          country: input.country,
          displayName: `${input.city}, ${input.country}`,
        },
        schedule: {
          startDate: input.startDate,
          endDate: input.endDate,
        },
        settings: {
          visibility: input.visibility,
          allowInvites: input.allowInvites,
          archived: false,
        },
        coverType: input.coverType,
        coverImage: input.coverImage,
      },
      {
        onSuccess: () => {
          setIsCreateOpen(false);
        },
      }
    );
  };

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 pt-4 animate-pulse">
        {[...Array(6)].map((_, i) => (
          <div key={i} className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl h-64" />
        ))}
      </div>
    );
  }

  if (isError) {
    return (
      <div className="text-center py-12 text-slate-500">
        <p className="font-semibold text-red-500">Failed to load trips</p>
        <p className="text-xs">{error?.message || 'Unexpected error'}</p>
      </div>
    );
  }

  const trips = data?.content || [];

  return (
    <div className="space-y-6 pt-4">
      {/* Header Controls */}
      <div className="flex justify-between items-center">
        <div>
          <h3 className="text-lg font-bold text-slate-800 dark:text-slate-100">
            Group Trips Workspace
          </h3>
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Plan collaborative journeys and manage travel schedules
          </p>
        </div>

        <button
          onClick={() => setIsCreateOpen(true)}
          className="flex items-center gap-1.5 px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-sm font-semibold transition-colors shadow-sm hover:shadow"
        >
          <Plus className="w-4 h-4" />
          Plan Trip
        </button>
      </div>

      {/* Trips list */}
      {trips.length === 0 ? (
        <div className="bg-slate-50/50 dark:bg-slate-950/20 border border-dashed border-slate-200 dark:border-slate-800 rounded-2xl p-12 text-center max-w-xl mx-auto mt-6">
          <div className="w-12 h-12 bg-slate-100 dark:bg-slate-900 rounded-xl flex items-center justify-center mx-auto mb-4 text-slate-400 dark:text-slate-500">
            <Compass className="w-6 h-6" />
          </div>
          <h4 className="text-base font-bold text-slate-800 dark:text-slate-200 mb-1">
            No Trips Planned Yet
          </h4>
          <p className="text-xs text-slate-500 dark:text-slate-450 max-w-sm mx-auto mb-6">
            Get started by planning your first destination, dates, and inviting group members to join.
          </p>
          <button
            onClick={() => setIsCreateOpen(true)}
            className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-sm font-semibold transition-colors"
          >
            Create Trip Workspace
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {trips.map((trip) => (
            <TripCard key={trip.id} trip={trip} />
          ))}
        </div>
      )}

      {/* Dialog for creation */}
      <CreateTripDialog
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onSubmit={handleCreateSubmit}
        isLoading={createMutation.isPending}
      />
    </div>
  );
};
export default TripList;
