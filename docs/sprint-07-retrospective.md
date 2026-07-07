# Sprint 07 Retrospective: Real-Time Notifications & UX Polish

## 🎯 Goals Achieved
* **Decoupled Notification Aggregate**: Built the independent context, converting domain events into persisted records post-transaction commit.
* **Server-Sent Events Broadcast Engine**: Implemented the stream controller, asynchronous publisher, thread-safe emitter registry, and periodic keep-alive scheduler.
* **Secure Cookie Bridge**: Configured JWT extraction to support standard header/cookie extraction for native browser SSE connections.
* **UI Bell Badge & Sliding Drawer**: Delivered custom layout bells with pulsing badges and a right-sliding sidebar drawer with page scrolling.
* **Collaboration Widgets**: Integrated recent notification logs, aggregate debt balances, and category budget warning alert banners onto the dashboard.

---

## 🛠️ ADR Decisions Implemented
* **ADR-006 Integration**: Followed strict command-query separations and event-driven decoupled context boundaries. The Notification context does not import any services from Expense or Settlement contexts.

---

## 💡 Lessons Learned
* **Native EventSource Limitations**: Browser-native `EventSource` connections cannot set custom headers. We bridged this by synchronizing JWT access tokens into a fallback cookie (`JWT`), enabling secure stream Handshakes without compromising backend token filters.
* **Buggy Tab Emitter Storms**: Multiple browser tabs open concurrent stream channels. Mitigated connection exhaustion by caching emitters per user in a thread-safe repository with a max connection count of 10 (FIFO eviction).

---

## ⏩ Deferred Work (Sprint 08)
* **Retention Cleanup Scheduler**: A background worker to clean up soft-deleted/expired notification records older than 90 days is deferred to Sprint 08 to prioritize UI dashboard polish.
