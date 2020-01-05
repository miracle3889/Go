package sample;

public class Constants {
    //棋盘上两条线之间的距离
    public static int GAP = 40;
    //星位圆的半径
    public static int DOT_R = 5;
    //棋子的半径
    public static int CHESS_R = 18;
    //棋盘的行数
    public static int ROWS = 19;
    //棋盘界面的宽度,20行[ROWS+1]的间隙
    public static int WIDTH = GAP * (ROWS + 1);

    public static int START = (WIDTH - (ROWS -1) * GAP)/2;

    public static int END = WIDTH - START;

    public static Tuple[] DOT_ARRAY = {
            new Tuple(3,3),new Tuple(3,9),new Tuple(3,15),
            new Tuple(9,3),new Tuple(9,9),new Tuple(9,15),
            new Tuple(15,3),new Tuple(15,9),new Tuple(15,15)
    };

    public static final int[][] DIRECTIONS = new int[][]{{-1, 0},{1, 0},{0, -1},{0, 1}};

    public static final String ERROR_TYPE_1 = "落子后无气，不能在该点落子";

    public static final String ERROR_TYPE_2 = "打劫，不能在该点落子";

    public static final String OK = "OK";
}
