package bloodborne;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by xudabiao on 2017/2/8.
 */
public class ChessGroup {
    public int liberty;
    public int color;
    public List<Chessman> chessmanList;

    public ChessGroup(int color,int liberty,Chessman chessman) {
        this.color = color;
        this.liberty = liberty;
        this.chessmanList = new ArrayList<>();
        chessmanList.add(chessman);
    }

    //����ͬɫ����ϲ�Ϊһ��
    public void merge(ChessGroup chessGroup){
        if(this.color != chessGroup.color)
            throw new IllegalStateException("Different color!");
        chessmanList.addAll(chessGroup.chessmanList);
        for(Chessman chessman:chessGroup.chessmanList)
            chessman.chessGroup = this;
    }
    //��һ����
    public void decLibertyBy1(){
        this.liberty--;
    }

    @Override
    public String toString() {
        return "ChessGroup{" +
                "liberty=" + liberty +
                ", color=" + color +
                ", chessmanList=" + chessmanList +
                '}';
    }
}
