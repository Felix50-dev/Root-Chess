package com.example.chess.presentation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0015\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B/\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\u0002\u0010\nJ\u000b\u0010\u0019\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\u001a\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0007H\u00c6\u0003J\u000b\u0010\u001c\u001a\u0004\u0018\u00010\tH\u00c6\u0003J7\u0010\u001d\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\tH\u00c6\u0001J\u0013\u0010\u001e\u001a\u00020\u001f2\b\u0010 \u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010!\u001a\u00020\"H\u00d6\u0001J\t\u0010#\u001a\u00020$H\u00d6\u0001R\u001c\u0010\u0002\u001a\u0004\u0018\u00010\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u001a\u0010\u0006\u001a\u00020\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R\u001c\u0010\b\u001a\u0004\u0018\u00010\tX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0015\u0010\u0016\"\u0004\b\u0017\u0010\u0018\u00a8\u0006%"}, d2 = {"Lcom/example/chess/presentation/BoardState;", "", "board", "Lcom/example/chess/data/model/Board;", "currentPlayer", "Lcom/example/chess/data/model/Player;", "gameStatus", "Lcom/example/chess/data/model/GameStatus;", "lastMovedFrom", "Lcom/example/chess/data/model/Position;", "(Lcom/example/chess/data/model/Board;Lcom/example/chess/data/model/Player;Lcom/example/chess/data/model/GameStatus;Lcom/example/chess/data/model/Position;)V", "getBoard", "()Lcom/example/chess/data/model/Board;", "setBoard", "(Lcom/example/chess/data/model/Board;)V", "getCurrentPlayer", "()Lcom/example/chess/data/model/Player;", "getGameStatus", "()Lcom/example/chess/data/model/GameStatus;", "setGameStatus", "(Lcom/example/chess/data/model/GameStatus;)V", "getLastMovedFrom", "()Lcom/example/chess/data/model/Position;", "setLastMovedFrom", "(Lcom/example/chess/data/model/Position;)V", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "", "app_debug"})
public final class BoardState {
    @org.jetbrains.annotations.Nullable()
    private com.example.chess.data.model.Board board;
    @org.jetbrains.annotations.Nullable()
    private final com.example.chess.data.model.Player currentPlayer = null;
    @org.jetbrains.annotations.NotNull()
    private com.example.chess.data.model.GameStatus gameStatus;
    @org.jetbrains.annotations.Nullable()
    private com.example.chess.data.model.Position lastMovedFrom;
    
    public BoardState(@org.jetbrains.annotations.Nullable()
    com.example.chess.data.model.Board board, @org.jetbrains.annotations.Nullable()
    com.example.chess.data.model.Player currentPlayer, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.GameStatus gameStatus, @org.jetbrains.annotations.Nullable()
    com.example.chess.data.model.Position lastMovedFrom) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.example.chess.data.model.Board getBoard() {
        return null;
    }
    
    public final void setBoard(@org.jetbrains.annotations.Nullable()
    com.example.chess.data.model.Board p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.example.chess.data.model.Player getCurrentPlayer() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.chess.data.model.GameStatus getGameStatus() {
        return null;
    }
    
    public final void setGameStatus(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.GameStatus p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.example.chess.data.model.Position getLastMovedFrom() {
        return null;
    }
    
    public final void setLastMovedFrom(@org.jetbrains.annotations.Nullable()
    com.example.chess.data.model.Position p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.example.chess.data.model.Board component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.example.chess.data.model.Player component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.chess.data.model.GameStatus component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.example.chess.data.model.Position component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.chess.presentation.BoardState copy(@org.jetbrains.annotations.Nullable()
    com.example.chess.data.model.Board board, @org.jetbrains.annotations.Nullable()
    com.example.chess.data.model.Player currentPlayer, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.GameStatus gameStatus, @org.jetbrains.annotations.Nullable()
    com.example.chess.data.model.Position lastMovedFrom) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}