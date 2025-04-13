package com.example.chess.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0090\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010!\n\u0002\b\u0007\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0012\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u001c\u00107\u001a\u0002082\f\u0010#\u001a\b\u0012\u0004\u0012\u0002090,H\u0086@\u00a2\u0006\u0002\u0010:J\b\u0010;\u001a\u00020<H\u0002J\u0010\u0010=\u001a\u00020<2\u0006\u0010>\u001a\u00020\u001bH\u0002J\u001a\u0010?\u001a\b\u0012\u0004\u0012\u0002090$2\f\u0010@\u001a\b\u0012\u0004\u0012\u00020\u000b0$J\u001a\u0010A\u001a\b\u0012\u0004\u0012\u00020B0$2\f\u0010C\u001a\b\u0012\u0004\u0012\u00020\u000b0,J\u000e\u0010D\u001a\u00020\u001f2\u0006\u0010>\u001a\u00020\u001bJ\u0018\u0010D\u001a\u00020\u001f2\u0006\u0010>\u001a\u00020\u001b2\u0006\u0010E\u001a\u00020\u0003H\u0002J\u0010\u0010F\u001a\u00020\u001f2\u0006\u0010>\u001a\u00020\u001bH\u0002J\u0018\u0010G\u001a\u0004\u0018\u00010H2\u0006\u0010E\u001a\u00020\u00032\u0006\u0010I\u001a\u00020JJ\u001e\u0010K\u001a\b\u0012\u0004\u0012\u00020\t0$2\u0006\u0010>\u001a\u00020\u001b2\u0006\u0010E\u001a\u00020\u0003H\u0002J\u001c\u0010L\u001a\b\u0012\u0004\u0012\u00020\u00110,2\u0006\u0010M\u001a\u00020\u00112\u0006\u0010>\u001a\u00020\u001bJ\u001e\u0010N\u001a\b\u0012\u0004\u0012\u00020\u00110$2\u0006\u0010M\u001a\u00020\u00112\u0006\u0010>\u001a\u00020\u001bH\u0002J\u001e\u0010O\u001a\u00020\u001f2\u0006\u0010>\u001a\u00020\u001b2\u0006\u0010P\u001a\u00020QH\u0086@\u00a2\u0006\u0002\u0010RJ\u001e\u0010S\u001a\u00020\u001f2\u0006\u0010>\u001a\u00020\u001b2\u0006\u0010T\u001a\u00020H2\u0006\u0010U\u001a\u00020HJ\u001a\u0010V\u001a\u0004\u0018\u00010\u000b2\u0006\u0010W\u001a\u0002092\u0006\u0010>\u001a\u00020\u001bH\u0002J\u0016\u0010X\u001a\u00020<2\u0006\u0010Y\u001a\u00020\u001b2\u0006\u0010Z\u001a\u00020\u001bJ\u0018\u0010[\u001a\u00020\u001f2\u0006\u0010>\u001a\u00020\u001b2\u0006\u0010I\u001a\u00020JH\u0002J\u0018\u0010\\\u001a\u00020\u001f2\u0006\u0010>\u001a\u00020\u001b2\u0006\u0010\u0002\u001a\u00020\u0003H\u0002J \u0010]\u001a\u00020\u001f2\u0006\u0010E\u001a\u00020\u00032\u0006\u0010^\u001a\u00020\u000b2\u0006\u0010>\u001a\u00020\u001bH\u0002J\u0016\u0010_\u001a\u00020<2\u0006\u0010^\u001a\u00020\u000b2\u0006\u0010E\u001a\u00020\u0003J\u0018\u0010`\u001a\u00020\u001f2\u0006\u0010^\u001a\u00020\u000b2\u0006\u0010>\u001a\u00020\u001bH\u0002J\u0018\u0010a\u001a\u00020<2\u0006\u0010^\u001a\u00020\u000b2\u0006\u0010E\u001a\u00020\u0003H\u0002J \u0010a\u001a\u00020<2\u0006\u0010^\u001a\u00020\u000b2\u0006\u0010E\u001a\u00020\u00032\u0006\u0010>\u001a\u00020\u001bH\u0002J\u000e\u0010b\u001a\u00020<2\u0006\u0010^\u001a\u00020\u000bR\u0016\u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fR\u001a\u0010\u0010\u001a\u00020\u0011X\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u0017\"\u0004\b\u0018\u0010\u0019R\u0019\u0010\u001a\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001b0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u001a\u0010\u001e\u001a\u00020\u001fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001e\u0010 \"\u0004\b!\u0010\"R\u0017\u0010#\u001a\b\u0012\u0004\u0012\u00020\u000b0$\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010&R\u0017\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u000b0$\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010&R\u0017\u0010)\u001a\b\u0012\u0004\u0012\u00020\t0$\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010&R\u0014\u0010+\u001a\b\u0012\u0004\u0012\u00020\u001b0,X\u0082.\u00a2\u0006\u0002\n\u0000R\u0016\u0010-\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0.X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010/\u001a\u000200X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b1\u00102\"\u0004\b3\u00104R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u00106\u00a8\u0006c"}, d2 = {"Lcom/example/chess/data/repository/GamePlay;", "", "board", "Lcom/example/chess/data/model/Board;", "stockfish", "Lcom/example/chess/data/StockfishEngine;", "(Lcom/example/chess/data/model/Board;Lcom/example/chess/data/StockfishEngine;)V", "_promotionPieceFlow", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/chess/data/model/ChessPiece;", "aiLastMove", "Lcom/example/chess/data/model/Move;", "getAiLastMove", "()Lcom/example/chess/data/model/Move;", "setAiLastMove", "(Lcom/example/chess/data/model/Move;)V", "aiStartPosition", "Lcom/example/chess/data/model/Position;", "getAiStartPosition", "()Lcom/example/chess/data/model/Position;", "setAiStartPosition", "(Lcom/example/chess/data/model/Position;)V", "getBoard", "()Lcom/example/chess/data/model/Board;", "setBoard", "(Lcom/example/chess/data/model/Board;)V", "currentTurn", "Lcom/example/chess/data/model/Player;", "getCurrentTurn", "()Lkotlinx/coroutines/flow/MutableStateFlow;", "isGameOver", "", "()Z", "setGameOver", "(Z)V", "movesPlayed", "", "getMovesPlayed", "()Ljava/util/List;", "movesUndone", "getMovesUndone", "piecesKilled", "getPiecesKilled", "players", "", "promotionPieceFlow", "Lkotlinx/coroutines/flow/StateFlow;", "status", "Lcom/example/chess/data/model/GameStatus;", "getStatus", "()Lcom/example/chess/data/model/GameStatus;", "setStatus", "(Lcom/example/chess/data/model/GameStatus;)V", "getStockfish", "()Lcom/example/chess/data/StockfishEngine;", "analyzeMoveScores", "Lcom/example/chess/data/model/AnalysisResult;", "", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "changeCurrentTurn", "", "checkForWinner", "player", "convertMovesToNotation", "allMoves", "convertMovesToSANNotation", "Lcom/example/chess/data/model/SANMovePair;", "moves", "detectCheck", "myBoard", "detectCheckMate", "findKing", "Lcom/example/chess/data/model/Spot;", "playerColor", "Lcom/example/chess/data/model/Color;", "getAlivePieces", "getRemainingValidMoves", "position", "getValidMoves", "handleAIMove", "level", "Lcom/example/chess/data/model/AILevel;", "(Lcom/example/chess/data/model/Player;Lcom/example/chess/data/model/AILevel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "handleHumanMove", "start", "end", "handleStockfishBestMove", "bestMove", "init", "p1", "p2", "isMoveValid", "isStalemate", "makeMove", "move", "redoMove", "simulateMove", "undoMove", "undoMoveByPlayer", "app_debug"})
public final class GamePlay {
    @org.jetbrains.annotations.NotNull()
    private com.example.chess.data.model.Board board;
    @org.jetbrains.annotations.NotNull()
    private final com.example.chess.data.StockfishEngine stockfish = null;
    private java.util.List<com.example.chess.data.model.Player> players;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.data.model.Player> currentTurn = null;
    @org.jetbrains.annotations.NotNull()
    private com.example.chess.data.model.GameStatus status = com.example.chess.data.model.GameStatus.ACTIVE;
    private boolean isGameOver = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.example.chess.data.model.Move> movesPlayed = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.example.chess.data.model.Move> movesUndone = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.example.chess.data.model.ChessPiece> piecesKilled = null;
    public com.example.chess.data.model.Position aiStartPosition;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.data.model.ChessPiece> _promotionPieceFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.ChessPiece> promotionPieceFlow = null;
    @org.jetbrains.annotations.Nullable()
    private com.example.chess.data.model.Move aiLastMove;
    
