/**
 * The NormalGameRule class handles all rules related logic,
 * including initial placement of pieces and validation of
 * movements.
 *
 */
public class NormalGameRule extends GameRule {
    int width = 15, height = 15, maxId = width * height - 1;
    SquareBoard board;
    ChessPiece[] kings = new ChessPiece[2];
    boolean customPieces = false;
    GameStatus gameStatus;
    GUIController guiController = new GUIController(this);
    ScoreKeeper scoreKeeper = new ScoreKeeper();

    public NormalGameRule(){
        this(new ScoreKeeper());
    }

    public NormalGameRule(ScoreKeeper scoreKeeper) {
        this.scoreKeeper = scoreKeeper;

        board = new SquareBoard(this);
        initBoard();
    }

    /**
     * Set up with a custom dimension.
     * @param width     Width
     * @param height    Height
     */
    public NormalGameRule(int width, int height){
        this.width = width;
        this.height = height;
        this.maxId = width * height - 1;

        board = new SquareBoard(this);
        initBoard();
    }

    /**
     * Initialize board. Populate board with initial chess pieces.
     */
    @Override
    public void initBoard() {
        gameStatus = new GameStatus(this);

        for (int side = 0; side < 2; side++) {
            System.out.println("Adding side " + side);
            board.add(new ChessPiece(PieceType.ROOK,   Side.fromInt(side), board.getLoc(0, 7 * side).id));
            board.add(new ChessPiece(PieceType.KNIGHT, Side.fromInt(side), board.getLoc(1, 7 * side).id));
            board.add(new ChessPiece(PieceType.BISHOP, Side.fromInt(side), board.getLoc(2, 7 * side).id));
            board.add(new ChessPiece(PieceType.QUEEN,  Side.fromInt(side), board.getLoc(3, 7 * side).id));
            board.add(new ChessPiece(PieceType.BISHOP, Side.fromInt(side), board.getLoc(5, 7 * side).id));
            board.add(new ChessPiece(PieceType.KNIGHT, Side.fromInt(side), board.getLoc(6, 7 * side).id));
            board.add(new ChessPiece(PieceType.ROOK,   Side.fromInt(side), board.getLoc(7, 7 * side).id));

            ChessPiece king = new ChessPiece(PieceType.KING,   Side.fromInt(side), board.getLoc(4, 7 * side).id);
            board.add(king);
            kings[side] = king;

            for (int col = 0; col < 8; col++) {
                board.add(new ChessPiece(PieceType.PAWN, Side.fromInt(side), board.getLoc(col, side == 0 ? 1 : 6).id));
            }

            if(customPieces) {
                //custom
                board.add(new ChessPiece(PieceType.CANNON, Side.fromInt(side), board.getLoc(2, side == 0 ? 2 : 5).id));
                board.add(new ChessPiece(PieceType.TURNER, Side.fromInt(side), board.getLoc(5, side == 0 ? 2 : 5).id));
            }
        }
    }

    /**
     * Set if custom pieces should be used
     */
    public void setCustomPieces(boolean value){
        customPieces = value;
    }

    /**
     * Check if a move is legal.
     *
     * @param piece Target chess piece
     * @param location  Destination
     * @return True if legal.
     */
    @Override
    public boolean isMoveLegal(ChessPiece piece, Object location) {
        Location oldLoc = board.getLoc(piece),
                newLoc = (Location) location;

        // reject if a chest piece already in place
        ChessPiece possibleTarget = board.getPieceAtLocation(newLoc);
        if(possibleTarget != null && piece.side == possibleTarget.side){
            return false;
        }

        // check path
        return new Validator(piece).validate(oldLoc, newLoc);
    }

    /**
     * Check if the king is being checked.
     * @param side    Specify the side which wanted to check
     * @return  A KingCheckCheckerResult containing meta info
     */
    public KingCheckCheckerResult KingBeingCheckChecker(Side side){
        ChessPiece king = kings[side.toInt()];
        KingCheckCheckerResult result = new KingCheckCheckerResult();

        for(ChessPiece piece : board.pieces){
            if(piece.side == side){
                continue;
            }
            boolean reachable = isMoveLegal(piece, board.getLoc(king.location));
            if(reachable){
                return result.yes(piece);
            }
        }
        return result.no();
    }

