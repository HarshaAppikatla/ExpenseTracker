import { useState, useEffect } from 'react';

const now = new Date();
let selectedMonth = now.getMonth() + 1; // 1-12
let selectedYear = now.getFullYear();

const listeners = new Set<() => void>();
const emit = () => listeners.forEach((l) => l());

export const useFilterStore = () => {
  const [month, setMonth] = useState(selectedMonth);
  const [year, setYear] = useState(selectedYear);

  useEffect(() => {
    const handleUpdate = () => {
      setMonth(selectedMonth);
      setYear(selectedYear);
    };

    listeners.add(handleUpdate);
    return () => {
      listeners.delete(handleUpdate);
    };
  }, []);

  const setFilters = (m: number, y: number) => {
    selectedMonth = m;
    selectedYear = y;
    emit();
  };

  return {
    month,
    year,
    setFilters,
  };
};
