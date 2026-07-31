package com.example.chess.data.repository

import android.content.Context
import com.example.chess.domain.OpeningDetector
import com.example.chess.domain.model.Opening
import com.example.chess.domain.model.OpeningMatch
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import javax.inject.Inject

private const val OPENINGS_ASSET_PATH = "openings.json"

// Only class that touches Context/asset I/O for opening detection — the actual
// matching logic lives in the pure OpeningDetector so it stays testable without
// Android or an asset fixture.
class OpeningBookRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val book: List<Opening> by lazy { loadBook() }

    fun findOpening(playedMoves: List<String>): OpeningMatch? =
        OpeningDetector.detect(playedMoves, book)

    private fun loadBook(): List<Opening> {
        val json = context.assets.open(OPENINGS_ASSET_PATH).bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val entry = array.getJSONObject(i)
            val movesArray = entry.getJSONArray("moves")
            Opening(
                eco = entry.getString("eco"),
                name = entry.getString("name"),
                moves = (0 until movesArray.length()).map { movesArray.getString(it) },
            )
        }
    }
}
