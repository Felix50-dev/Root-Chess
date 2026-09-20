## 2025-05-18 - Avoid O(N²) Square Scans for Board Position Validation

**Learning:** `Board.isValidPosition` performed a 64-square nested loop traversal on `boxes` to check if a position existed, introducing unnecessary computation on move validations.
**Action:** Replace `boxes` array searches with O(1) range checks (`row in 0 until NUM_ROWS && column in 0 until NUM_COLUMNS`) for coordinate validation.
