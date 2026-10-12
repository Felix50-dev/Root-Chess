package com.example.chess

import com.example.chess.data.remote.GroqApi
import com.example.chess.data.remote.GroqApiException
import com.example.chess.data.repository.MoveExplainer
import com.example.chess.data.repository.MoveExplanationValidationException
import com.example.chess.domain.model.DetectedTactic
import com.example.chess.domain.model.MoveExplanationRequest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import java.util.Locale

class MoveExplainerTest {

    private lateinit var groqApi: GroqApi
    private lateinit var explainer: MoveExplainer
    private val defaultLocale: Locale = Locale.getDefault()

    @Before
    fun setup() {
        groqApi = Mockito.mock(GroqApi::class.java)
        // A bare Context mock is fine: the TEMP-CAPTURE file writer swallows all failures.
        explainer = MoveExplainer(groqApi, Mockito.mock(android.content.Context::class.java))
    }

    @After
    fun restoreLocale() {
        Locale.setDefault(defaultLocale)
    }

    private fun request(
        type: String = "Blunder",
        bestMove: String? = "g1f3",
        evalDelta: Float = -3.5f,
        mateIn: Int? = null,
        allowedMateIn: Int? = null,
        tactic: List<DetectedTactic>? = null,
    ) = MoveExplanationRequest(
        type = type,
        movePlayed = "a2a3",
        bestMove = bestMove,
        chessPiece = null,
        evalDelta = evalDelta,
        tactic = tactic,
        mateIn = mateIn,
        matingPiece = null,
        allowedMateIn = allowedMateIn,
        facts = listOf("Move played: White Pawn moves from a2 to a3"),
        playerElo = 1200,
        moveNumber = 1,
        playerColor = "White",
    )

    @Test
    fun blankResponse_throws_andIsNotCached() = runBlocking<Unit> {
        Mockito.`when`(groqApi.complete(anyString())).thenReturn("", "a real explanation")

        try {
            explainer.explain(request())
            fail("blank explanation should throw")
        } catch (expected: IllegalStateException) {
        }

        // The blank must not have been cached — the retry reaches the API and succeeds.
        assertEquals("a real explanation", explainer.explain(request()))
    }

    // ArgumentCaptor.capture() returns null, which trips Kotlin's non-null check on
    // complete(prompt: String) — record the prompt from inside the stub instead.
    private fun capturePrompt(): () -> String {
        var lastPrompt: String? = null
        runBlocking {
            Mockito.`when`(groqApi.complete(anyString())).thenAnswer { invocation ->
                lastPrompt = invocation.getArgument(0)
                "ok"
            }
        }
        return { lastPrompt ?: fail("groqApi.complete was never called").let { "" } }
    }

    @Test
    fun prompt_neverQuotesRawEvalNumbers() = runBlocking<Unit> {
        val prompt = capturePrompt()

        explainer.explain(request(evalDelta = -3.5f))

        // The QA run showed raw deltas being narrated as "lost half a pawn of material" —
        // the number line is gone entirely; severity reaches the model only as words.
        assertFalse("no raw eval line: ${prompt()}", prompt().contains("Eval change"))
        assertFalse("no numeric pawn delta: ${prompt()}",
            Regex("""-?\d+[.,]\d+ pawns""").containsMatchIn(prompt()))
        assertTrue("severity still present in words: ${prompt()}",
            prompt().contains("a major blunder losing significant material"))
        assertTrue("words-only rule present: ${prompt()}",
            prompt().contains("describe move quality in plain words only"))
        assertTrue("checkmate-lead rule present: ${prompt()}",
            prompt().contains("delivers checkmate, lead with that"))
    }

    @Test
    fun missedMate_neverDescribedAsReasonable() = runBlocking<Unit> {
        val prompt = capturePrompt()

        // calculateLoss ignores mate scores, so a missed mate arrives with evalDelta ≈ 0.
        explainer.explain(request(type = "MissedMate", evalDelta = 0f, mateIn = 2))

        assertTrue("missed mate must be called out: ${prompt()}",
            prompt().contains("threw away a forced checkmate in 2"))
        assertFalse("missed mate must not read as reasonable: ${prompt()}",
            prompt().contains("a reasonable move"))
    }

