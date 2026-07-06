import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createTripSchema, CreateTripInput } from '../schemas/tripSchemas';
import { X } from 'lucide-react';

export const TRIP_COVERS = [
  { id: 'linear-gradient(135deg, #f6d365 0%, #fda085 100%)', name: 'Sunset' },
  { id: 'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)', name: 'Forest' },
  { id: 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)', name: 'Ocean' },
  { id: 'linear-gradient(135deg, #cfd9df 0%, #e2ebf0 100%)', name: 'Snow' },
  { id: 'linear-gradient(135deg, #fbc2eb 0%, #a6c1ee 100%)', name: 'Lavender' },
  { id: 'linear-gradient(135deg, #fdcbf1 0%, #e6dee9 100%)', name: 'Sakura' },
  { id: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)', name: 'Desert' },
  { id: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', name: 'Neon' },
];

interface CreateTripDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: CreateTripInput) => void;
  isLoading: boolean;
}

export const CreateTripDialog: React.FC<CreateTripDialogProps> = ({
  isOpen,
  onClose,
  onSubmit,
  isLoading,
}) => {
  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors },
  } = useForm<CreateTripInput>({
    resolver: zodResolver(createTripSchema),
    defaultValues: {
      title: '',
      description: '',
      city: '',
      country: '',
      startDate: '',
      endDate: '',
      coverType: 'PRESET',
      coverImage: TRIP_COVERS[0].id,
      allowInvites: true,
      visibility: 'GROUP',
    },
  });

  const watchCoverType = watch('coverType');
  const watchCoverImage = watch('coverImage');

  React.useEffect(() => {
    if (isOpen) {
      reset();
    }
  }, [isOpen, reset]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={onClose}></div>

      {/* Modal Dialog */}
      <div className="relative bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl w-full max-w-lg p-6 shadow-xl animate-in fade-in zoom-in-95 duration-250 max-h-[90vh] overflow-y-auto">
        <button
          onClick={onClose}
          className="absolute right-4 top-4 p-1.5 hover:bg-slate-50 dark:hover:bg-slate-800 rounded-lg text-slate-400 dark:text-slate-500 hover:text-slate-600 dark:hover:text-slate-350 transition-colors"
        >
          <X className="w-5 h-5" />
        </button>

        <h3 className="text-xl font-bold text-slate-800 dark:text-slate-100 mb-6">
          Plan New Trip
        </h3>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {/* Title */}
          <div>
            <label className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Trip Title *
            </label>
            <input
              type="text"
              {...register('title')}
              className={`w-full px-3 py-2 border rounded-lg bg-white dark:bg-slate-950 text-slate-850 dark:text-slate-100 text-sm focus:outline-none focus:ring-2 ${
                errors.title
                  ? 'border-red-500 focus:ring-red-200'
                  : 'border-slate-200 dark:border-slate-800 focus:border-slate-400 focus:ring-slate-100'
              }`}
              placeholder="e.g. Summer in Rome"
              disabled={isLoading}
            />
            {errors.title && (
              <span className="text-xs text-red-500 mt-1 block font-medium">
                {errors.title.message}
              </span>
            )}
          </div>

          {/* Description */}
          <div>
            <label className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Description
            </label>
            <textarea
              {...register('description')}
              rows={2}
              className="w-full px-3 py-2 border border-slate-200 dark:border-slate-800 focus:border-slate-400 rounded-lg bg-white dark:bg-slate-950 text-slate-850 dark:text-slate-100 text-sm focus:outline-none focus:ring-2 focus:ring-slate-100"
              placeholder="What is this trip about?"
              disabled={isLoading}
            />
          </div>

          {/* Destination inputs */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                City *
              </label>
              <input
                type="text"
                {...register('city')}
                className={`w-full px-3 py-2 border rounded-lg bg-white dark:bg-slate-950 text-slate-850 dark:text-slate-100 text-sm focus:outline-none focus:ring-2 ${
                  errors.city
                    ? 'border-red-500 focus:ring-red-200'
                    : 'border-slate-200 dark:border-slate-800 focus:border-slate-400 focus:ring-slate-100'
                }`}
                placeholder="e.g. Rome"
                disabled={isLoading}
              />
              {errors.city && (
                <span className="text-xs text-red-500 mt-1 block font-medium">
                  {errors.city.message}
                </span>
              )}
            </div>

            <div>
              <label className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                Country *
              </label>
              <input
                type="text"
                {...register('country')}
                className={`w-full px-3 py-2 border rounded-lg bg-white dark:bg-slate-950 text-slate-850 dark:text-slate-100 text-sm focus:outline-none focus:ring-2 ${
                  errors.country
                    ? 'border-red-500 focus:ring-red-200'
                    : 'border-slate-200 dark:border-slate-800 focus:border-slate-400 focus:ring-slate-100'
                }`}
                placeholder="e.g. Italy"
                disabled={isLoading}
              />
              {errors.country && (
                <span className="text-xs text-red-500 mt-1 block font-medium">
                  {errors.country.message}
                </span>
              )}
            </div>
          </div>

          {/* Dates */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                Start Date *
              </label>
              <input
                type="date"
                {...register('startDate')}
                className={`w-full px-3 py-2 border rounded-lg bg-white dark:bg-slate-950 text-slate-850 dark:text-slate-100 text-sm focus:outline-none focus:ring-2 ${
                  errors.startDate
                    ? 'border-red-500 focus:ring-red-200'
                    : 'border-slate-200 dark:border-slate-800 focus:border-slate-400 focus:ring-slate-100'
                }`}
                disabled={isLoading}
              />
              {errors.startDate && (
                <span className="text-xs text-red-500 mt-1 block font-medium">
                  {errors.startDate.message}
                </span>
              )}
            </div>

            <div>
              <label className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                End Date *
              </label>
              <input
                type="date"
                {...register('endDate')}
                className={`w-full px-3 py-2 border rounded-lg bg-white dark:bg-slate-950 text-slate-850 dark:text-slate-100 text-sm focus:outline-none focus:ring-2 ${
                  errors.endDate
                    ? 'border-red-500 focus:ring-red-200'
                    : 'border-slate-200 dark:border-slate-800 focus:border-slate-400 focus:ring-slate-100'
                }`}
                disabled={isLoading}
              />
              {errors.endDate && (
                <span className="text-xs text-red-500 mt-1 block font-medium">
                  {errors.endDate.message}
                </span>
              )}
            </div>
          </div>

          {/* Cover Image Selector */}
          <div>
            <label className="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2">
              Trip Cover Style
            </label>
            <div className="flex gap-4 mb-3">
              <label className="flex items-center gap-1.5 text-sm text-slate-700 dark:text-slate-300 cursor-pointer">
                <input
                  type="radio"
                  value="PRESET"
                  {...register('coverType')}
                  disabled={isLoading}
                  className="text-slate-900 focus:ring-slate-900"
                />
                Preset Illustration
              </label>
              <label className="flex items-center gap-1.5 text-sm text-slate-700 dark:text-slate-300 cursor-pointer">
                <input
                  type="radio"
                  value="CUSTOM"
                  {...register('coverType')}
                  disabled={isLoading}
                  className="text-slate-900 focus:ring-slate-900"
                />
                Custom Image URL
              </label>
            </div>

            {watchCoverType === 'PRESET' ? (
              <div className="grid grid-cols-4 gap-2.5">
                {TRIP_COVERS.map((cover) => (
                  <button
                    key={cover.id}
                    type="button"
                    onClick={() => setValue('coverImage', cover.id)}
                    style={{ background: cover.id }}
                    className={`h-12 rounded-xl relative transition-all border-2 ${
                      watchCoverImage === cover.id
                        ? 'border-slate-900 dark:border-white scale-95 shadow-md'
                        : 'border-transparent hover:scale-105'
                    }`}
                    title={cover.name}
                    disabled={isLoading}
                  >
                    {watchCoverImage === cover.id && (
                      <span className="absolute inset-0 flex items-center justify-center bg-black/10 dark:bg-white/10 rounded-[10px]">
                        <span className="w-2 h-2 bg-white dark:bg-slate-900 rounded-full"></span>
                      </span>
                    )}
                  </button>
                ))}
              </div>
            ) : (
              <div>
                <input
                  type="text"
                  placeholder="https://images.unsplash.com/photo-..."
                  className="w-full px-3 py-2 border border-slate-200 dark:border-slate-800 rounded-lg bg-white dark:bg-slate-950 text-slate-850 dark:text-slate-100 text-sm focus:outline-none focus:ring-2 focus:ring-slate-100"
                  disabled={isLoading}
                  value={watchCoverType === 'CUSTOM' && watchCoverImage?.startsWith('linear-gradient') ? '' : watchCoverImage}
                  onChange={(e) => setValue('coverImage', e.target.value)}
                />
              </div>
            )}
          </div>

          {/* Settings Allow invites & visibility */}
          <div className="flex flex-col sm:flex-row gap-4 pt-2">
            <label className="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300 cursor-pointer">
              <input
                type="checkbox"
                {...register('allowInvites')}
                className="rounded border-slate-200 text-slate-900 focus:ring-slate-900"
                disabled={isLoading}
              />
              Allow participants to invite others
            </label>

            <div className="sm:ml-auto flex items-center gap-2">
              <span className="text-sm text-slate-700 dark:text-slate-300">Visibility:</span>
              <select
                {...register('visibility')}
                className="px-2 py-1 border border-slate-200 dark:border-slate-800 rounded-md bg-white dark:bg-slate-950 text-slate-850 dark:text-slate-100 text-xs focus:outline-none focus:ring-2 focus:ring-slate-100"
                disabled={isLoading}
              >
                <option value="GROUP">Group Only</option>
                <option value="PRIVATE">Private</option>
              </select>
            </div>
          </div>

          {/* Buttons Actions */}
          <div className="flex gap-3 justify-end pt-6 border-t border-slate-100 dark:border-slate-800/60 mt-6">
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
              {isLoading ? 'Planning...' : 'Plan Trip'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
export default CreateTripDialog;
