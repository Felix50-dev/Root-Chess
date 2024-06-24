package com.example.chess.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000|\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0011\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0013\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010%\u001a\u00020&H\u0002J\u0010\u0010\'\u001a\u00020&2\u0006\u0010(\u001a\u00020\tH\u0002J\u0010\u0010)\u001a\u00020\u00172\u0006\u0010(\u001a\u00020\tH\u0002J\u0010\u0010*\u001a\u00020\u000f2\u0006\u0010(\u001a\u00020\tH\u0002J\u0018\u0010*\u001a\u00020\u000f2\u0006\u0010(\u001a\u00020\t2\u0006\u0010+\u001a\u00020\u0003H\u0002J\u0010\u0010,\u001a\u00020\u000f2\u0006\u0010(\u001a\u00020\tH\u0002J\u0018\u0010-\u001a\u00020\u000f2\u0006\u0010.\u001a\u00020\u00172\u0006\u0010(\u001a\u00020\tH\u0002J\u0010\u0010/\u001a\u00020\u00142\u0006\u0010\u0002\u001a\u00020\u0003H\u0002J\u0018\u0010/\u001a\u00020\u00142\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010(\u001a\u00020\tH\u0002J\u0010\u00100\u001a\u00020\u00142\u0006\u0010.\u001a\u00020\u0017H\u0002J \u00101\u001a\u00020\u00142\u0006\u00102\u001a\u0002032\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010(\u001a\u00020\tH\u0002J\u0018\u00104\u001a\u00020\u00142\u0006\u0010.\u001a\u00020\u00172\u0006\u0010(\u001a\u00020\tH\u0002J\u0016\u00105\u001a\u00020\u00172\f\u00106\u001a\b\u0012\u0004\u0012\u00020\u001707H\u0002J\u001e\u00108\u001a\u00020\u00172\f\u00106\u001a\b\u0012\u0004\u0012\u00020\u0017072\u0006\u0010(\u001a\u00020\tH\u0002J\u0012\u00109\u001a\u00020\u00142\b\u0010:\u001a\u0004\u0018\u00010;H\u0002J \u0010<\u001a\u00020\u00172\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010=\u001a\u00020\u00142\u0006\u0010>\u001a\u00020\tH\u0002J\u001a\u0010?\u001a\u0004\u0018\u0001032\u0006\u0010+\u001a\u00020\u00032\u0006\u0010@\u001a\u00020AH\u0002J\u001e\u0010B\u001a\b\u0012\u0004\u0012\u00020\u0017072\u0006\u0010(\u001a\u00020\t2\u0006\u0010\u0002\u001a\u00020\u0003H\u0002J\u001e\u0010C\u001a\b\u0012\u0004\u0012\u00020;0\u00162\u0006\u0010(\u001a\u00020\t2\u0006\u0010+\u001a\u00020\u0003H\u0002J\u001c\u0010D\u001a\b\u0012\u0004\u0012\u00020E072\u0006\u0010F\u001a\u00020E2\u0006\u0010(\u001a\u00020\tJ\u001e\u0010G\u001a\b\u0012\u0004\u0012\u00020E0\u00162\u0006\u0010F\u001a\u00020E2\u0006\u0010(\u001a\u00020\tH\u0002J\u0018\u0010H\u001a\u00020\u000f2\u0006\u0010(\u001a\u00020\t2\u0006\u0010I\u001a\u00020JH\u0002J(\u0010K\u001a\u00020\u000f2\u0006\u0010(\u001a\u00020\t2\u0006\u0010L\u001a\u0002032\u0006\u0010M\u001a\u0002032\u0006\u0010I\u001a\u00020JH\u0002J\u001e\u0010N\u001a\u00020&2\u0006\u0010O\u001a\u00020\t2\u0006\u0010P\u001a\u00020\t2\u0006\u0010I\u001a\u00020JJ\u0018\u0010Q\u001a\u00020\u000f2\u0006\u0010(\u001a\u00020\t2\u0006\u0010@\u001a\u00020AH\u0002J\u0010\u0010R\u001a\u00020\u000f2\u0006\u0010\u0013\u001a\u00020\u0014H\u0002J \u0010S\u001a\u00020\u000f2\u0006\u0010T\u001a\u0002032\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010(\u001a\u00020\tH\u0002J\u0018\u0010U\u001a\u00020\u000f2\u0006\u0010(\u001a\u00020\t2\u0006\u0010\u0002\u001a\u00020\u0003H\u0002J \u0010V\u001a\u00020\u000f2\u0006\u0010+\u001a\u00020\u00032\u0006\u0010.\u001a\u00020\u00172\u0006\u0010(\u001a\u00020\tH\u0002J(\u0010W\u001a\u00020\u00142\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010=\u001a\u00020\u00142\u0006\u0010X\u001a\u00020\u000f2\u0006\u0010>\u001a\u00020\tH\u0003J&\u0010Y\u001a\u00020\u000f2\u0006\u0010(\u001a\u00020\t2\u0006\u0010L\u001a\u0002032\u0006\u0010M\u001a\u0002032\u0006\u0010I\u001a\u00020JJ\u0016\u0010Z\u001a\u00020&2\u0006\u0010.\u001a\u00020\u00172\u0006\u0010+\u001a\u00020\u0003J\u0018\u0010[\u001a\u00020&2\u0006\u0010.\u001a\u00020\u00172\u0006\u0010+\u001a\u00020\u0003H\u0002J \u0010[\u001a\u00020&2\u0006\u0010.\u001a\u00020\u00172\u0006\u0010+\u001a\u00020\u00032\u0006\u0010(\u001a\u00020\tH\u0002J\u0016\u0010\\\u001a\u00020&2\u0006\u0010.\u001a\u00020\u00172\u0006\u0010+\u001a\u00020\u0003R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\u0004R\u001a\u0010\b\u001a\u00020\tX\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR\u001a\u0010\u000e\u001a\u00020\u000fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u0010\"\u0004\b\u0011\u0010\u0012R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00170\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0017\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00170\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0019R\u0016\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\t0\u001dX\u0082.\u00a2\u0006\u0004\n\u0002\u0010\u001eR\u001a\u0010\u001f\u001a\u00020 X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\"\"\u0004\b#\u0010$\u00a8\u0006]"}, d2 = {"Lcom/example/chess/data/repository/GamePlay;", "", "board", "Lcom/example/chess/data/model/Board;", "(Lcom/example/chess/data/model/Board;)V", "getBoard", "()Lcom/example/chess/data/model/Board;", "setBoard", "currentTurn", "Lcom/example/chess/data/model/Player;", "getCurrentTurn", "()Lcom/example/chess/data/model/Player;", "setCurrentTurn", "(Lcom/example/chess/data/model/Player;)V", "isGameOver", "", "()Z", "setGameOver", "(Z)V", "moveCount", "", "movesPlayed", "", "Lcom/example/chess/data/model/Move;", "getMovesPlayed", "()Ljava/util/List;", "movesUndone", "getMovesUndone", "players", "", "[Lcom/example/chess/data/model/Player;", "status", "Lcom/example/chess/data/model/GameStatus;", "getStatus", "()Lcom/example/chess/data/model/GameStatus;", "setStatus", "(Lcom/example/chess/data/model/GameStatus;)V", "changeCurrentTurn", "", "checkForWinner", "player", "chooseOpeningMove", "detectCheck", "myBoard", "detectCheckMate", "determineMove", "move", "evaluateBoard", "evaluateCaptureMove", "evaluateKingSafety", "kingSpot", "Lcom/example/chess/data/model/Spot;", "evaluateMove", "evaluateMoves", "possibleMoves", "", "evaluateMovesLevel2", "evaluatePieceValue", "piece", "Lcom/example/chess/data/model/ChessPiece;", "findBestMove", "depth", "myPlayer", "findKing", "playerColor", "Lcom/example/chess/data/model/Color;", "generateAllLegalMoves", "getAlivePieces", "getRemainingValidMoves", "Lcom/example/chess/data/model/Position;", "position", "getValidMoves", "handleAIMove", "aiLevel", "Lcom/example/chess/data/model/AILevel;", "handleHumanMove", "start", "end", "init", "p1", "p2", "isMoveValid", "isOpeningPhase", "isPieceInDanger", "spot", "isStalemate", "makeMove", "minimax", "isMaximizing", "playerMove", "redoMove", "undoMove", "undoMoveByPlayer", "app_debug"})
public final class GamePlay {
    @org.jetbrains.annotations.NotNull
    private com.example.chess.data.model.Board board;
    private com.example.chess.data.model.Player[] players;
    public com.example.chess.data.model.Player currentTurn;
    @org.jetbrains.annotations.NotNull
    private com.example.chess.data.model.GameStatus status = com.example.chess.data.model.GameStatus.ACTIVE;
    private boolean isGameOver = false;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<com.example.chess.data.model.Move> movesPlayed = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<com.example.chess.data.model.Move> movesUndone = null;
    private int moveCount = 0;
    
