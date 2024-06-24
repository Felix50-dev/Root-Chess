package com.example.chess.presentation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u0002\n\u0002\b\u0014\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020\u0014J\u000e\u0010-\u001a\u00020+2\u0006\u0010.\u001a\u00020\u0011J \u0010/\u001a\u00020\t2\u0006\u00100\u001a\u00020\u00112\u0006\u00101\u001a\u00020\u00112\u0006\u00102\u001a\u00020\rH\u0002J\u0006\u00103\u001a\u00020+J\u0006\u00104\u001a\u00020+J\u000e\u00105\u001a\u00020+2\u0006\u00102\u001a\u00020\rJ\u000e\u00106\u001a\u00020+2\u0006\u00107\u001a\u00020\tJ\u000e\u00108\u001a\u00020+2\u0006\u0010\u001b\u001a\u00020\u000bJ\u000e\u00109\u001a\u00020+2\u0006\u0010:\u001a\u00020\u000fJ\u0006\u0010;\u001a\u00020+J\u0006\u0010<\u001a\u00020+J\u0010\u0010=\u001a\u00020+2\u0006\u0010>\u001a\u00020\u0014H\u0002R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000e\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0010\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00110\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u00130\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00070\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0017\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\t0\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0018R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0018R\u0017\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\r0\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0018R\u000e\u0010\u001f\u001a\u00020 X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020 X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\"\u0010\"\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\u0016X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b#\u0010\u0018\"\u0004\b$\u0010%R\u0019\u0010&\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00110\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u0018R\u001d\u0010(\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u00130\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u0018\u00a8\u0006?"}, d2 = {"Lcom/example/chess/presentation/ChessGameViewModel;", "Landroidx/lifecycle/ViewModel;", "game", "Lcom/example/chess/data/repository/GamePlay;", "(Lcom/example/chess/data/repository/GamePlay;)V", "_boardState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/chess/presentation/BoardState;", "_color", "", "_gameMode", "Lcom/example/chess/data/model/GameMode;", "_level", "Lcom/example/chess/data/model/AILevel;", "_promotionPiece", "Lcom/example/chess/data/model/ChessPiece;", "_selectedPiecePosition", "Lcom/example/chess/data/model/Spot;", "_validMoves", "", "Lcom/example/chess/data/model/Position;", "boardState", "Lkotlinx/coroutines/flow/StateFlow;", "getBoardState", "()Lkotlinx/coroutines/flow/StateFlow;", "color", "getColor", "gameMode", "getGameMode", "level", "getLevel", "playerDark", "Lcom/example/chess/data/model/Player;", "playerWhite", "promotionPiece", "getPromotionPiece", "setPromotionPiece", "(Lkotlinx/coroutines/flow/StateFlow;)V", "selectedStartSpot", "getSelectedStartSpot", "validMoves", "getValidMoves", "movePieceTo", "", "targetPosition", "onSelectSpot", "spot", "playerMove", "start", "end", "aiLevel", "quitGame", "redoMove", "setAILevel", "setColor", "isWhite", "setGameMode", "setPromotedPiece", "promotedPiece", "startNewGame", "undoMove", "updateValidMoves", "position", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class ChessGameViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.example.chess.data.repository.GamePlay game = null;
    @org.jetbrains.annotations.NotNull
    private final com.example.chess.data.model.Player playerWhite = null;
    @org.jetbrains.annotations.NotNull
    private final com.example.chess.data.model.Player playerDark = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.presentation.BoardState> _boardState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.example.chess.presentation.BoardState> boardState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _color = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> color = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.data.model.AILevel> _level = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.AILevel> level = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.data.model.GameMode> _gameMode = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.GameMode> gameMode = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.chess.data.model.Position>> _validMoves = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.chess.data.model.Position>> validMoves = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.data.model.Spot> _selectedPiecePosition = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.Spot> selectedStartSpot = null;
    @org.jetbrains.annotations.NotNull
    private kotlinx.coroutines.flow.MutableStateFlow<com.example.chess.data.model.ChessPiece> _promotionPiece;
    @org.jetbrains.annotations.NotNull
    private kotlinx.coroutines.flow.StateFlow<? extends com.example.chess.data.model.ChessPiece> promotionPiece;
    
    @javax.inject.Inject
    public ChessGameViewModel(@org.jetbrains.annotations.NotNull
    com.example.chess.data.repository.GamePlay game) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.example.chess.presentation.BoardState> getBoardState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getColor() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.AILevel> getLevel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.GameMode> getGameMode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.chess.data.model.Position>> getValidMoves() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.Spot> getSelectedStartSpot() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.example.chess.data.model.ChessPiece> getPromotionPiece() {
        return null;
    }
    
    public final void setPromotionPiece(@org.jetbrains.annotations.NotNull
    kotlinx.coroutines.flow.StateFlow<? extends com.example.chess.data.model.ChessPiece> p0) {
    }
    
    public final void setGameMode(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.GameMode gameMode) {
    }
    
    public final void setAILevel(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.AILevel aiLevel) {
    }
    
    public final void setColor(boolean isWhite) {
    }
    
    public final void onSelectSpot(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Spot spot) {
    }
    
    public final void setPromotedPiece(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.ChessPiece promotedPiece) {
    }
    
    public final void startNewGame() {
    }
    
    public final void quitGame() {
    }
    
    public final void movePieceTo(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Position targetPosition) {
    }
    
    private final void updateValidMoves(com.example.chess.data.model.Position position) {
    }
    
    private final boolean playerMove(com.example.chess.data.model.Spot start, com.example.chess.data.model.Spot end, com.example.chess.data.model.AILevel aiLevel) {
        return false;
    }
    
    public final void undoMove() {
    }
    
    public final void redoMove() {
    }
}