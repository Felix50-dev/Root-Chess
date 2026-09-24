package com.example.chess

import android.content.Context
import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.data.repository.ExplanationFacts
import com.example.chess.data.repository.GamePlay
import com.example.chess.data.repository.OpeningBookRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

// JVM tests for the deterministic explanation-facts pipeline. GamePlay is deliberately
// constructed WITHOUT calling init(): explanationFacts must work on isolated replay
// boards with no live-game state (regression test for the old detectCheck path that
// read the lateinit `players` field and crashed here).
class GamePlayExplanationFactsTest {

    private lateinit var game: GamePlay
    private val white = Player(isWhite = true, playerType = PlayerType.HUMAN)
    private val black = Player(isWhite = false, playerType = PlayerType.HUMAN)

    @Before
    fun setup() {
        game = GamePlay(Board(), StockfishEngine(), OpeningBookRepository(Mockito.mock(Context::class.java)))
    }

    private fun emptyBoard(): Board = Board().also { b ->
        for (row in 0 until Board.NUM_ROWS) {
            for (col in 0 until Board.NUM_COLUMNS) {
                b.getBox(row, col).chessPiece = null
            }
        }
    }

    // Scholar's mate: 1.e4 e5 2.Qh5 Nc6 3.Bc4 Nf6 4.Qxf7#
    private val scholarsMate = listOf("e2e4", "e7e5", "d1h5", "b8c6", "f1c4", "g8f6", "h5f7")

    @Test
    fun explanationFacts_worksWithoutInit_andReportsCheck() {
        val facts = game.explanationFacts(scholarsMate, index = 6, hungPieceSquare = null)

        assertTrue(facts.isNotEmpty())
        assertTrue("first fact should describe the move: ${facts[0]}",
            facts[0].contains("White Queen moves from h5 to f7"))
        assertTrue("capture should be reported: ${facts[0]}",
            facts[0].contains("capturing Black Pawn on f7"))
        assertTrue("check should be reported: ${facts[0]}", facts[0].contains("giving check"))
    }

    @Test
    fun checkEscape_squareBehindKingOnCheckingRay_isReportedAttacked() {
        // Black king e4 checked by White rook e1 up the e-file. e5 lies directly behind
        // the king on the checking ray: with the king's own body blocking the ray (the
        // old bug), e5 was wrongly reported safe.
        val board = emptyBoard()
        val king = King(color = Color.BLACK, position = Position(4, 4), isKilled = false)
        board.getBox(4, 4).chessPiece = king
        board.getBox(7, 4).chessPiece = Rook(color = Color.WHITE, position = Position(7, 4), isKilled = false)

        val facts = game.buildCheckEscapeFacts(board, kingOwner = black, attacker = white)

        assertTrue("e5 must be reported attacked, got: $facts",
            facts.contains("King cannot move to e5 (attacked by Rook on e1)"))
        // The temporary king-lift must be undone.
        assertSame(king, board.getBox(4, 4).chessPiece)
    }

