import '@testing-library/jest-dom';
import { vi } from 'vitest';

// Mock BroadcastChannel globally for all test suites
class MockBroadcastChannel {
  name: string;
  onmessage: ((ev: MessageEvent) => any) | null = null;
  onmessageerror: ((ev: MessageEvent) => any) | null = null;

  constructor(name: string) {
    this.name = name;
  }

  postMessage(message: any) {
    // In-memory dispatch to other listeners of same channel name
    const event = new MessageEvent('message', { data: message });
    setTimeout(() => {
      if (this.onmessage) this.onmessage(event);
    }, 0);
  }

  close() {}
  addEventListener() {}
  removeEventListener() {}
  dispatchEvent() { return true; }
}

global.BroadcastChannel = MockBroadcastChannel as any;

// Mock window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(), // deprecated
    removeListener: vi.fn(), // deprecated
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});
