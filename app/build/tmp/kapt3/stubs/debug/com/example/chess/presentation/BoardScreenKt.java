package com.example.chess.presentation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000d\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0016\n\u0002\u0018\u0002\n\u0002\b\u0006\u001a\"\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\b\b\u0010\t\u001a,\u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\u00012\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00030\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00030\rH\u0007\u001a\u001e\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00030\rH\u0007\u001a\u00f1\u0001\u0010\u0013\u001a\u00020\u00032\b\b\u0002\u0010\u0014\u001a\u00020\u00152\u0012\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00180\u00170\u00172\b\u0010\u0019\u001a\u0004\u0018\u00010\u00182\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001d2\f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020 0\u001f2\u0012\u0010!\u001a\u000e\u0012\u0004\u0012\u00020\u0018\u0012\u0004\u0012\u00020\u00030\"2\u0012\u0010#\u001a\u000e\u0012\u0004\u0012\u00020 \u0012\u0004\u0012\u00020\u00030\"2\u0012\u0010$\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00030\"2\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00030\r2\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00030\r2\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00030\r2\f\u0010(\u001a\b\u0012\u0004\u0012\u00020\u00030\r2\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00030\r2\b\u0010)\u001a\u0004\u0018\u00010\u00052\b\u0010*\u001a\u0004\u0018\u00010\u00052\u0006\u0010+\u001a\u00020 H\u0007\u00a2\u0006\u0002\u0010,\u001a\u0080\u0001\u0010-\u001a\u00020\u00032\u0006\u0010.\u001a\u00020\u001b2\u0006\u0010/\u001a\u00020\u00182\u0006\u00100\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010+\u001a\u00020 2\f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020 0\u001f2\u0012\u0010!\u001a\u000e\u0012\u0004\u0012\u00020\u0018\u0012\u0004\u0012\u00020\u00030\"2\u0012\u0010#\u001a\u000e\u0012\u0004\u0012\u00020 \u0012\u0004\u0012\u00020\u00030\"2\u0006\u0010\u0006\u001a\u00020\u0007H\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\b1\u00102\u001a$\u00103\u001a\u00020\u00032\f\u00104\u001a\b\u0012\u0004\u0012\u00020\u00030\r2\f\u00105\u001a\b\u0012\u0004\u0012\u00020\u00030\rH\u0007\u001a2\u00106\u001a\u00020\u00032\u0012\u00107\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00030\"2\u0006\u00108\u001a\u0002092\f\u00105\u001a\b\u0012\u0004\u0012\u00020\u00030\rH\u0007\u001a4\u0010:\u001a\u00020\u00032\u0006\u0010;\u001a\u00020\u00052\u0012\u0010<\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00030\"2\u0006\u0010=\u001a\u00020\u001b2\u0006\u0010\u000b\u001a\u00020\u0001H\u0007\u001a$\u0010>\u001a\u00020\u00032\f\u00104\u001a\b\u0012\u0004\u0012\u00020\u00030\r2\f\u00105\u001a\b\u0012\u0004\u0012\u00020\u00030\rH\u0007\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082T\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006?"}, d2 = {"TAG", "", "AnimatedChessPiece", "", "chessPiece", "Lcom/example/chess/data/model/ChessPiece;", "squareSize", "Landroidx/compose/ui/unit/Dp;", "AnimatedChessPiece-3ABfNKs", "(Lcom/example/chess/data/model/ChessPiece;F)V", "CheckmateDialog", "text", "onNewGameSelected", "Lkotlin/Function0;", "onViewBoardSelected", "ChessGameScreen", "chessGameViewModel", "Lcom/example/chess/presentation/ChessGameViewModel;", "onExitGame", "ChessGrid", "modifier", "Landroidx/compose/ui/Modifier;", "boardState", "", "Lcom/example/chess/data/model/Spot;", "selectedStartSpot", "isInCheck", "", "player", "Lcom/example/chess/data/model/Player;", "validMoves", "", "Lcom/example/chess/data/model/Position;", "onSelectSpot", "Lkotlin/Function1;", "onMovePieceTo", "onSetPromotionPiece", "onUndoMove", "onRedoMove", "onStartNewGame", "onQuitGame", "promotionPiece", "promotionPieceAI", "checkPosition", "(Landroidx/compose/ui/Modifier;[[Lcom/example/chess/data/model/Spot;Lcom/example/chess/data/model/Spot;ZLcom/example/chess/data/model/Player;Ljava/util/List;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lcom/example/chess/data/model/ChessPiece;Lcom/example/chess/data/model/ChessPiece;Lcom/example/chess/data/model/Position;)V", "ChessSquare", "isWhite", "spot", "isSelected", "ChessSquare-pPrIpRY", "(ZLcom/example/chess/data/model/Spot;ZLcom/example/chess/data/model/Player;ZLcom/example/chess/data/model/Position;Ljava/util/List;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;F)V", "ExitDialog", "onConfirm", "onDismiss", "PawnPromotionDialog", "onPromotionSelected", "color", "Lcom/example/chess/data/model/Color;", "PromotionOption", "piece", "onSelected", "selected", "StartGameDialog", "app_debug"})
public final class BoardScreenKt {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "BoardScreen";
    
    @androidx.compose.runtime.Composable()
    public static final void ChessGameScreen(@org.jetbrains.annotations.NotNull()
    com.example.chess.presentation.ChessGameViewModel chessGameViewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onExitGame) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void PawnPromotionDialog(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.example.chess.data.model.ChessPiece, kotlin.Unit> onPromotionSelected, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Color color, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void PromotionOption(@org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.ChessPiece piece, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.example.chess.data.model.ChessPiece, kotlin.Unit> onSelected, boolean selected, @org.jetbrains.annotations.NotNull()
    java.lang.String text) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void CheckmateDialog(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNewGameSelected, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onViewBoardSelected) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void StartGameDialog(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onConfirm, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ChessGrid(@org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Spot[][] boardState, @org.jetbrains.annotations.Nullable()
    com.example.chess.data.model.Spot selectedStartSpot, boolean isInCheck, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Player player, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.chess.data.model.Position> validMoves, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.example.chess.data.model.Spot, kotlin.Unit> onSelectSpot, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.example.chess.data.model.Position, kotlin.Unit> onMovePieceTo, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.example.chess.data.model.ChessPiece, kotlin.Unit> onSetPromotionPiece, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onUndoMove, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onRedoMove, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onStartNewGame, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onQuitGame, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onExitGame, @org.jetbrains.annotations.Nullable()
    com.example.chess.data.model.ChessPiece promotionPiece, @org.jetbrains.annotations.Nullable()
    com.example.chess.data.model.ChessPiece promotionPieceAI, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Position checkPosition) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ExitDialog(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onConfirm, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}