    @Test
    fun prompt_forbidsDerivedPositionalAndMaterialClaims() = runBlocking<Unit> {
        val prompt = capturePrompt()

        explainer.explain(request())

        // The grounding rules added after the QA run: no self-derived geometry, no
        // defense/connection claims outside the Facts, no own-piece interactions, and
        // no material stories beyond what the Facts state.
        assertTrue("geometry rule missing: ${prompt()}",
            prompt().contains("never work out square-to-square geometry yourself"))
        assertTrue("connected-rooks rule missing: ${prompt()}",
            prompt().contains("connected rooks"))
        assertTrue("own-pieces rule missing: ${prompt()}",
            prompt().contains("A side never attacks, pressures, or wins material from its own pieces"))
        assertTrue("material rule missing: ${prompt()}",
            prompt().contains("a piece that can be captured but is defended is a trade, not a loss"))
    }

    @Test
    fun benefitOfAlternative_statedInWordsPerSeverityBand() = runBlocking<Unit> {
        val prompt = capturePrompt()

        explainer.explain(request(evalDelta = -3.5f))
        assertTrue("big loss → winning-position wording: ${prompt()}",
            prompt().contains("The alternative would have won material or given a winning position."))

        explainer.explain(request(evalDelta = -1.5f))
        assertTrue("medium loss → clearly better: ${prompt()}",
            prompt().contains("The alternative was clearly better."))

        explainer.explain(request(evalDelta = -0.4f))
        assertTrue("small loss → only slightly better: ${prompt()}",
            prompt().contains("The alternative was only slightly better."))
    }

    @Test
    fun allowedMate_contextLinePresentOnlyWhenSet() = runBlocking<Unit> {
        val prompt = capturePrompt()

        explainer.explain(request(allowedMateIn = 1))
        assertTrue("allowed-mate context expected: ${prompt()}",
            prompt().contains("This move allows the opponent a forced checkmate in 1"))
        assertTrue("prevention claim expected: ${prompt()}",
            prompt().contains("the stronger alternative would have prevented that forced mate"))
        // The mate stakes replace the generic benefit wording.
        assertFalse("no generic benefit line alongside mate context: ${prompt()}",
            prompt().contains("The alternative would have won material"))

        explainer.explain(request(evalDelta = -0.4f))
        assertFalse("no allowed-mate context by default: ${prompt()}",
            prompt().contains("allows the opponent a forced checkmate"))
    }

    @Test
    fun tactic_renderedAsGroundedFact_notFreeFloatingLine() = runBlocking<Unit> {
        val prompt = capturePrompt()
        val pin = DetectedTactic(
            name = "Pin",
            description = "The White Bishop on e6 pins the Black Knight on d5 against the Black King on b3."
        )

        explainer.explain(request(tactic = listOf(pin)))

        // The tactic must live INSIDE the Facts block (the rules only allow the model
        // to reference pieces/squares stated in the Facts) …
        assertTrue("tactic fact expected: ${prompt()}",
            prompt().contains("- Tactic: The White Bishop on e6 pins the Black Knight on d5 against the Black King on b3."))
        // … and the old ungrounded one-liner must be gone.
        assertFalse("no free-floating tactic line: ${prompt()}",
            prompt().contains("Tactic detected:"))
        assertTrue("tactic-grounding rule present: ${prompt()}",
            prompt().contains("never re-derive or extend its geometry"))
    }

    @Test
    fun cache_distinguishesRequestsThatDifferOnlyInBestMove() = runBlocking<Unit> {
        Mockito.`when`(groqApi.complete(anyString())).thenReturn("one", "two")

        assertEquals("one", explainer.explain(request(bestMove = "g1f3")))
        assertEquals("two", explainer.explain(request(bestMove = "d2d4")))
        // Identical request hits the cache — no third API call.
        assertEquals("one", explainer.explain(request(bestMove = "g1f3")))

        Mockito.verify(groqApi, Mockito.times(2)).complete(anyString())
    }

    private fun stubResponse(text: String) {
        runBlocking { Mockito.`when`(groqApi.complete(anyString())).thenReturn(text) }
    }

