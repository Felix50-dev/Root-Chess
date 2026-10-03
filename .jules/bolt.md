# Bolt's Performance Journal

## 2025-02-17 - Direct Matrix Access and Caching Target Spots in Piece Scanning
**Learning:** Calling `board.getBox(row, col)` in nested loops (64 iterations per scan in `isUnderAttack` and `isCheck`, as well as ray-casting path clearance loops) introduces significant bounds-checking and function-call overhead on hot paths.
**Action:** Access `board.boxes` (2D List) directly and cache target `Spot` references before loop entry to avoid repeated index validation and method dispatching during move validation.
