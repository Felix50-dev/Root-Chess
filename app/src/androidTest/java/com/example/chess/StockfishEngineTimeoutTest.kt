package com.example.chess

import com.example.chess.data.StockfishEngine
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PipedInputStream
import java.io.PipedOutputStream

/**
 * Covers the reader-persistence and timeout fixes to StockfishEngine.getBestMove()/
 * getDetailedEval(): previously a fresh BufferedReader was created on every call (silently
 * losing any buffered-but-unread output from the previous call) and there was no timeout at
 * all — a wedged engine hung the caller forever with no error surfaced. Uses a
 * PipedInputStream/PipedOutputStream pair instead of a real Stockfish process, so these run
 * without needing the native binary — fully portable, deterministic, no subprocess.
 */
class StockfishEngineTimeoutTest {

    // ── No reader yet (engine never started, or start failed) ──────────────

    @Test
    fun getBestMove_withNoReader_returnsEmptyStringImmediately() = runBlocking {
        val engine = StockfishEngine()
        assertEquals("", engine.getBestMove())
    }

    @Test
    fun getDetailedEval_withNoReader_returnsZeroedEvalImmediately() = runBlocking {
        val engine = StockfishEngine()
        val result = engine.getDetailedEval()
        assertEquals(0, result.score)
        assertEquals("", result.bestMove)
        assertEquals(0, result.mateIn)
        assertTrue(result.principalVariation.isEmpty())
    }

    // ── Wedged engine (never emits "bestmove") — must time out, not hang ───

    @Test
    fun getBestMove_timesOut_ratherThanHangingForever_whenEngineNeverResponds() = runBlocking {
        val engine = StockfishEngine()
        val pipedOutput = PipedOutputStream()
        val pipedInput = PipedInputStream(pipedOutput)
        engine.reader = BufferedReader(InputStreamReader(pipedInput))
        // Nothing is ever written to pipedOutput — simulates a wedged engine process.

        val start = System.currentTimeMillis()
        val result = engine.getBestMove()
        val elapsed = System.currentTimeMillis() - start

        assertEquals("Timed-out call should fall back to an empty move", "", result)
        assertTrue(
            "Should time out well under 10s (configured timeout is 5s), not hang forever",
            elapsed < 10_000
        )
    }

    @Test
    fun getDetailedEval_timesOut_ratherThanHangingForever_whenEngineNeverResponds() = runBlocking {
        val engine = StockfishEngine()
        val pipedOutput = PipedOutputStream()
        val pipedInput = PipedInputStream(pipedOutput)
        engine.reader = BufferedReader(InputStreamReader(pipedInput))

        val start = System.currentTimeMillis()
        val result = engine.getDetailedEval()
        val elapsed = System.currentTimeMillis() - start

        assertEquals("", result.bestMove)
        assertTrue(
            "Should time out well under 10s (configured timeout is 5s), not hang forever",
            elapsed < 10_000
        )
    }

    // ── Healthy read still works correctly with the persistent reader ──────

    @Test
    fun getBestMove_readsNormally_whenEngineRespondsPromptly() = runBlocking {
        val engine = StockfishEngine()
        val pipedOutput = PipedOutputStream()
        val pipedInput = PipedInputStream(pipedOutput)
        engine.reader = BufferedReader(InputStreamReader(pipedInput))

        val writer = pipedOutput.bufferedWriter()
        writer.write("info depth 10 score cp 42 pv e2e4 e7e5\n")
        writer.write("bestmove e2e4 ponder e7e5\n")
        writer.flush()

        val result = engine.getBestMove()
        assertEquals("e2e4", result)
    }

    // ── Two calls in a row don't lose data — proves the reader is reused ───

    @Test
    fun consecutiveCalls_shareThePersistentReader_andBothSucceed() = runBlocking {
        val engine = StockfishEngine()
        val pipedOutput = PipedOutputStream()
        val pipedInput = PipedInputStream(pipedOutput)
        engine.reader = BufferedReader(InputStreamReader(pipedInput))
        val writer = pipedOutput.bufferedWriter()

        writer.write("info depth 5 score cp 10 pv e2e4\n")
        writer.write("bestmove e2e4\n")
        writer.flush()
        assertEquals("e2e4", engine.getBestMove())

        writer.write("info depth 6 score cp 20 pv d2d4\n")
        writer.write("bestmove d2d4\n")
        writer.flush()
        assertEquals(
            "A second call using the SAME reader must still read correctly",
            "d2d4",
            engine.getBestMove()
        )
    }
}
