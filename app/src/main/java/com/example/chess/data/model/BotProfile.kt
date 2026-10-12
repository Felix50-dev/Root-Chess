package com.example.chess.data.model

// Rating-anchored "world" a bot belongs to on the progression roadmap. Order here is
// display order; unlock order is BotCatalog.bots' list order, not this enum's.
enum class RoadmapWorld(val displayName: String, val eloRangeLabel: String) {
    FIRST_MOVES("First Moves", "400–800"),
    CLUB_STEPS("Club Steps", "800–1200"),
    RISING_PLAYER("Rising Player", "1200–1600"),
    STRONG_CLUB("Strong Club", "1600–2000"),
    EXPERT("Expert", "2000–2200"),
    MASTER("Master", "2200–2400"),
    GRANDMASTER("Grandmaster", "2500+"),
}

// A single named roadmap opponent. Replaces AILevel — skillLevel/moveTimeMs play the
// exact same role (fed straight to StockfishEngine.setDifficultyLevel()/"go movetime"),
// just with many tiers instead of two, plus the identity/flavor needed to render a node.
data class BotProfile(
    val id: String,
    val name: String,
    val elo: Int,
    val flavorText: String,
    val world: RoadmapWorld,
    val isBoss: Boolean,
    val skillLevel: Int,
    val moveTimeMs: Int,
)
