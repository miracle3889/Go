package bloodborne;

import sample.Tuple;

import java.util.*;
import static sample.Constants.*;
import static sample.Constants.ERROR_TYPE_2;

public class Board {

    public HashSet<ChessGroup> allGroup = new HashSet<>();

    public Chessman[][] chessmenArray = new Chessman[ROWS][ROWS];

    public List<Tuple> records = new ArrayList();

    /**
     * 当每次只提掉一颗子时记录，用于打劫判断
     */
    private String formerDelete;

    public String addMove(Tuple tuple, int step){
        Chessman chessman = new Chessman(tuple.i, tuple.j, this, step);
        HashSet<ChessGroup> sameColor = new HashSet();
        HashSet<ChessGroup> diffColor = new HashSet();
        int x = chessman.x, y = chessman.y;
        ChessGroup chessmanGroup = chessman.chessGroup;
        int nx, ny;
        for (int[] direction : DIRECTIONS) {
            nx = x + direction[0];
            ny = y + direction[1];
            if (nx >= 0 && nx < ROWS && ny >= 0 && ny < ROWS && chessmenArray[nx][ny] != null) {
                if(chessmenArray[nx][ny].color == chessman.color)
                    sameColor.add(chessmenArray[nx][ny].chessGroup);
                else
                    diffColor.add(chessmenArray[nx][ny].chessGroup);
            }
        }
        int mergedLiberty = chessmanGroup.liberty;
        for (ChessGroup chessGroup : sameColor){
            mergedLiberty = mergedLiberty + chessGroup.liberty - 1;
        }
        List<ChessGroup> deleteList = new ArrayList<>();
        for (ChessGroup chessGroup : diffColor)
            if (chessGroup.liberty == 1) {
                deleteList.add(chessGroup);
            }
        //落子后自己无气
        if (deleteList.size() == 0 && mergedLiberty == 0)
            return ERROR_TYPE_1;
        //只提一个子
        boolean delOne = deleteList.size() == 1 && deleteList.get(0).chessmanList.size() == 1;
        if (delOne) {
            Chessman cm = deleteList.get(0).chessmanList.get(0);
            //落的子是上一轮提掉且这一轮提的是上一轮下的子 是为打劫
            if (key(chessman).equals(formerDelete) && cm.x == records.get(records.size()-1).i
                    && cm.y == records.get(records.size()-1).j)
                return ERROR_TYPE_2;
            //不是打劫，记录下这次提的子
            formerDelete = key(cm);
        }
        //可以落子，把该子加入Array
        records.add(tuple);
        chessmenArray[x][y] = chessman;
        allGroup.add(chessmanGroup);
        //合并同色棋
        for (ChessGroup chessGroup : sameColor){
            chessmanGroup.merge(chessGroup);
            allGroup.remove(chessGroup);
        }
        //去掉被提掉的棋
        for (ChessGroup chessGroup : deleteList) {
            for (Chessman chessman1 : chessGroup.chessmanList) {
                chessmenArray[chessman1.x][chessman1.y] = null;
            }
            allGroup.remove(chessGroup);
        }
        //提掉不止一颗棋子，那么不需要记录打劫信息
        if (!delOne){
            formerDelete = null;
        }
        //全部重新算气
        for (ChessGroup group : allGroup) {
            group.calc();
        }
        return OK;
    }

    //清空
    public void clear(){
        allGroup.clear();
        for (Chessman[] chessmen : chessmenArray) {
            Arrays.fill(chessmen, null);
        }
    }

    public String key(Chessman chessman){
        return chessman.x + "-" + chessman.y;
    }

    public Board revert(){
        Board board = new Board();
        for (int i = 0; i < records.size() - 1; i++) {
            board.addMove(records.get(i), i);
        }
        return board;
    }
}
