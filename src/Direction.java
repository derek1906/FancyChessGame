/**
 * Universal Direction type for a SquareBoard
 */
public enum Direction{
    N  ((id, dimension) -> id - dimension),         /**< Top */
    NE ((id, dimension) -> id - (dimension - 1)),   /**< Top right */
    E  ((id, dimension) -> id + 1),                 /**< Right */
    SE ((id, dimension) -> id + (dimension + 1)),   /**< Bottom right */
    S  ((id, dimension) -> id + dimension),         /**< Bottom */
    SW ((id, dimension) -> id + (dimension - 1)),   /**< Bottom left */
    W  ((id, dimension) -> id - 1),                 /**< Left */
    NW ((id, dimension) -> id - (dimension + 1)),   /**< Top left */
    NONE ((id, dimension) -> id);

    DirectionActions actions;   /**< Interface for the Direction Enum @see DirectionActions#next*/
    Direction(DirectionActions actions){
        this.actions = actions;
    }

    interface DirectionActions{
        /**
         * Get the next location id in the direction.
         * @warning This function does not check for board boundary. This only perform a simple computation based on the location id.
         * @param id    Location id
         * @param dimension    Width of the board
         * @return  Next location id in the direction
         */
        int next(int id, int dimension);
    }
}

