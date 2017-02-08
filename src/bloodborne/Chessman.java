package bloodborne;
/**
 * Created by xudabiao on 2017/2/8.
 */
public class Chessman {
    public int x,y;
    public ChessGroup chessGroup;

    public Chessman(int x, int y,int color,int liberty) {
        this.x = x;
        this.y = y;
        this.chessGroup = new ChessGroup(color,liberty,this);
    }

    @Override
    public String toString() {
        return "Chessman{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