    @Test
    fun doubleCheck_emitsDoubleCheckFact() {
        // Black king e4 checked by White rooks on e1 and a4 simultaneously.
        val board = emptyBoard()
        board.getBox(4, 4).chessPiece = King(color = Color.BLACK, position = Position(4, 4), isKilled = false)
        board.getBox(7, 4).chessPiece = Rook(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        board.getBox(4, 0).chessPiece = Rook(color = Color.WHITE, position = Position(4, 0), isKilled = false)

        val facts = game.buildCheckEscapeFacts(board, kingOwner = black, attacker = white)
        val doubleCheckFact = facts.find { it.contains("double check") }

        assertTrue("double check fact expected, got: $facts", doubleCheckFact != null)
        assertTrue(doubleCheckFact!!.contains("Rook on e1"))
        assertTrue(doubleCheckFact.contains("Rook on a4"))
    }

    @Test
    fun enPassantFact_namesActualCapturedPawnSquare() {
        // 1.e4 Nf6 2.e5 d5 3.exd6 e.p. — the captured pawn is on d5, not the landing square d6.
        val moves = listOf("e2e4", "g8f6", "e4e5", "d7d5", "e5d6")
        val facts = game.explanationFacts(moves, index = 4, hungPieceSquare = null)

        assertTrue("expected en passant capture on d5: ${facts[0]}",
            facts[0].contains("capturing Black Pawn on d5 en passant"))
        assertFalse("must not claim the pawn was on d6: ${facts[0]}",
            facts[0].contains("Pawn on d6"))
    }

    private val promotionGame = listOf(
        "a2a4", "b7b5", "a4b5", "b8a6", "b5b6", "a6b4", "b6b7", "b4a6", "b7b8q",
    )

    @Test
    fun promotionFact_mentionsPromotion() {
        val facts = game.explanationFacts(promotionGame, index = 8, hungPieceSquare = null)

        assertTrue("promotion must be described: ${facts[0]}",
            facts[0].contains("promoting to a Queen"))
    }

    @Test
    fun hungPieceFact_defendedPieceNotCalledUndefended() {
        // After 1.d4 the d4 pawn is defended by the queen on d1.
        val facts = game.explanationFacts(listOf("d2d4"), index = 0, hungPieceSquare = "d4")
        val hungFact = facts.find { it.contains("can be captured") }

        assertTrue("hung-piece fact expected, got: $facts", hungFact != null)
        assertEquals("The Pawn on d4 can be captured.", hungFact)
    }

    @Test
    fun hungPieceFact_trulyUndefendedPieceStillCalledUndefended() {
        // After 1.e4 nothing defends the e4 pawn.
        val facts = game.explanationFacts(listOf("e2e4"), index = 0, hungPieceSquare = "e4")

        assertTrue("undefended fact expected, got: $facts",
            facts.contains("The Pawn on e4 is undefended and can be captured."))
    }

    @Test
    fun bestMoveDescription_namesPieceFromPreMoveBoard() {
        val facts = game.explanationFacts(
            listOf("a2a3"), index = 0, hungPieceSquare = null, bestMoveUci = "g1f3",
        )
        val altFact = facts.find { it.startsWith("Stronger alternative (NOT played): White Knight from g1 to f3") }

        assertTrue("stronger-alternative fact expected, got: $facts", altFact != null)
        // The alternative's consequences are verified, not guessed: Nf3 from the start
        // position attacks nothing and cannot be captured.
        assertTrue("safety consequence expected: $altFact",
            altFact!!.contains("it would be safe from capture there"))
    }

    @Test
    fun bestMoveDescription_statesWhenAlternativeGivesCheck() {
        // After 1.e4 f6 White played 2.a3; the alternative Qd1-h5 gives check (not mate —
        // ...g6 blocks).
        val facts = game.explanationFacts(
            listOf("e2e4", "f7f6", "a2a3"), index = 2, hungPieceSquare = null, bestMoveUci = "d1h5",
        )
        val altFact = facts.find { it.startsWith("Stronger alternative") }

        assertTrue("stronger-alternative fact expected, got: $facts", altFact != null)
        assertTrue("check consequence expected: $altFact", altFact!!.contains("it would give check"))
        assertFalse("must not claim mate: $altFact", altFact.contains("checkmate"))
    }

    @Test
    fun bestMoveDescription_statesWhenAlternativeDeliversMate() {
        // Scholar's-mate position, but White played 4.a3 instead of Qxf7# — the missed
        // alternative must be labeled as delivering checkmate.
        val moves = scholarsMate.dropLast(1) + "a2a3"
        val facts = game.explanationFacts(moves, index = 6, hungPieceSquare = null, bestMoveUci = "h5f7")
        val altFact = facts.find { it.startsWith("Stronger alternative") }

        assertTrue("stronger-alternative fact expected, got: $facts", altFact != null)
        assertTrue("capture named: $altFact", altFact!!.contains("capturing Pawn on f7"))
        assertTrue("mate consequence expected: $altFact", altFact.contains("it would deliver checkmate"))
    }

    @Test
    fun describeContinuation_rendersPieceNamesAndCaptures() {
        val quiet = game.describeContinuation(listOf("e2e4"), index = 1, continuation = listOf("g8f6", "b1c3"))
        assertEquals(listOf("Black Knight g8 to f6", "White Knight b1 to c3"), quiet)

        val withCapture = game.describeContinuation(
            listOf("e2e4", "d7d5"), index = 2, continuation = listOf("e4d5", "d8d5"),
        )
        assertEquals(
            listOf("White Pawn e4 takes Pawn on d5", "Black Queen d8 takes Pawn on d5"),
            withCapture,
        )
    }

    @Test
    fun movedPiece_hangingIsNamedWithCapturerAndNoDefender() {
        // 3.Ng5?? walks into hxg5: the fact must name the capturing pawn and state
        // that nothing defends the knight (the QA run showed the LLM never learns
        // WHO captures from a bare "can be captured").
        val moves = listOf("e2e4", "h7h6", "g1f3", "a7a6", "f3g5")
        val facts = game.explanationFacts(moves, index = 4, hungPieceSquare = null)

        assertTrue("safety fact expected, got: $facts",
            facts.contains("The White Knight on g5 can be captured by: Black Pawn on h6. No White piece defends it — it can be taken for free."))
        val attacksFact = facts.find { it.startsWith("The White Knight on g5 now attacks") }
        assertTrue("attacks fact expected, got: $facts", attacksFact != null)
        assertTrue("Ng5 attacks f7: $attacksFact", attacksFact!!.contains("Black Pawn on f7"))
    }

    @Test
    fun movedPiece_defendedPieceListsItsDefenders() {
        // 3.Bd2 blocking Bb4+: capturable, but defended — the facts must say by what,
        // so the LLM stops narrating an even trade as "losing material" (seen in QA).
        val moves = listOf("e2e4", "e7e6", "d2d4", "f8b4", "c1d2")
        val facts = game.explanationFacts(moves, index = 4, hungPieceSquare = null)
        val safetyFact = facts.find { it.startsWith("The White Bishop on d2 can be captured by") }

        assertTrue("safety fact expected, got: $facts", safetyFact != null)
        assertTrue("capturer named: $safetyFact", safetyFact!!.contains("Black Bishop on b4"))
        assertTrue("defenders listed: $safetyFact", safetyFact.contains("It is defended by:"))
        assertTrue("queen defends d2: $safetyFact", safetyFact.contains("White Queen on d1"))
        // Bishop takes bishop: the deterministic exchange verdict must call it a trade,
        // so the LLM stops narrating defended captures as "losing material".
        assertTrue("even-trade verdict expected: $safetyFact",
            safetyFact.contains("A capture would be an even trade"))
    }

    @Test
    fun movedPiece_badTradeVerdict_whenOnlyExpensiveAttacker() {
        // ...Ng4 attacked only by the queen on d1 but defended by the c8-bishop: the QA
        // run showed the LLM concluding "loses a piece" — the fact must state the truth,
        // that Qxg4 would lose the queen for a knight.
        val moves = listOf("e2e4", "d7d5", "d2d4", "g8f6", "b1c3", "f6g4")
        val facts = game.explanationFacts(moves, index = 5, hungPieceSquare = null)
        val safetyFact = facts.find { it.startsWith("The Black Knight on g4 can be captured by") }

        assertTrue("safety fact expected, got: $facts", safetyFact != null)
        assertTrue("bad-trade verdict expected: $safetyFact",
            safetyFact!!.contains("would lose the Queen for the Knight"))
    }

    @Test
    fun checkmate_isStatedAsAFact() {
        // Scholar's mate: 4.Qxf7# must be labeled as checkmate, not just "giving check" —
        // the QA run showed the mating move explained as merely "the strongest continuation".
        val facts = game.explanationFacts(scholarsMate, index = 6, hungPieceSquare = null)

        assertTrue("checkmate fact expected, got: $facts",
            facts.contains("This move delivers checkmate — White wins and the game is over."))
    }

    @Test
    fun nonMatingCheck_hasNoCheckmateFact() {
        // 1.e4 e6 2.d4 Bb4+ is check but easily blocked — no mate fact may appear.
        val moves = listOf("e2e4", "e7e6", "d2d4", "f8b4")
        val facts = game.explanationFacts(moves, index = 3, hungPieceSquare = null)

        assertTrue("check must be reported: ${facts[0]}", facts[0].contains("giving check"))
        assertFalse("no checkmate fact for a blockable check: $facts",
            facts.any { it.contains("delivers checkmate") })
    }

    @Test
    fun rookFact_notEmittedOnQuietPawnMove() {
        // 1.e4 neither moves a rook/king nor changes connectivity — the old unconditional
        // "NOT connected — six pieces stand between them" line was prompt noise.
        val facts = game.explanationFacts(listOf("e2e4"), index = 0, hungPieceSquare = null)

        assertFalse("no rook-connection fact on a quiet pawn move: $facts",
            facts.any { it.contains("rooks") })
    }

    @Test
    fun rookFact_emittedOnCastling() {
        // Castling is exactly where the LLM invents "castling connects the rooks" — the
        // verified state (still blocked by the b1/c1/d1 pieces) must be stated.
        val moves = listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "f8c5", "e1g1")
        val facts = game.explanationFacts(moves, index = 6, hungPieceSquare = null)
        val rookFact = facts.find { it.contains("White's rooks on a1 and f1") }

        assertTrue("rook-connection fact expected on castling, got: $facts", rookFact != null)
        assertTrue("rooks are still blocked: $rookFact", rookFact!!.contains("NOT connected"))
        assertTrue("blocker named: $rookFact", rookFact.contains("White Queen on d1"))
    }

