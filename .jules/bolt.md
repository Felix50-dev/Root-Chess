# Bolt's Performance Journal

This journal documents critical, high-impact performance learnings and patterns discovered in the Root Chess codebase.

## 2025-02-14 - Piece-Scanning Redundancies and O(N²) Board Checks
**Learning:** In the chess engine/board representation, scanning methods (e.g., `isUnderAttack` and `isCheck` in `King`, and `getAlivePieces` in `GamePlay`) frequently query invariant board positions (like the king's current spot or box targets) inside loops, leading to O(N²) complexity and redundant boundary checks. Calling `board.getBox()` inside nested loops triggers unnecessary safety boundaries and exceptional logic. Furthermore, allocating new `Position` objects inside hotspots increases garbage collector (GC) load.
**Action:**
1. Cache target and invariant spots outside loops.
2. Directly iterate over `board.boxes` (List<List<Spot>>) to skip `getBox` boundary overhead.
3. Reuse existing `spot.position` objects to eliminate redundant GC overhead from new allocations in loops.
