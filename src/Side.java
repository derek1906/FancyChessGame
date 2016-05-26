/**
 * Side - Black or White
 */
public enum Side {
    BLACK (0, "Black"), WHITE (1, "White");

    int id;
    String desc;
    Side(int side, String desc){
        this.id = side;
        this.desc = desc;
    }

    /**
     * Get the corresponding id of the Side.
     * @return  Corresponding id
     */
    int toInt(){
        return this.id;
    }

    /**
     * Get an English description.
     * @return  String
     */
    @Override
    public String toString(){
        return this.desc;
    }

    /**
     * Get the opposite color.
     * @return  A Side corresponding to its opposite.
     */
    Side opposite(){
        return Side.fromInt(1 - this.toInt());
    }

    static private Side lookup[] = {BLACK, WHITE};
    static private int total = 2;
    /**
     * Reverse loopkup of a Side from an id.
     * @param id    id of a Side
     * @return  A Side corresponding to the id. null if not a valid id.
     */
    static Side fromInt(int id){
        return id >= total || id < 0 ? null :  lookup[id];
    }
}
