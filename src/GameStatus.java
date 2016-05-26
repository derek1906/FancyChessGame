public class GameStatus{
    EndGameReason status = EndGameReason.NOT_ENDED;
    Side winningSide;
    NormalGameRule rule;

    public GameStatus(NormalGameRule rule){
        this.rule = rule;
    }

    public void setCheckmate(Side winningSide){
        status = EndGameReason.CHECKMATE;
        this.winningSide = winningSide;
        rule.scoreKeeper.addPoint(winningSide);
    }

    public void setStalemate(){
        status = EndGameReason.STALEMATE;
        this.winningSide = null;
    }

    public void setForfeited(Side winningSide){
        status = EndGameReason.FORFEITED;
        this.winningSide = winningSide;
        rule.scoreKeeper.addPoint(winningSide);
    }

    public enum EndGameReason {
        NOT_ENDED, CHECKMATE, STALEMATE, FORFEITED
    }

}

