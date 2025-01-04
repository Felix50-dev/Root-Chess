package com.example.chess.di

import android.content.Context
import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Board
import com.example.chess.data.repository.GamePlay
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
    fun provideGame(board: Board, stockfishEngine: StockfishEngine, @ApplicationContext context: Context): GamePlay {
        return GamePlay(board, stockfishEngine)
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

}