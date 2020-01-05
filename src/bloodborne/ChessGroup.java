package bloodborne;
import sample.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
/**
 * Created by xudabiao on 2017/2/8.
 */
public class ChessGroup {
    public int liberty;
    public int color;
    public List<Chessman> chessmanList;
    private Board board;

    public ChessGroup(int color, Chessman chessman, Board board) {
        this.color = color;
        chessmanList = new ArrayList<>();
        chessmanList.add(chessman);
        this.board = board;
        calc();
    }

    //两块同色的棋合并为一块
    public void merge(ChessGroup chessGroup){
        chessmanList.addAll(chessGroup.chessmanList);
        for(Chessman chessman : chessGroup.chessmanList)
            chessman.chessGroup = this;
    }

    /**
     * 计算该棋块的气
     */
    public void calc() {
        int x = 0, y = 0;
        HashSet<String> set = new HashSet();
        int nx, ny;
        for (Chessman chessman : chessmanList){
            for (int[] direction : Constants.DIRECTIONS) {
                nx = chessman.x + direction[0];
                ny = chessman.y + direction[1];
                if (nx >= 0 && nx < Constants.ROWS && ny >= 0 && ny < Constants.ROWS && board.chessmenArray[nx][ny] == null){
                    set.add(nx+"-"+ny);
                }
            }
        }
        liberty = set.size();
    }
}
