package Logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Game engine. Not so fast... but at least it works fine.
 */
public class Game {
    public Game(int width, int height) {
        this.width = width;
        this.height = height;
        this.map = new int[width * height];
    }

    // Map info
    private int width;
    private int height;
    private int[] map;
    private ArrayList<int[]> boosters;

    // Robot info
    private ArrayList<Integer> index = new ArrayList<Integer>();
    private ArrayList<Robot> robots = new ArrayList<Robot>();
    private ArrayList<Integer> robotsLastMoveIndex = new ArrayList<Integer>();
    private int whichMoves = 0;

    // Booster info
    // Clone
    private int cloneBoosterAvailable = 0;
    private int maxClones = 0;
    // Extra manipulator
    private int extraManipulatorAvailable = 0;
    private int extraManipulatorOnMapCount = 0;
    // On which move last booster was picked up
    private int lastBoosterPickedUp = 0;

    /**
     * Provides convenient to draw map representation.
     */
    public Cell[][] getCellMap() {
        Cell[][] cells = new Cell[this.width][this.height];
        for (int i = 0; i < this.map.length; i++) {
            cells[i % width][i / width] = Cell.valueOf(this.map[i]);
        }
        return cells;
    }

    /**
     * @return int representation of the boosters.
     */
    public ArrayList<int[]> getBoosters() {
        return this.boosters;
    }

    /**
     * @return int representation of the map.
     */
    public int[] getIntMap() {
        return map;
    }

    /**
     * @return available CLONE booster count.
     */
    public int getCloneBoosterAvailable() {
        return cloneBoosterAvailable;
    }

    public void decreaseBoosters(int value) {
        lastBoosterPickedUp -= value;
    }

    /**
     * Gives other robot turn.
     */
    public void nextRobot() {
        if (++whichMoves == robots.size() || whichMoves < 0) {
            whichMoves = 0;
        }
    }

    /**
     * @return count of active robots.
     */
    public int robotCount() {
        return robots.size();
    }

    /**
     * @return position of robot to move as map index.
     */
    public int getIndex() {
        return index.get(whichMoves);
    }

    /**
     * @return width of the map.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return number of CLONE booster found on map.
     */
    public int getMaxClones() {
        return maxClones;
    }

    /**
     * @return robot's to move index
     */
    public int getWhichMoves() {
        return whichMoves;
    }

    /**
     * Debug method.
     * Fully unpaints map. (Changes PAINTED cells to EMPTY)
     */
    public void unPaintMap() {
        for (int i = 0; i < map.length; i++) {
            if (map[i] == Cell.valueOf(Cell.PAINTED)) {
                map[i] = Cell.valueOf(Cell.EMPTY);
            }
        }
    }

    /**
     * @return EXTRA_MANIPULATOR booster count.
     */
    public int getExtraManipulatorOnMapCount() {
        return extraManipulatorOnMapCount;
    }

    /**
     * @return TRUE if robot have EMPTY cell in one move distance.
     */
    public boolean isEmptyClose() {
        return robots.get(whichMoves).isEmptyClose(map, width);
    }

    /**
     * Used by search algorithms.
     *
     * @return available moves.
     */
    public List<Move> getMoves() {
        List<Move> moves = new LinkedList<Move>();

        // Disabled. Clones not allowed to create clones.
//        if (cloneAvailable(0, 0)) {
//            moves.add(new Move(Move.Type.CREATE_CLONE, 1, whichMoves));
//            return moves;
//        }

        // Booster (EXTRA_MANIPULATOR)
        if (lastBoosterPickedUp < robotsLastMoveIndex.get(whichMoves) - 2 && extraManipulatorAvailable > 0) {
            moves.add(new Move(Move.Type.APPLY_MANIPULATOR, 1, whichMoves));
            return moves;
        }

        // Moves
        // Right
        if (index.get(whichMoves) % width < width - 1) {
            if (map[index.get(whichMoves) + 1] != Cell.valueOf(Cell.WALL)) {
                moves.add(new Move(Move.Type.MOVERIGHT, checkMove(1, 0), whichMoves));
            }
        }
        // Left
        if (index.get(whichMoves) % width > 0) {
            if (map[index.get(whichMoves) - 1] != Cell.valueOf(Cell.WALL)) {
                moves.add(new Move(Move.Type.MOVELEFT, checkMove(-1, 0), whichMoves));
            }
        }
        // Down
        if (index.get(whichMoves) / width > 0) {
            if (map[index.get(whichMoves) - width] != Cell.valueOf(Cell.WALL)) {
                moves.add(new Move(Move.Type.MOVEDOWN, checkMove(0, -1), whichMoves));
            }
        }
        // Up
        if (index.get(whichMoves) / width < height - 1) {
            if (map[index.get(whichMoves) + width] != Cell.valueOf(Cell.WALL)) {
                moves.add(new Move(Move.Type.MOVEUP, checkMove(0, 1), whichMoves));
            }
        }

        // Rotation
        moves.add(new Move(Move.Type.ROTATE_Q, 0, whichMoves));
        moves.add(new Move(Move.Type.ROTATE_E, 0, whichMoves));

        return moves;
    }


