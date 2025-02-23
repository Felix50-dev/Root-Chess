package com.example.chess.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001BE\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u0007\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0007\u0012\b\b\u0002\u0010\f\u001a\u00020\t\u00a2\u0006\u0002\u0010\rJ \u0010\u000e\u001a\u00020\u00072\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0012H\u0016J\"\u0010\u0014\u001a\u00020\u00072\u0006\u0010\u0004\u001a\u00020\u00052\b\u0010\u0015\u001a\u0004\u0018\u00010\u00012\u0006\u0010\u000f\u001a\u00020\u0010H\u0002J\u0010\u0010\u0016\u001a\u00020\u00072\u0006\u0010\u0017\u001a\u00020\u0012H\u0002J\u001e\u0010\u0018\u001a\u00020\u00072\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0012J\u001e\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0012J \u0010\u001b\u001a\u00020\u001a2\u0006\u0010\u0004\u001a\u00020\u00052\b\u0010\u0015\u001a\u0004\u0018\u00010\u00012\u0006\u0010\u000f\u001a\u00020\u0010J\f\u0010\u001c\u001a\u00020\u0007*\u00020\u0005H\u0002\u00a8\u0006\u001d"}, d2 = {"Lcom/example/chess/data/model/Pawn;", "Lcom/example/chess/data/model/ChessPiece;", "color", "Lcom/example/chess/data/model/Color;", "position", "Lcom/example/chess/data/model/Position;", "isKilled", "", "vectorAsset", "", "hasMoved", "canEnPassant", "value", "(Lcom/example/chess/data/model/Color;Lcom/example/chess/data/model/Position;ZIZZI)V", "canMove", "board", "Lcom/example/chess/data/model/Board;", "start", "Lcom/example/chess/data/model/Spot;", "end", "canPromotePawn", "promotionPiece", "isEmpty", "spot", "isEnPassant", "performEnPassant", "", "promotePawn", "isOnStartingRow", "app_debug"})
public final class Pawn extends com.example.chess.data.model.ChessPiece {
    
    public Pawn(@org.jetbrains.annotations.NotNull()
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
    
    private final boolean isEmpty(com.example.chess.data.model.Spot spot) {
        return false;
    }
    
    private final boolean isOnStartingRow(com.example.chess.data.model.Position $this$isOnStartingRow) {
        return false;
    }
    
    private final boolean canPromotePawn(com.example.chess.data.model.Position position, com.example.chess.data.model.ChessPiece promotionPiece, com.example.chess.data.model.Board board) {
        return false;
    }
    
    public final void promotePawn(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Position position, @org.jetbrains.annotations.Nullable()
    com.example.chess.data.model.ChessPiece promotionPiece, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Board board) {
    }
    
    public final boolean isEnPassant(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Board board, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Spot start, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Spot end) {
        return false;
    }
    
    public final void performEnPassant(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Board board, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Spot start, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Spot end) {
    }
}