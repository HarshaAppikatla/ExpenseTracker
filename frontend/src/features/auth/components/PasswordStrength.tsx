import React from 'react';
import { Check, X } from 'lucide-react';

interface PasswordStrengthProps {
  password?: string;
}

export const PasswordStrength: React.FC<PasswordStrengthProps> = ({ password = '' }) => {
  const rules = [
    { label: 'At least 8 characters', test: (p: string) => p.length >= 8 },
    { label: 'One uppercase letter', test: (p: string) => /[A-Z]/.test(p) },
    { label: 'One lowercase letter', test: (p: string) => /[a-z]/.test(p) },
    { label: 'One number', test: (p: string) => /\d/.test(p) },
    { label: 'One special character (@$!%*?&#)', test: (p: string) => /[@$!%*?&#]/.test(p) },
  ];

  const score = password ? rules.filter((r) => r.test(password)).length : 0;

  const getStrengthLabel = () => {
    if (!password) return '';
    if (score <= 2) return 'Weak';
    if (score <= 4) return 'Medium';
    return 'Strong';
  };

  const getStrengthColor = () => {
    if (score <= 2) return 'bg-red-500';
    if (score <= 4) return 'bg-amber-500';
    return 'bg-green-500';
  };

  const getTextColor = () => {
    if (score <= 2) return 'text-red-500';
    if (score <= 4) return 'text-amber-500';
    return 'text-green-500';
  };

  return (
    <div className="w-full flex flex-col gap-3">
      {/* Strength Indicator Bars */}
      {password && (
        <div className="flex flex-col gap-1.5">
          <div className="flex justify-between items-center">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">
              Password Strength:
            </span>
            <span className={`text-xs font-bold ${getTextColor()}`}>
              {getStrengthLabel()}
            </span>
          </div>
          <div className="flex gap-1 h-1 w-full bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
            {[1, 2, 3, 4, 5].map((index) => (
              <div
                key={index}
                className={`h-full flex-1 transition-all duration-300 ${
                  index <= score ? getStrengthColor() : 'bg-transparent'
                }`}
              />
            ))}
          </div>
        </div>
      )}

      {/* Rules Checklist */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-x-4 gap-y-1.5 mt-1">
        {rules.map((rule, idx) => {
          const isPassed = rule.test(password);
          return (
            <div key={idx} className="flex items-center gap-1.5">
              <span
                className={`flex-shrink-0 w-16 h-16 rounded-full flex items-center justify-center ${
                  isPassed
                    ? 'bg-green-50 dark:bg-green-950/40 text-green-500'
                    : 'bg-slate-50 dark:bg-slate-800/40 text-slate-400'
                }`}
              >
                {isPassed ? <Check size={10} strokeWidth={3} /> : <X size={10} strokeWidth={3} />}
              </span>
              <span
                className={`text-[10px] md:text-xs transition-colors ${
                  isPassed
                    ? 'text-slate-600 dark:text-slate-300 font-medium'
                    : 'text-slate-400 dark:text-slate-500'
                }`}
              >
                {rule.label}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
};
export type PasswordStrengthPropsType = PasswordStrengthProps;
