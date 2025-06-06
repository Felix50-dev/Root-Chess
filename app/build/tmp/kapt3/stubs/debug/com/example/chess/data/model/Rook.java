package com.example.chess.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001BE\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u0007\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0007\u0012\b\b\u0002\u0010\f\u001a\u00020\t\u00a2\u0006\u0002\u0010\rJ \u0010\u000e\u001a\u00020\u00072\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0012H\u0016J \u0010\u0014\u001a\u00020\u00072\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00052\u0006\u0010\u0013\u001a\u00020\u0005H\u0002J\u0014\u0010\u0015\u001a\u00020\u0007*\u00020\u00052\u0006\u0010\u0016\u001a\u00020\u0005H\u0002\u00a8\u0006\u0017"}, d2 = {"Lcom/example/chess/data/model/Rook;", "Lcom/example/chess/data/model/ChessPiece;", "color", "Lcom/example/chess/data/model/Color;", "position", "Lcom/example/chess/data/model/Position;", "isKilled", "", "vectorAsset", "", "hasMoved", "canEnPassant", "value", "(Lcom/example/chess/data/model/Color;Lcom/example/chess/data/model/Position;ZIZZI)V", "canMove", "board", "Lcom/example/chess/data/model/Board;", "start", "Lcom/example/chess/data/model/Spot;", "end", "isPathClear", "isSameRowOrColumnAs", "other", "app_debug"})
public final class Rook extends com.example.chess.data.model.ChessPiece {
    
    public Rook(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Color color, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Position position, boolean isKilled, int vectorAsset, boolean hasMoved, boolean canEnPassant, int value) {
    }
    
    @java.lang.Override()
    public boolean canMove(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Board board, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Spot start, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Spot end) {
        return false;
    }
    
    private final boolean isSameRowOrColumnAs(com.example.chess.data.model.Position $this$isSameRowOrColumnAs, com.example.chess.data.model.Position other) {
        return false;
    }
    
    private final boolean isPathClear(com.example.chess.data.model.Board board, com.example.chess.data.model.Position start, com.example.chess.data.model.Position end) {
        return false;
    }
}