## 2024-07-14 - [Kotlin Constructor Initialization Order]
**Learning:** In Kotlin, `init` blocks and property initializers execute before the body of any secondary constructor. To allow bypassing default initialization logic (like `resetBoard()`) when manually instantiating a class (e.g., during a `clone()` operation), the initialization parameters must be part of the primary constructor.
**Action:** Always use primary constructors with default/optional parameters for objects that require both DI-controlled injection and optimized manual cloning.
