package com.example.chess.presentation;

import android.content.Context;
import com.example.chess.data.repository.GamePlay;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class ChessGameViewModel_Factory implements Factory<ChessGameViewModel> {
  private final Provider<GamePlay> gameProvider;

  private final Provider<Context> contextProvider;

  public ChessGameViewModel_Factory(Provider<GamePlay> gameProvider,
      Provider<Context> contextProvider) {
    this.gameProvider = gameProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public ChessGameViewModel get() {
    return newInstance(gameProvider.get(), contextProvider.get());
  }

  public static ChessGameViewModel_Factory create(Provider<GamePlay> gameProvider,
      Provider<Context> contextProvider) {
    return new ChessGameViewModel_Factory(gameProvider, contextProvider);
  }

  public static ChessGameViewModel newInstance(GamePlay game, Context context) {
    return new ChessGameViewModel(game, context);
  }
}
