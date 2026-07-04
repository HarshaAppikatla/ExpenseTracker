/**
 * AnimatedCard.tsx
 * Motion-wrapped card with entrance animation and hover lift.
 * Replaces plain <Card> for bento grid items that benefit from animation.
 *
 * Usage:
 *   <AnimatedCard delay={0.1} className="p-5">
 *     ...content...
 *   </AnimatedCard>
 */
import React from 'react';
import { motion } from 'framer-motion';
import { cardVariants, cardHover } from '@/animations/variants';

interface AnimatedCardProps {
  children: React.ReactNode;
  className?: string;
  /** Stagger delay offset in seconds (e.g. index * 0.07) */
  delay?: number;
  /** Disable hover lift for non-interactive cards */
  noHover?: boolean;
}

export const AnimatedCard: React.FC<AnimatedCardProps> = ({
  children,
  className = '',
  delay = 0,
  noHover = false,
}) => (
  <motion.div
    variants={cardVariants}
    initial="hidden"
    animate="visible"
    transition={{ delay }}
    whileHover={noHover ? undefined : cardHover}
    className={`bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-card shadow-sm ${className}`}
  >
    {children}
  </motion.div>
);