    public class KingCheckCheckerResult {
        boolean isCheck;
        ChessPiece offensivePiece;

        /**
         * Returns itself, configured for king being checked.
         * @param offensivePiece    One offensive piece
         * @return  Itself
         */
        protected KingCheckCheckerResult yes(ChessPiece offensivePiece){
            isCheck = true;
            this.offensivePiece = offensivePiece;
            return this;
        }

        /**
         * Returns itself, configured for king not being checked.
         * @return  Itself
         */
        protected KingCheckCheckerResult no(){
            isCheck = false;
            return this;
        }
    }

    /**
     * Using an offensive piece (putting the king in check) as hint,
     * check if there is any step to prevent it.
     *
     * @param offensivePiece    Hint offensive piece
     * @return  True if no step can be taken to resolve conflict
     */
    public boolean checkCheckmate(ChessPiece offensivePiece){
        ChessPiece selfKing = kings[offensivePiece.side.opposite().toInt()];
        Location kingLocation = board.getLoc(selfKing);

        /**
         * Case 1
         *
         * King can move (or stay) to avoid being checked.
         */
        for(int x = Math.max(kingLocation.x - 1, 0); x <= Math.min(kingLocation.x + 1, width - 1); x++){
            for(int y = Math.max(kingLocation.y - 1, 0); y <= Math.min(kingLocation.y + 1, height - 1); y++){
                System.out.println("Testing if moving king to " + board.getLoc(x, y) + " can solve conflict");
                if(board.move(selfKing, board.getLoc(x, y), true)){
                    System.out.println("Checkmate can be prevented by " + selfKing.type.name() + " moving to " + board.getLoc(x, y));
                    return false;
                }
            }
        }

        /**
         * Case 2
         *
         * Offensive piece can be eliminated.
         */
        for(ChessPiece piece : board.pieces){
            if(piece.side == offensivePiece.side)  continue;
            if(isMoveLegal(piece, board.getLoc(offensivePiece.location))){
                /**
                 * Offensive piece can be eliminated,
                 * Check if conflict remains after move, reject if yes.
                 */
                if(!board.move(piece, board.getLoc(offensivePiece.location), true)){
                    continue;
                }

                System.out.println(
                        "Checkmate can be prevented by " + piece.type.name() + " at " + board.getLoc(piece) +
                                " eliminating " + offensivePiece.type.name() + " at " + board.getLoc(offensivePiece)
                );
                return false;
            }
        }

        /**
         * Case 3
         *
         * Path to offensive piece can be blocked.
         */
        //Get direction from offensive piece to king
        Direction direction = board.getLoc(offensivePiece).getDirection(kingLocation);
        int initialLocId = direction.actions.next(offensivePiece.location, board.dimension);

        // loop over the path
        for(int currentId = initialLocId; currentId != selfKing.location; currentId = direction.actions.next(currentId, board.dimension)){
            Location pointInPath = board.getLoc(currentId);

            for(ChessPiece piece : board.pieces){
                if(piece.side != selfKing.side || piece.type == PieceType.KING) continue;
                if(isMoveLegal(piece, pointInPath)){
                    /**
                     * Path can be blocked.
                     * Check if conflict remains after move, reject if yes.
                     */
                    if(!board.move(piece, pointInPath, true)){
                        continue;
                    }

                    System.out.println(
                            "Checkmate can be prevented by " + piece.type.name() + " at " + board.getLoc(piece) +
                                    " moving to " + pointInPath);
                    return false;
                }
            }
        }

        /**
         * If all tests fail, we can conclude that it is a checkmate
         */
        return true;
    }

