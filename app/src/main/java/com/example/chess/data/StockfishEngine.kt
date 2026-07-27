package com.example.chess.data

import android.util.Log
import com.example.chess.domain.model.EvalParseState
import com.example.chess.domain.model.PositionEval
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

private const val STOCKFISH_READ_TIMEOUT_MS = 5000L

class StockfishEngine {
    private var process: Process? = null
    private var outputStream: BufferedWriter? = null

    // True once startStockfish() has successfully spawned the engine process. False if the
    // process was never started, failed to start (e.g. missing native binary for this
    // device's ABI — startStockfish() swallows that as an IOException), or was stopped.
    val isRunning: Boolean
        get() = process != null

    // A single reader for the lifetime of the process, created once in startStockfish()
    // and reused by every getBestMove()/getDetailedEval() call. A fresh BufferedReader
    // per call would silently lose whatever it had already buffered-but-not-yet-returned
    // from process.inputStream when discarded — corrupting or hanging the next read.
    // internal (not private) so tests can inject a fake reader without a real engine process.
    internal var reader: BufferedReader? = null

    // Read loops run here, detached from the calling coroutine. withTimeout/withTimeoutOrNull
    // cannot interrupt a blocking BufferedReader.readLine() call — coroutine cancellation is
    // cooperative and a plain blocking read never checks for it — so a read job started on this
    // scope keeps running (and is abandoned) even after the caller times out and returns.
    private var ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _bestMoveFlow = MutableStateFlow("")
    val bestMoveFlow: StateFlow<String> = _bestMoveFlow.asStateFlow()


    fun startStockfish(stockfishPath: String) {
        val stockfishFile = File(stockfishPath)
        try {
            process = Runtime.getRuntime().exec(stockfishFile.absolutePath)
            Log.d("Stockfish", "Stockfish process started successfully")
            outputStream = BufferedWriter(OutputStreamWriter(process?.outputStream))
            reader = process?.inputStream?.let { BufferedReader(InputStreamReader(it)) }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Stockfish", "Error starting Stockfish: ${e.message}")
        }
    }

    suspend fun getBestMove(): String {
        val activeReader = reader ?: return ""
        var bestMove = ""

        awaitReadOrAbandon(activeReader) {
            var line: String?
            while (activeReader.readLine().also { line = it } != null) {
                Log.d("Stockfish", "Stockfish Output: $line")
                if (line!!.startsWith("bestmove")) {
                    bestMove = line!!.split(" ")[1]
                    break
                }
            }
        }

        return bestMove
    }

    suspend fun getDetailedEval(): PositionEval {
        val activeReader = reader ?: return PositionEval(0, "", 0, emptyList(), 0)
        val state = EvalParseState()

        awaitReadOrAbandon(activeReader) {
            var line: String?
            while (activeReader.readLine().also { line = it } != null) {
                parseLine(line!!, state)       // ← parse as it streams, no list
                if (state.done) break          // ← stop immediately on bestmove
            }
        }

        return PositionEval(
            state.score, state.bestMove, state.mateIn, state.pv, state.depth,
            state.secondBestScore, state.secondBestMove,
        )
    }

    // Runs `readLoop` (a blocking readLine() loop) on `ioScope` and waits up to
    // STOCKFISH_READ_TIMEOUT_MS for it. If it doesn't finish in time, we give up waiting —
    // withTimeoutOrNull only stops US, it doesn't stop the still-running read job, since a
    // blocking readLine() call never reaches a cancellation checkpoint. We still try to tear
    // the connection down so the abandoned job eventually unblocks: destroying the process is
    // the reliable path for a real engine (kills the pipe from the other end, so the pending
    // read gets EOF); closing our own reader is best-effort only, and MUST happen off this
    // thread — verified empirically that BufferedReader/InputStreamReader.close() shares a lock
    // with read(), so calling it here while the abandoned job is still parked inside readLine()
    // would itself block this thread until that read unblocks, i.e. forever. Either way, this
    // function returns on time; the abandoned job cleans itself up whenever (if ever) it does.
    private suspend fun awaitReadOrAbandon(activeReader: BufferedReader, readLoop: () -> Unit) {
        val readJob = ioScope.launch {
            try {
                readLoop()
            } catch (e: IOException) {
                Log.e("Stockfish", "Stockfish read IO error: ${e.message}", e)
            }
        }

        val completed = withTimeoutOrNull(STOCKFISH_READ_TIMEOUT_MS) { readJob.join() } != null
        if (!completed) {
            Log.e(
                "Stockfish",
                "Stockfish read timed out after ${STOCKFISH_READ_TIMEOUT_MS}ms — engine appears wedged, tearing down connection"
            )
            process?.destroy()
            reader = null
            ioScope.launch { runCatching { activeReader.close() } }
            readJob.cancel()
        }
    }

    private fun parsePvTokens(pv: String): List<String> = pv
        .trim()
        .split(" ")
        .filter { token ->
            token.length in 4..5
                    && token[0] in 'a'..'h'
                    && token[1] in '1'..'8'
                    && token[2] in 'a'..'h'
                    && token[3] in '1'..'8'
        }
        .take(4)

    fun parseLine(line: String, state: EvalParseState) {
        if (line.startsWith("info depth")) {
            // Absent "multipv N" means the engine wasn't asked for extra lines — treat
            // that as line 1 so single-PV callers/tests keep working unchanged.
            val multipv = Regex("multipv (\\d+)").find(line)?.groupValues?.get(1)?.toIntOrNull() ?: 1
            when (multipv) {
                1 -> {
                    Regex("depth (\\d+)").find(line)?.let {
                        state.depth = it.groupValues[1].toInt()
                    }
                    Regex("score cp (-?\\d+)").find(line)?.let {
                        state.score = it.groupValues[1].toInt()
                    }
                    Regex("score mate (-?\\d+)").find(line)?.let {
                        state.mateIn = it.groupValues[1].toInt()
                    }
                    Regex("pv (.+)$").find(line)?.let {
                        state.pv = parsePvTokens(it.groupValues[1])
                    }
                }
                2 -> {
                    Regex("score cp (-?\\d+)").find(line)?.let {
                        state.secondBestScore = it.groupValues[1].toInt()
                    }
                    Regex("pv (.+)$").find(line)?.let {
                        state.secondBestMove = parsePvTokens(it.groupValues[1]).firstOrNull()
                    }
                }
                // multipv >= 3 ignored — only the top 2 lines are requested/used.
            }
        }
        if (line.startsWith("bestmove")) {
            state.bestMove = line.split(" ")[1]
            state.done     = true
        }
    }

    // Convenience for tests — feed a list of lines
    fun parseStockfishOutput(lines: List<String>): PositionEval {
        val state = EvalParseState()
        for (line in lines) {
            parseLine(line, state)
            if (state.done) break
        }
        return PositionEval(
            state.score, state.bestMove, state.mateIn, state.pv, state.depth,
            state.secondBestScore, state.secondBestMove,
        )
    }

    fun sendCommand(command: String) {
        outputStream?.write("$command\n")
        outputStream?.flush()
    }

    fun setDifficultyLevel(level: Int) {
        // Send UCI commands to set difficulty (like "setoption name Skill Level value X")
        sendCommand("setoption name Skill Level value $level")
        Log.d(TAG, "setDifficultyLevel: $level")
    }

    fun stopStockfish() {
        sendCommand("quit")
        process?.destroy()
        try {
            reader?.close()
        } catch (e: IOException) {
            Log.e("Stockfish", "Error closing reader: ${e.message}")
        }
        reader = null
        ioScope.cancel()
        ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}

private const val TAG = "StockfishEngine"