    @Test
    fun movedPiece_defendsFactListsProtectedPieces() {
        // 2.Nf3 defends the d2 and h2 pawns and attacks the e5 pawn.
        val moves = listOf("e2e4", "e7e5", "g1f3")
        val facts = game.explanationFacts(moves, index = 2, hungPieceSquare = null)

        assertTrue("attacks fact expected, got: $facts",
            facts.any { it.startsWith("The White Knight on f3 now attacks") && it.contains("Black Pawn on e5") })
        val defendsFact = facts.find { it.startsWith("The White Knight on f3 defends") }
        assertTrue("defends fact expected, got: $facts", defendsFact != null)
        assertTrue("Nf3 defends d2: $defendsFact", defendsFact!!.contains("White Pawn on d2"))
    }

    @Test
    fun materialBalance_evenAtStart_andReflectsCaptures() {
        val evenFacts = game.explanationFacts(listOf("e2e4"), index = 0, hungPieceSquare = null)
        assertTrue("even balance expected, got: $evenFacts",
            evenFacts.contains("Material balance: even."))

        // Scholar's mate ends with Qxf7#: White is up exactly one pawn.
        val mateFacts = game.explanationFacts(scholarsMate, index = 6, hungPieceSquare = null)
        assertTrue("White +1 expected, got: $mateFacts",
            mateFacts.contains("Material balance: White is ahead by 1 point."))
    }

