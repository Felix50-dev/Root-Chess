## 2025-02-21 - Hot-loop Logging and Board Scanning Allocations

**Learning:** `Log.d` statements inside 64-square board loops (like `findKing`) create significant string allocation and logging overhead in hot check-detection and move validation paths. Additionally, calling `Position(row, col)` inside 64-square loops in `getValidMoves` allocates 64 unnecessary objects per call when `spot.position` is already available on `board.boxes`.

**Action:** Directly traverse `board.boxes` (`List<List<Spot>>`), reuse existing `Spot.position` instances, and ensure hot loop paths are free of debug logging.
