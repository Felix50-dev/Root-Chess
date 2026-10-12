package com.example.chess.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.example.chess.data.StockfishEngine
import com.example.chess.data.billing.EpochClock
import com.example.chess.data.local.BotProgressDao
import com.example.chess.data.local.PuzzleDao
import com.example.chess.data.local.PuzzleDatabase
import com.example.chess.data.model.Board
import com.example.chess.data.remote.GroqApi
import com.example.chess.data.repository.GamePlay
import com.example.chess.data.repository.OpeningBookRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGame(
        board: Board,
        stockfishEngine: StockfishEngine,
        openingBookRepository: OpeningBookRepository,
        @ApplicationContext context: Context
    ): GamePlay {
        return GamePlay(board, stockfishEngine, openingBookRepository)
    }

    @Provides
    @Singleton
    fun provideOpeningBookRepository(@ApplicationContext context: Context): OpeningBookRepository {
        return OpeningBookRepository(context)
    }

    @Provides
    @Singleton
    fun createBoard(): Board {
        return Board()
    }

    @Provides
    @Singleton
    fun createStockfishEngine(): StockfishEngine {
        return StockfishEngine()
    }

    @Provides
    @Singleton
    fun provideGroqApi(): GroqApi = GroqApi()

    @Provides
    @Singleton
    fun providePuzzleDatabase(@ApplicationContext context: Context): PuzzleDatabase {
        // No Migration objects exist yet (single-entity schema until the bot_progress
        // table was added for version 2) — destructive fallback is a deliberate,
        // pre-release choice: it wipes any existing install's saved puzzles rather than
        // crashing on launch. Revisit once this ships to real users.
        return Room.databaseBuilder(context, PuzzleDatabase::class.java, "puzzles.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providePuzzleDao(database: PuzzleDatabase): PuzzleDao = database.puzzleDao()

    @Provides
    @Singleton
    fun provideBotProgressDao(database: PuzzleDatabase): BotProgressDao = database.botProgressDao()

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("app_prefs") }

    @Provides
    @Singleton
    fun provideEpochClock(): EpochClock = EpochClock { System.currentTimeMillis() }

}
