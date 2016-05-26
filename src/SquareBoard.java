import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

/**
 * The SquareBoard class handles all location related data.
 */

public class SquareBoard extends Board{
    int dimension;          /**< Dimension of the board */
    int maxId;              /**< Max location id for the board */
    NormalGameRule rule;    /**< Rule chosen for the board */
    ChessPiece[][] grid;    /**< 2D grid of the board */
    int move;               /**< Keeps track of moves */
    Stack<Mover> history;   /**< Move history*/
    Side sideInCheck;

    SquareBoard(NormalGameRule rule){
        this.dimension = rule.width;
        this.maxId = dimension * dimension - 1;
        this.rule = rule;

        init();
    }

    /**
     * Initialize the 2 internal data structures.
     */
    private void init(){
        pieces = new ArrayList<>();                     //1D list of all pieces
        grid = new ChessPiece[dimension][dimension];    //2D array of all pieces

        history = new Stack<>();    //List of results as history

        move = 0;
        sideInCheck = null;
    }

    /**
     * Handles adding and initializing a chess piece to the board.
     * @param piece    A pre-configured ChessPiece
     */
    public void add(ChessPiece piece){
        if(piece == null)   return;

        pieces.add(piece);
        Location location = getLoc(piece.location);
        grid[location.x][location.y] = piece;

        System.out.println("Created " + piece.type.name() + " at " + location);
    }

    /**
     * Remove a piece from the board
     * @param piece    Reference to a piece
     */
    public void remove(ChessPiece piece){
        if(piece == null)   return;

        Location location = getLoc(piece);
        grid[location.x][location.y] = null;
        piece.location = -1;
        pieces.remove(piece);
    }

    /**
     * Request moving a piece on the board.
     * @param piece     Target piece
     * @param newLoc    New location
     * @param tryMove   True if you wish to immediately revert the move action.
     * @return  True if the request is successful.
     */
    public boolean move(ChessPiece piece, Location newLoc, boolean tryMove){
        if(piece == null)   return false;
        if(piece.location == -1)    return false;   // already eaten
        if(piece.side != getTurn()){
            System.out.println("Cannot move " + piece.type.name() + ". Wait for your turn.");
            return false;
        }
        if(piece.location == newLoc.id){
            return false;
        }

        Side prevSideInCheck = sideInCheck;
        sideInCheck = null;

        //Try moving piece
        Mover mover = new Mover(piece);
        Mover.MoveResult result = mover.move(newLoc);

        if(tryMove){
            if(result.success){
                // only revert if success
                mover.revert();
            }
            sideInCheck = prevSideInCheck;
            return result.success;
        }else{
            if(result.success) {
                // only add if success
                history.push(mover);
            }else{
                sideInCheck = prevSideInCheck;
            }
        }

        if(result.success){
            System.out.println("Moved " + piece.type.name() + " to " + newLoc.toString());
            System.out.println("(Internal: " + getLoc(piece) + ")");
            if(result.eaten != null){
                System.out.println(piece.type.name() + " has eaten " + result.eaten.type.name() + " at " + newLoc);
            }

            move++;

            //check if opponent king is in check
            NormalGameRule.KingCheckCheckerResult kingCheckResult = rule.KingBeingCheckChecker(piece.side.opposite());
            if(kingCheckResult.isCheck){
                puttingInCheck(piece.side.opposite());
                if(rule.checkCheckmate(kingCheckResult.offensivePiece)){
                    //checkmate
                    rule.gameStatus.setCheckmate(piece.side);
                }
            }else{
                if(rule.checkStalemate(piece.side.opposite())){
                    //stalemate
                    rule.gameStatus.setStalemate();
                }
            }

        }else {
            System.out.println("Cannot move " + piece.type.name() + " to " + newLoc.toString());
        }

        return result.success;
    }

    /**
     * Same as SquareBoard.move with `tryMove` set to false.
     * @see #move(ChessPiece, Location, boolean)
     * @param piece     Target piece
     * @param newLoc    New Location
     * @return  True if can move
     */
    public boolean move(ChessPiece piece, Location newLoc){
        return move(piece, newLoc, false);
    }

    private void puttingInCheck(Side checkSide){
        System.out.println("King is in check");
        sideInCheck = checkSide;
    }

    /**
     * Private Mover which is responsible for moving pieces on the board
     */
    private class Mover{
        ChessPiece piece;
        MoveResult record;

        Mover(ChessPiece piece){
            this.piece = piece;
        }

        public MoveResult move(Location newLoc){
            record = new MoveResult();

            if(rule.isMoveLegal(piece, newLoc)) {
                record.origin = getLoc(piece.location);
                record.destination = newLoc;
                record._moved = piece.moved;
                record._previousDirection = piece.previousDirection;

                //remove eaten piece
                ChessPiece possibleTarget = record.eaten = getPieceAtLocation(newLoc);
                if(possibleTarget != null) {
                    remove(possibleTarget);
                }

                //remove old location record
                setPieceAtLocation(null, getLoc(piece.location));

                //update internal location
                piece.moved = true;
                piece.previousDirection = record.origin.getDirection(record.destination);
                setPieceAtLocation(piece, newLoc);

                record.actionDone = true;

                //check if self king is in check after move
                if(rule.KingBeingCheckChecker(piece.side).isCheck){
                    //invalidate move
                    revert();
                    record.eaten = null;
                    return fail();
                }

                return success();
            }

            return fail();
        }

        private MoveResult fail(){
            record.success = false;
            return record;
        }
        private MoveResult success(){
            record.success = true;
            return record;
        }

