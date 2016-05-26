import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles player information
 */
class ScoreKeeper {
    Map<Side, Player> players = new EnumMap<>(Side.class);

    ScoreKeeper(String whiteName, String blackName) {
        players.put(Side.WHITE, new Player(whiteName));
        players.put(Side.BLACK, new Player(blackName));
    }
    ScoreKeeper(){
        this("Player 1", "Player 2");
    }

    /**
     * Get Player by Side.
     * @param side  Side.WHITE or Side.BLACK
     * @return  Player if exists
     */
    public Player getPlayer(Side side){
        return players.get(side);
    }

    /**
     * Switch both players' sides.
     */
    public void switchSide(){
        Player A = players.get(Side.WHITE),
                B = players.get(Side.BLACK);
        players.put(Side.WHITE, B);
        players.put(Side.BLACK, A);
    }

    /**
     * Add a point to the Player in the Side specified.
     * @param side    Side
     */
    public void addPoint(Side side){
        getPlayer(side).score++;
    }

    /**
     * POJO for holding player information
     */
    public class Player {
        String name;
        int score;

        Player(String name) {
            this.name = name;
        }


    }
}
