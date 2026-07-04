/**
 * useCountUp.ts
 * Animates a number from 0 (or a previous value) to `target` over `duration` ms.
 * Uses requestAnimationFrame for a butter-smooth number counter effect.
 *
 * Usage:
 *   const displayed = useCountUp(5974.50, 1200);
 *   <span>{displayed.toFixed(2)}</span>
 */
import { useState, useEffect, useRef } from 'react';

export function useCountUp(target: number, duration = 900): number {
  const [value, setValue] = useState(0);
  const rafRef = useRef<number | null>(null);
  const startRef = useRef<number | null>(null);
  const prevTarget = useRef(0);

  useEffect(() => {
    if (isNaN(target)) return;

    const from = prevTarget.current;
    const distance = target - from;
    startRef.current = null;

    const animate = (ts: number) => {
      if (startRef.current === null) startRef.current = ts;
      const elapsed = ts - startRef.current;
      const progress = Math.min(elapsed / duration, 1);
      // Ease out cubic
      const eased = 1 - Math.pow(1 - progress, 3);
      setValue(from + distance * eased);

      if (progress < 1) {
        rafRef.current = requestAnimationFrame(animate);
      } else {
        prevTarget.current = target;
      }
    };

    if (rafRef.current !== null) cancelAnimationFrame(rafRef.current);
    rafRef.current = requestAnimationFrame(animate);

    return () => {
      if (rafRef.current !== null) cancelAnimationFrame(rafRef.current);
    };
  }, [target, duration]);

  return value;
}
