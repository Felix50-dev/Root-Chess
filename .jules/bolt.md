# Bolt's Journal

## 2024-11-20 - Kotlin Constructor Initialization Order in Cloned Boards
**Learning:** In Kotlin, property initializers and `init` blocks always execute before any secondary constructors. When manual instantiation of a Hilt-injected class (like `Board`) requires bypassing expensive default logic (such as `resetBoard()` allocating 160+ objects), these optional parameters must be specified in the primary constructor. To remain compatible with Dagger/Hilt's lack of parameter support, use an `@Inject` annotated secondary constructor that delegates to the primary constructor with default/nullable values.
**Action:** Always place custom initialization parameters in the primary constructor and delegate to it via `@Inject constructor() : this(null)` for DI compatibility.

## 2024-11-20 - Deep Copying Volatile Game State in Chess Pieces
**Learning:** When deep-cloning an MVVM board state in chess gameplay engines, relying on standard model copying can inadvertently drop critical volatile states (like `hasMoved` for castling rights and `canEnPassant` for en passant capture eligibility). This causes silent game state corruption during simulations and AI-analysis paths.
**Action:** Always explicitly copy state flags like `hasMoved` and `canEnPassant` when cloning individual piece types during board deep copies.
