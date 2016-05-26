public enum PieceType {
    KING (0), QUEEN (1), ROOK (2), BISHOP (3), KNIGHT (4), PAWN (5), CANNON (6), TURNER (7);

    int index;
    PieceType(int index){
        this.index = index;
    }

    static char[][] symbols = {
            {'♚', '♛', '♜', '♝', '♞', '♟', '★', '☗'},
            {'♔', '♕', '♖', '♗', '♘', '♙', '☆', '☖'}
    };

    /**
     * Get the corresponding symbol for a Side.
     * @param side    Side of a piece
     * @return  A character representing the PieceType
     */
    public char toSymbol(Side side){
        return symbols[side.toInt()][index];
    }
}