        /**
         * Revert the actions done by .move
         * Useful when testing hypothetical moves
         */
        private void revert(){
            if(!record.actionDone)  return;

            //empty destination
            setPieceAtLocation(null, record.destination);

            //re-add eaten piece (IF ANY)
            if(record.eaten != null) {
                record.eaten.location = record.destination.id;
                add(record.eaten);
            }

            //revert to old location
            piece.moved = record._moved;
            piece.previousDirection = record._previousDirection;
            setPieceAtLocation(piece, record.origin);
        }

        /**
         * Convert move to algebraic notation.
         * @return  A string
         */
        @Override
        public String toString(){
            if(record == null){
                return "";
            }
            return piece.type.name().charAt(0) + record.destination.toNotation(dimension);
        }

        /**
         * Result POJO which contains meta info
         */
        public class MoveResult{
            boolean success;
            ChessPiece eaten;
            Location origin, destination;
            boolean _moved;
            Direction _previousDirection;
            boolean actionDone;
        }
    }

    /**
     * Get piece at a certain location on board.
     * @param location    Location object
     * @return  ChessPiece at the provided location, null if nothing is there
     */
    public ChessPiece getPieceAtLocation(Location location){
        return grid[location.x][location.y];
    }

    /**
     * Get piece at a certain location on board.
     * @param x    Columns from left
     * @param y    Rows from top
     * @return  ChessPiece at location, null if nothing is there
     */
    public ChessPiece getPieceAtLocation(int x, int y){
        return getPieceAtLocation(getLoc(x, y));
    }


    /**
     * Get piece at a certain location on board.
     * @param locationId    Location id
     * @return  ChessPiece at location, null if nothing is there
     */
    public ChessPiece getPieceAtLocation(int locationId){
        return getPieceAtLocation(getLoc(locationId));
    }

    /**
     * Set piece at location on board.
     * @warning    This method does not check if move is valid or not.
     * @param piece    Piece in which you want to change its location
     * @param x        x
     * @param y        y
     */
    public void setPieceAtLocation(ChessPiece piece, int x, int y){
        grid[x][y] = piece;
        if(piece != null) {
            piece.location = getLoc(x, y).id;
        }
    }

    /**
     * Set piece at location on board.
     * @warning    This method does not check if move is valid or not.
     * @param piece       Piece in which you want to change its location
     * @param location    Location object
     */
    public void setPieceAtLocation(ChessPiece piece, Location location){
        grid[location.x][location.y] = piece;
        if(piece != null){
            piece.location = location.id;
        }
    }


    /**
     * Generate a Location object by a ChessPiece's location
     * @param piece    Chess piece
     * @return  A new location object
     */
    public Location getLoc(ChessPiece piece){
        return new Location(piece.location, rule);
    }

    /**
     * Generate a Location object by a location id
     * @param id    id
     * @return  A new location object
     */
    public Location getLoc(int id){
        return new Location(id, rule);
    }

    /**
     * Generate a Location object by x and y
     * @param x    x
     * @param y    y
     * @return  A new location object
     */
    public Location getLoc(int x, int y){
        return new Location(x, y, rule);
    }


    /**
     * Find any chess piece that is in the path. (exclusive)
     * @param direction    Direction to the destination
     * @param oldId        Origin
     * @param newId        Destination
     * @return  A chess piece that is in the way if any
     */
    public ChessPiece findCollision(Direction direction, int oldId, int newId){
        int currentId = direction.actions.next(oldId, dimension);
        if(currentId == newId)  return null;

        for(;newId != currentId; currentId = direction.actions.next(currentId, dimension)){
            ChessPiece collision = getPieceAtLocation(getLoc(currentId));
            if(collision != null){
                return collision;
            }
        }
        return null;
    }

    /**
     * Get the current turn of the game.
     * @return  A Side
     */
    public Side getTurn(){
        return move % 2 == 0 ? Side.WHITE : Side.BLACK;
    }

    /**
     * Undo a number of consecutive moves.
     * @param moves    Number of moves to be reverted
     */
    public boolean undo(int moves){
        for(int i = 0; i < moves; i++){
            try {
                Mover entry = history.pop();
                entry.revert();
                move--;
            }catch(EmptyStackException e){
                return false;
            }
        }
        return true;
    }

    public String[] getMoveHistory(){
        String result[] = new String[history.size()];
        int i = 0;
        for(Mover mover : history){
            result[i] = mover.toString();
            i++;
        }
        return result;
    }

    /**
     * Reset pieces to initial positions
     */
    public void resetPosition(){
        init();
        rule.initBoard();
    }

    public void printBoard(){
        for(int x = 0; x < dimension + 1; x++){
            System.out.print(x == 0 ? '　' : (char) (x + 65344));
            System.out.print('|');
        }
        System.out.print('\n');

        for(int y = 0; y < dimension; y++){
            System.out.print((char)(dimension - y - 1 + 65297) + "|");
            for(int x = 0; x < dimension; x++){
                ChessPiece cell = grid[x][y];
                System.out.print(cell != null ? cell.getSymbol() : '　');
                System.out.print('|');
            }
            System.out.print('\n');
        }
    }

    public void printDivider(){
        System.out.println("========================");
    }

    /**
     * Get a new GUI drawer.
     * @param title    Preferred title of the GUI
     * @return  A BoardDrawer
     */
    public BoardDrawer getDrawer(String title){
        return new BoardDrawer(this, title);
    }
}
