package com.example.chess.data

import android.util.Log
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

    suspend fun getMoveScore(): Int {
        val evaluationDeferred = CompletableDeferred<Int>()

        withContext(Dispatchers.IO) {
            process?.let { p ->
                val reader = BufferedReader(InputStreamReader(p.inputStream))
                try {
                    var line: String?
                    var bestEvaluation: Int? = null  // Store the best evaluation found

                    while (reader.readLine().also { line = it } != null) {
                        Log.d("Stockfish", "Stockfish Output: $line")

                        // Extract evaluation score
                        if (line!!.startsWith("info depth")) {
                            val match = Regex("score cp (-?\\d+)").find(line!!)
                            if (match != null) {
                                bestEvaluation = match.groupValues[1].toInt()
                                Log.d("Stockfish", "Evaluation: $bestEvaluation")
                            }
                        }

                        // Stop when Stockfish outputs "bestmove"
                        if (line!!.startsWith("bestmove")) {
                            if (bestEvaluation != null) {
                                evaluationDeferred.complete(bestEvaluation)
                            } else {
                                evaluationDeferred.complete(0)  // Default to 0 if no evaluation was found
                            }
                            break
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    evaluationDeferred.complete(0)  // Default to 0 in case of an error
                }
            }
        }
        return evaluationDeferred.await()
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
