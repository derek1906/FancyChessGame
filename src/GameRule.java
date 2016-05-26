public abstract class GameRule {
    public abstract void initBoard();
    public abstract boolean isMoveLegal(ChessPiece piece, Object newLoc);
    public abstract boolean checkCheckmate(ChessPiece offensivePiece);
}
