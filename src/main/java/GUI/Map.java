package GUI;

import Logic.Game;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Map extends Region {
    public Map(Game game, int cellSize) {
        this.game = game;
        this.cellSize = cellSize;
        draw();
    }

    public Map(int cellSize) {
        this.cellSize = cellSize;
    }

    private Game game;
    private Canvas canvas;
    private int cellSize;

    public void setGame(Game game) {
        this.game = game;
        draw();
    }

    public void setCellSize(int size) {
        this.cellSize = size;
    }

    public int getCellSize() {
        return cellSize;
    }

    public void draw() {
        drawStandart();
    }

    private void drawStandart() {
        if (game != null) {
            Game.Cell[][] map = game.getCellMap();
            int size = Math.min(map.length * cellSize, map[0].length * cellSize);
            if (size < 400) {
                this.cellSize = 400 / Math.min(map.length, map[0].length);
            }
            this.getChildren().clear();
            this.canvas = new Canvas(map.length * cellSize, map[0].length * cellSize);
            this.getChildren().add(canvas);
            for (int column = 0; column < map.length; column++) {
                for (int row = 0; row < map[column].length; row++) {
                    canvas.getGraphicsContext2D().setFill(Color.GRAY);
                    canvas.getGraphicsContext2D().fillRect(column * cellSize, (map[column].length - row - 1) * cellSize, cellSize, cellSize);
                    canvas.getGraphicsContext2D().setFill(getColor(map[column][row]));
                    canvas.getGraphicsContext2D()
                            .fillRect(column * cellSize, (map[column].length - row - 1) * cellSize, cellSize - 1, cellSize - 1);
                }
            }
            ArrayList<int[]> boosters = game.getBoosters();
            for (int i = 0; i < boosters.size(); i++) {
                canvas.getGraphicsContext2D().setFill(getColor(Game.Booster.valueOf(boosters.get(i)[0])));
                canvas.getGraphicsContext2D()
                        .fillRect(boosters.get(i)[1] * cellSize + 4, (map[0].length - boosters.get(i)[2] - 1) * cellSize + 4, cellSize - 8, cellSize - 8);
            }
        }
    }

    private Color getColor(Game.Cell cell) {
        switch (cell) {
            case WALL:
                return Color.BLACK;
            case EMPTY:
                return Color.WHITE;
            case PAINTED:
                return Color.YELLOW;
            case ROBOT:
                return Color.RED;
            case MANIPULATOR:
                return Color.DARKORANGE;
            default:
                return Color.LIGHTGRAY;
        }
    }

    private Color getColor(Game.Booster booster) {
        switch (booster) {
            case EXTRA_MANIPULATOR:
                return Color.BLUE;
            case SPEED:
                return Color.GREEN;
            case DRILL:
                return Color.MAGENTA;
            case X_POINT:
                return Color.DIMGRAY;
            case ROBOT:
                return Color.RED;
            case CLONE:
                return Color.BLACK;
            default:
                return new Color(1, 1, 1, 1);
        }
    }
}
