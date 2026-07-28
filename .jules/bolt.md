# Bolt's Journal

## 2024-07-28 - Optimization Strategy for Root Chess
**Learning:** In a Chess application with minimax or engine search capabilities (such as Stockfish or custom AI search), cloning the board, validating positions, and scanning for attacks/checks are executed millions of times. Unnecessary object allocations and high-overhead iteration loops directly cause performance degradation. Avoiding redundant initializations in constructors and caching reusable objects is the most effective approach.
**Action:** When cloning the board, avoid redundant board reset initialization in primary constructors and bypass it using parameter/secondary constructor injection. Replace O(N^2) searches with O(1) checks where mathematically possible, and lift invariant lookup operations out of piece-scanning loops to maximize CPU efficiency.