    @javax.inject.Inject()
    public GamePlay(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Board board, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.StockfishEngine stockfish) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.chess.data.model.Board getBoard() {
        return null;
    }
    
    public final void setBoard(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Board p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.chess.data.StockfishEngine getStockfish() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.data.model.Player> getCurrentTurn() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.chess.data.model.GameStatus getStatus() {
        return null;
    }
    
    public final void setStatus(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.GameStatus p0) {
    }
    
    public final boolean isGameOver() {
        return false;
    }
    
    public final void setGameOver(boolean p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.chess.data.model.Move> getMovesPlayed() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.chess.data.model.Move> getMovesUndone() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.chess.data.model.ChessPiece> getPiecesKilled() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.chess.data.model.Position getAiStartPosition() {
        return null;
    }
    
    public final void setAiStartPosition(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Position p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.example.chess.data.model.Move getAiLastMove() {
        return null;
    }
    
    public final void setAiLastMove(@org.jetbrains.annotations.Nullable()
    com.example.chess.data.model.Move p0) {
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Player p1, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Player p2) {
    }
    
    public final boolean handleHumanMove(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Player player, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Spot start, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Spot end) {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object analyzeMoveScores(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> movesPlayed, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.chess.data.model.AnalysisResult> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object handleAIMove(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Player player, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.AILevel level, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> convertMovesToNotation(@org.jetbrains.annotations.NotNull()
    java.util.List<com.example.chess.data.model.Move> allMoves) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.chess.data.model.SANMovePair> convertMovesToSANNotation(@org.jetbrains.annotations.NotNull()
    java.util.List<com.example.chess.data.model.Move> moves) {
        return null;
    }
    
    private final com.example.chess.data.model.Move handleStockfishBestMove(java.lang.String bestMove, com.example.chess.data.model.Player player) {
        return null;
    }
    
    private final boolean isMoveValid(com.example.chess.data.model.Player player, com.example.chess.data.model.Color playerColor) {
        return false;
    }
    
    private final boolean makeMove(com.example.chess.data.model.Board myBoard, com.example.chess.data.model.Move move, com.example.chess.data.model.Player player) {
        return false;
    }
    
    private final void checkForWinner(com.example.chess.data.model.Player player) {
    }
    
    private final boolean simulateMove(com.example.chess.data.model.Move move, com.example.chess.data.model.Player player) {
        return false;
    }
    
    private final void changeCurrentTurn() {
    }
    
    private final java.util.List<com.example.chess.data.model.Position> getValidMoves(com.example.chess.data.model.Position position, com.example.chess.data.model.Player player) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.chess.data.model.Position> getRemainingValidMoves(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Position position, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Player player) {
        return null;
    }
    
    private final boolean detectCheck(com.example.chess.data.model.Player player, com.example.chess.data.model.Board myBoard) {
        return false;
    }
    
    public final boolean detectCheck(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Player player) {
        return false;
    }
    
    private final boolean detectCheckMate(com.example.chess.data.model.Player player) {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.example.chess.data.model.Spot findKing(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Board myBoard, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Color playerColor) {
        return null;
    }
    
    private final java.util.List<com.example.chess.data.model.ChessPiece> getAlivePieces(com.example.chess.data.model.Player player, com.example.chess.data.model.Board myBoard) {
        return null;
    }
    
    private final boolean isStalemate(com.example.chess.data.model.Player player, com.example.chess.data.model.Board board) {
        return false;
    }
    
    private final void undoMove(com.example.chess.data.model.Move move, com.example.chess.data.model.Board myBoard, com.example.chess.data.model.Player player) {
    }
    
    private final void undoMove(com.example.chess.data.model.Move move, com.example.chess.data.model.Board myBoard) {
    }
    
    public final void undoMoveByPlayer(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Move move) {
    }
    
    public final void redoMove(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Move move, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Board myBoard) {
    }
}