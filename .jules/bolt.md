# Bolt's Performance Journal

## 2025-05-18 - Kotlin Primary Constructor Parameter Passing for Cloning
**Learning:** In Kotlin, `init` blocks always execute when creating a class instance. Creating a new instance via a zero-argument secondary constructor triggers default board initialization (`resetBoard()`), allocating 64 `Spot` instances and 32 `ChessPiece` instances that are immediately overwritten when cloning. Passing optional initial state through the primary constructor allows bypassing redundant allocations during cloning.
**Action:** Always provide initial parameters in the primary constructor (or default/nullable primary constructor) when optimizing clone/copy methods, delegating `@Inject constructor()` to `this(null)`.