    @Test
    fun rookConnection_openRankIsConnected() {
        val board = emptyBoard()
        board.getBox(7, 0).chessPiece = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        board.getBox(7, 5).chessPiece = Rook(color = Color.WHITE, position = Position(7, 5), isKilled = false)
        // Enemy rooks elsewhere must not affect White's pair.
        board.getBox(0, 0).chessPiece = Rook(color = Color.BLACK, position = Position(0, 0), isKilled = false)
        board.getBox(0, 7).chessPiece = Rook(color = Color.BLACK, position = Position(0, 7), isKilled = false)

        assertEquals(
            "White's rooks on a1 and f1 are connected (all squares between them are empty).",
            ExplanationFacts.describeRookConnection(board, Color.WHITE),
        )
    }

    @Test
    fun rookConnection_blockedRankNamesTheBlocker() {
        // The reported bug scenario: rooks on the 8th with the queen still between them.
        val board = emptyBoard()
        board.getBox(0, 0).chessPiece = Rook(color = Color.BLACK, position = Position(0, 0), isKilled = false)
        board.getBox(0, 7).chessPiece = Rook(color = Color.BLACK, position = Position(0, 7), isKilled = false)
        board.getBox(0, 3).chessPiece = Queen(color = Color.BLACK, position = Position(0, 3), isKilled = false)

        assertEquals(
            "Black's rooks on a8 and h8 are NOT connected — the Black Queen on d8 stands between them.",
            ExplanationFacts.describeRookConnection(board, Color.BLACK),
        )
    }

    @Test
    fun rookConnection_differentRankAndFile_notConnected() {
        val board = emptyBoard()
        board.getBox(7, 0).chessPiece = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        board.getBox(3, 7).chessPiece = Rook(color = Color.WHITE, position = Position(3, 7), isKilled = false)

        assertEquals(
            "White's rooks on a1 and h5 are not connected (not on the same rank or file).",
            ExplanationFacts.describeRookConnection(board, Color.WHITE),
        )
    }

    @Test
    fun rookConnection_zeroOneOrThreeRooks_emitsNothing() {
        val oneRook = emptyBoard()
        oneRook.getBox(7, 0).chessPiece = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        assertEquals(null, ExplanationFacts.describeRookConnection(oneRook, Color.WHITE))
        assertEquals(null, ExplanationFacts.describeRookConnection(emptyBoard(), Color.WHITE))

        val threeRooks = emptyBoard()
        threeRooks.getBox(7, 0).chessPiece = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        threeRooks.getBox(7, 3).chessPiece = Rook(color = Color.WHITE, position = Position(7, 3), isKilled = false)
        threeRooks.getBox(7, 7).chessPiece = Rook(color = Color.WHITE, position = Position(7, 7), isKilled = false)
        assertEquals(null, ExplanationFacts.describeRookConnection(threeRooks, Color.WHITE))
    }

    @Test
    fun promotionFlow_untouchedByAnalysisReplay() {
        val sentinel = Queen(color = Color.WHITE, position = Position(0, 0), isKilled = false)
        game._promotionPieceFlow.value = sentinel

        game.explanationFacts(promotionGame, index = 8, hungPieceSquare = null)

        assertSame("replaying a promotion must not clobber the live promotion flow",
            sentinel, game._promotionPieceFlow.value)
    }
}
