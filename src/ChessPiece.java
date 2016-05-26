public class ChessPiece {
    public Side side;        /**< Color of the piece */
    public PieceType type;  /**< Type of the piece */
    public int location;    /**< Internal location id of the piece */
    public boolean moved;   /**< Indicates if the piece has moved */
    public Direction previousDirection = Direction.NONE;

    /**
     * Constructor of ChessPiece.
     * @param type        Type to be set for the piece
     * @param side        Color to be set for the piece
     * @param location    Initial location id
     */
    ChessPiece(PieceType type, Side side, int location){
        this.type = type;
        this.side = side;
        this.location = location;
    }


    /**
     * Query the appropriate symbol of the piece.
     * @return  A character representing the type
     */
    public char getSymbol(){
        return this.type.toSymbol(side);
    }
}
