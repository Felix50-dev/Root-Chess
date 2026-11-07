# Bolt's Performance Journal

## 2026-10-12 - Initial Startup Profiling
**Learning:** Cold startup is dominated by first-touch ART class verification of generated Hilt factories, Room, and Compose runtime across uncompiled multidex files, exacerbated by synchronous main-thread initialization.
**Action:** Favor baseline profiles, dispatching background work off main thread, and eliminating unnecessary main-thread allocations.
