package sample;

import bloodborne.Board;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;

import java.sql.Time;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static sample.Constants.*;

public class Main extends Application {

    /**
     * 所有的棋子
     * 提前new 所有 Circle 对象
     * 通过设置颜色和visible属性来改变
     */
    private Circle[][] cs = new Circle[ROWS][ROWS];
    private Label[][] csl = new Label[ROWS][ROWS];

    /**
     * 主处理
     */
    private Board board = new Board();

    private Label error1, error2;

    private ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

    private LinkedList<Tuple> records = new LinkedList();
    //用于前后进，在records范围内
    private int step = 0;

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        stage.setTitle("Go");
        Scene scene = new Scene(root, WIDTH, WIDTH);
        stage.setResizable(false);
        paintBoard(root);
        //当鼠标移动时的虚拟棋子，用来显示落子点
        Circle vitual = newCircle(null, null, false);
        root.getChildren().add(vitual);
        scene.setOnMouseMoved((event)->{
            Tuple position = getClosestPosition(new Tuple((int)event.getX(),(int)event.getY()));
            if (validPosition(position)){
                Tuple coord = getCoordinate(position);
                vitual.setCenterX(coord.i);
                vitual.setCenterY(coord.j);
                vitual.setFill(getColor(board.records.size()));
                vitual.setVisible(true);
            }else{
                vitual.setVisible(false);
            }
        });
        //落子的逻辑
        scene.setOnMouseClicked((event)->{
            Tuple position = getClosestPosition(new Tuple((int)event.getX(),(int)event.getY()));
            if (validPosition(position)){
                vitual.setVisible(false);
                String msg = board.addMove(position, step);
                if (!OK.equals(msg)){
                    Label label = ERROR_TYPE_1.equals(msg) ? error1 : error2;
                    label.setVisible(true);
                    service.schedule(() -> Platform.runLater(() -> label.setVisible(false)), 1, TimeUnit.SECONDS);
                    return;
                }
                while (step < records.size()){
                    records.pollLast();
                }
                records.add(position);
                step = records.size();
                render();
            }
        });
        scene.setOnKeyPressed(event -> {
            if ("Left".equals(event.getCode().getName())){
                if (step > 0) {
                    board = board.revert();
                    step--;
                    render();
                }
            }
            else if ("Right".equals(event.getCode().getName())){
                if (step < records.size()) {
                    board.addMove(records.get(step), step);
                    step++;
                    render();
                }
            }
            else if ("Up".equals(event.getCode().getName())) {
                System.out.println("UUUp");
                board = new Board();
                step = 0;
                render();
            }
            else if ("Down".equals(event.getCode().getName())){
                while (board.records.size() > 0){
                    board = board.revert();
                    render();
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    }catch (Exception e){}
                }
            }
        });
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> service.shutdownNow());
        stage.show();
    }

    private void render() {
        for (int i = 0; i < cs.length; i++) {
            for (int j = 0; j < cs[0].length; j++) {
                if (board.chessmenArray[i][j] == null){
                    cs[i][j].setVisible(false);
//                    csl[i][j].setVisible(false);
                }
                else{
                    cs[i][j].setFill(getColor(board.chessmenArray[i][j].color));
                    cs[i][j].setVisible(true);
//                    csl[i][j].setText(board.chessmenArray[i][j].number+"");
//                    csl[i][j].setTextFill(getColor(1-board.chessmenArray[i][j].color));
//                    csl[i][j].setVisible(true);
                }
            }
        }
    }

    /**
     * 绘制棋盘
     * 1。棋盘线
     * 2。星位
     * 3。每个棋子【隐藏且不着色】
     * @param root
     */
    private void paintBoard(Group root) {
        ObservableList<Node> list = root.getChildren();
        int cr = START;
        //画线
        for (int i = 0; i < ROWS; i++) {
            list.add(new Line(START, cr, END, cr));
            list.add(new Line(cr, START, cr, END));
            cr += GAP;
        }
        //画星位
        for (Tuple tuple : DOT_ARRAY) {
            Tuple xy = getCoordinate(tuple);
            Circle circle = new Circle(xy.i, xy.j, DOT_R);
            circle.setFill(Color.BLACK);
            list.add(circle);
        }
        //棋子
        for (int i = 0; i < cs.length; i++) {
            for (int j = 0; j < cs.length; j++) {
                Tuple tuple = getCoordinate(new Tuple(i, j));
                cs[i][j] = newCircle(tuple.i, tuple.j, false);
                csl[i][j] = new Label();
                csl[i][j].setLayoutX(tuple.i-GAP/4);
                csl[i][j].setLayoutY(tuple.j-GAP/4);
                csl[i][j].setVisible(false);
                root.getChildren().add(cs[i][j]);
                root.getChildren().add(csl[i][j]);
            }
        }
        //提示
        error1 = new Label(ERROR_TYPE_1);
        error2 = new Label(ERROR_TYPE_2);
        error1.setVisible(false);
        error2.setVisible(false);
        root.getChildren().add(error1);
        root.getChildren().add(error2);
    }

    private Circle newCircle(Integer x, Integer y, boolean visible){
        Circle circle = new Circle();
        if (x != null)
            circle.setCenterX(x);
        if (y != null)
            circle.setCenterY(y);
        circle.setRadius(CHESS_R);
        circle.setVisible(visible);
        //设置圆环的边框为黑
        circle.setStrokeType(StrokeType.INSIDE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(1);
        return circle;
    }
    //根据鼠标位置获得数组坐标
    private Tuple getClosestPosition(Tuple tuple){
        int row = tuple.i / GAP + (tuple.i % GAP < GAP / 2? -1:0);
        int col = tuple.j / GAP + (tuple.j % GAP < GAP / 2? -1:0);
        return new Tuple(row,col);
    }
    //根据数组坐标获得像素坐标
    private Tuple getCoordinate(Tuple tuple){
        return new Tuple(tuple.i * GAP + START,tuple.j * GAP + START);
    }

    private boolean validPosition(Tuple position){
        return position.i >= 0 && position.i < ROWS && position.j >= 0 && position.j < ROWS && board.chessmenArray[position.i][position.j] == null;
    }

    private Color getColor(int p){
        return p % 2 == 0 ? Color.BLACK : Color.WHITE;
    }

    public static void main(String[] args) {
        launch(args);
    }
}