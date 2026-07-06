import React from 'react';
import { useNavigate } from 'react-router-dom';
import { TripDto } from '../types/trip';
import { Calendar, MapPin, Users } from 'lucide-react';

interface TripCardProps {
  trip: TripDto;
}

export const TripCard: React.FC<TripCardProps> = ({ trip }) => {
  const navigate = useNavigate();

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PLANNING':
        return 'bg-blue-50 text-blue-700 dark:bg-blue-950/30 dark:text-blue-300 border-blue-200 dark:border-blue-900/30';
      case 'ACTIVE':
        return 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/30 dark:text-emerald-300 border-emerald-200 dark:border-emerald-900/30';
      case 'COMPLETED':
        return 'bg-slate-50 text-slate-750 dark:bg-slate-950/30 dark:text-slate-300 border-slate-200 dark:border-slate-800/30';
      case 'CANCELLED':
        return 'bg-red-50 text-red-700 dark:bg-red-950/30 dark:text-red-300 border-red-200 dark:border-red-900/30';
      default:
        return 'bg-slate-50 text-slate-700';
    }
  };

  const formatDateRange = (startStr: string, endStr: string) => {
    const start = new Date(startStr);
    const end = new Date(endStr);
    const options: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric' };
    
    if (start.getFullYear() !== new Date().getFullYear()) {
      options.year = 'numeric';
    }

    const startFormatted = start.toLocaleDateString('en-US', options);
    const endFormatted = end.toLocaleDateString('en-US', options);

    return `${startFormatted} – ${endFormatted}`;
  };

  const isPreset = trip.coverType === 'PRESET' || trip.coverImage?.startsWith('linear-gradient');

  return (
    <div
      onClick={() => navigate(`/groups/${trip.groupId}/trips/${trip.id}`)}
      className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 hover:border-slate-350 dark:hover:border-slate-700 rounded-2xl shadow-sm hover:shadow-md cursor-pointer transition-all overflow-hidden flex flex-col group h-full"
    >
      {/* Cover Header */}
      {isPreset ? (
        <div
          style={{ background: trip.coverImage }}
          className="h-28 w-full transition-transform duration-500 group-hover:scale-102"
        />
      ) : (
        <div className="h-28 w-full overflow-hidden relative">
          <img
            src={trip.coverImage || 'https://images.unsplash.com/photo-1501785888041-af3ef285b470?q=80&w=600'}
            alt={trip.title}
            className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
            onError={(e) => {
              (e.target as HTMLImageElement).src = '';
              (e.target as HTMLElement).style.background = 'linear-gradient(135deg, #f6d365 0%, #fda085 100%)';
            }}
          />
        </div>
      )}

      {/* Content */}
      <div className="p-5 flex-1 flex flex-col space-y-4">
        <div>
          <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold border ${getStatusColor(trip.status)}`}>
            {trip.status}
          </span>
          <h4 className="text-base font-bold text-slate-850 dark:text-slate-100 mt-2 line-clamp-1 group-hover:text-slate-900 dark:group-hover:text-white transition-colors">
            {trip.title}
          </h4>
          {trip.description && (
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-1 line-clamp-2">
              {trip.description}
            </p>
          )}
        </div>

        <div className="flex-1 flex flex-col justify-end space-y-2 text-xs text-slate-650 dark:text-slate-300">
          <div className="flex items-center gap-1.5">
            <MapPin className="w-3.5 h-3.5 text-slate-400 dark:text-slate-500 shrink-0" />
            <span className="truncate">{trip.destination.displayName}</span>
          </div>

          <div className="flex items-center gap-1.5">
            <Calendar className="w-3.5 h-3.5 text-slate-400 dark:text-slate-500 shrink-0" />
            <span>{formatDateRange(trip.schedule.startDate, trip.schedule.endDate)}</span>
          </div>

          <div className="flex items-center gap-1.5 pt-2 border-t border-slate-100 dark:border-slate-800/60">
            <Users className="w-3.5 h-3.5 text-slate-400 dark:text-slate-500 shrink-0" />
            <span>
              {trip.participants.filter((p) => p.status === 'ACCEPTED').length} Joined
              {trip.participants.some((p) => p.status === 'INVITED') && 
                ` (${trip.participants.filter((p) => p.status === 'INVITED').length} Invited)`}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
export default TripCard;
