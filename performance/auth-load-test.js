import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    login_profile: {
      executor: 'constant-vus',
      vus: 100,
      duration: '30s',
      exec: 'loginAndProfileScenario',
    },
    refresh_profile: {
      executor: 'constant-vus',
      vus: 100,
      duration: '30s',
      exec: 'refreshScenario',
    },
    registration: {
      executor: 'constant-vus',
      vus: 50,
      duration: '30s',
      exec: 'registerScenario',
    },
  },
  thresholds: {
    // Pass/Fail Performance Targets
    'http_req_duration{scenario:login_profile}': ['p(95)<500'], // Login < 500ms
    'http_req_duration{scenario:refresh_profile}': ['p(95)<300'], // Refresh < 300ms
    'http_req_duration{scenario:registration}': ['p(95)<700'], // Register < 700ms
    'http_req_duration': ['p(95)<400'], // General < 400ms
    http_req_failed: ['rate<0.01'], // Error rate < 1%
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api/v1';

export function loginAndProfileScenario() {
  const loginPayload = JSON.stringify({
    email: `load-test-${__VU}@example.com`,
    password: 'Password123!',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // 1. Login
  const loginRes = http.post(`${BASE_URL}/auth/login`, loginPayload, params);
  check(loginRes, {
    'login status is 200': (r) => r.status === 200,
    'has access token': (r) => r.json('data.accessToken') !== undefined,
  });

  const token = loginRes.json('data.accessToken');
  if (token) {
    // 2. Profile fetch (/users/me)
    const profileParams = {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    };
    const profileRes = http.get(`${BASE_URL}/users/me`, profileParams);
    check(profileRes, {
      'profile status is 200': (r) => r.status === 200,
    });
  }

  sleep(1);
}

export function refreshScenario() {
  // Mock refresh token request
  const refreshPayload = JSON.stringify({
    refreshToken: 'mock-refresh-token-uuid-1234',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(`${BASE_URL}/auth/refresh`, refreshPayload, params);
  check(res, {
    'refresh status is 200 or 401': (r) => r.status === 200 || r.status === 401,
  });

  sleep(1);
}

export function registerScenario() {
  const registerPayload = JSON.stringify({
    fullName: `Load User ${__VU}-${Date.now()}`,
    email: `load-user-${__VU}-${Date.now()}@example.com`,
    password: 'Password123!',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(`${BASE_URL}/auth/register`, registerPayload, params);
  check(res, {
    'register status is 200': (r) => r.status === 200,
  });

  sleep(1);
}