    /**
     * @return 1 (unMove unavailable) if booster collection possible.
     */
    private int checkMove(int offsetX, int offsetY) {
        return (checkForBoosters(offsetX, offsetY) ? 1 : 0);
    }

    /**
     * Checks for booster at location regarding to robot's position.
     *
     * @param offsetX - to be checked about robot's to move position.
     * @param offsetY - to be checked about robot's to move position.
     * @return true, if there's booster.
     */
    private boolean checkForBoosters(int offsetX, int offsetY) {
        for (int i = 0; i < boosters.size() - robots.size(); i++) {
            if (boosters.get(i)[0] == Booster.valueOf(Booster.CLONE)
                    && boosters.get(i)[1] == robots.get(whichMoves).getX() + offsetX
                    && boosters.get(i)[2] == robots.get(whichMoves).getY() + offsetY) {
                return true;
            }
            if (boosters.get(i)[0] == Booster.valueOf(Booster.EXTRA_MANIPULATOR)
                    && boosters.get(i)[1] == robots.get(whichMoves).getX() + offsetX
                    && boosters.get(i)[2] == robots.get(whichMoves).getY() + offsetY) {
                return true;
            }
        }
        return false;
    }

    /**
     * Collect boosters if possibly.
     */
    private void collectBoosters() {
        for (int i = 0; i < boosters.size() - robots.size(); i++) {
            if (boosters.get(i)[0] == Booster.valueOf(Booster.CLONE)
                    && boosters.get(i)[1] == robots.get(whichMoves).getX()
                    && boosters.get(i)[2] == robots.get(whichMoves).getY()) {
                cloneBoosterAvailable++;
                boosters.get(i)[0] = Booster.valueOf(Booster.NULL);
                if (robotsLastMoveIndex.get(whichMoves) > lastBoosterPickedUp) {
                    lastBoosterPickedUp = robotsLastMoveIndex.get(whichMoves);
                }
            }
            if (boosters.get(i)[0] == Booster.valueOf(Booster.EXTRA_MANIPULATOR)
                    && boosters.get(i)[1] == robots.get(whichMoves).getX()
                    && boosters.get(i)[2] == robots.get(whichMoves).getY()) {
                extraManipulatorAvailable++;
                boosters.get(i)[0] = Booster.valueOf(Booster.NULL);
                if (robotsLastMoveIndex.get(whichMoves) > lastBoosterPickedUp) {
                    lastBoosterPickedUp = robotsLastMoveIndex.get(whichMoves);
                }
                extraManipulatorOnMapCount--;
            }
        }
    }

    /**
     * @return TRUE, if possible to create clone.
     */
    private boolean cloneAvailable() {
        for (int i = 0; i < boosters.size() - robots.size(); i++) {
            if (boosters.get(i)[0] == Booster.valueOf(Booster.X_POINT)
                    && boosters.get(i)[1] == robots.get(whichMoves).getX()
                    && boosters.get(i)[2] == robots.get(whichMoves).getY()) {
                return cloneBoosterAvailable > 0;
            }
        }
        return false;
    }

