package com.example.chess.presentation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000~\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010!\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0011\n\u0002\u0018\u0002\n\u0002\b\u0007\u001a.\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00030\nH\u0007\u001a,\u0010\u000b\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0010\u0010\u0011\u001a,\u0010\u0012\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u00012\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00030\nH\u0007\u001a,\u0010\u0015\u001a\u00020\u00032\u0006\u0010\u0016\u001a\u00020\u00172\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00030\nH\u0007\u001a\u0088\u0002\u0010\u001a\u001a\u00020\u00032\u0006\u0010\u001b\u001a\u00020\u001c2\b\b\u0002\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 2\b\u0010!\u001a\u0004\u0018\u00010\"2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020%0$2\u0012\u0010&\u001a\u000e\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020\u00030\'2\u0012\u0010)\u001a\u000e\u0012\u0004\u0012\u00020%\u0012\u0004\u0012\u00020\u00030\'2\u0012\u0010*\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00030\'2\f\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\f\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\f\u0010.\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\f\u0010/\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\u0006\u00100\u001a\u00020%2\f\u00101\u001a\b\u0012\u0004\u0012\u00020\r022\f\u00103\u001a\b\u0012\u0004\u0012\u000204022\u0006\u00105\u001a\u00020 2\b\u00106\u001a\u0004\u0018\u00010\"H\u0007\u001a\u0084\u0001\u00107\u001a\u00020\u00032\u0006\u00108\u001a\u00020 2\u0006\u00109\u001a\u00020(2\u0006\u0010\u001f\u001a\u00020 2\u0006\u00100\u001a\u00020%2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020%0$2\u0012\u0010&\u001a\u000e\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020\u00030\'2\u0012\u0010)\u001a\u000e\u0012\u0004\u0012\u00020%\u0012\u0004\u0012\u00020\u00030\'2\u0006\u0010\u000e\u001a\u00020\u000f2\b\u0010!\u001a\u0004\u0018\u00010\"2\b\u00106\u001a\u0004\u0018\u00010\"H\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\b:\u0010;\u001a$\u0010<\u001a\u00020\u00032\f\u0010=\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\f\u0010>\u001a\b\u0012\u0004\u0012\u00020\u00030\nH\u0007\u001a8\u0010?\u001a\u00020\u00032\u000e\u0010@\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r022\f\u0010A\u001a\b\u0012\u0004\u0012\u0002040$2\b\b\u0002\u0010\u0004\u001a\u00020\u00052\u0006\u0010B\u001a\u00020\bH\u0007\u001a2\u0010C\u001a\u00020\u00032\u0012\u0010D\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00030\'2\u0006\u0010E\u001a\u00020F2\f\u0010>\u001a\b\u0012\u0004\u0012\u00020\u00030\nH\u0007\u001a4\u0010G\u001a\u00020\u00032\u0006\u0010H\u001a\u00020\r2\u0012\u0010I\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00030\'2\u0006\u0010J\u001a\u00020 2\u0006\u0010\u0006\u001a\u00020\u0001H\u0007\u001a$\u0010K\u001a\u00020\u00032\f\u0010=\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\f\u0010>\u001a\b\u0012\u0004\u0012\u00020\u00030\nH\u0007\u001a\u0016\u0010L\u001a\u00020\b2\u000e\u0010@\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0$\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082T\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006M"}, d2 = {"TAG", "", "AnalyzeGameButton", "", "modifier", "Landroidx/compose/ui/Modifier;", "text", "iconRes", "", "onClick", "Lkotlin/Function0;", "AnimatedChessPiece", "chessPiece", "Lcom/example/chess/data/model/ChessPiece;", "squareSize", "Landroidx/compose/ui/unit/Dp;", "AnimatedChessPiece-lG28NQ4", "(Lcom/example/chess/data/model/ChessPiece;FLandroidx/compose/ui/Modifier;)V", "CheckmateDialog", "onNewGameSelected", "onViewBoardSelected", "ChessGameScreen", "chessGameViewModel", "Lcom/example/chess/presentation/ChessGameViewModel;", "onExitGame", "onAnalyzeGame", "ChessGrid", "currentPlayer", "Lcom/example/chess/data/model/Player;", "boardState", "Lcom/example/chess/presentation/BoardState;", "isInCheck", "", "lastMove", "Lcom/example/chess/presentation/Move;", "validMoves", "", "Lcom/example/chess/data/model/Position;", "onSelectSpot", "Lkotlin/Function1;", "Lcom/example/chess/data/model/Spot;", "onMovePieceTo", "onSetPromotionPiece", "onUndoMove", "onRedoMove", "onStartNewGame", "onQuitGame", "onAnalzeGame", "checkPosition", "piecesKilled", "", "movesMadeSan", "Lcom/example/chess/data/model/SANMovePair;", "showPromotionDialog", "aiLastMove", "ChessSquare", "isWhite", "spot", "ChessSquare-1sJa4KU", "(ZLcom/example/chess/data/model/Spot;ZLcom/example/chess/data/model/Position;Ljava/util/List;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;FLcom/example/chess/presentation/Move;Lcom/example/chess/presentation/Move;)V", "ExitDialog", "onConfirm", "onDismiss", "Panel", "lostPieces", "moves", "advantage", "PawnPromotionDialog", "onPromotionSelected", "color", "Lcom/example/chess/data/model/Color;", "PromotionOption", "piece", "onSelected", "selected", "StartGameDialog", "calculateMaterialAdvantage", "app_debug"})
public final class BoardScreenKt {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "BoardScreen";
    
    @androidx.compose.runtime.Composable()
    public static final void ChessGameScreen(@org.jetbrains.annotations.NotNull()
    com.example.chess.presentation.ChessGameViewModel chessGameViewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onExitGame, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onAnalyzeGame) {
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
    com.example.chess.data.model.Player currentPlayer, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull()
    com.example.chess.presentation.BoardState boardState, boolean isInCheck, @org.jetbrains.annotations.Nullable()
    com.example.chess.presentation.Move lastMove, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.chess.data.model.Position> validMoves, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.example.chess.data.model.Spot, kotlin.Unit> onSelectSpot, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.example.chess.data.model.Position, kotlin.Unit> onMovePieceTo, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.example.chess.data.model.ChessPiece, kotlin.Unit> onSetPromotionPiece, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onUndoMove, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onRedoMove, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onStartNewGame, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onQuitGame, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onExitGame, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onAnalzeGame, @org.jetbrains.annotations.NotNull()
    com.example.chess.data.model.Position checkPosition, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.chess.data.model.ChessPiece> piecesKilled, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.chess.data.model.SANMovePair> movesMadeSan, boolean showPromotionDialog, @org.jetbrains.annotations.Nullable()
    com.example.chess.presentation.Move aiLastMove) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ExitDialog(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onConfirm, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void AnalyzeGameButton(@org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull()
    java.lang.String text, int iconRes, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void Panel(@org.jetbrains.annotations.NotNull()
    java.util.List<com.example.chess.data.model.ChessPiece> lostPieces, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.chess.data.model.SANMovePair> moves, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, int advantage) {
    }
    
    public static final int calculateMaterialAdvantage(@org.jetbrains.annotations.NotNull()
    java.util.List<? extends com.example.chess.data.model.ChessPiece> lostPieces) {
        return 0;
    }
}