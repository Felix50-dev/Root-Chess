package com.example.chess.data.model

// The full roadmap roster. List order IS unlock order (see RoadmapProgressRepository.
// deriveNodeStates) — a bot is "Current" when it's the first entry here with no recorded
// BotProgressEntity, so reordering this list changes the unlock sequence.
//
// skillLevel/moveTimeMs below are a starting calibration, not a derived fact: Stockfish's
// "Skill Level" has no official Elo mapping and is least reliable at the low end (see
// StockfishEngine.setDifficultyLevel — only "Skill Level" is wired up, not UCI_LimitStrength/
// UCI_Elo). These need real playtesting against each bot's stated Elo before shipping.
object BotCatalog {
    val bots: List<BotProfile> = listOf(
        BotProfile("pip", "Pip", 450, "Still learning how pieces move.", RoadmapWorld.FIRST_MOVES, isBoss = false, skillLevel = 0, moveTimeMs = 150),
        BotProfile("oli", "Oli", 750, "Eager, but forgets to defend.", RoadmapWorld.FIRST_MOVES, isBoss = true, skillLevel = 1, moveTimeMs = 250),

        BotProfile("dana", "Dana", 950, "Plays it safe, trades pieces early.", RoadmapWorld.CLUB_STEPS, isBoss = false, skillLevel = 2, moveTimeMs = 300),
        BotProfile("theo", "Theo", 1150, "Always hunting for a quick checkmate.", RoadmapWorld.CLUB_STEPS, isBoss = true, skillLevel = 4, moveTimeMs = 400),

        BotProfile("mara", "Mara", 1350, "Sharp in the opening, shaky in the endgame.", RoadmapWorld.RISING_PLAYER, isBoss = false, skillLevel = 5, moveTimeMs = 450),
        BotProfile("benno", "Benno", 1550, "Builds slow, overwhelming positions.", RoadmapWorld.RISING_PLAYER, isBoss = true, skillLevel = 7, moveTimeMs = 600),

        BotProfile("ines", "Ines", 1800, "Aggressive attacker, sacrifices for initiative.", RoadmapWorld.STRONG_CLUB, isBoss = false, skillLevel = 8, moveTimeMs = 700),
        BotProfile("soren", "Soren", 1950, "Grinds positionally into favorable endgames.", RoadmapWorld.STRONG_CLUB, isBoss = true, skillLevel = 10, moveTimeMs = 900),

        BotProfile("yusuf", "Yusuf", 2100, "Deep opening prep, punishes imprecision.", RoadmapWorld.EXPERT, isBoss = false, skillLevel = 11, moveTimeMs = 1000),
        BotProfile("nadia", "Nadia", 2200, "Calculates sharp tactics flawlessly.", RoadmapWorld.EXPERT, isBoss = true, skillLevel = 14, moveTimeMs = 1200),

        BotProfile("vera", "Vera", 2300, "Relentless technique, gives no second chances.", RoadmapWorld.MASTER, isBoss = false, skillLevel = 15, moveTimeMs = 1400),
        BotProfile("karel", "Karel", 2400, "A wall — almost impossible to crack open.", RoadmapWorld.MASTER, isBoss = true, skillLevel = 17, moveTimeMs = 1800),

        BotProfile("grandmaster", "The Grandmaster", 2600, "Full strength. Good luck.", RoadmapWorld.GRANDMASTER, isBoss = true, skillLevel = 20, moveTimeMs = 3000),
    )
}
