import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { dashboardService, DashboardSummary, FinancialDashboard } from '../services/dashboardService';

export const useDashboardSummary = (): UseQueryResult<DashboardSummary, Error> => {
  return useQuery({
    queryKey: ['dashboard-summary'],
    queryFn: () => dashboardService.getSummary(),
  });
};

export const useFinancialDashboard = (): UseQueryResult<FinancialDashboard, Error> => {
  return useQuery({
    queryKey: ['dashboard-financial'],
    queryFn: () => dashboardService.getFinancialDashboard(),
  });
};

