import { test, expect } from '@playwright/test';

test.describe('Authentication and Session E2E Flows', () => {

  test.beforeEach(async ({ page }) => {
    // Go to landing page
    await page.goto('/');
  });

  test('should complete registration, email verification, and login cycle', async ({ page }) => {
    // 1. Go to register page
    await page.click('text=Register');
    await expect(page).toHaveURL('/register');

    // Fill registration
    await page.fill('input[placeholder="John Doe"]', 'Jane Doe');
    await page.fill('input[placeholder="you@example.com"]', 'jane.doe@example.com');
    
    const pwdFields = page.locator('input[type="password"]');
    await pwdFields.nth(0).fill('Password123!');
    await pwdFields.nth(1).fill('Password123!');

    await page.check('input[type="checkbox"]');
    await page.click('button[type="submit"]');

    // Verify success redirect or message
    await expect(page.locator('text=Account created successfully')).toBeVisible();
    await expect(page).toHaveURL('/login');

    // 2. Email verification
    // Simulate navigating to email verification link received in email
    await page.goto('/verify-email?token=my-verification-token');
    await expect(page.locator('text=Email verified successfully')).toBeVisible();

    // 3. Login
    await page.goto('/login');
    await page.fill('input[type="email"]', 'jane.doe@example.com');
    await page.locator('input[type="password"]').fill('Password123!');
    await page.click('button[type="submit"]');

    // Should redirect to dashboard
    await expect(page).toHaveURL('/dashboard');
    await expect(page.locator('text=Welcome to your workspace')).toBeVisible();

    // 4. Logout
    await page.click('text=Logout');
    await expect(page).toHaveURL('/login');
  });

  test('should lock account after multiple failed logins', async ({ page }) => {
    await page.goto('/login');
    
    // Attempt 5 incorrect logins
    for (let i = 0; i < 5; i++) {
      await page.fill('input[type="email"]', 'lock.me@example.com');
      await page.locator('input[type="password"]').fill('wrong-pass');
      await page.click('button[type="submit"]');
      
      if (i < 4) {
        await expect(page.locator('text=Invalid email or password')).toBeVisible();
      }
    }

    // On 5th attempt, should show locked message
    await expect(page.locator('text=locked')).toBeVisible();
  });
});
