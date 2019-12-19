package GUI;

import Logic.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;

/**
 * Visualisation and debugging tool.
 */

public class Main extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    private final int WIDTH = 1200;
    private final int HEIGHT = 1000;

    private double mouseStartX;
    private double mouseStartY;
    private double startX;
    private double startY;

    final static Game game = Game.loadFromFileString("src\\main\\resources\\prob-237.desc");
    final static Map map = new Map(game, 5);

    private Group root = new Group();
    private Scene scene;

    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("SolverIDA");
        primaryStage.setWidth(WIDTH);
        primaryStage.setHeight(HEIGHT);

        scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);

        // ----------------------------- //

        final Solver solver = new SolverFID();

        // ----------------------------- //
        root.getChildren().add(map);

        Button buttonReset = new Button("Reset");
        buttonReset.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                map.setLayoutX(0);
                map.setLayoutY(0);
            }
        });
        buttonReset.setLayoutX(67);
        root.getChildren().add(buttonReset);

        Button buttonSolver = new Button("Solve");
        buttonSolver.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Platform.runLater(new Thread() {
                    @Override
                    public void run() {
                        solver.solve(game);
                        System.out.println(((SolverFID) solver).moves.size());
                        map.draw();
                    }
                });
            }
        });
        buttonSolver.setLayoutX(161);
        root.getChildren().add(buttonSolver);

        Button buttonSolverMove = new Button("Move");
        buttonSolverMove.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Platform.runLater(new Thread() {
                    @Override
                    public void run() {
                        System.out.println(solver.nextMove(game));
                        game.nextRobot();
                        map.draw();
                    }
                });
            }
        });
        buttonSolverMove.setLayoutX(113);
        root.getChildren().add(buttonSolverMove);

        primaryStage.show();

        root.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    mouseStartX = event.getX();
                    mouseStartY = event.getY();
                    startX = root.getChildren().get(0).getLayoutX();
                    startY = root.getChildren().get(0).getLayoutY();
                }
                if (event.isAltDown() && event.isPrimaryButtonDown() && event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    root.getChildren().get(0).setLayoutX(startX + (event.getX() - mouseStartX) * 1.5);
                    root.getChildren().get(0).setLayoutY(startY + (event.getY() - mouseStartY) * 1.5);
                }
            }
        });

        root.addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            public void handle(ScrollEvent event) {
                if (event.isAltDown()) {
                    map.setCellSize((int) (map.getCellSize() + event.getDeltaY() * 0.05));
                    map.draw();
                }
            }
        });

        root.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.W) {
                    game.makeMove(new Move(Move.Type.MOVEUP));
                }
                if (event.getCode() == KeyCode.S) {
                    game.makeMove(new Move(Move.Type.MOVEDOWN));
                }
                if (event.getCode() == KeyCode.D) {
                    game.makeMove(new Move(Move.Type.MOVERIGHT));
                }
                if (event.getCode() == KeyCode.A) {
                    game.makeMove(new Move(Move.Type.MOVELEFT));
                }
                if (event.getCode() == KeyCode.Q) {
                    game.makeMove(new Move(Move.Type.ROTATE_Q));
                }
                if (event.getCode() == KeyCode.E) {
                    game.makeMove(new Move(Move.Type.ROTATE_E));
                }
                if (event.getCode() == KeyCode.C) {
                    game.unPaintMap();
                }
                if (event.getCode() == KeyCode.B) {
                    game.makeMove(new Move(Move.Type.APPLY_MANIPULATOR, 1, 0));
                }

                if (event.getCode() == KeyCode.G) {
                    Platform.runLater(new Thread() {
                        @Override
                        public void run() {
                            System.out.println(solver.nextMove(game));
                            game.nextRobot();
                            map.draw();
                        }
                    });
                }
                // Un move
                if (event.getCode() == KeyCode.UP) {
                    game.unMove(new Move(Move.Type.MOVEUP));
                }
                if (event.getCode() == KeyCode.DOWN) {
                    game.unMove(new Move(Move.Type.MOVEDOWN));
                }
                if (event.getCode() == KeyCode.RIGHT) {
                    game.unMove(new Move(Move.Type.MOVERIGHT));
                }
                if (event.getCode() == KeyCode.LEFT) {
                    game.unMove(new Move(Move.Type.MOVELEFT));
                }
                if (event.getCode() == KeyCode.HOME) {
                    game.unMove(new Move(Move.Type.ROTATE_Q));
                }
                if (event.getCode() == KeyCode.PAGE_UP) {
                    game.unMove(new Move(Move.Type.ROTATE_E));
                }
                map.draw();
            }
        });
    }

    public static void update() {
        map.draw();
    }
}
