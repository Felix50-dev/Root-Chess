package com.example.chess.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 \u00192\u00020\u0001:\u0001\u0019B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\n\u001a\u00020\u0000J \u0010\u000b\u001a\u00020\u00052\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\r2\u0006\u0010\u000f\u001a\u00020\u0010H\u0002J\u0016\u0010\u0011\u001a\u00020\u00052\u0006\u0010\u0012\u001a\u00020\r2\u0006\u0010\u0013\u001a\u00020\rJ\u000e\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017J\u0012\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0004R&\u0010\u0003\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\t\u00a8\u0006\u001a"}, d2 = {"Lcom/example/chess/data/model/Board;", "", "()V", "boxes", "", "Lcom/example/chess/data/model/Spot;", "getBoxes", "()Ljava/util/List;", "setBoxes", "(Ljava/util/List;)V", "clone", "createPieceSpot", "row", "", "col", "color", "Lcom/example/chess/data/model/Color;", "getBox", "x", "y", "isValidPosition", "", "position", "Lcom/example/chess/data/model/Position;", "resetBoard", "Companion", "app_debug"})
public final class Board {
    @org.jetbrains.annotations.NotNull()
    private java.util.List<? extends java.util.List<com.example.chess.data.model.Spot>> boxes;
    public static final int NUM_ROWS = 8;
    public static final int NUM_COLUMNS = 8;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.chess.data.model.Board.Companion Companion = null;
    
    @javax.inject.Inject()
    public Board() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.util.List<com.example.chess.data.model.Spot>> getBoxes() {
        return null;
    }
    
    public final void setBoxes(@org.jetbrains.annotations.NotNull()
    java.util.List<? extends java.util.List<com.example.chess.data.model.Spot>> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.chess.data.model.Spot getBox(int x, int y) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.util.List<com.example.chess.data.model.Spot>> resetBoard() {
        return null;
    }
    
    private final com.example.chess.data.model.Spot createPieceSpot(int row, int col, com.example.chess.data.model.Color color) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.chess.data.model.Board clone() {
        return null;
    }
    
    public final boolean isValidPosition(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Position position) {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/example/chess/data/model/Board$Companion;", "", "()V", "NUM_COLUMNS", "", "NUM_ROWS", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}