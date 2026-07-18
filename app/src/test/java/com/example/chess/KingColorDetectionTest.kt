package com.example.chess

import com.example.chess.data.model.Color
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * ChessGameViewModel.getKingPosition() used to compare the whole `currentTurn` Player
 * against a hard-coded `playerWhite` template (`Player(isWhite=true, playerType=HUMAN)`).
 * Since Player is a data class, `==` is structural and includes playerType — so whenever
 * White was actually played by the AI (`Player(isWhite=true, playerType=AI)`), the
 * comparison was false and the check indicator was attributed to the wrong king.
 *
 * The fix compares only `.isWhite`. These tests exercise that exact expression — the
 * ViewModel itself can't be constructed in a unit test (its init block starts a real
 * Stockfish subprocess and needs SoundPool/Context), so this isolates the corrected
 * logic using the real Player/PlayerType types, mirroring this codebase's existing
 * VisualPieceAnimationTest pattern for ViewModel-adjacent logic.
 */
class KingColorDetectionTest {

    private val playerWhiteTemplate = Player(isWhite = true, playerType = PlayerType.HUMAN)
    private val playerDarkTemplate = Player(isWhite = false, playerType = PlayerType.HUMAN)

    // The buggy expression, kept verbatim to document what was wrong.
    private fun oldBuggyColorOf(currentTurn: Player?): Color =
        if (currentTurn == playerWhiteTemplate) Color.WHITE else Color.BLACK

    // The fixed expression, exactly as it now appears in getKingPosition().
    private fun fixedColorOf(currentTurn: Player?): Color =
        if (currentTurn?.isWhite == true) Color.WHITE else Color.BLACK

    @Test
    fun oldBug_aiPlayingWhite_wasMisidentifiedAsBlack() {
        val aiPlayingWhite = Player(isWhite = true, playerType = PlayerType.AI)

        assertEquals(
            "CONFIRMED BUG: structural Player comparison misidentifies an AI playing White as Black",
            Color.BLACK,
            oldBuggyColorOf(aiPlayingWhite)
        )
    }

    @Test
    fun fix_aiPlayingWhite_isCorrectlyIdentifiedAsWhite() {
        val aiPlayingWhite = Player(isWhite = true, playerType = PlayerType.AI)
        assertEquals(Color.WHITE, fixedColorOf(aiPlayingWhite))
    }

    @Test
    fun fix_humanPlayingWhite_isCorrectlyIdentifiedAsWhite() {
        val humanPlayingWhite = Player(isWhite = true, playerType = PlayerType.HUMAN)
        assertEquals(Color.WHITE, fixedColorOf(humanPlayingWhite))
    }

    @Test
    fun fix_anyMoverPlayingBlack_isCorrectlyIdentifiedAsBlackRegardlessOfPlayerType() {
        val aiPlayingBlack = Player(isWhite = false, playerType = PlayerType.AI)
        val humanPlayingBlack = Player(isWhite = false, playerType = PlayerType.HUMAN)
        assertEquals(Color.BLACK, fixedColorOf(aiPlayingBlack))
        assertEquals(Color.BLACK, fixedColorOf(humanPlayingBlack))
    }

    @Test
    fun fix_nullCurrentTurn_fallsBackToBlackWithoutCrashing() {
        assertEquals(Color.BLACK, fixedColorOf(null))
    }

    @Test
    fun sanityCheck_theseTemplatesAreWhatMadeTheOldBugPossible() {
        // playerWhiteTemplate/playerDarkTemplate are never reassigned in the ViewModel —
        // only .copy()'d — so playerType is permanently stuck at HUMAN.
        assertEquals(PlayerType.HUMAN, playerWhiteTemplate.playerType)
        assertEquals(PlayerType.HUMAN, playerDarkTemplate.playerType)
    }
}
