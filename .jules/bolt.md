# Bolt's Performance Journal

## 2024-08-10 - O(1) Board Bounds & Deep Cloning Bypasses
**Learning:** In Jetpack Compose / MVVM, `Board.clone()` is a very hot path called on every game state emission. The original `clone()` implementation redundantly initialized a default board via `resetBoard()` and allocated 64 new `Position` objects. Additionally, the boundary-checking `isValidPosition` performed an unnecessary $O(N^2)$ iteration over 64 cells.
**Action:**
1. Optimize `isValidPosition` to use $O(1)$ coordinate bounds checks.
2. Refactor `Board` to accept nullable `initialBoxes` in a primary constructor while keeping a secondary `@Inject` delegation constructor for Dagger/Hilt, completely bypassing redundant `resetBoard()` execution during clones.
3. Reuse existing immutable `Position` objects in `Board.clone()`.
4. Copy volatile states like `hasMoved` and `canEnPassant` in clones to prevent AI analysis bugs.
5. Cache static spots / row arrays before loop entry in piece-scanning methods to avoid expensive redundant indexing, index boundary checks, and double-bang non-null assertions inside nested loops.
