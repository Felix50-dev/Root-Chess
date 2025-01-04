package com.example.chess.di;

import android.content.Context;
import com.example.chess.data.StockfishEngine;
import com.example.chess.data.model.Board;
import com.example.chess.data.repository.GamePlay;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppModule_ProvideGameFactory implements Factory<GamePlay> {
  private final Provider<Board> boardProvider;

  private final Provider<StockfishEngine> stockfishEngineProvider;

  private final Provider<Context> contextProvider;

  public AppModule_ProvideGameFactory(Provider<Board> boardProvider,
      Provider<StockfishEngine> stockfishEngineProvider, Provider<Context> contextProvider) {
    this.boardProvider = boardProvider;
    this.stockfishEngineProvider = stockfishEngineProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public GamePlay get() {
    return provideGame(boardProvider.get(), stockfishEngineProvider.get(), contextProvider.get());
  }

  public static AppModule_ProvideGameFactory create(Provider<Board> boardProvider,
      Provider<StockfishEngine> stockfishEngineProvider, Provider<Context> contextProvider) {
    return new AppModule_ProvideGameFactory(boardProvider, stockfishEngineProvider, contextProvider);
  }

  public static GamePlay provideGame(Board board, StockfishEngine stockfishEngine,
      Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideGame(board, stockfishEngine, context));
  }
}
