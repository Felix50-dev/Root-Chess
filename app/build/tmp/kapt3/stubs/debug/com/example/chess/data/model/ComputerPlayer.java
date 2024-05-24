package com.example.chess.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rH\u0002J\u0014\u0010\u000e\u001a\u00020\r2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\r0\u0010J\u0012\u0010\u0011\u001a\u00020\u000b2\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u0002R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u0014\u0010\u0007\u001a\u00020\bX\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\t\u00a8\u0006\u0014"}, d2 = {"Lcom/example/chess/data/model/ComputerPlayer;", "", "gamePlay", "Lcom/example/chess/data/repository/GamePlay;", "(Lcom/example/chess/data/repository/GamePlay;)V", "getGamePlay", "()Lcom/example/chess/data/repository/GamePlay;", "isWhite", "", "()Z", "evaluateCaptureMove", "", "move", "Lcom/example/chess/data/model/Move;", "evaluateMoves", "possibleMoves", "", "evaluatePieceValue", "piece", "Lcom/example/chess/data/model/ChessPiece;", "app_debug"})
public final class ComputerPlayer {
    @org.jetbrains.annotations.NotNull
    private final com.example.chess.data.repository.GamePlay gamePlay = null;
    private final boolean isWhite = false;
    
    public ComputerPlayer(@org.jetbrains.annotations.NotNull
    com.example.chess.data.repository.GamePlay gamePlay) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.chess.data.repository.GamePlay getGamePlay() {
        return null;
    }
    
    public final boolean isWhite() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.chess.data.model.Move evaluateMoves(@org.jetbrains.annotations.NotNull
    java.util.List<com.example.chess.data.model.Move> possibleMoves) {
        return null;
    }
    
    private final int evaluateCaptureMove(com.example.chess.data.model.Move move) {
        return 0;
    }
    
    private final int evaluatePieceValue(com.example.chess.data.model.ChessPiece piece) {
        return 0;
    }
}