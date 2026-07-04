/**
 * animations/variants.ts
 * Centralized Framer Motion animation variants for the ExpenseFlow design system.
 * Import named variants; never hard-code animation values in components.
 */

import type { Variants } from 'framer-motion';

// ─── Page Transitions ────────────────────────────────────────────────────────

/** Standard page fade + slide-up entrance. Use on every route's root element. */
export const pageVariants: Variants = {
  hidden:  { opacity: 0, y: 18 },
  visible: { opacity: 1, y: 0,  transition: { duration: 0.32, ease: [0.22, 1, 0.36, 1] } },
  exit:    { opacity: 0, y: -10, transition: { duration: 0.18, ease: 'easeIn' } },
};

// ─── Stagger Children ─────────────────────────────────────────────────────────

/** Parent wrapper — staggers its direct children on mount. */
export const staggerParent: Variants = {
  hidden:  { opacity: 0 },
  visible: {
    opacity: 1,
    transition: { staggerChildren: 0.07, delayChildren: 0.05 },
  },
};

/** Child item for stagger lists — slides up and fades in. */
export const staggerChild: Variants = {
  hidden:  { opacity: 0, y: 14 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.28, ease: [0.22, 1, 0.36, 1] } },
};

// ─── Cards ────────────────────────────────────────────────────────────────────

/** Stat / bento card fade-in with gentle scale-up. */
export const cardVariants: Variants = {
  hidden:  { opacity: 0, scale: 0.97 },
  visible: { opacity: 1, scale: 1, transition: { duration: 0.3, ease: [0.22, 1, 0.36, 1] } },
};

/** Hover lift effect. Apply with whileHover. */
export const cardHover = {
  scale: 1.015,
  y: -2,
  transition: { duration: 0.18, ease: 'easeOut' },
};

// ─── Drawers / Modals ─────────────────────────────────────────────────────────

/** Right-side slide-over drawer (notification drawer). */
export const drawerVariants: Variants = {
  hidden:  { x: '100%',  opacity: 0 },
  visible: { x: 0,       opacity: 1, transition: { duration: 0.28, ease: [0.22, 1, 0.36, 1] } },
  exit:    { x: '100%',  opacity: 0, transition: { duration: 0.2,  ease: 'easeIn' } },
};

/** Generic dialog fade + scale. */
export const dialogVariants: Variants = {
  hidden:  { opacity: 0, scale: 0.96 },
  visible: { opacity: 1, scale: 1,    transition: { duration: 0.22, ease: [0.22, 1, 0.36, 1] } },
  exit:    { opacity: 0, scale: 0.96, transition: { duration: 0.15, ease: 'easeIn' } },
};

/** Overlay / backdrop fade. */
export const overlayVariants: Variants = {
  hidden:  { opacity: 0 },
  visible: { opacity: 1, transition: { duration: 0.2 } },
  exit:    { opacity: 0, transition: { duration: 0.15 } },
};

// ─── FAB Speed Dial ───────────────────────────────────────────────────────────

/** FAB container — staggers the action items. */
export const fabParent: Variants = {
  closed: { opacity: 0 },
  open:   { opacity: 1, transition: { staggerChildren: 0.06 } },
};

/** Individual FAB action item. */
export const fabItem: Variants = {
  closed: { opacity: 0, y: 10, scale: 0.9 },
  open:   { opacity: 1, y: 0,  scale: 1,   transition: { duration: 0.2, ease: 'easeOut' } },
};

/** FAB main button rotation when open. */
export const fabIconRotate = (isOpen: boolean) => ({
  rotate: isOpen ? 45 : 0,
  transition: { duration: 0.22, ease: 'easeInOut' },
});

// ─── Chart Reveal ─────────────────────────────────────────────────────────────

/** Slides in from the bottom — good for chart containers. */
export const chartReveal: Variants = {
  hidden:  { opacity: 0, y: 24 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.4, ease: [0.22, 1, 0.36, 1], delay: 0.1 } },
};

// ─── Number Counter ───────────────────────────────────────────────────────────
// Use with motion.span + animate={{ opacity: [0,1] }} and a useEffect counter.
// See: useCountUp hook in hooks/useCountUp.ts

// ─── Sidebar ──────────────────────────────────────────────────────────────────

/** Mobile sidebar slide-in from left. */
export const sidebarVariants: Variants = {
  hidden:  { x: '-100%' },
  visible: { x: 0,      transition: { duration: 0.25, ease: [0.22, 1, 0.36, 1] } },
  exit:    { x: '-100%', transition: { duration: 0.2,  ease: 'easeIn' } },
};

// ─── Toast / Badge ────────────────────────────────────────────────────────────

export const badgePop: Variants = {
  hidden:  { scale: 0 },
  visible: { scale: 1, transition: { type: 'spring', stiffness: 400, damping: 20 } },
};
