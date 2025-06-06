package com.example.chess.presentation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u009c\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0013\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b!\b\u0007\u0018\u00002\u00020\u0001B\u0019\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010O\u001a\u00020PJ\u0010\u0010Q\u001a\u00020P2\u0006\u0010R\u001a\u00020!H\u0002J\u0010\u0010S\u001a\u00020P2\u0006\u0010R\u001a\u00020!H\u0002J\u000f\u0010T\u001a\u0004\u0018\u00010\u0011H\u0002\u00a2\u0006\u0002\u0010UJ\b\u0010V\u001a\u00020PH\u0002J\u0016\u0010W\u001a\u00020P2\u0006\u0010X\u001a\u00020DH\u0082@\u00a2\u0006\u0002\u0010YJ\u000e\u0010Z\u001a\u00020P2\u0006\u0010\u0004\u001a\u00020\u0005J \u0010[\u001a\u00020\u00112\u0006\u0010X\u001a\u00020D2\u0006\u0010\\\u001a\u00020!2\u0006\u0010]\u001a\u00020!H\u0002J\u000e\u0010^\u001a\u00020P2\u0006\u0010_\u001a\u00020\u000fJ\u000e\u0010`\u001a\u00020P2\u0006\u0010a\u001a\u00020!J\u0018\u0010b\u001a\u00020\u00112\u0006\u0010\\\u001a\u00020!2\u0006\u0010]\u001a\u00020!H\u0002J\u0006\u0010c\u001a\u00020PJ\u0006\u0010d\u001a\u00020PJ\u000e\u0010e\u001a\u00020P2\u0006\u0010f\u001a\u00020\u0017J\u000e\u0010g\u001a\u00020P2\u0006\u0010h\u001a\u00020\u0011J\u000e\u0010i\u001a\u00020P2\u0006\u0010j\u001a\u00020\u0013J\u000e\u0010k\u001a\u00020P2\u0006\u0010l\u001a\u00020\u001eJ\u0006\u0010m\u001a\u00020PJ\u0006\u0010n\u001a\u00020PJ\u0010\u0010o\u001a\u00020P2\u0006\u0010p\u001a\u00020\u000fH\u0002R\u0016\u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00110\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0015\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00170\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001a0\u00190\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001c0\u00190\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001e0\u00190\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u001f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001e0\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010 \u001a\n\u0012\u0006\u0012\u0004\u0018\u00010!0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00110\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010#\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0$0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010%\u001a\u0004\u0018\u00010&X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0019\u0010\'\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0(\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010*R\u0019\u0010+\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0(\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010*R\u0017\u0010-\u001a\b\u0012\u0004\u0012\u00020\r0(\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010*R\u000e\u0010/\u001a\u000200X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u00101\u001a\b\u0012\u0004\u0012\u00020\u000f0(\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010*R\u0017\u00103\u001a\b\u0012\u0004\u0012\u00020\u00110(\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010*R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u00105\u001a\b\u0012\u0004\u0012\u00020\u00110(\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u0010*R\u0019\u00106\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0(\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u0010*R\u0017\u00108\u001a\b\u0012\u0004\u0012\u00020\u00170(\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u0010*R\u000e\u0010:\u001a\u000200X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001d\u0010;\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001a0\u00190(\u00a2\u0006\b\n\u0000\u001a\u0004\b<\u0010*R\u001d\u0010=\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001c0\u00190(\u00a2\u0006\b\n\u0000\u001a\u0004\b>\u0010*R&\u0010?\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001e0\u00190(X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b@\u0010*\"\u0004\bA\u0010BR\u000e\u0010C\u001a\u00020DX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010E\u001a\u00020DX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\"\u0010F\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001e0(X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bG\u0010*\"\u0004\bH\u0010BR\u0017\u0010I\u001a\b\u0012\u0004\u0012\u00020\u00110(\u00a2\u0006\b\n\u0000\u001a\u0004\bJ\u0010*R\u000e\u0010K\u001a\u00020LX\u0082.\u00a2\u0006\u0002\n\u0000R\u001d\u0010M\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0$0(\u00a2\u0006\b\n\u0000\u001a\u0004\bN\u0010*\u00a8\u0006q"}, d2 = {"Lcom/example/chess/presentation/ChessGameViewModel;", "Landroidx/lifecycle/ViewModel;", "game", "Lcom/example/chess/data/repository/GamePlay;", "context", "Landroid/content/Context;", "(Lcom/example/chess/data/repository/GamePlay;Landroid/content/Context;)V", "_aiLastMove", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/chess/presentation/Move;", "_analysisResult", "Lcom/example/chess/data/model/AnalysisResult;", "_boardState", "Lcom/example/chess/presentation/BoardState;", "_checkPosition", "Lcom/example/chess/data/model/Position;", "_color", "", "_gameMode", "Lcom/example/chess/data/model/GameMode;", "_isInCheck", "_lastMove", "_level", "Lcom/example/chess/data/model/AILevel;", "_movesMade", "", "", "_movesMadeSAN", "Lcom/example/chess/data/model/SANMovePair;", "_piecesKilled", "Lcom/example/chess/data/model/ChessPiece;", "_promotionPiece", "_selectedPiecePosition", "Lcom/example/chess/data/model/Spot;", "_showPawnPromotion", "_validMoves", "", "aiJob", "Lkotlinx/coroutines/Job;", "aiLastMove", "Lkotlinx/coroutines/flow/StateFlow;", "getAiLastMove", "()Lkotlinx/coroutines/flow/StateFlow;", "analysisResult", "getAnalysisResult", "boardState", "getBoardState", "captureSoundId", "", "checkPosition", "getCheckPosition", "color", "getColor", "isInCheck", "lastMove", "getLastMove", "level", "getLevel", "moveSoundId", "movesMade", "getMovesMade", "movesMadeSAN", "getMovesMadeSAN", "piecesKilled", "getPiecesKilled", "setPiecesKilled", "(Lkotlinx/coroutines/flow/StateFlow;)V", "playerDark", "Lcom/example/chess/data/model/Player;", "playerWhite", "promotionPiece", "getPromotionPiece", "setPromotionPiece", "showPawnPromotion", "getShowPawnPromotion", "soundPool", "Landroid/media/SoundPool;", "validMoves", "getValidMoves", "analyzeGame", "", "applyPromotion", "targetSpot", "checkForPawnPromotion", "detectCheck", "()Ljava/lang/Boolean;", "getKingPosition", "handleAIMoveAndUpdateState", "player", "(Lcom/example/chess/data/model/Player;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "initializeSounds", "makeMove", "start", "end", "movePieceTo", "targetPosition", "onSelectSpot", "spot", "playerMove", "quitGame", "redoMove", "setAILevel", "aiLevel", "setColor", "isWhite", "setGameMode", "gameMode", "setPromotedPiece", "promotedPiece", "startNewGame", "undoMove", "updateValidMoves", "position", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ChessGameViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.chess.data.repository.GamePlay game = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.presentation.Move> _lastMove = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.chess.presentation.Move> lastMove = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.presentation.Move> _aiLastMove = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.chess.presentation.Move> aiLastMove = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _showPawnPromotion = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> showPawnPromotion = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.chess.data.model.Player playerWhite = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.chess.data.model.Player playerDark = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.presentation.BoardState> _boardState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.chess.presentation.BoardState> boardState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isInCheck = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isInCheck = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.data.model.Position> _checkPosition = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.Position> checkPosition = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _color = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> color = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.data.model.AILevel> _level = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.AILevel> level = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.data.model.GameMode> _gameMode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.chess.data.model.Position>> _validMoves = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.chess.data.model.Position>> validMoves = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.data.model.Spot> _selectedPiecePosition = null;
    @org.jetbrains.annotations.NotNull()
    private kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.data.model.ChessPiece> _promotionPiece;
    @org.jetbrains.annotations.NotNull()
    private kotlinx.coroutines.flow.StateFlow<? extends com.example.chess.data.model.ChessPiece> promotionPiece;
    @org.jetbrains.annotations.NotNull()
    private kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.chess.data.model.ChessPiece>> _piecesKilled;
    @org.jetbrains.annotations.NotNull()
    private kotlinx.coroutines.flow.StateFlow<? extends java.util.List<com.example.chess.data.model.ChessPiece>> piecesKilled;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<java.lang.String>> _movesMade = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.String>> movesMade = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.chess.data.model.SANMovePair>> _movesMadeSAN = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.chess.data.model.SANMovePair>> movesMadeSAN = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.data.model.AnalysisResult> _analysisResult = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.AnalysisResult> analysisResult = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job aiJob;
    private android.media.SoundPool soundPool;
    private int moveSoundId = 0;
    private int captureSoundId = 0;
    
    @javax.inject.Inject()
    public ChessGameViewModel(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.repository.GamePlay game, @dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.chess.presentation.Move> getLastMove() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.chess.presentation.Move> getAiLastMove() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getShowPawnPromotion() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.chess.presentation.BoardState> getBoardState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isInCheck() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.Position> getCheckPosition() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getColor() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.AILevel> getLevel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.chess.data.model.Position>> getValidMoves() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.ChessPiece> getPromotionPiece() {
        return null;
    }
    
    public final void setPromotionPiece(@org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.flow.StateFlow<? extends com.example.chess.data.model.ChessPiece> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.chess.data.model.ChessPiece>> getPiecesKilled() {
        return null;
    }
    
    public final void setPiecesKilled(@org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.flow.StateFlow<? extends java.util.List<com.example.chess.data.model.ChessPiece>> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.String>> getMovesMade() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.chess.data.model.SANMovePair>> getMovesMadeSAN() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.AnalysisResult> getAnalysisResult() {
        return null;
    }
    
    public final void initializeSounds(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void setGameMode(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.GameMode gameMode) {
    }
    
    public final void setAILevel(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.AILevel aiLevel) {
    }
    
    public final void setColor(boolean isWhite) {
    }
    
    public final void analyzeGame() {
    }
    
    private final java.lang.Object handleAIMoveAndUpdateState(com.example.chess.data.model.Player player, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void onSelectSpot(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Spot spot) {
    }
    
    public final void setPromotedPiece(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.ChessPiece promotedPiece) {
    }
    
    public final void startNewGame() {
    }
    
    public final void quitGame() {
    }
    
    private final java.lang.Boolean detectCheck() {
        return null;
    }
    
    private final void getKingPosition() {
    }
    
    private final void checkForPawnPromotion(com.example.chess.data.model.Spot targetSpot) {
    }
    
    private final void applyPromotion(com.example.chess.data.model.Spot targetSpot) {
    }
    
    public final void movePieceTo(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Position targetPosition) {
    }
    
    private final void updateValidMoves(com.example.chess.data.model.Position position) {
    }
    
    private final boolean playerMove(com.example.chess.data.model.Spot start, com.example.chess.data.model.Spot end) {
        return false;
    }
    
    private final boolean makeMove(com.example.chess.data.model.Player player, com.example.chess.data.model.Spot start, com.example.chess.data.model.Spot end) {
        return false;
    }
    
    public final void undoMove() {
    }
    
    public final void redoMove() {
    }
}