    /**
     * Makes move. Some moves available to be unmoved.
     */
    public void makeMove(Move move) {
        if (move == null) {
            System.out.println("WRONG");
        }

        robotsLastMoveIndex.set(whichMoves, robotsLastMoveIndex.get(whichMoves) + 1);

        switch (move.getType()) {
            case MOVERIGHT:
                index.set(whichMoves, index.get(whichMoves) + 1);
                moveRobot(move);
                break;
            case MOVELEFT:
                index.set(whichMoves, index.get(whichMoves) - 1);
                moveRobot(move);
                break;
            case MOVEUP:
                index.set(whichMoves, index.get(whichMoves) + width);
                moveRobot(move);
                break;
            case MOVEDOWN:
                index.set(whichMoves, index.get(whichMoves) - width);
                moveRobot(move);
                break;
            case ROTATE_Q:
                robots.get(whichMoves).rotate(-1);
                moveRobot(move);
                break;
            case ROTATE_E:
                robots.get(whichMoves).rotate(1);
                moveRobot(move);
                break;
            case CREATE_CLONE:
                if (cloneAvailable()) {
                    index.add(index.get(whichMoves));
                    cloneBoosterAvailable--;
                    robots.add(new Robot(robots.get(whichMoves).getX(), robots.get(whichMoves).getY()));
                    robots.get(robots.size() - 1).paintMap(map, width, new Move(Move.Type.ROTATE_Q));
                    boosters.add(new int[3]);
                    boosters.get(boosters.size() - 1)[0] = Booster.valueOf(Booster.ROBOT);
                    boosters.get(boosters.size() - 1)[1] = robots.get(robots.size() - 1).getX();
                    boosters.get(boosters.size() - 1)[2] = robots.get(robots.size() - 1).getY();
                    robotsLastMoveIndex.add(robotsLastMoveIndex.get(whichMoves));
                }
                break;
            case APPLY_MANIPULATOR:
                extraManipulatorAvailable--;
                robots.get(whichMoves).addHand(move);
                robots.get(whichMoves).paintMap(map, width, move);
        }

        collectBoosters();

        if (move.getType() == Move.Type.CREATE_CLONE) {
            whichMoves--;
            return;
        }

        if (map[index.get(whichMoves)] == Cell.valueOf(Cell.WALL)) {
            System.out.println("---------------------------");
            unMove(move);
        }
    }

    /**
     * Move back method. Not all moves supported.
     */
    public void unMove(Move move) {

        robotsLastMoveIndex.set(whichMoves, robotsLastMoveIndex.get(whichMoves) - 1);

        switch (move.getType()) {
            case MOVERIGHT:
                index.set(whichMoves, index.get(whichMoves) - 1);
                unMoveRobot(move);
                break;
            case MOVELEFT:
                index.set(whichMoves, index.get(whichMoves) + 1);
                unMoveRobot(move);
                break;
            case MOVEUP:
                index.set(whichMoves, index.get(whichMoves) - width);
                unMoveRobot(move);
                break;
            case MOVEDOWN:
                index.set(whichMoves, index.get(whichMoves) + width);
                unMoveRobot(move);
                break;
            case ROTATE_Q:
                robots.get(whichMoves).unPaintMap(map, move);
                robots.get(whichMoves).rotate(1);
                break;
            case ROTATE_E:
                robots.get(whichMoves).unPaintMap(map, move);
                robots.get(whichMoves).rotate(-1);
                break;
        }
    }

    /**
     * Makes robot move.
     */
    private void moveRobot(Move move) {
        boosters.get(boosters.size() - robots.size() + whichMoves)[1] = index.get(whichMoves) % width;
        boosters.get(boosters.size() - robots.size() + whichMoves)[2] = index.get(whichMoves) / width;
        robots.get(whichMoves).setX(index.get(whichMoves) % width);
        robots.get(whichMoves).setY(index.get(whichMoves) / width);
        robots.get(whichMoves).paintMap(map, width, move);
    }

    /**
     * Move back robot.
     */
    private void unMoveRobot(Move move) {
        robots.get(whichMoves).unPaintMap(map, move);
        boosters.get(boosters.size() - robots.size() + whichMoves)[1] = index.get(whichMoves) % width;
        boosters.get(boosters.size() - robots.size() + whichMoves)[2] = index.get(whichMoves) / width;
        robots.get(whichMoves).setX(index.get(whichMoves) % width);
        robots.get(whichMoves).setY(index.get(whichMoves) / width);
    }

