export const matchCategory = (expenseCategory: string, budgetCategoryName: string): boolean => {
  if (!expenseCategory || !budgetCategoryName) return false;
  return expenseCategory.trim().toUpperCase() === budgetCategoryName.trim().toUpperCase();
};
