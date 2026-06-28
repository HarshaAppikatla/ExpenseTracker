import React from 'react';
import { APP_NAME } from '@/core/constants';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-slate-900 text-slate-400 py-32 px-24 border-t border-slate-800">
      <div className="max-w-6xl mx-auto flex flex-col md:flex-row justify-between items-center gap-16">
        <div className="flex items-center gap-8">
          <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-white font-bold text-xs">
            EF
          </div>
          <span className="font-bold text-base text-white">{APP_NAME}</span>
        </div>
        
        <p className="text-sm">
          &copy; {new Date().getFullYear()} {APP_NAME}. All rights reserved. Created for collaborative finances.
        </p>

        <div className="flex gap-16 text-sm">
          <a href="#" className="hover:text-white transition-colors">Privacy Policy</a>
          <a href="#" className="hover:text-white transition-colors">Terms of Service</a>
        </div>
      </div>
    </footer>
  );
};
