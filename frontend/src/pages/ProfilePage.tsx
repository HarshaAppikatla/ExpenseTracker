import React from 'react';
import { motion } from 'framer-motion';
import { useAuthContext } from '@/hooks/useAuthContext';
import { useProfile } from '@/features/profile/hooks/useProfile';
import { useFinancialDashboard } from '@/features/dashboard/hooks/useDashboard';
import { useMyGroups } from '@/features/group/hooks/useGroups';
import { pageVariants } from '@/animations/variants';
import { Wallet, TrendingUp, PieChart, Users } from 'lucide-react';

import { HeroProfileBanner } from '@/features/profile/components/HeroProfileBanner';
import { ProfileStatsCard } from '@/features/profile/components/ProfileStatsCard';
import { AccountDetailsCard } from '@/features/profile/components/AccountDetailsCard';
import { ActivityTimeline } from '@/features/profile/components/ActivityTimeline';
import { QuickActionsCard } from '@/features/profile/components/QuickActionsCard';

export const ProfilePage: React.FC = () => {
  const { user } = useAuthContext();
  
  // Data queries
  const { data: profile } = useProfile();
  const { data: financial, isLoading: isFinancialLoading } = useFinancialDashboard();
  const { data: groups, isLoading: isGroupsLoading } = useMyGroups(undefined, 0, 100);

  const currencySymbol = profile?.preferredCurrency || '$';

  // Determine top category details
  const topCategoryName = financial?.topSpendingCategories?.[0]?.categoryName || 'N/A';

  return (
    <motion.div
      variants={pageVariants}
      initial="hidden"
      animate="visible"
      exit="exit"
      className="space-y-[24px]"
    >
      {/* Row 1 — Hero Profile Banner */}
      <HeroProfileBanner
        fullName={user?.fullName || 'User'}
        email={user?.email || ''}
        status={user?.status || 'ACTIVE'}
        onEditClick={() => console.log('Edit Profile Clicked')}
      />

      {/* Row 2 — Statistics Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-[24px]">
        <ProfileStatsCard
          title="Total Expenses"
          value={
            isFinancialLoading
              ? '...'
              : `${currencySymbol}${financial?.totalSpentCurrentMonth?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}`
          }
          subtext="This Month"
          icon={Wallet}
          iconBgClass="bg-purple-50/80 dark:bg-purple-950/20"
          iconColorClass="text-purple-650 dark:text-purple-400"
        />

        <ProfileStatsCard
          title="Total Income"
          value={
            isFinancialLoading
              ? '...'
              : `${currencySymbol}${financial?.totalIncomeCurrentMonth?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}`
          }
          subtext="This Month"
          icon={TrendingUp}
          iconBgClass="bg-emerald-50/80 dark:bg-emerald-950/20"
          iconColorClass="text-emerald-600 dark:text-emerald-400"
        />

        <ProfileStatsCard
          title="Top Category"
          value={isFinancialLoading ? '...' : topCategoryName}
          subtext="Highest Spending Category"
          icon={PieChart}
          iconBgClass="bg-amber-50/80 dark:bg-amber-950/20"
          iconColorClass="text-amber-600 dark:text-amber-400"
        />

        <ProfileStatsCard
          title="Active Groups"
          value={isGroupsLoading ? '...' : groups?.totalElements || groups?.content?.length || 0}
          subtext="Your Collaborative Groups"
          icon={Users}
          iconBgClass="bg-blue-50/80 dark:bg-blue-950/20"
          iconColorClass="text-blue-600 dark:text-blue-400"
        />
      </div>

      {/* Row 3 — Two-column details & activity layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-[24px]">
        <div className="lg:col-span-2">
          <AccountDetailsCard
            email={user?.email}
            roles={user?.roles}
            createdAt={user?.createdAt}
            loginProvider={user?.loginProvider}
            status={user?.status}
          />
        </div>
        <div className="lg:col-span-1">
          <ActivityTimeline
            createdAt={user?.createdAt}
            emailVerified={true}
          />
        </div>
      </div>

      {/* Row 4 — Quick Actions */}
      <QuickActionsCard />
    </motion.div>
  );
};

export default ProfilePage;
