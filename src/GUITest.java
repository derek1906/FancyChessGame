import org.junit.Test;

public class GUITest {
    @Test
    public void InitialConfiguration(){
        NormalGameRule rule = new NormalGameRule();

        BoardDrawer drawer = rule.board.getDrawer("Initial Configuration Test");
        drawer.update();
        while(true){}
    }

    @Test
    public void Configuration1(){
        NormalGameRule rule = new NormalGameRule();

        String[] sequence = {"pc3"};
        moveSequence(rule, sequence);

        BoardDrawer drawer = rule.board.getDrawer("Configuration Test #1");
        drawer.update();
        while(true){}
    }

    @Test
    public void Configuration2(){
        NormalGameRule rule = new NormalGameRule();

        String[] sequence = {"pe3", "pa5", "qh5", "ra6", "qa5", "ph5", "qc7", "rh6", "ph4", "pf6", "qd7", "kf7", "qb7", "qd3", "qb8", "qh7", "qc8", "kg6", "qe6"};
        moveSequence(rule, sequence);

        BoardDrawer drawer = rule.board.getDrawer("Configuration Test #2");
        drawer.update();
        while(true){}
    }

    @Test
    public void DimensionLarge(){
        NormalGameRule rule = new NormalGameRule(16, 16);

        BoardDrawer drawer = rule.board.getDrawer("Dimension Large Test");
        drawer.update();
        while(true){}
    }


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
}
