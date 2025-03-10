package com.example.chess.di;

import com.example.chess.data.model.Board;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_CreateBoardFactory implements Factory<Board> {
  @Override
  public Board get() {
    return createBoard();
  }

  public static AppModule_CreateBoardFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static Board createBoard() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.createBoard());
  }

  private static final class InstanceHolder {
    private static final AppModule_CreateBoardFactory INSTANCE = new AppModule_CreateBoardFactory();
  }
}
