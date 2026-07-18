package com.example.chess.data

import android.util.Log
import com.example.chess.domain.model.EvalParseState
import com.example.chess.domain.model.PositionEval
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
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

    // A single reader for the lifetime of the process, created once in startStockfish()
    // and reused by every getBestMove()/getDetailedEval() call. A fresh BufferedReader
    // per call would silently lose whatever it had already buffered-but-not-yet-returned
    // from process.inputStream when discarded — corrupting or hanging the next read.
    // internal (not private) so tests can inject a fake reader without a real engine process.
    internal var reader: BufferedReader? = null

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

        return try {
            withTimeout(STOCKFISH_READ_TIMEOUT_MS) {
                withContext(Dispatchers.IO) {
                    var line: String?
                    var evaluation: Int?
                    var bestMove = ""
                    while (activeReader.readLine().also { line = it } != null) {
                        Log.d("Stockfish", "Stockfish Output: $line")

                        // Extract evaluation score
                        if (line!!.startsWith("info depth")) {
                            val match = Regex("score cp (-?\\d+)").find(line!!)
                            if (match != null) {
                                evaluation = match.groupValues[1].toInt()
                                Log.d("Stockfish", "Evaluation: $evaluation")
                            }
                        }

                        if (line!!.startsWith("bestmove")) {
                            bestMove = line!!.split(" ")[1]
                            break
                        }
                    }
                    bestMove
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(
                "Stockfish",
                "getBestMove timed out after ${STOCKFISH_READ_TIMEOUT_MS}ms — engine may be wedged"
            )
            ""
        } catch (e: IOException) {
            Log.e("Stockfish", "getBestMove IO error: ${e.message}", e)
            ""
        }
    }

    suspend fun getDetailedEval(): PositionEval {
        val activeReader = reader ?: return PositionEval(0, "", 0, emptyList(), 0)
        val state = EvalParseState()

        try {
            withTimeout(STOCKFISH_READ_TIMEOUT_MS) {
                withContext(Dispatchers.IO) {
                    var line: String?
                    while (activeReader.readLine().also { line = it } != null) {
                        parseLine(line!!, state)       // ← parse as it streams, no list
                        if (state.done) break          // ← stop immediately on bestmove
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(
                "Stockfish",
                "getDetailedEval timed out after ${STOCKFISH_READ_TIMEOUT_MS}ms — engine may be wedged"
            )
        } catch (e: IOException) {
            Log.e("Stockfish", "getDetailedEval IO error: ${e.message}", e)
        }

        return PositionEval(state.score, state.bestMove, state.mateIn, state.pv, state.depth)
    }

    fun parseLine(line: String, state: EvalParseState) {
        if (line.startsWith("info depth")) {
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
                state.pv = it.groupValues[1]
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
        return PositionEval(state.score, state.bestMove, state.mateIn, state.pv, state.depth)
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
    }
}

private const val TAG = "StockfishEngine"
