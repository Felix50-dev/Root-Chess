package com.example.chess

import androidx.test.core.app.ApplicationProvider
import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Board
import com.example.chess.data.repository.GamePlay
import com.example.chess.data.repository.OpeningBookRepository

// Single place that satisfies GamePlay's OpeningBookRepository dependency for
// instrumented tests — the book loads lazily off the real app context, so tests
// that never touch opening detection pay nothing for it.
fun testGamePlay(
    board: Board = Board(),
    stockfish: StockfishEngine = StockfishEngine(),
): GamePlay = GamePlay(
    board,
    stockfish,
    OpeningBookRepository(ApplicationProvider.getApplicationContext()),
)
