import React from 'react';
import { Pencil } from 'lucide-react';

interface ProfileAvatarProps {
  fullName?: string;
  onEditClick?: () => void;
  className?: string;
}

export const ProfileAvatar: React.FC<ProfileAvatarProps> = ({
  fullName = 'User',
  onEditClick,
  className = '',
}) => {
  // Get initials (up to 2 characters)
  const initials = fullName
    .split(' ')
    .filter(Boolean)
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2) || 'DU';

  return (
    <div className={`relative inline-block w-[96px] h-[96px] select-none ${className}`}>
      {/* Circle initials container */}
      <div className="w-full h-full rounded-full bg-blue-100 dark:bg-blue-950 flex items-center justify-center border border-[#EAECEF] dark:border-slate-800 text-blue-600 dark:text-blue-400 font-extrabold text-[32px] tracking-tight shadow-inner">
        {initials}
      </div>

      {/* Overlapping Edit Button */}
      <button
        onClick={onEditClick}
        aria-label="Edit Profile Avatar"
        className="absolute bottom-0 right-0 w-[36px] h-[36px] bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 rounded-full flex items-center justify-center shadow-md hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500/20 active:scale-95"
      >
        <Pencil className="w-[14px] h-[14px]" />
      </button>
    </div>
  );
};

export default ProfileAvatar;
