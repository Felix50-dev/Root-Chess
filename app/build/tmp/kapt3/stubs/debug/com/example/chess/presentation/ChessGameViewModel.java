package com.example.chess.presentation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0010\u0002\n\u0002\b\u0019\b\u0007\u0018\u00002\u00020\u0001B\u0019\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000f\u00101\u001a\u0004\u0018\u00010\rH\u0002\u00a2\u0006\u0002\u00102J\b\u00103\u001a\u000204H\u0002J&\u00105\u001a\u00020\r2\u0006\u00106\u001a\u00020%2\u0006\u00107\u001a\u00020\u00162\u0006\u00108\u001a\u00020\u0016H\u0082@\u00a2\u0006\u0002\u00109J\u000e\u0010:\u001a\u0002042\u0006\u0010;\u001a\u00020\u000bJ\u000e\u0010<\u001a\u0002042\u0006\u0010=\u001a\u00020\u0016J\u0018\u0010>\u001a\u00020\r2\u0006\u00107\u001a\u00020\u00162\u0006\u00108\u001a\u00020\u0016H\u0002J\u0006\u0010?\u001a\u000204J\u0006\u0010@\u001a\u000204J\u000e\u0010A\u001a\u0002042\u0006\u0010B\u001a\u00020\u0012J\u000e\u0010C\u001a\u0002042\u0006\u0010D\u001a\u00020\rJ\u000e\u0010E\u001a\u0002042\u0006\u0010F\u001a\u00020\u000fJ\u000e\u0010G\u001a\u0002042\u0006\u0010H\u001a\u00020\u0014J\u0006\u0010I\u001a\u000204J\u0006\u0010J\u001a\u000204J\u0010\u0010K\u001a\u0002042\u0006\u0010L\u001a\u00020\u000bH\u0002R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\r0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0013\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00140\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0015\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00160\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\u00180\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\t0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0017\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u000b0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001cR\u0017\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\r0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001cR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010!\u001a\b\u0012\u0004\u0012\u00020\r0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001cR\u0017\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00120\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u001cR\u000e\u0010$\u001a\u00020%X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010&\u001a\u00020%X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\"\u0010\'\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00140\u001aX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b(\u0010\u001c\"\u0004\b)\u0010*R\u0019\u0010+\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00140\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010\u001cR\u0019\u0010-\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00160\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010\u001cR\u001d\u0010/\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\u00180\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010\u001c\u00a8\u0006M"}, d2 = {"Lcom/example/chess/presentation/ChessGameViewModel;", "Landroidx/lifecycle/ViewModel;", "game", "Lcom/example/chess/data/repository/GamePlay;", "context", "Landroid/content/Context;", "(Lcom/example/chess/data/repository/GamePlay;Landroid/content/Context;)V", "_boardState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/chess/presentation/BoardState;", "_checkPosition", "Lcom/example/chess/data/model/Position;", "_color", "", "_gameMode", "Lcom/example/chess/data/model/GameMode;", "_isInCheck", "_level", "Lcom/example/chess/data/model/AILevel;", "_promotionPiece", "Lcom/example/chess/data/model/ChessPiece;", "_selectedPiecePosition", "Lcom/example/chess/data/model/Spot;", "_validMoves", "", "boardState", "Lkotlinx/coroutines/flow/StateFlow;", "getBoardState", "()Lkotlinx/coroutines/flow/StateFlow;", "checkPosition", "getCheckPosition", "color", "getColor", "isInCheck", "level", "getLevel", "playerDark", "Lcom/example/chess/data/model/Player;", "playerWhite", "promotionPiece", "getPromotionPiece", "setPromotionPiece", "(Lkotlinx/coroutines/flow/StateFlow;)V", "promotionPieceAI", "getPromotionPieceAI", "selectedStartSpot", "getSelectedStartSpot", "validMoves", "getValidMoves", "detectCheck", "()Ljava/lang/Boolean;", "getKingPosition", "", "makeMove", "player", "start", "end", "(Lcom/example/chess/data/model/Player;Lcom/example/chess/data/model/Spot;Lcom/example/chess/data/model/Spot;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "movePieceTo", "targetPosition", "onSelectSpot", "spot", "playerMove", "quitGame", "redoMove", "setAILevel", "aiLevel", "setColor", "isWhite", "setGameMode", "gameMode", "setPromotedPiece", "promotedPiece", "startNewGame", "undoMove", "updateValidMoves", "position", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ChessGameViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.chess.data.repository.GamePlay game = null;
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
    private final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.Spot> selectedStartSpot = null;
    @org.jetbrains.annotations.NotNull()
    private kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.data.model.ChessPiece> _promotionPiece;
    @org.jetbrains.annotations.NotNull()
    private kotlinx.coroutines.flow.StateFlow<? extends com.example.chess.data.model.ChessPiece> promotionPiece;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.ChessPiece> promotionPieceAI = null;
    
    @javax.inject.Inject()
    public ChessGameViewModel(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.repository.GamePlay game, @dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
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
    public final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.Spot> getSelectedStartSpot() {
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
    public final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.ChessPiece> getPromotionPieceAI() {
        return null;
    }
    
    public final void setGameMode(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.GameMode gameMode) {
    }
    
    public final void setAILevel(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.AILevel aiLevel) {
    }
    
    public final void setColor(boolean isWhite) {
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
    
    public final void movePieceTo(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Position targetPosition) {
    }
    
    private final void updateValidMoves(com.example.chess.data.model.Position position) {
    }
    
    private final boolean playerMove(com.example.chess.data.model.Spot start, com.example.chess.data.model.Spot end) {
        return false;
    }
    
    private final java.lang.Object makeMove(com.example.chess.data.model.Player player, com.example.chess.data.model.Spot start, com.example.chess.data.model.Spot end, kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    public final void undoMove() {
    }
    
    public final void redoMove() {
    }
}