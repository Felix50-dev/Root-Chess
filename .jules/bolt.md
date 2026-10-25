## 2025-02-23 - [Opening Detector Sequence & SubList Allocation Optimization]
**Learning:** In hot execution paths like opening detection (evaluated after every move), using `.asSequence()` and `.subList()` creates unnecessary heap allocations and iterator/sequence wrapper overhead for each candidate opening in the book.
**Action:** Replace Kotlin Sequence/subList calls with indexed `for` loops and element-wise comparisons when matching prefix slices of collections.
