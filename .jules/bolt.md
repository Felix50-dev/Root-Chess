## 2025-02-18 - Avoid Redundant Default Initialization in Model Cloning

**Learning:** In Kotlin models with default property initializers or `init` blocks (such as `Board.boxes = resetBoard()`), secondary or default constructors execute initialization logic before property overrides occur. Calling `Board()` during `clone()` allocated 64 unused `Spot`s and 32 unused `ChessPiece`s every clone operation before immediately overwriting `boxes`.

**Action:** Provide a constructor that accepts nullable/optional pre-constructed data (`boxesParam`), delegating `@Inject constructor()` to `this(null)`, so manual cloning can bypass default initialization routines completely.
