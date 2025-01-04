package com.example.chess.data.repository;

import com.example.chess.data.StockfishEngine;
import com.example.chess.data.model.Board;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class GamePlay_Factory implements Factory<GamePlay> {
  private final Provider<Board> boardProvider;

  private final Provider<StockfishEngine> stockfishProvider;

  public GamePlay_Factory(Provider<Board> boardProvider,
      Provider<StockfishEngine> stockfishProvider) {
    this.boardProvider = boardProvider;
    this.stockfishProvider = stockfishProvider;
  }

  @Override
  public GamePlay get() {
    return newInstance(boardProvider.get(), stockfishProvider.get());
  }

  public static GamePlay_Factory create(Provider<Board> boardProvider,
      Provider<StockfishEngine> stockfishProvider) {
    return new GamePlay_Factory(boardProvider, stockfishProvider);
  }

  public static GamePlay newInstance(Board board, StockfishEngine stockfish) {
    return new GamePlay(board, stockfish);
  }
}