    /**
     * Check if stalemate happened.
     * @param side    Side to be checked.
     * @return  True if there is no legal moves left.
     */
    public boolean checkStalemate(Side side){
        for(ChessPiece piece : board.pieces){
            if(piece.side == side){
                for(int x = 0; x < width; x++){
                    for(int y = 0; y < height; y++){
                        boolean movable = board.move(piece, board.getLoc(x, y), true);
                        if(movable){
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Internal move validator
     */
    private class Validator{
        Side side;
        Location _from, _to, from, to;
        ChessPiece validatee;
        ChessPiece possibleTarget;
        boolean isValid;

        /**
         * Initialize validator with a chess piece
         * @param piece    Piece to run validation on
         */
        Validator(ChessPiece piece){
            this.validatee = piece;
            this.side = piece.side;
        }

        /**
         * Validate move
         * @param oldLoc    Origin
         * @param newLoc    Destination
         * @return boolean
         */
        boolean validate(Location oldLoc, Location newLoc){
            possibleTarget = board.getPieceAtLocation(newLoc);
            _from = oldLoc.clone();
            _to = newLoc.clone();
            from = oldLoc.clone();
            to = newLoc.clone();

            if(side == Side.WHITE){
                from.y = (height - 1) - from.y;
                to.y = (height - 1) - to.y;
            }

            isValid = false;

            switch (validatee.type){
                case PAWN:   Pawn(); break;
                case ROOK:   Rook(); break;
                case BISHOP: Bishop(); break;
                case QUEEN:  Queen(); break;
                case KING:   King(); break;
                case KNIGHT: Knight(); break;
                case CANNON: Cannon(); break;
                case TURNER: Turner(); break;
            }

            return isValid;
        }

        /**
         * isCross
         * @return  True if a movement is horizontal or vertical
         */
        boolean isCross(){
            return to.x == from.x || to.y == from.y;
        }

        /**
         * isDiagonal
         * @return  True if a movement is diagonal
         */
        boolean isDiagonal(){
            //return (to.id - from.id) % (width - 1) == 0 || (to.id - from.id) % (width + 1) == 0;
            return Math.abs(to.x - from.x) == Math.abs(to.y - from.y);
        }

        void Pawn(){
            //valid cases
            if(to.x == from.x && to.y - from.y == 1 && possibleTarget == null){
                isValid = true;
            }else if(Math.abs(to.x - from.x) == 1 && to.y - from.y == 1 && possibleTarget != null){
                isValid = true;
            }else if(!validatee.moved && to.x == from.x && to.y - from.y == 2 && possibleTarget == null){
                isValid = true;
            }

            if(isValid){
                if(board.findCollision(_from.getDirection(_to), _from.id, _to.id) != null){
                    isValid = false;
                }
            }
        }

        void Rook(){
            //valid cases
            isValid = isCross();

            if(isValid){
                if(board.findCollision(_from.getDirection(_to), _from.id, _to.id) != null){
                    isValid = false;
                }
            }
        }

        void Bishop(){
            //valid cases
            isValid = isDiagonal();

            if(isValid){
                if(board.findCollision(_from.getDirection(_to), _from.id, _to.id) != null){
                    isValid = false;
                }
            }
        }

        void Queen(){
            //valid cases
            isValid = isCross() || isDiagonal();

            if(isValid){
                if(board.findCollision(_from.getDirection(_to), _from.id, _to.id) != null){
                    isValid = false;
                }
            }
        }

        void King(){
            //valid cases
            if(Math.abs(to.x - from.x) <= 1 && Math.abs(to.y - from.y) <= 1){
                isValid = true;
            }
        }

        void Knight(){
            //valid cases
            isValid = !isCross() && !isDiagonal() && Math.abs(to.x - from.x) <= 2 && Math.abs(to.y - from.y) <= 2;
        }

        /**
         * A Cannon is a special piece which its movement mimics the 炮 in Chinese Chess, except
         * the king cannot be captured when hopping over another piece.
         */
        void Cannon(){
            //valid cases
            if(isCross() && !isDiagonal()){
                Direction direction = _from.getDirection(_to);
                Location location = board.getLoc(direction.actions.next(_from.id, width));
                int numberOfPieces = 0;
                while (!location.equals(_to)) {
                    if (board.getPieceAtLocation(location) != null) {
                        // not valid if there are more than 1 piece in between
                        if (numberOfPieces == 1) return;
                        else numberOfPieces++;
                    }
                    location = board.getLoc(direction.actions.next(location.id, width));
                }
                // only valid when there is only 0 or 1 piece in between
                isValid = !(numberOfPieces == 1 && possibleTarget != null && possibleTarget.type == PieceType.KING);
            }
        }

        /**
         * A Turner has the same movement as a Rook, however it must go to a different direction
         * every time it moves.
         */
        void Turner(){
            //valid cases
            Rook();
            if(validatee.previousDirection == _from.getDirection(_to)){
                isValid = false;
            }
        }
    }


    /**
     * Parse chessboard commands from standard in. It mimics the standard algebraic notation
     * for chess.
     *
     * For example, to move a pawn to e5, type `pe5`. This function will automatically decide
     * the most appropriate piece to move.
     *
     * @param cmd    A command
     * @return  True if the move is successful
     */
    public boolean parseNotation(String cmd){
        char t = cmd.charAt(0);
        int x = cmd.charAt(1) - 'a',
                y = board.dimension - Character.getNumericValue(cmd.charAt(2));

        PieceType type;
        Location dest = board.getLoc(x, y);
        switch (t) {
            case 'k':
                type = PieceType.KING;
                break;
            case 'q':
                type = PieceType.QUEEN;
                break;
            case 'n':
                type = PieceType.KNIGHT;
                break;
            case 'r':
                type = PieceType.ROOK;
                break;
            case 'b':
                type = PieceType.BISHOP;
                break;
            case 'p':
                type = PieceType.PAWN;
                break;
            default:
                return false;
        }

        for(ChessPiece piece : board.pieces){
            if(piece.side == board.getTurn() && piece.type == type && board.move(piece, dest)){
                return true;
            }
        }

        return false;
    }


    /**
     * Handles click events from GUI.
     * @param location    Location clicked
     * @return  ConsiderResult
     */
    public ConsiderResult handleClickEvents(Location location){
        return guiController.consider(location);
    }

    /**
     * GUI Controller singleton
     */
    private class GUIController{
        State state = State.PICKING_PRIMARY;
        NormalGameRule rule;
        ChessPiece activePiece;
        Location origin;


        GUIController(NormalGameRule rule){
            this.rule = rule;
        }

        public ConsiderResult consider(Location location){
            switch (state){
                case PICKING_PRIMARY:
                    return primaryAction(location);
                case PICKING_SECONDARY:
                    return secondaryAction(location);
            }

            return null;
        }

        /**
         * Selecting state
         * @param location    Clicked location
         * @return  ConsiderResult
         */
        private ConsiderResult primaryAction(Location location){
            ConsiderResult result = new ConsiderResult();
            result.state = State.PICKING_PRIMARY;

            ChessPiece piece = rule.board.getPieceAtLocation(location);

            if(piece == null){
                result.success = false;
            }else if(piece.side != rule.board.getTurn()){
                result.success = false;
            }else{
                activePiece = piece;
                state = State.PICKING_SECONDARY;
                origin = location;
                result.success = true;
            }
            return result;
        }

        /**
         * Setting state
         * @param location    Clicked location
         * @return  ConsiderResult
         */
        private ConsiderResult secondaryAction(Location location){
            ConsiderResult result = new ConsiderResult();
            result.state = State.PICKING_SECONDARY;

            // Check if same grid
            if(location.equals(origin)){
                state = State.PICKING_PRIMARY;
                result.success = true;
                return result;
            }

            // Check if same side
            ChessPiece potentialPiece = rule.board.getPieceAtLocation(location);
            if(potentialPiece != null && potentialPiece.side == activePiece.side){
                state = State.PICKING_SECONDARY;
                result.success = true;
                result.origin = origin;
                result.switchToggle = true;

                origin = location;
                activePiece = potentialPiece;
                return result;
            }

            boolean canMove = rule.board.move(activePiece, location);

            if(canMove){
                state = State.PICKING_PRIMARY;

                result.success = true;
                result.origin = origin;

                activePiece = null;
                origin = null;
            }else{
                result.success = false;
            }
            return result;
        }

    }

    public class ConsiderResult{
        boolean success;
        State state;
        Location origin;
        boolean switchToggle;
    }

    public enum State{
        PICKING_PRIMARY, PICKING_SECONDARY
    }
}