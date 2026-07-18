package com.example.chess.data

import android.util.Log
import com.example.chess.domain.model.EvalParseState
import com.example.chess.domain.model.PositionEval
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class StockfishEngine {
    private var process: Process? = null
    private var outputStream: BufferedWriter? = null
    private val _bestMoveFlow = MutableStateFlow("")
    val bestMoveFlow: StateFlow<String> = _bestMoveFlow.asStateFlow()


    fun startStockfish(stockfishPath: String) {
        val stockfishFile = File(stockfishPath)
        try {
            process = Runtime.getRuntime().exec(stockfishFile.absolutePath)
            Log.d("Stockfish", "Stockfish process started successfully")
            outputStream = BufferedWriter(OutputStreamWriter(process?.outputStream))
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Stockfish", "Error starting Stockfish: ${e.message}")
        }
    }

    suspend fun getBestMove(): String {
        // Create a CompletableDeferred that will be completed with the best move
        val bestMoveDeferred = CompletableDeferred<String>()

        withContext(Dispatchers.IO) {
            process?.let { p ->
                val reader = BufferedReader(InputStreamReader(p.inputStream))
                try {
                    var line: String?
                    var evaluation: Int?
                    while (reader.readLine().also { line = it } != null) {
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
                            val move = line!!.split(" ")[1]
                            // Complete the deferred with the best move
                            bestMoveDeferred.complete(move)
                            break
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        // Wait for the best move to be available and return it
        return bestMoveDeferred.await()
    }

    suspend fun getDetailedEval(): PositionEval = withContext(Dispatchers.IO) {
        val state = EvalParseState()
        process?.let { p ->
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                parseLine(line!!, state)       // ← parse as it streams, no list
                if (state.done) break          // ← stop immediately on bestmove
            }
        }
        PositionEval(state.score, state.bestMove, state.mateIn, state.pv, state.depth)
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
    }
}

private const val TAG = "StockfishEngine"
