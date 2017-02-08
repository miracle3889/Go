package sample;

import bloodborne.ChessGroup;
import bloodborne.Chessman;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;
import org.omg.PortableInterceptor.INACTIVE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

public class Main extends Application {

    private static int gap = 40;

    private static int dot_radius = 5;

    private static int chess_piece = 18;

    private static int rows = 19;

    private static int dots = 9;

    private static int width = 800;

    private static int height = 800;

    private static int st = (width-(rows-1)*gap)/2;

    private static int ed = width-st;

    private static Tuple[] dotArray = {
            new Tuple(3,3),new Tuple(3,9),new Tuple(3,15),
            new Tuple(9,3),new Tuple(9,9),new Tuple(9,15),
            new Tuple(15,3),new Tuple(15,9),new Tuple(15,15)
    };

    private Circle[][] cs = new Circle[rows][rows];

    private Chessman[][] chessmenArray = new Chessman[rows][rows];
    //true -> black,false->white(start with true)
    private boolean nextStep = true;

    private HashSet<ChessGroup> sameColor = new HashSet<>();
    private HashSet<ChessGroup> diffColor = new HashSet<>();
    private HashSet<ChessGroup> allGroup = new HashSet<>();
    private Stack<ChessGroup> needDelete = new Stack<>();
    private List<Circle> deleteList = new ArrayList<>();
    private Group group;
    HashSet<Integer> liberty = new HashSet<>();

