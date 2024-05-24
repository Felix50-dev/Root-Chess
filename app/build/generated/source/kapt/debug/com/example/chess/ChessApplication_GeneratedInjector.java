package com.example.chess;

import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.internal.GeneratedEntryPoint;

@OriginatingElement(
    topLevelClass = ChessApplication.class
)
@GeneratedEntryPoint
@InstallIn(SingletonComponent.class)
public interface ChessApplication_GeneratedInjector {
  void injectChessApplication(ChessApplication chessApplication);
}
