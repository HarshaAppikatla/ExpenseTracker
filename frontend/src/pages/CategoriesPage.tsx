import React, { useState } from 'react';
import { useCategories, useCreateCategory, useDeleteCategory } from '@/features/category/hooks/useCategories';
import toast from 'react-hot-toast';
import { FolderPlus, Trash2, HelpCircle } from 'lucide-react';

const PRESET_ICONS = [
  { name: 'restaurant', label: 'Food & Dining' },
  { name: 'shopping_bag', label: 'Shopping' },
  { name: 'directions_car', label: 'Transport' },
  { name: 'receipt_long', label: 'Bills & Utilities' },
  { name: 'medical_services', label: 'Health & Medical' },
  { name: 'school', label: 'Education' },
  { name: 'sports_esports', label: 'Entertainment' },
  { name: 'flight', label: 'Travel & Trips' },
  { name: 'payments', label: 'Salary & Income' },
  { name: 'home', label: 'Housing' },
  { name: 'work', label: 'Work' },
  { name: 'fitness_center', label: 'Fitness' },
  { name: 'movie', label: 'Leisure' },
  { name: 'pets', label: 'Pets' },
  { name: 'more_horiz', label: 'Other' },
];

const PRESET_COLORS = [
  '#FF5733', // Red-Orange
  '#E0115F', // Ruby Red
  '#1F75FE', // Blue
  '#F4D03F', // Yellow
  '#2ECC71', // Green
  '#9B59B6', // Purple
  '#E67E22', // Orange
  '#1ABC9C', // Teal
  '#34495E', // Slate
  '#7F8C8D', // Grey
];

export const CategoriesPage: React.FC = () => {
  const { data: categories = [], isLoading } = useCategories();
  const createMutation = useCreateCategory();
  const deleteMutation = useDeleteCategory();

  const [name, setName] = useState('');
  const [selectedIcon, setSelectedIcon] = useState('restaurant');
  const [selectedColor, setSelectedColor] = useState('#FF5733');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      toast.error('Category name is required');
      return;
    }

    setIsSubmitting(true);
    createMutation.mutate(
      {
        name: name.trim(),
        icon: selectedIcon,
        color: selectedColor,
      },
      {
        onSuccess: () => {
          toast.success('Category created successfully!');
          setName('');
          setIsSubmitting(false);
        },
        onError: (err: any) => {
          toast.error(err.message || 'Failed to create category');
          setIsSubmitting(false);
        },
      }
    );
  };

  const handleDelete = (id: string) => {
    if (!window.confirm('Are you sure you want to delete this custom category?')) return;

    deleteMutation.mutate(id, {
      onSuccess: () => {
        toast.success('Category deleted successfully!');
      },
      onError: (err: any) => {
        toast.error(err.message || 'Failed to delete category');
      },
    });
  };

  if (isLoading) {
    return <div className="text-center py-12 text-slate-500">Loading categories...</div>;
  }

  return (
    <div className="flex flex-col gap-6 w-full">
      <div className="flex flex-col md:flex-row gap-6 w-full">
        {/* Create Category Panel */}
        <div className="w-full md:w-1/3 bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border h-fit">
          <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50 mb-4 flex items-center gap-2">
            <FolderPlus className="w-5 h-5 text-indigo-600" />
            Add Custom Category
          </h2>
          <form onSubmit={handleCreate} className="flex flex-col gap-4">
            {/* Category Name */}
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                Category Name
              </label>
              <input
                type="text"
                placeholder="e.g. Groceries"
                value={name}
                onChange={(e) => setName(e.target.value)}
                maxLength={50}
                className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 focus:ring-indigo-600 outline-none text-sm bg-transparent text-slate-900 dark:text-slate-50"
              />
            </div>

            {/* Icon Grid */}
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                Select Icon
              </label>
              <div className="grid grid-cols-5 gap-2 max-h-[160px] overflow-y-auto p-1 border border-slate-100 dark:border-slate-800 rounded-xl">
                {PRESET_ICONS.map((i) => (
                  <button
                    key={i.name}
                    type="button"
                    title={i.label}
                    onClick={() => setSelectedIcon(i.name)}
                    className={`h-10 rounded-xl flex items-center justify-center text-xs font-semibold transition-all border ${
                      selectedIcon === i.name
                        ? 'bg-indigo-50 border-indigo-600 text-indigo-600 dark:bg-indigo-950/40 dark:text-indigo-400'
                        : 'border-slate-100 dark:border-slate-800 text-slate-500 hover:bg-slate-50 dark:hover:bg-slate-800'
                    }`}
                  >
                    <span className="material-icons-outlined text-base">{i.name}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* Color Palette */}
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-semibold text-slate-500 dark:text-slate-400">
                Select Color
              </label>
              <div className="flex flex-wrap gap-2">
                {PRESET_COLORS.map((c) => (
                  <button
                    key={c}
                    type="button"
                    onClick={() => setSelectedColor(c)}
                    style={{ backgroundColor: c }}
                    className={`w-7 h-7 rounded-full transition-all ${
                      selectedColor === c ? 'ring-2 ring-indigo-600 ring-offset-2 dark:ring-offset-slate-900 scale-110' : ''
                    }`}
                  />
                ))}
              </div>
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full mt-2 py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white font-semibold text-sm transition-all"
            >
              {isSubmitting ? 'Creating...' : 'Create Category'}
            </button>
          </form>
        </div>

        {/* Categories List */}
        <div className="flex-1 bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border">
          <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50 mb-4">
            Default & Custom Categories
          </h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {categories.map((c) => (
              <div
                key={c.id}
                className="flex items-center justify-between p-4 rounded-xl border border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900/20"
              >
                <div className="flex items-center gap-3">
                  <div
                    style={{ backgroundColor: `${c.color}15`, color: c.color }}
                    className="w-10 h-10 rounded-xl flex items-center justify-center font-bold"
                  >
                    <span className="material-icons-outlined text-lg">{c.icon || 'folder'}</span>
                  </div>
                  <div>
                    <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-50">
                      {c.name}
                    </h3>
                    <span className="text-[10px] font-medium text-slate-400">
                      {c.systemCategory ? 'System Default' : 'Custom'}
                    </span>
                  </div>
                </div>

                {!c.systemCategory && (
                  <button
                    onClick={() => handleDelete(c.id)}
                    className="text-slate-400 hover:text-red-500 p-1.5 hover:bg-red-50 dark:hover:bg-red-950/20 rounded-lg transition-all"
                    title="Delete custom category"
                  >
                    <Trash2 size={16} />
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
export default CategoriesPage;
