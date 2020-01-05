package bloodborne;

/**
 * Created by xudabiao on 2017/2/8.
 */
public class Chessman {
    public int x, y, color, number;
    public ChessGroup chessGroup;

    public Chessman(int x, int y, Board board, int number) {
        this.x = x;
        this.y = y;
        this.color = board.records.size() % 2;
        this.number = number;
        this.chessGroup = new ChessGroup(color, this, board);
    }
}
