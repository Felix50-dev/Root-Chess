## 2025-05-18 - Direct Board Array Access and Object Allocation Reduction

**Learning:** Repeated calls to `Board.getBox(row, col)` during 64-square board scans introduce unnecessary bounds checking, row indexing, and temporary `Position` allocations.
**Action:** Access `board.boxes` (List<List<Spot>>) directly in inner loops, cache target `Spot` references outside the loop, and reuse existing `Spot.position` instances.
