import com.sun.istack.internal.NotNull;

/**
 * Universal Location type for a SquareBoard
 */
public class Location{
    protected int x;
    protected int y;
    protected int id;
    Location(Location location){
        this.x = location.x;
        this.y = location.y;
        this.id = location.id;
    }
    Location(int id, NormalGameRule rule){
        if(id > rule.maxId)  throw new IllegalArgumentException("Invalid ID");
        x = id % rule.width;
        y = id / rule.width;
        this.id = id;
    }
    Location(int x, int y, NormalGameRule rule){
        if(x >= rule.width || y >= rule.height)  throw new IllegalArgumentException("Invalid dimension");
        this.x = x;
        this.y = y;
        this.id = y * rule.width + x;
    }

    /**
     * Clone a Location object
     * @return  A cloned Location object
     */
    public Location clone(){
        return new Location(this);
    }

    /**
     * Calculate the direction relative to anther location.
     * @param to    Target location
     * @return  Direction enum, Null if not describable.
     */
    public Direction getDirection(Location to){
        //equal
        if(this.equals(to)){
            return null;
        }

        //horizontal
        if(to.y == this.y){
            return to.x > this.x ? Direction.E : Direction.W;
        }

        //vertical
        if(to.x == this.x){
            return to.y > this.y ? Direction.S : Direction.N;
        }

        //Diagonal
        int diffx = to.x - this.x, diffy = to.y - this.y;
        if(diffx == diffy){
            return diffy > 0 ? Direction.SE : Direction.NW;
        }else if(diffx == -diffy){
            return diffy > 0 ? Direction.SW : Direction.NE;
        }

        //none
        return null;
    }

    public boolean equals(@NotNull Location b){
        return this.id == b.id;
    }

    @Override
    public String toString(){
        return x + "," + y;
    }

    /**
     * Convert a Location into chess algebaric notation.
     * @param height    Height of board
     * @return  Location in the correct notation
     */
    public String toNotation(int height){
        return "" + (char) ('a' + x) + (height - y);
    }
}