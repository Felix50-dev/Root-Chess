package com.example.chess.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u0000 \u00162\u00020\u0001:\u0001\u0016B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u000b\u001a\u00020\u0000J\u0016\u0010\f\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000eJ\u000e\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013J\u0006\u0010\u0014\u001a\u00020\u0015R(\u0010\u0003\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0004X\u0086.\u00a2\u0006\u0010\n\u0002\u0010\n\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\t\u00a8\u0006\u0017"}, d2 = {"Lcom/example/chess/data/model/Board;", "", "()V", "boxes", "", "Lcom/example/chess/data/model/Spot;", "getBoxes", "()[[Lcom/example/chess/data/model/Spot;", "setBoxes", "([[Lcom/example/chess/data/model/Spot;)V", "[[Lcom/example/chess/data/model/Spot;", "clone", "getBox", "x", "", "y", "isValidPosition", "", "position", "Lcom/example/chess/data/model/Position;", "resetBoard", "", "Companion", "app_debug"})
public final class Board {
    public com.example.chess.data.model.Spot[][] boxes;
    public static final int NUM_ROWS = 8;
    public static final int NUM_COLUMNS = 8;
    @org.jetbrains.annotations.NotNull
    public static final com.example.chess.data.model.Board.Companion Companion = null;
    
    @javax.inject.Inject
    public Board() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.chess.data.model.Spot[][] getBoxes() {
        return null;
    }
    
    public final void setBoxes(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Spot[][] p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.chess.data.model.Spot getBox(int x, int y) {
        return null;
    }
    
    public final void resetBoard() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.chess.data.model.Board clone() {
        return null;
    }
    
    public final boolean isValidPosition(@org.jetbrains.annotations.NotNull
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