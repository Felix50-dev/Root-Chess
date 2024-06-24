package com.example.chess.presentation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0006\u001a-\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0007\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\n\u0010\u000b\u001a,\u0010\f\u001a\u00020\u00032\u0006\u0010\r\u001a\u00020\u00012\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00030\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00030\u000fH\u0007\u001a\u001e\u0010\u0011\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\u00132\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00030\u000fH\u0007\u001a\u00cf\u0001\u0010\u0015\u001a\u00020\u00032\b\b\u0002\u0010\u0016\u001a\u00020\u00172\u0012\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001a0\u00190\u00192\b\u0010\u001b\u001a\u0004\u0018\u00010\u001a2\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\t0\u001d2\u0012\u0010\u001e\u001a\u000e\u0012\u0004\u0012\u00020\u001a\u0012\u0004\u0012\u00020\u00030\u001f2\u0012\u0010 \u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00030\u001f2\u0012\u0010!\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00030\u001f2\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00030\u000f2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00030\u000f2\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00030\u000f2\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00030\u000f2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00030\u000f2\b\u0010&\u001a\u0004\u0018\u00010\u0005H\u0007\u00a2\u0006\u0002\u0010\'\u001ak\u0010(\u001a\u00020\u00032\u0006\u0010)\u001a\u00020*2\u0006\u0010+\u001a\u00020\u001a2\u0006\u0010,\u001a\u00020*2\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\t0\u001d2\u0012\u0010\u001e\u001a\u000e\u0012\u0004\u0012\u00020\u001a\u0012\u0004\u0012\u00020\u00030\u001f2\u0012\u0010 \u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00030\u001f2\u0006\u0010\u0006\u001a\u00020\u0007H\u0007\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b-\u0010.\u001a$\u0010/\u001a\u00020\u00032\f\u00100\u001a\b\u0012\u0004\u0012\u00020\u00030\u000f2\f\u00101\u001a\b\u0012\u0004\u0012\u00020\u00030\u000fH\u0007\u001a2\u00102\u001a\u00020\u00032\u0012\u00103\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00030\u001f2\u0006\u00104\u001a\u0002052\f\u00101\u001a\b\u0012\u0004\u0012\u00020\u00030\u000fH\u0007\u001a4\u00106\u001a\u00020\u00032\u0006\u00107\u001a\u00020\u00052\u0012\u00108\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00030\u001f2\u0006\u00109\u001a\u00020*2\u0006\u0010\r\u001a\u00020\u0001H\u0007\u001a$\u0010:\u001a\u00020\u00032\f\u00100\u001a\b\u0012\u0004\u0012\u00020\u00030\u000f2\f\u00101\u001a\b\u0012\u0004\u0012\u00020\u00030\u000fH\u0007\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082T\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0005\b\u00a1\u001e0\u0001\n\u0002\b\u0019\u00a8\u0006;"}, d2 = {"TAG", "", "AnimatedChessPiece", "", "chessPiece", "Lcom/example/chess/data/model/ChessPiece;", "squareSize", "Landroidx/compose/ui/unit/Dp;", "currentPosition", "Lcom/example/chess/data/model/Position;", "AnimatedChessPiece-lG28NQ4", "(Lcom/example/chess/data/model/ChessPiece;FLcom/example/chess/data/model/Position;)V", "CheckmateDialog", "text", "onNewGameSelected", "Lkotlin/Function0;", "onViewBoardSelected", "ChessGameScreen", "chessGameViewModel", "Lcom/example/chess/presentation/ChessGameViewModel;", "onExitGame", "ChessGrid", "modifier", "Landroidx/compose/ui/Modifier;", "boardState", "", "Lcom/example/chess/data/model/Spot;", "selectedStartSpot", "validMoves", "", "onSelectSpot", "Lkotlin/Function1;", "onMovePieceTo", "onSetPromotionPiece", "onUndoMove", "onRedoMove", "onStartNewGame", "onQuitGame", "promotionPiece", "(Landroidx/compose/ui/Modifier;[[Lcom/example/chess/data/model/Spot;Lcom/example/chess/data/model/Spot;Ljava/util/List;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lcom/example/chess/data/model/ChessPiece;)V", "ChessSquare", "isWhite", "", "spot", "isSelected", "ChessSquare-6ZxE2Lo", "(ZLcom/example/chess/data/model/Spot;ZLjava/util/List;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;F)V", "ExitDialog", "onConfirm", "onDismiss", "PawnPromotionDialog", "onPromotionSelected", "color", "Lcom/example/chess/data/model/Color;", "PromotionOption", "piece", "onSelected", "selected", "StartGameDialog", "app_debug"})
public final class BoardScreenKt {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "BoardScreen";
    
    @androidx.compose.runtime.Composable
    public static final void ChessGameScreen(@org.jetbrains.annotations.NotNull
    com.example.chess.presentation.ChessGameViewModel chessGameViewModel, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onExitGame) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void ChessGrid(@org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Spot[][] boardState, @org.jetbrains.annotations.Nullable
    com.example.chess.data.model.Spot selectedStartSpot, @org.jetbrains.annotations.NotNull
    java.util.List<com.example.chess.data.model.Position> validMoves, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.chess.data.model.Spot, kotlin.Unit> onSelectSpot, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.chess.data.model.Position, kotlin.Unit> onMovePieceTo, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.chess.data.model.ChessPiece, kotlin.Unit> onSetPromotionPiece, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onUndoMove, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onRedoMove, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onStartNewGame, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onQuitGame, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onExitGame, @org.jetbrains.annotations.Nullable
    com.example.chess.data.model.ChessPiece promotionPiece) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    public static final void PawnPromotionDialog(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.chess.data.model.ChessPiece, kotlin.Unit> onPromotionSelected, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Color color, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void PromotionOption(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.ChessPiece piece, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.chess.data.model.ChessPiece, kotlin.Unit> onSelected, boolean selected, @org.jetbrains.annotations.NotNull
    java.lang.String text) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    public static final void CheckmateDialog(@org.jetbrains.annotations.NotNull
    java.lang.String text, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNewGameSelected, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onViewBoardSelected) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void ExitDialog(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onConfirm, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void StartGameDialog(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onConfirm, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}