/**
 * PageTransition.tsx
 * Wraps a page's root element with AnimatePresence + motion.div
 * using the shared pageVariants. Drop this as the outermost element
 * in any page component to get consistent fade + slide-up transitions.
 *
 * Usage:
 *   export const SomePage = () => (
 *     <PageTransition>
 *       ...page content...
 *     </PageTransition>
 *   );
 */
import React from 'react';
import { motion } from 'framer-motion';
import { pageVariants } from '@/animations/variants';

interface PageTransitionProps {
  children: React.ReactNode;
  className?: string;
}

export const PageTransition: React.FC<PageTransitionProps> = ({ children, className }) => (
  <motion.div
    variants={pageVariants}
    initial="hidden"
    animate="visible"
    exit="exit"
    className={className}
  >
    {children}
  </motion.div>
);
