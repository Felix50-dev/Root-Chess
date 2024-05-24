package com.example.chess.presentation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\n\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u000eJ\u000e\u0010!\u001a\u00020\u001f2\u0006\u0010\"\u001a\u00020\u000bJ\u0018\u0010#\u001a\u00020$2\u0006\u0010%\u001a\u00020\u000b2\u0006\u0010&\u001a\u00020\u000bH\u0002J\u0006\u0010\'\u001a\u00020\u001fJ\u000e\u0010(\u001a\u00020\u001f2\u0006\u0010)\u001a\u00020\tJ\u0006\u0010*\u001a\u00020\u001fJ\u0006\u0010+\u001a\u00020\u001fJ\u0010\u0010,\u001a\u00020\u001f2\u0006\u0010-\u001a\u00020\u000eH\u0002R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00070\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\"\u0010\u0016\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0012\"\u0004\b\u0018\u0010\u0019R\u0019\u0010\u001a\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0012R\u001d\u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0012\u00a8\u0006."}, d2 = {"Lcom/example/chess/presentation/ChessGameViewModel;", "Landroidx/lifecycle/ViewModel;", "game", "Lcom/example/chess/data/repository/GamePlay;", "(Lcom/example/chess/data/repository/GamePlay;)V", "_boardState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/chess/presentation/BoardState;", "_promotionPiece", "Lcom/example/chess/data/model/ChessPiece;", "_selectedPiecePosition", "Lcom/example/chess/data/model/Spot;", "_validMoves", "", "Lcom/example/chess/data/model/Position;", "boardState", "Lkotlinx/coroutines/flow/StateFlow;", "getBoardState", "()Lkotlinx/coroutines/flow/StateFlow;", "playerDark", "Lcom/example/chess/data/model/Player;", "playerWhite", "promotionPiece", "getPromotionPiece", "setPromotionPiece", "(Lkotlinx/coroutines/flow/StateFlow;)V", "selectedStartSpot", "getSelectedStartSpot", "validMoves", "getValidMoves", "movePieceTo", "", "targetPosition", "onSelectSpot", "spot", "playerMove", "", "start", "end", "redoMove", "setPromotedPiece", "promotedPiece", "startNewGame", "undoMove", "updateValidMoves", "position", "app_debug"})
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
    
    public final void onSelectSpot(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Spot spot) {
    }
    
    public final void setPromotedPiece(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.ChessPiece promotedPiece) {
    }
    
    public final void startNewGame() {
    }
    
    public final void movePieceTo(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Position targetPosition) {
    }
    
    private final void updateValidMoves(com.example.chess.data.model.Position position) {
    }
    
    private final boolean playerMove(com.example.chess.data.model.Spot start, com.example.chess.data.model.Spot end) {
        return false;
    }
    
    public final void undoMove() {
    }
    
    public final void redoMove() {
    }
}