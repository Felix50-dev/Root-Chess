package com.example.chess.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B1\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\u000bJ \u0010\f\u001a\u00020\u00072\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0016J \u0010\u0012\u001a\u00020\u00072\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002\u00a8\u0006\u0013"}, d2 = {"Lcom/example/chess/data/model/Bishop;", "Lcom/example/chess/data/model/ChessPiece;", "color", "Lcom/example/chess/data/model/Color;", "position", "Lcom/example/chess/data/model/Position;", "isKilled", "", "vectorAsset", "", "hasMoved", "(Lcom/example/chess/data/model/Color;Lcom/example/chess/data/model/Position;ZIZ)V", "canMove", "board", "Lcom/example/chess/data/model/Board;", "start", "Lcom/example/chess/data/model/Spot;", "end", "isPathClearForBishop", "app_debug"})
public final class Bishop extends com.example.chess.data.model.ChessPiece {
    
    public Bishop(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Color color, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Position position, boolean isKilled, int vectorAsset, boolean hasMoved) {
    }
    
    @java.lang.Override
    public boolean canMove(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Board board, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Spot start, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Spot end) {
        return false;
    }
    
    private final boolean isPathClearForBishop(com.example.chess.data.model.Board board, com.example.chess.data.model.Spot start, com.example.chess.data.model.Spot end) {
        return false;
    }
}