    private void clear(){
        allGroup.clear();
        for (int i =0 ;i<rows;i++)
            for (int j = 0; j <rows ; j++) {
                group.getChildren().remove(cs[i][j]);
                cs[i][j]=null;
                chessmenArray[i][j] = null;
        }

    }

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        group = root;
        stage.setTitle("Go");
        Scene scene = new Scene(root, width, height);
        stage.setResizable(false);
        List<Line> LineList = new ArrayList<>(rows);
        List<Circle> dotList = new ArrayList<>(dots);
        int cr = st;
        //paint line
        for (int i = 0; i < rows; i++) {
            LineList.add(new Line(st,cr,ed,cr));
            LineList.add(new Line(cr,st,cr,ed));
            cr+=gap;
        }
        //paint dot
        for (Tuple tuple:dotArray) {
            Tuple xy = getCoordinate(tuple);
            Circle circle = new Circle(xy.i,xy.j,dot_radius);
            circle.setFill(Color.BLACK);
            dotList.add(circle);
        }
        //vitual circle
        Circle vitual = new Circle();
        vitual.setRadius(chess_piece);
        vitual.setVisible(false);
        setStroke(vitual);
        root.getChildren().add(vitual);
        //
        scene.setOnMouseClicked((event)->{
            System.out.println(Thread.currentThread());
            Tuple position = getClosestPosition(new Tuple((int)event.getX(),(int)event.getY()));
            if (validPosition(position)){
                int x = position.i,y = position.j;
                int liberty = getLiberty(x,y);
                Chessman chessman = new Chessman(position.i,position.j,nextStep?0:1,liberty);
                int result =  mergeSameColorAndDecDiff(chessman);
                if(result == 2){
                    System.err.println("不能在该位置落子");
                    for (ChessGroup chessGroup:allGroup)
                        System.out.println(chessGroup);
                    return;
                }
                //add gui circle
                Tuple coord = getCoordinate(position);
                Circle circle = new Circle(coord.i,coord.j,chess_piece);
                circle.setFill(getColorAndReverse());
                setStroke(circle);
                cs[position.i][position.j]=circle;
                root.getChildren().add(circle);
                while(needDelete.size()>0){
                    remove(needDelete.pop());
                }
                for (ChessGroup chessGroup:allGroup)
                    System.out.println(chessGroup);
            }
        });
        //
        scene.setOnMouseMoved((event)->{
            Tuple position = getClosestPosition(new Tuple((int)event.getX(),(int)event.getY()));
            if (validPosition(position)){
                Tuple coord = getCoordinate(position);
                vitual.setCenterX(coord.i);
                vitual.setCenterY(coord.j);
                vitual.setFill(getColor());
                vitual.setVisible(true);
            }else{
                vitual.setVisible(false);
            }
        });
//        scene.setOnKeyTyped((event) -> {
//            System.out.println(event.getCharacter());
//        });
//        scene.setOnKeyPressed((event) -> {
//            System.out.println(event.getCharacter());
//        });
        stage.setScene(scene);
        root.getChildren().addAll(LineList);
        root.getChildren().addAll(dotList);
        stage.show();
    }

    private void remove(ChessGroup chessGroup) {
        for(Chessman chessman:chessGroup.chessmanList){
            deleteList.add(cs[chessman.x][chessman.y]);
            cs[chessman.x][chessman.y] = null;
            chessmenArray[chessman.x][chessman.y] = null;
        }
        group.getChildren().removeAll(deleteList);
        deleteList.clear();
        recalculLiberty(Math.abs(chessGroup.color-1));
    }

    private void recalculLiberty(int color){
        for(ChessGroup chessGroup:allGroup)
            if(chessGroup.color == color)
                recalculLiberty(chessGroup);
    }

    private void recalculLiberty(ChessGroup chessGroup) {
        int x = 0,y = 0;
        for (Chessman chessman:chessGroup.chessmanList){
            x = chessman.x;
            y = chessman.y;
            if((x-1)>=0&&chessmenArray[x-1][y]==null)
                liberty.add(_2to1(x-1,y));
            if((x+1)<=18&&chessmenArray[x+1][y]==null)
                liberty.add(_2to1(x+1,y));
            if((y-1)>=0&&chessmenArray[x][y-1]==null)
                liberty.add(_2to1(x,y-1));
            if((y+1)<=18&&chessmenArray[x][y+1]==null)
                liberty.add(_2to1(x,y+1));
        }
        chessGroup.liberty = liberty.size();
        liberty.clear();
    }

    private int _2to1(int i,int j){
        return 100*i+j;
    }

    private int mergeSameColorAndDecDiff(Chessman chessman) {
        int x = chessman.x,y = chessman.y;
        ChessGroup chessmanGroup = chessman.chessGroup;
        if((x-1)>=0&&chessmenArray[x-1][y]!=null)
            if(chessmenArray[x-1][y].chessGroup.color == chessman.chessGroup.color)
                sameColor.add(chessmenArray[x-1][y].chessGroup);
            else
                diffColor.add(chessmenArray[x-1][y].chessGroup);
        if((y-1)>=0&&chessmenArray[x][y-1]!=null)
            if(chessmenArray[x][y-1].chessGroup.color == chessman.chessGroup.color)
                sameColor.add(chessmenArray[x][y-1].chessGroup);
            else
                diffColor.add(chessmenArray[x][y-1].chessGroup);
        if((x+1)<=18&&chessmenArray[x+1][y]!=null)
            if(chessmenArray[x+1][y].chessGroup.color == chessman.chessGroup.color)
                sameColor.add(chessmenArray[x+1][y].chessGroup);
            else
                diffColor.add(chessmenArray[x+1][y].chessGroup);
        if((y+1)<=18&&chessmenArray[x][y+1]!=null)
            if(chessmenArray[x][y+1].chessGroup.color == chessman.chessGroup.color)
                sameColor.add(chessmenArray[x][y+1].chessGroup);
            else
                diffColor.add(chessmenArray[x][y+1].chessGroup);
        //first test
        int mergedLiberty = chessmanGroup.liberty;
        for (ChessGroup chessGroup:sameColor){
            mergedLiberty = mergedLiberty+chessGroup.liberty-1;
        }
        boolean hasDeleteSomething = false;
        for (ChessGroup chessGroup:diffColor)
            if (chessGroup.liberty -1 == 0) {
                hasDeleteSomething = true;
                break;
            }
        if (!hasDeleteSomething&&mergedLiberty == 0)
            return 2;
        //second do change
        allGroup.add(chessmanGroup);
        chessmenArray[x][y] = chessman;
        for (ChessGroup chessGroup:sameColor){
            chessmanGroup.merge(chessGroup);
            allGroup.remove(chessGroup);
        }
        recalculLiberty(chessmanGroup);
        for (ChessGroup chessGroup:diffColor) {
            chessGroup.decLibertyBy1();
            if(chessGroup.liberty==0) {
                needDelete.push(chessGroup);
                allGroup.remove(chessGroup);
            }
        }
        sameColor.clear();
        diffColor.clear();
        return 1;
    }

    //计算这个子有几口气
    private int getLiberty(int x,int y) {
        int liberty = 0 ;
        if((x-1)>=0&&chessmenArray[x-1][y]==null)
            liberty++;
        if((x+1)<=18&&chessmenArray[x+1][y]==null)
            liberty++;
        if((y-1)>=0&&chessmenArray[x][y-1]==null)
            liberty++;
        if((y+1)<=18&&chessmenArray[x][y+1]==null)
            liberty++;
        return liberty;
    }

    //设置圆边界
    private void setStroke(Circle circle){
        circle.setStrokeType(StrokeType.INSIDE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(1);
    }
    //计算相邻最近点的相对坐标
    private Tuple getClosestPosition(Tuple tuple){
        int row = tuple.i/gap+(tuple.i%gap<gap/2?-1:0);
        int col = tuple.j/gap+(tuple.j%gap<gap/2?-1:0);
        return new Tuple(row,col);
    }
    //根据坐标计算xy坐标
    private Tuple getCoordinate(Tuple tuple){
        return new Tuple(tuple.i*gap+st,tuple.j*gap+st);
    }

    //位置是否落于棋盘内
    private boolean validPosition(Tuple position){
        return (position.i>=0&&position.i<=18&&position.j>=0&&position.j<=18&&cs[position.i][position.j]==null);
    }

    private Color getColorAndReverse(){
        Color color = getColor();
        reverseColor();
        return color;
    }

    private Color getColor(){
        return nextStep?Color.BLACK:Color.WHITE;
    }

    private void reverseColor(){
        nextStep = !nextStep;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}