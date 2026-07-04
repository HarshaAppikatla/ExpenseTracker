import React from 'react';
import { GroupDto } from '../types/group';
import { Copy, Check, Link, Key } from 'lucide-react';
import toast from 'react-hot-toast';

interface RoomCodeCardProps {
  group: GroupDto;
}

export const RoomCodeCard: React.FC<RoomCodeCardProps> = ({ group }) => {
  const [copiedCode, setCopiedCode] = React.useState(false);
  const [copiedLink, setCopiedLink] = React.useState(false);

  const copyToClipboard = async (text: string, type: 'code' | 'link') => {
    try {
      await navigator.clipboard.writeText(text);
      if (type === 'code') {
        setCopiedCode(true);
        setTimeout(() => setCopiedCode(false), 2000);
        toast.success('Room code copied to clipboard!');
      } else {
        setCopiedLink(true);
        setTimeout(() => setCopiedLink(false), 2000);
        toast.success('Invite link copied to clipboard!');
      }
    } catch (err) {
      toast.error('Failed to copy to clipboard');
    }
  };

  const inviteUrl = group.joinLink || `${window.location.origin}/join/${group.groupCode}`;

  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6 shadow-sm">
      <h3 className="text-sm font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-4">
        Invite Collaborators
      </h3>
      <div className="space-y-4">
        {/* Room Code */}
        <div>
          <label className="block text-xs font-medium text-slate-400 dark:text-slate-500 mb-1.5">
            Room Code
          </label>
          <div className="flex items-center gap-2">
            <div className="flex-1 flex items-center gap-2 px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-lg text-slate-800 dark:text-slate-100 font-mono text-sm tracking-wider font-semibold">
              <Key className="w-4 h-4 text-slate-400" />
              {group.groupCode}
            </div>
            <button
              onClick={() => copyToClipboard(group.groupCode, 'code')}
              className="p-2 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 rounded-lg transition-colors text-slate-600 dark:text-slate-300"
              title="Copy room code"
            >
              {copiedCode ? <Check className="w-4 h-4 text-green-500" /> : <Copy className="w-4 h-4" />}
            </button>
          </div>
        </div>

        {/* Invite Link */}
        <div>
          <label className="block text-xs font-medium text-slate-400 dark:text-slate-500 mb-1.5">
            Invite Link
          </label>
          <div className="flex items-center gap-2">
            <div className="flex-1 flex items-center gap-2 px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-lg text-slate-800 dark:text-slate-100 text-sm truncate font-medium">
              <Link className="w-4 h-4 text-slate-400 shrink-0" />
              <span className="truncate">{inviteUrl}</span>
            </div>
            <button
              onClick={() => copyToClipboard(inviteUrl, 'link')}
              className="p-2 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 rounded-lg transition-colors text-slate-600 dark:text-slate-300"
              title="Copy invite link"
            >
              {copiedLink ? <Check className="w-4 h-4 text-green-500" /> : <Copy className="w-4 h-4" />}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
export default RoomCodeCard;
