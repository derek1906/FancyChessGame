import java.awt.event.WindowEvent;
import java.util.Scanner;

/**
 * Implements Game loop for command line
 */
public class Game {
    /**
     * Main loop
     * @param args  Command line args
     */
    public static void main(String[] args){
        // Initialize score keeper
        ScoreKeeper scoreKeeper = new ScoreKeeper("Player 1", "Player 2");

        //8 x 8 square chessboard
        NormalGameRule rule = new NormalGameRule(scoreKeeper);
        SquareBoard board = rule.board;

        BoardDrawer drawer = board.getDrawer("Fancy Chess Game");

        do {
            updateDisplay(drawer, board);
        } while(parseMovement(rule));

        updateDisplay(drawer,board);

        drawer.window.dispatchEvent(new WindowEvent(drawer.window, WindowEvent.WINDOW_CLOSING));
    }

    /**
     * Update display info.
     * @param drawer    GUI drawer
     */
    private static void updateDisplay(BoardDrawer drawer, SquareBoard board){
        drawer.update();
        board.printBoard();
    }

    /**
     * Parse console commands.
     * @param rule    Rule that the board is using
     * @return  True if move is success and the game can be progressed.
     */
    public static boolean parseMovement(NormalGameRule rule){
        Outer:
        while(true) {
            System.out.println("Command:");
            Scanner reader = new Scanner(System.in);
            String cmd = reader.nextLine();

            if(cmd.equals("exit")){
                return false;
            }

            if(rule.parseNotation(cmd)){
                switch (rule.gameStatus.status){
                    case NOT_ENDED:
                        break Outer;
                    case CHECKMATE:
                        System.out.println("Checkmate! " + rule.gameStatus.winningSide.desc + " wins!");
                        return false;
                    case STALEMATE:
                        System.out.println("Stalemate! Nobody wins!");
                        return false;
                }
            }
        }

        return true;
    }
}