    /**
     * @return GameContainer for search algorithms.
     */
    public GameContainer getGC() {
        return new GameContainer(this);
    }

    /**
     * Creates Game using *.desc file path
     *
     * @return created G/ame
     */
    public static Game loadFromFileString(String path) {
        File file = new File(path);
        return loadFromFile(file);
    }

    /**
     * Creates Game using *.desc file.
     *
     * @return created Game.
     */
    public static Game loadFromFile(File file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String string = br.readLine();
            String[] parts = string.split("#");

            // ---------------------------------
            // Borders:

            // Split to numbers.
            parts[0] = parts[0].replaceAll("\\)", "");
            String[] borders = parts[0].split("\\(");

            // Filling numbers array.
            int[] numbers = new int[borders.length * 2];
            int minX = Integer.MAX_VALUE;
            int maxX = 0;
            int minY = Integer.MAX_VALUE;
            int maxY = 0;
            for (int i = 0; i < borders.length - 1; i++) {
                String[] pts = borders[i + 1].split(",");
                numbers[i * 2] = Integer.valueOf(pts[0]);
                if (numbers[i * 2] < minX) {
                    minX = numbers[i * 2];
                } else {
                    if (numbers[i * 2] > maxX) {
                        maxX = numbers[i * 2];
                    }
                }
                numbers[i * 2 + 1] = Integer.valueOf(pts[1]);
                if (numbers[i * 2 + 1] < minY) {
                    minY = numbers[i * 2 + 1];
                } else {
                    if (numbers[i * 2 + 1] > maxY) {
                        maxY = numbers[i * 2 + 1];
                    }
                }
            }

            numbers[numbers.length - 2] = numbers[0];
            numbers[numbers.length - 1] = numbers[1];

            int width = maxX - minX + 2;
            int height = maxY - minY + 2;

            int[] map = new int[width * height];
            Arrays.fill(map, -1);

            // Filing with wall cells.
            int prevX = numbers[0];
            int prevY = numbers[1];
            for (int i = 2; i < numbers.length; i += 2) {
                if (prevX == numbers[i]) {
                    if (prevY < numbers[i + 1]) {
                        // Up
                        for (int j = prevY; j < numbers[i + 1]; j++) {
                            map[(prevX + 1) + (j + 1) * width] = Cell.valueOf(Cell.WALL);
                        }
                    } else {
                        // Down
                        for (int j = numbers[i + 1]; j < prevY; j++) {
                            map[(prevX) + (j + 1) * width] = Cell.valueOf(Cell.WALL);
                        }
                    }
                } else {
                    if (prevX < numbers[i]) {
                        // Right
                        for (int j = prevX; j < numbers[i]; j++) {
                            map[(j + 1) + prevY * width] = Cell.valueOf(Cell.WALL);
                        }
                    } else {
                        // Left
                        for (int j = numbers[i]; j < prevX; j++) {
                            map[(j + 1) + (prevY + 1) * width] = Cell.valueOf(Cell.WALL);
                        }
                    }
                }
                prevX = numbers[i];
                prevY = numbers[i + 1];
            }

            // End borders.
            // ---------------------------------
            // Start pos:

            parts[1] = parts[1].replaceAll("\\)", "");
            parts[1] = parts[1].replaceAll("\\(", "");
            String[] startPos = parts[1].split(",");
            int startX = (Integer.valueOf(startPos[0]) + 1);
            int startY = (Integer.valueOf(startPos[1]) + 1);

            // End start pos.
            // ---------------------------------
            // Obstacles:

            if (parts.length > 2) {
                String[] obstacles = parts[2].split(";");
                for (String obstacle : obstacles) {
                    // Split to numbers.
                    borders = obstacle.replaceAll("\\)", "").split("\\(");

                    // Filling numbers array.
                    numbers = new int[borders.length * 2];
                    for (int i = 0; i < borders.length - 1; i++) {
                        String[] pts = borders[i + 1].split(",");
                        numbers[i * 2] = Integer.valueOf(pts[0]);
                        if (numbers[i * 2] < minX) {
                            minX = numbers[i * 2];
                        } else {
                            if (numbers[i * 2] > maxX) {
                                maxX = numbers[i * 2];
                            }
                        }
                        numbers[i * 2 + 1] = Integer.valueOf(pts[1]);
                        if (numbers[i * 2 + 1] < minY) {
                            minY = numbers[i * 2 + 1];
                        } else {
                            if (numbers[i * 2 + 1] > maxY) {
                                maxY = numbers[i * 2 + 1];
                            }
                        }
                    }
                    numbers[numbers.length - 2] = numbers[0];
                    numbers[numbers.length - 1] = numbers[1];

                    // Filing with wall cells.
                    prevX = numbers[0];
                    prevY = numbers[1];
                    for (int i = 2; i < numbers.length; i += 2) {
                        if (prevX == numbers[i]) {
                            if (prevY < numbers[i + 1]) {
                                // Up
                                for (int j = prevY; j < numbers[i + 1]; j++) {
                                    map[(prevX) + (j + 1) * width] = Cell.valueOf(Cell.WALL);
                                }
                            } else {
                                // Down
                                for (int j = numbers[i + 1]; j < prevY; j++) {
                                    map[(prevX + 1) + (j + 1) * width] = Cell.valueOf(Cell.WALL);
                                }
                            }
                        } else {
                            if (prevX < numbers[i]) {
                                // Right
                                for (int j = prevX; j < numbers[i]; j++) {
                                    map[(j + 1) + (prevY + 1) * width] = Cell.valueOf(Cell.WALL);
                                }
                            } else {
                                // Left
                                for (int j = numbers[i]; j < prevX; j++) {
                                    map[(j + 1) + (prevY) * width] = Cell.valueOf(Cell.WALL);
                                }
                            }
                        }
                        prevX = numbers[i];
                        prevY = numbers[i + 1];
                    }
                }
            }

            // Filling with empty cells and walls.
            depthFill(map, startX, startY, width);

            // End obstacles.
            // ---------------------------------
            // Boosters:

            int clones = 0;
            int extraSize = 0;
            ArrayList<int[]> boosters = new ArrayList<int[]>();
            if (parts.length > 3) {
                String[] boosterStrings = parts[3].split(";");
                for (int i = 0; i < boosterStrings.length; i++) {
                    String[] prt = boosterStrings[i].replaceAll("[()A-Z]", "").split(",");
                    boosters.add(new int[3]);
                    boosters.get(i)[0] = Booster.valueOf(Booster.valueOf(boosterStrings[i].charAt(0)));
                    boosters.get(i)[1] = Integer.valueOf(prt[0]) + 1;
                    boosters.get(i)[2] = Integer.valueOf(prt[1]) + 1;
                    if (boosters.get(i)[0] == Booster.valueOf(Booster.CLONE)) {
                        clones++;
                    }
                    if (boosters.get(i)[0] == Booster.valueOf(Booster.EXTRA_MANIPULATOR)) {
                        extraSize++;
                    }
                }
            }

            boosters.add(new int[3]);
            boosters.get(boosters.size() - 1)[0] = Booster.valueOf(Booster.ROBOT);
            boosters.get(boosters.size() - 1)[1] = startX;
            boosters.get(boosters.size() - 1)[2] = startY;

            // End boosters.
            // ---------------------------------

            Game game = new Game(width, height);
            game.map = map;
            game.boosters = boosters;
            game.index.add(startX + startY * width);
            game.robots.add(new Robot(startX, startY));
            game.robots.get(0).paintMap(game.map, width, new Move(Move.Type.ROTATE_Q));
            game.maxClones = clones;
            game.extraManipulatorOnMapCount = extraSize;
            game.robotsLastMoveIndex.add(0);

            return game;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Game(20, 20);
    }