    @Test
    fun bannedPhrase_throwsValidationException_andIsNotCached() = runBlocking<Unit> {
        Mockito.`when`(groqApi.complete(anyString())).thenReturn(
            "This looks fine, but the engine prefers a different plan.",
            "Moving the pawn from a2 to a3 gains a little space.",
        )

        try {
            explainer.explain(request())
            fail("banned phrase should throw")
        } catch (expected: MoveExplanationValidationException) {
        }

        // Not cached — the retry reaches the API again and this clean response succeeds.
        assertEquals("Moving the pawn from a2 to a3 gains a little space.", explainer.explain(request()))
    }

    @Test
    fun numericEvalLeak_throwsValidationException() = runBlocking<Unit> {
        stubResponse("This move is worth about +2.3 in the position.")

        try {
            explainer.explain(request())
            fail("numeric eval leak should throw")
        } catch (expected: MoveExplanationValidationException) {
        }
    }

    @Test
    fun ungroundedSquare_throwsValidationException() = runBlocking<Unit> {
        // Default request's Facts/movePlayed/bestMove only ground a2, a3, g1, f3 — e5 is
        // never mentioned anywhere in this request.
        stubResponse("The knight on e5 controls the center.")

        try {
            explainer.explain(request())
            fail("ungrounded square should throw")
        } catch (expected: MoveExplanationValidationException) {
        }
    }

    @Test
    fun groundedResponse_passesAndIsCached() = runBlocking<Unit> {
        stubResponse("Moving the pawn from a2 to a3 gains a little space.")

        val result = explainer.explain(request())
        assertEquals("Moving the pawn from a2 to a3 gains a little space.", result)

        // Second identical request hits the cache — no second API call.
        explainer.explain(request())
        Mockito.verify(groqApi, Mockito.times(1)).complete(anyString())
    }

    @Test
    fun squareGroundedOnlyViaTacticDescription_passesValidation() = runBlocking<Unit> {
        val pin = DetectedTactic(
            name = "Pin",
            description = "The White Bishop on e6 pins the Black Knight on d5 against the Black King on b3."
        )
        // d5 appears only inside the tactic's description, not in facts/movePlayed/bestMove.
        stubResponse("This pins the knight on d5 in place.")

        val result = explainer.explain(request(tactic = listOf(pin)))
        assertEquals("This pins the knight on d5 in place.", result)
    }

    // Statically provable (MoveExplanationValidationException and GroqApiException share no
    // inheritance relationship) — the compiler's "always false" warning IS the point: it
    // confirms this contract holds at the type level, not just at runtime. Kept as an
    // explicit, named regression test rather than relying on readers to notice the type
    // hierarchy — callers check `e is GroqApiException && e.statusCode == 429` to trip their
    // disable/cooldown flags (ChessGameViewModel, ChatViewModel), and a content rejection
    // must never be mistaken for that.
    @Suppress("USELESS_IS_CHECK")
    @Test
    fun validationFailure_isNeverAGroqApiException() = runBlocking<Unit> {
        stubResponse("The knight on e5 controls the center.")

        try {
            explainer.explain(request())
            fail("ungrounded square should throw")
        } catch (e: MoveExplanationValidationException) {
            assertFalse(e is GroqApiException)
        }
    }

    // Documents a known, deliberate limitation rather than a passing guarantee: the
    // validator only checks banned phrases and square grounding, so a response that stays
    // within the grounded vocabulary but mischaracterizes what a Fact actually says (the
    // real "even trade" vs. "White can win material" example found via manual audit) is
    // NOT caught here. A future reader should not assume this validator is a semantic
    // fact-checker.
    @Test
    fun knownGap_semanticMisstatementOfGroundedFacts_passesValidation() = runBlocking<Unit> {
        stubResponse(
            "Trading on a3 is an even trade, so nothing is won or lost here."
        )

        // Does not throw, even though this may misstate the real material consequences —
        // both squares mentioned (a3) are grounded, which is all this validator checks.
        val result = explainer.explain(request())
        assertEquals("Trading on a3 is an even trade, so nothing is won or lost here.", result)
    }
}
