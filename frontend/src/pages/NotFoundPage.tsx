import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Compass } from 'lucide-react';
import { Button } from '@/components/ui/Button';

export const NotFoundPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col items-center justify-center p-24 text-center gap-24 relative overflow-hidden">
      <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-blue-900/10 rounded-full blur-[100px]"></div>
      <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-teal-900/10 rounded-full blur-[100px]"></div>

      <div className="p-16 bg-blue-950 border border-blue-800 text-blue-400 rounded-full animate-bounce">
        <Compass className="w-48 h-48" />
      </div>

      <div className="space-y-8">
        <h2 className="text-6xl md:text-8xl font-black tracking-widest text-transparent bg-clip-text bg-gradient-to-r from-blue-500 to-teal-400">
          404
        </h2>
        <h3 className="text-lg md:text-xl font-bold text-white">Route Navigation Lost</h3>
        <p className="text-sm text-slate-400 max-w-sm mx-auto leading-relaxed">
          The page or layout you are trying to access does not exist yet or has been moved. Let's return you to the main interface.
        </p>
      </div>

      <div className="flex gap-16 mt-16">
        <Button variant="outlined" onClick={() => navigate(-1)} className="border-slate-700 text-slate-300 hover:bg-slate-900">
          <ArrowLeft className="w-16 h-16" />
          <span>Go Back</span>
        </Button>
        <Button variant="filled" onClick={() => navigate('/')}>
          <span>Landing Home</span>
        </Button>
      </div>
    </div>
  );
};
