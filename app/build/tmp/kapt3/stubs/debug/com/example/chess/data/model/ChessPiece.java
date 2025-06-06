package com.example.chess.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0016\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b7\u0018\u00002\u00020\u0001BE\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u0007\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0007\u0012\b\b\u0002\u0010\f\u001a\u00020\t\u00a2\u0006\u0002\u0010\rJ \u0010\u001e\u001a\u00020\u00072\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\"H&R\u001a\u0010\u000b\u001a\u00020\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u001a\u0010\n\u001a\u00020\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\u000f\"\u0004\b\u0015\u0010\u0011R\u001a\u0010\u0006\u001a\u00020\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u000f\"\u0004\b\u0016\u0010\u0011R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u001aR\u0011\u0010\f\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001c\u0082\u0001\u0006$%&\'()\u00a8\u0006*"}, d2 = {"Lcom/example/chess/data/model/ChessPiece;", "", "color", "Lcom/example/chess/data/model/Color;", "position", "Lcom/example/chess/data/model/Position;", "isKilled", "", "vectorAsset", "", "hasMoved", "canEnPassant", "value", "(Lcom/example/chess/data/model/Color;Lcom/example/chess/data/model/Position;ZIZZI)V", "getCanEnPassant", "()Z", "setCanEnPassant", "(Z)V", "getColor", "()Lcom/example/chess/data/model/Color;", "getHasMoved", "setHasMoved", "setKilled", "getPosition", "()Lcom/example/chess/data/model/Position;", "setPosition", "(Lcom/example/chess/data/model/Position;)V", "getValue", "()I", "getVectorAsset", "canMove", "board", "Lcom/example/chess/data/model/Board;", "start", "Lcom/example/chess/data/model/Spot;", "end", "Lcom/example/chess/data/model/Bishop;", "Lcom/example/chess/data/model/King;", "Lcom/example/chess/data/model/Knight;", "Lcom/example/chess/data/model/Pawn;", "Lcom/example/chess/data/model/Queen;", "Lcom/example/chess/data/model/Rook;", "app_debug"})
public abstract class ChessPiece {
    @org.jetbrains.annotations.NotNull()
    private final com.example.chess.data.model.Color color = null;
    @org.jetbrains.annotations.NotNull()
    private com.example.chess.data.model.Position position;
    private boolean isKilled;
    private final int vectorAsset = 0;
    private boolean hasMoved;
    private boolean canEnPassant;
    private final int value = 0;
    
    private ChessPiece(com.example.chess.data.model.Color color, com.example.chess.data.model.Position position, boolean isKilled, int vectorAsset, boolean hasMoved, boolean canEnPassant, int value) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.chess.data.model.Color getColor() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.chess.data.model.Position getPosition() {
        return null;
    }
    
    public final void setPosition(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Position p0) {
    }
    
    public final boolean isKilled() {
        return false;
    }
    
    public final void setKilled(boolean p0) {
    }
    
    public final int getVectorAsset() {
        return 0;
    }
    
    public final boolean getHasMoved() {
        return false;
    }
    
    public final void setHasMoved(boolean p0) {
    }
    
    public final boolean getCanEnPassant() {
        return false;
    }
    
    public final void setCanEnPassant(boolean p0) {
    }
    
    public final int getValue() {
        return 0;
    }
    
    public abstract boolean canMove(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Board board, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Spot start, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Spot end);
}