# Bolt's Performance Journal

## 2024-12-16 - Kotlin Constructor Delegation and Bypassing Initialization Overhead
**Learning:** In Kotlin, all initializer blocks (`init`) and property initializers execute prior to any secondary constructors. When manual instantiation requires bypassing heavy default initialization (like `resetBoard()`), the configuration parameters must be supplied via a primary constructor.
**Action:** Define a primary constructor that accepts pre-initialized components (e.g., nullable/default list of Spots) and conditionally trigger default generation in `init` only when it is not provided.