    /**
     * Fills map with empty squares and walls. Uses depth search.
     *
     * @param map    to fill
     * @param startX start point to fill (usually robot's x)
     * @param startY start point to fill (usually robot's y)
     * @param width  of the map
     */
    private static void depthFill(int[] map, int startX, int startY, int width) {
        ArrayList<Point> points = new ArrayList<Point>();
        points.add(new Point(startX, startY));
        while (points.size() > 0) {
            Point point = points.get(0);
            if (map[point.x + width * point.y] == -1) {
                map[point.x + width * point.y] = Cell.valueOf(Cell.EMPTY);
                points.add(new Point(point.x + 1, point.y));
                points.add(new Point(point.x - 1, point.y));
                points.add(new Point(point.x, point.y + 1));
                points.add(new Point(point.x, point.y - 1));
            }
            points.remove(0);
        }

        // Filling left cells as wall.
        for (int i = 0; i < map.length; i++) {
            if (map[i] == -1) {
                map[i] = Cell.valueOf(Cell.WALL);
            }
        }
        System.out.println();
    }

    /**
     * Enum for representing map. Contains 3 elements in use:
     * <p>
     * EMPTY
     * WALL
     * PAINTED
     * <p>
     * All engine logic uses this enum.
     */
    public enum Cell {
        EMPTY(1), WALL(2), PAINTED(3), ROBOT(4), MANIPULATOR(5);

