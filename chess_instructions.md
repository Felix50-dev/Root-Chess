# CLAUDE.md — Chess Coach

## Product Identity
Not a chess-playing app — a chess *learning* app. Every feature decision:
"Will this help the user become a stronger chess player?"

## Core Principle
Stockfish is an input, not the coach. It tells us what's best; our domain
logic explains why/how/when. Never let an LLM invent chess analysis — it
may only rephrase facts we've already computed deterministically.

## Current Architecture (as of this doc)
Single module: `data/`, `domain/model/`, `presentation/`, `stockfish/`, `ui/`.
MVVM-ish: ViewModels call into repositories/analysis code; keep chess logic
out of Composables and out of ViewModels where practical.

Direction, not a mandate yet: as analysis logic grows, split it into
focused analyzers (one concept per class) and push business rules into
use-case-shaped functions rather than growing god-objects in
ViewModels/repositories. Don't pre-build this structure — extract it when
a class actually becomes unwieldy.

## Explanation Quality Bar
Every explanation should say what happened, why it matters, which
principle is involved, and what to remember next time.
Bad: "Blunder." Good: "This move leaves your bishop undefended — before
attacking, check whether the attacking piece is protected."

## Brilliancy / classification logic
Personalize where cheap to do so (player level, move rarity) but don't
block shipping a working classifier on having every dimension (engine
accuracy, uniqueness, educational value, skill level) implemented at once.

## Testing
Analyzers and mistake/brilliancy classifiers need unit tests that don't
assert on raw Stockfish output strings — assert on the structured facts
your code derives from it.

## Not doing (for now)
No multi-module split (`feature-analysis`, `feature-lessons`, etc.), no
formal Use Case layer, no LearningProfile/AI-coach conversational system,
no 4-tier explanation-level system — until there's a concrete feature that
needs them. Revisit this doc when one does.
