package com.example.chess.domain.model

/**
 * A tactic found by TacticsRecognizer.
 *
 * [name] is one of the six exact strings consumers switch on:
 * "Pin", "Fork", "Skewer", "Discovered Check", "Discovered Attack", "Double Check".
 *
 * [description] is a fully grounded sentence naming colors, piece types and algebraic
 * squares, safe to inject verbatim into the LLM Facts block — the model may only
 * describe tactic geometry that is stated here.
 */
data class DetectedTactic(
    val name: String,
    val description: String,
)