        Cell(int value) {
            this.value = value;
        }

        private int value;

        public static Cell valueOf(int value) {
            switch (value) {
                case 1:
                    return EMPTY;
                case 2:
                    return WALL;
                case 3:
                    return PAINTED;
                case 4:
                    return ROBOT;
                case 5:
                    return MANIPULATOR;
                default:
                    return PAINTED;
            }
        }

        public static int valueOf(Cell cell) {
            return cell.value;
        }

        @Override
        public String toString() {
            switch (value) {
                case 1:
                    return "EMPTY";
                case 2:
                    return "WALL";
                case 3:
                    return "PAINTED";
                case 4:
                    return "ROBOT";
                case 5:
                    return "MANIPULATOR";
                default:
                    return "WRONG";
            }
        }
    }

    /**
     * Enum for representing booster. Contains 4 elements in use:
     * <p>
     * EXTRA_MANIPULATOR
     * ROBOT
     * CLONE
     * NULL
     * <p>
     * Booster logic and robot position based on this enum.
     */
    public enum Booster {
        EXTRA_MANIPULATOR(0), SPEED(1), DRILL(2), X_POINT(3), ROBOT(4), NULL(5), CLONE(6);

        Booster(int value) {
            this.value = value;
        }

        private int value;

        public static Booster valueOf(char value) {
            switch (value) {
                case 'B':
                    return EXTRA_MANIPULATOR;
//                case 'F':
//                    return SPEED;
//                case 'L':
//                    return DRILL;
                case 'X':
                    return X_POINT;
                case 'C':
                    return CLONE;
                default:
                    return NULL;
            }
        }

        public static Booster valueOf(int value) {
            switch (value) {
                case 0:
                    return EXTRA_MANIPULATOR;
                case 1:
                    return SPEED;
                case 2:
                    return DRILL;
                case 3:
                    return X_POINT;
                case 4:
                    return ROBOT;
                case 5:
                    return NULL;
                case 6:
                    return CLONE;
                default:
                    return NULL;
            }
        }

        public static int valueOf(Booster booster) {
            return booster.value;
        }
    }

    /**
     * Class for depthFill() method. Contains point information.
     */
    private static class Point {
        private Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private int x;
        private int y;

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.getClass() != this.getClass()) {
                return false;
            }
            return ((Point) obj).x == this.x && ((Point) obj).y == this.y;
        }
    }

    /**
     * Class for solvers. A little bit compacted info about the game.
     */
    public static class GameContainer {
        GameContainer(Game game) {
            this.map = Arrays.copyOf(game.map, game.map.length);
            this.boosters = new ArrayList<int[]>();
            for (int i = 0; i < game.boosters.size(); i++) {
                this.boosters.add(Arrays.copyOf(game.boosters.get(i), game.boosters.get(i).length));
            }
        }

        private int[] map;
        private ArrayList<int[]> boosters;
        int rotation;

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            GameContainer gc = (GameContainer) obj;
            if (gc.boosters.size() != this.boosters.size()) {
                return false;
            }
            for (int i = 0; i < this.boosters.size(); i++) {
                if (this.boosters.get(i)[0] != gc.boosters.get(i)[0] || this.boosters.get(i)[1] != gc.boosters.get(i)[1] || this.boosters.get(i)[2] != gc.boosters.get(i)[2]) {
                    return false;
                }
            }
            return Arrays.equals(this.map, gc.map) && this.rotation == gc.rotation;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.map);
        }
    }

}
