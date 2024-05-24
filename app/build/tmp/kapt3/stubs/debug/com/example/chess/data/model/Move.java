package com.example.chess.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u001b\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001BG\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\b\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\rJ\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u0006H\u00c6\u0003J\u000b\u0010\u001f\u001a\u0004\u0018\u00010\bH\u00c6\u0003J\u000b\u0010 \u001a\u0004\u0018\u00010\bH\u00c6\u0003J\t\u0010!\u001a\u00020\u000bH\u00c6\u0003J\t\u0010\"\u001a\u00020\u000bH\u00c6\u0003JS\u0010#\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\u000bH\u00c6\u0001J\u0013\u0010$\u001a\u00020\u000b2\b\u0010%\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010&\u001a\u00020\'H\u00d6\u0001J\t\u0010(\u001a\u00020)H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u001a\u0010\n\u001a\u00020\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u0010\"\u0004\b\u0011\u0010\u0012R\u001a\u0010\f\u001a\u00020\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\u0010\"\u0004\b\u0013\u0010\u0012R\u001c\u0010\t\u001a\u0004\u0018\u00010\bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\u0015\"\u0004\b\u0016\u0010\u0017R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0015R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u000f\u00a8\u0006*"}, d2 = {"Lcom/example/chess/data/model/Move;", "", "from", "Lcom/example/chess/data/model/Spot;", "to", "player", "Lcom/example/chess/data/model/Player;", "pieceMoved", "Lcom/example/chess/data/model/ChessPiece;", "pieceKilled", "isCastlingMove", "", "isEnPassant", "(Lcom/example/chess/data/model/Spot;Lcom/example/chess/data/model/Spot;Lcom/example/chess/data/model/Player;Lcom/example/chess/data/model/ChessPiece;Lcom/example/chess/data/model/ChessPiece;ZZ)V", "getFrom", "()Lcom/example/chess/data/model/Spot;", "()Z", "setCastlingMove", "(Z)V", "setEnPassant", "getPieceKilled", "()Lcom/example/chess/data/model/ChessPiece;", "setPieceKilled", "(Lcom/example/chess/data/model/ChessPiece;)V", "getPieceMoved", "getPlayer", "()Lcom/example/chess/data/model/Player;", "getTo", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "other", "hashCode", "", "toString", "", "app_debug"})
public final class Move {
    @org.jetbrains.annotations.NotNull
    private final com.example.chess.data.model.Spot from = null;
    @org.jetbrains.annotations.NotNull
    private final com.example.chess.data.model.Spot to = null;
    @org.jetbrains.annotations.NotNull
    private final com.example.chess.data.model.Player player = null;
    @org.jetbrains.annotations.Nullable
    private final com.example.chess.data.model.ChessPiece pieceMoved = null;
    @org.jetbrains.annotations.Nullable
    private com.example.chess.data.model.ChessPiece pieceKilled;
    private boolean isCastlingMove;
    private boolean isEnPassant;
    
    public Move(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Spot from, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Spot to, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Player player, @org.jetbrains.annotations.Nullable
    com.example.chess.data.model.ChessPiece pieceMoved, @org.jetbrains.annotations.Nullable
    com.example.chess.data.model.ChessPiece pieceKilled, boolean isCastlingMove, boolean isEnPassant) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.chess.data.model.Spot getFrom() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.chess.data.model.Spot getTo() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.chess.data.model.Player getPlayer() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.example.chess.data.model.ChessPiece getPieceMoved() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.example.chess.data.model.ChessPiece getPieceKilled() {
        return null;
    }
    
    public final void setPieceKilled(@org.jetbrains.annotations.Nullable
    com.example.chess.data.model.ChessPiece p0) {
    }
    
    public final boolean isCastlingMove() {
        return false;
    }
    
    public final void setCastlingMove(boolean p0) {
    }
    
    public final boolean isEnPassant() {
        return false;
    }
    
    public final void setEnPassant(boolean p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.chess.data.model.Spot component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.chess.data.model.Spot component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.chess.data.model.Player component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.example.chess.data.model.ChessPiece component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.example.chess.data.model.ChessPiece component5() {
        return null;
    }
    
    public final boolean component6() {
        return false;
    }
    
    public final boolean component7() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.chess.data.model.Move copy(@org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Spot from, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Spot to, @org.jetbrains.annotations.NotNull
    com.example.chess.data.model.Player player, @org.jetbrains.annotations.Nullable
    com.example.chess.data.model.ChessPiece pieceMoved, @org.jetbrains.annotations.Nullable
    com.example.chess.data.model.ChessPiece pieceKilled, boolean isCastlingMove, boolean isEnPassant) {
        return null;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
}