    @javax.inject.Inject
    public GamePlay(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Board board) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.chess.data.model.Board getBoard() {
        return null;
    }
    
    public final void setBoard(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Board p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.chess.data.model.Player getCurrentTurn() {
        return null;
    }
    
    public final void setCurrentTurn(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Player p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.chess.data.model.GameStatus getStatus() {
        return null;
    }
    
    public final void setStatus(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.GameStatus p0) {
    }
    
    public final boolean isGameOver() {
        return false;
    }
    
    public final void setGameOver(boolean p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.example.chess.data.model.Move> getMovesPlayed() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.example.chess.data.model.Move> getMovesUndone() {
        return null;
    }
    
    public final void init(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Player p1, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Player p2, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.AILevel aiLevel) {
    }
    
    public final boolean playerMove(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Player player, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Spot start, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Spot end, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.AILevel aiLevel) {
        return false;
    }
    
    private final boolean handleHumanMove(com.example.chess.data.model.Player player, com.example.chess.data.model.Spot start, com.example.chess.data.model.Spot end, com.example.chess.data.model.AILevel aiLevel) {
        return false;
    }
    
    private final boolean handleAIMove(com.example.chess.data.model.Player player, com.example.chess.data.model.AILevel aiLevel) {
        return false;
    }
    
    private final com.example.chess.data.model.Move chooseOpeningMove(com.example.chess.data.model.Player player) {
        return null;
    }
    
    private final boolean isOpeningPhase(int moveCount) {
        return false;
    }
    
    private final java.util.List<com.example.chess.data.model.Move> generateAllLegalMoves(com.example.chess.data.model.Player player, com.example.chess.data.model.Board board) {
        return null;
    }
    
    private final com.example.chess.data.model.Move evaluateMoves(java.util.List<com.example.chess.data.model.Move> possibleMoves) {
        return null;
    }
    
    private final int evaluateCaptureMove(com.example.chess.data.model.Move move) {
        return 0;
    }
    
    private final int evaluatePieceValue(com.example.chess.data.model.ChessPiece piece) {
        return 0;
    }
    
    private final int evaluateMove(com.example.chess.data.model.Move move, com.example.chess.data.model.Player player) {
        return 0;
    }
    
    private final int evaluateBoard(com.example.chess.data.model.Board board) {
        return 0;
    }
    
    private final int evaluateBoard(com.example.chess.data.model.Board board, com.example.chess.data.model.Player player) {
        return 0;
    }
    
    @androidx.annotation.WorkerThread
    private final int minimax(com.example.chess.data.model.Board board, int depth, boolean isMaximizing, com.example.chess.data.model.Player myPlayer) {
        return 0;
    }
    
    private final com.example.chess.data.model.Move findBestMove(com.example.chess.data.model.Board board, int depth, com.example.chess.data.model.Player myPlayer) {
        return null;
    }
    
    private final com.example.chess.data.model.Move evaluateMovesLevel2(java.util.List<com.example.chess.data.model.Move> possibleMoves, com.example.chess.data.model.Player player) {
        return null;
    }
    
    private final int evaluateKingSafety(com.example.chess.data.model.Spot kingSpot, com.example.chess.data.model.Board board, com.example.chess.data.model.Player player) {
        return 0;
    }
    
    private final boolean isPieceInDanger(com.example.chess.data.model.Spot spot, com.example.chess.data.model.Board board, com.example.chess.data.model.Player player) {
        return false;
    }
    
    private final boolean isMoveValid(com.example.chess.data.model.Player player, com.example.chess.data.model.Color playerColor) {
        return false;
    }
    
    private final boolean makeMove(com.example.chess.data.model.Board myBoard, com.example.chess.data.model.Move move, com.example.chess.data.model.Player player) {
        return false;
    }
    
    private final void checkForWinner(com.example.chess.data.model.Player player) {
    }
    
    private final boolean determineMove(com.example.chess.data.model.Move move, com.example.chess.data.model.Player player) {
        return false;
    }
    
    private final void changeCurrentTurn() {
    }
    
    private final java.util.List<com.example.chess.data.model.Position> getValidMoves(com.example.chess.data.model.Position position, com.example.chess.data.model.Player player) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.example.chess.data.model.Position> getRemainingValidMoves(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Position position, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Player player) {
        return null;
    }
    
    private final boolean detectCheck(com.example.chess.data.model.Player player, com.example.chess.data.model.Board myBoard) {
        return false;
    }
    
    private final boolean detectCheck(com.example.chess.data.model.Player player) {
        return false;
    }
    
    private final boolean detectCheckMate(com.example.chess.data.model.Player player) {
        return false;
    }
    
    private final com.example.chess.data.model.Spot findKing(com.example.chess.data.model.Board myBoard, com.example.chess.data.model.Color playerColor) {
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
    
    public final void undoMoveByPlayer(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Move move, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Board myBoard) {
    }
    
    public final void redoMove(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Move move, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Board myBoard) {
    }
}