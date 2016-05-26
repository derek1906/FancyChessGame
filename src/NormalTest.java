import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class NormalTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    /**
     * Execute an array of move notations.
     * @param rule    NormalGameRule object
     * @param cmds    An array of moves in algebraic notation
     * @return  True if all moves succeeded
     */
    public boolean moveSequence(NormalGameRule rule, String[] cmds){
        for(String cmd : cmds){
            boolean result = rule.parseNotation(cmd);
            if(!result){
                return false;
            }
        }
        return true;
    }

    /**
     * Test printing out a board
     */
    @Test
    public void DisplayBoard(){
        NormalGameRule rule = new NormalGameRule();

        SquareBoard board = rule.board;
        board.printBoard();
        board.printDivider();
    }

    /**
     * Test basic movement
     * @throws Exception
     */
    @Test
    public void BasicMovement() throws Exception{
        NormalGameRule rule = new NormalGameRule();

        SquareBoard board = rule.board;
        ChessPiece pawn1 = board.getPieceAtLocation(0, 6),
                pawn2 = board.getPieceAtLocation(7, 1);
        boolean okay;

        okay = board.move(pawn1, board.getLoc(0, 5));
        assertTrue(okay);
        assertEquals(pawn1, board.getPieceAtLocation(0, 5));    //check piece actually moved
        assertEquals(pawn1.location, board.getLoc(0, 5).id);    //check internal location id is updated
        assertEquals(null, board.getPieceAtLocation(0, 6));     //check previous location is updated

        okay = board.move(pawn2, board.getLoc(7, 3));
        assertTrue(okay);
        assertEquals(pawn2, board.getPieceAtLocation(7, 3));
        assertEquals(pawn2.location, board.getLoc(7, 3).id);
        assertEquals(null, board.getPieceAtLocation(7, 1));
    }

    /**
     * Test invalid movements
     */
    @Test
    public void InvalidMovement(){
        NormalGameRule rule = new NormalGameRule();

        SquareBoard board = rule.board;
        ChessPiece pawn1 = board.getPieceAtLocation(0, 6);

        boolean okay = board.move(pawn1, board.getLoc(0, 3));   // illegal move
        assertFalse(okay);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Invalid dimension");

        board.move(pawn1, board.getLoc(0, 8));  // out of the board
    }

    /**
     * Test basic capturing of pieces
     */
    @Test
    public void BasicCapture(){
        NormalGameRule rule = new NormalGameRule();

        SquareBoard board = rule.board;
        ChessPiece pawn1 = board.getPieceAtLocation(0, 6),
                pawn2 = board.getPieceAtLocation(1, 1);

        board.move(pawn1, board.getLoc(0, 4));
        board.move(pawn2, board.getLoc(1, 3));

        boolean okay = board.move(pawn1, board.getLoc(pawn2));  // let pawn1 eats pawn2
        assertTrue(okay);
        assertEquals(pawn1.location, board.getLoc(1, 3).id);
        assertEquals(pawn2.location, -1);                       // check internal location
        assertEquals(board.getPieceAtLocation(1, 3), pawn1);    // check pawn1 is moved to pawn2 previous location
        assertEquals(board.getPieceAtLocation(0, 4), null);     // check pawn1 previous location is updated to null

        okay = board.move(pawn2, board.getLoc(1, 4));   // check if eaten piece can still be moved
        assertFalse(okay);
    }

    /**
     * Test illegal self capturing and turn based mechanics.
     */
    @Test
    public void SelfCapturingAndTurnBasedMechanism(){
        NormalGameRule rule = new NormalGameRule();

        SquareBoard board = rule.board;
        ChessPiece pawn1 = board.getPieceAtLocation(0, 6),
                pawn2 = board.getPieceAtLocation(1, 6),
                pawn3 = board.getPieceAtLocation(0, 1);

        board.move(pawn1, board.getLoc(0, 5));  // move pawn1 forward 1
        board.move(pawn3, board.getLoc(0, 2));

        boolean okay = board.move(pawn2, board.getLoc(pawn1));  // test if pawn2 can eat pawn1
        assertFalse(okay);

        ChessPiece rook1 = board.getPieceAtLocation(0, 7);
        okay = board.move(rook1, board.getLoc(1, 7));   // test if rook1 can eat the piece next to it
        assertFalse(okay);

        ChessPiece queen1 = board.getPieceAtLocation(3, 7);
        okay = board.move(queen1, board.getLoc(5, 5));  // test if queen1 can jump across pieces
        assertFalse(okay);

        okay = board.move(pawn3, board.getLoc(0, 3));   // test if opponent can move before current player takes a move
        assertFalse(okay);
    }

    /**
     * Test NormalGameRule.Validator.
     * @see NormalGameRule.Validator#validate(Location, Location)
     */
    @Test
    public void Validator(){
        NormalGameRule rule = new NormalGameRule();
        SquareBoard board = rule.board;

        //Pawn
        ChessPiece pawn = board.getPieceAtLocation(0, 6);
        assertTrue(rule.isMoveLegal(pawn, board.getLoc(0, 5)));
        assertTrue(rule.isMoveLegal(pawn, board.getLoc(0, 4)));
        assertFalse(rule.isMoveLegal(pawn, board.getLoc(0, 3)));
        assertFalse(rule.isMoveLegal(pawn, board.getLoc(1, 5)));
        for(int i = 0; i < 8; i++){
            board.remove(board.getPieceAtLocation(i, 6));
        }

        //Rook
        ChessPiece rook = board.getPieceAtLocation(0, 7);
        assertTrue(rule.isMoveLegal(rook, board.getLoc(0, 5)));
        assertTrue(rule.isMoveLegal(rook, board.getLoc(0, 2)));
        assertFalse(rule.isMoveLegal(rook, board.getLoc(1, 3)));

        //Knight
        ChessPiece knight = board.getPieceAtLocation(1, 7);
        assertTrue(rule.isMoveLegal(knight, board.getLoc(3, 6)));
        assertFalse(rule.isMoveLegal(knight, board.getLoc(3, 5)));

        //Bishop
        ChessPiece bishop = board.getPieceAtLocation(2, 7);
        assertTrue(rule.isMoveLegal(bishop, board.getLoc(5, 4)));   //diagonal
        assertFalse(rule.isMoveLegal(bishop, board.getLoc(7, 4)));  //wrap

        //Queen
        ChessPiece queen = board.getPieceAtLocation(3, 7);
        assertTrue(rule.isMoveLegal(queen, board.getLoc(3, 3)));    //vertical
        assertTrue(rule.isMoveLegal(queen, board.getLoc(4, 6)));    //diagonal

        //King
        ChessPiece king = board.getPieceAtLocation(4, 7);
        assertTrue(rule.isMoveLegal(king, board.getLoc(4, 6)));     //up
        assertTrue(rule.isMoveLegal(king, board.getLoc(3, 6)));     //corner
        assertFalse(rule.isMoveLegal(king, board.getLoc(4, 5)));    //two step up

    }

    /**
     * Validation test for custom pieces
     */
    @Test
    public void ValidateCustomPieces(){
        NormalGameRule rule;
        SquareBoard board;

        // Cannon
        rule = new NormalGameRule(); board = rule.board;

        ChessPiece cannon = new ChessPiece(PieceType.CANNON, Side.WHITE, board.getLoc(2, 5).id);
        board.add(cannon);
        assertTrue(board.move(cannon, board.getLoc(2, 3)));     // test if it can move forward similar to rook
        board.move++;
        assertTrue(board.move(cannon, board.getLoc(2, 0)));     // test if it can hop over pieces
        board.move++;
        board.printBoard();
        assertFalse(board.move(cannon, board.getLoc(5, 0)));    // test if it can hop over >1 pieces
        assertFalse(board.move(cannon, board.getLoc(4, 0)));    // test if it can capture king
        assertTrue(rule.gameStatus.status == GameStatus.EndGameReason.NOT_ENDED);   // test if it is not checkmate

        // Turner
        rule = new NormalGameRule(); board = rule.board;

        ChessPiece turner = new ChessPiece(PieceType.TURNER, Side.WHITE, board.getLoc(2, 5).id);
        board.add(turner);
        assertTrue(board.move(turner, board.getLoc(2, 3)));     // test if it can move forward similar to rook
        board.move(board.getPieceAtLocation(4, 1), board.getLoc(4, 3));
        assertFalse(board.move(turner, board.getLoc(2, 2)));    // test if it can continue to move forward
        assertTrue(board.move(turner, board.getLoc(2, 4)));     // test if it can move when direction is changed
        board.move(board.getPieceAtLocation(4, 0), board.getLoc(4, 1)); // move king forward
        assertTrue(board.move(turner, board.getLoc(5, 4)));
        board.move(board.getPieceAtLocation(7, 1), board.getLoc(7, 2));
        assertTrue(board.move(turner, board.getLoc(5, 3)));
        board.move(board.getPieceAtLocation(7, 2), board.getLoc(7, 3));

        board.add(new ChessPiece(PieceType.TURNER, Side.WHITE, board.getLoc(3, 5).id));     // eliminate escape spaces
        board.add(new ChessPiece(PieceType.TURNER, Side.WHITE, board.getLoc(5, 5).id));

        assertTrue(board.move(turner, board.getLoc(4, 3)));
        board.printBoard();
        assertTrue(rule.gameStatus.status == GameStatus.EndGameReason.CHECKMATE);   // test if checkmate
    }

    /**
     * Test the tryMove feature for SquareBoard.move.
     * @see SquareBoard#move(ChessPiece, Location, boolean)
     */
    @Test
    public void TestTryMove(){
        NormalGameRule rule = new NormalGameRule();
        SquareBoard board = rule.board;

        boolean okay;
        ChessPiece pawn = board.getPieceAtLocation(0, 6);
        int location = pawn.location;

        //valid
        okay = board.move(pawn, board.getLoc(0, 4), true);
        assertTrue(okay);
        assertEquals(location, pawn.location);  //make sure internal locations have not changed
        assertEquals(pawn, board.getPieceAtLocation(0, 6));

        //invalid
        okay = board.move(pawn, board.getLoc(0, 3), true);
        assertFalse(okay);
        assertEquals(location, pawn.location);  //make sure internal locations have not changed
        assertEquals(pawn, board.getPieceAtLocation(0, 6));
    }

    /**
     * Perform a basic test of checkmate
     */
    @Test
    public void TestCheckmate(){
        NormalGameRule rule = new NormalGameRule();

        String[] sequence = {"pf4", "pe6", "pg4", "qh4"};
        assertTrue(moveSequence(rule, sequence));
        assertTrue(rule.gameStatus.status == GameStatus.EndGameReason.CHECKMATE);
        assertTrue(rule.gameStatus.winningSide == Side.BLACK);
    }

    /**
     * Perform a basic test of stalemate
     */
    @Test
    public void TestStalemate(){
        NormalGameRule rule = new NormalGameRule();

        String[] sequence = {"pe3", "pa5", "qh5", "ra6", "qa5", "ph5", "qc7", "rh6", "ph4", "pf6", "qd7", "kf7", "qb7", "qd3", "qb8", "qh7", "qc8", "kg6", "qe6"};
        assertTrue(moveSequence(rule, sequence));
        assertTrue(rule.gameStatus.status == GameStatus.EndGameReason.STALEMATE);
    }

    /**
     * Test undo feature.
     */
    @Test
    public void UndoTest(){
        NormalGameRule rule = new NormalGameRule();

        // Store initial positions
        ChessPiece board[][] = new ChessPiece[8][8];
        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){
                board[x][y] = rule.board.getPieceAtLocation(x, y);
            }
        }

        String[] sequence = {"pe3", "pa5", "qh5", "ra6", "qa5", "ph5", "qc7", "rh6", "ph4", "pf6", "qd7", "kf7", "qb7", "qd3", "qb8", "qh7", "qc8", "kg6"};
        assertTrue(moveSequence(rule, sequence));

        // Undo a sequence of moves
        assertTrue(rule.board.undo(sequence.length));

        // Make sure everything remains the same as the initial positions
        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){
                assertEquals(rule.board.getPieceAtLocation(x, y), board[x][y]);
            }
        }
    }
}
