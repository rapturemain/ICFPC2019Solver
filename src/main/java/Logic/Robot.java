package Logic;

import java.util.ArrayList;

/**
 * Robot class. All robot tools here.
 */

public class Robot {
    public Robot(int x, int y) {
        this.x = x;
        this.y = y;
        hands = new ArrayList<Point>(4);
        hands.add(new Point(0, 0));
        hands.add(new Point(1, 0));
        hands.add(new Point(1, 1));
        hands.add(new Point(1, -1));
        rotation = 0;
    }

    // Global coordinates.
    private int x;
    private int y;
    // 0 - EAST, 1 - SOUTH, 2 - WEST, 3 - NORTH
    private int rotation;
    // Robot's hand coordinates.
    private ArrayList<Point> hands;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    /**
     *  Adds manipulator to the robot. Pattern is always same - extend basic T shaped manipulators.
     *  Saves to move connection coordinates.
     *  This action cannot be canceled.
     */
    public void addHand(Move move) {
        if (hands.size() % 2 == 0) {
            switch (rotation % 4) {
                case 0:
                    hands.add(new Point(1, hands.size() / 2));
                    move.infoX = 1;
                    move.infoY = hands.size() / 2;
                    break;
                case 1:
                    hands.add(new Point(hands.size() / 2, -1));
                    move.infoX = hands.size() / 2;
                    move.infoY = -1;
                    break;
                case 2:
                    hands.add(new Point(-1, -hands.size() / 2));
                    move.infoX = -1;
                    move.infoY = -hands.size() / 2;
                    break;
                case 3:
                    hands.add(new Point(-hands.size() / 2, 1));
                    move.infoX = -hands.size() / 2;
                    move.infoY = 1;
                    break;
            }
            return;
        }
        if (hands.size() % 2 == 1) {
            switch (rotation % 4) {
                case 0:
                    hands.add(new Point(1, -hands.size() / 2));
                    move.infoX = 1;
                    move.infoY = -hands.size() / 2 + 1;
                    break;
                case 1:
                    hands.add(new Point(-hands.size() / 2, -1));
                    move.infoX = -hands.size() / 2 + 1;
                    move.infoY = -1;
                    break;
                case 2:
                    hands.add(new Point(-1, hands.size() / 2));
                    move.infoX = -1;
                    move.infoY = hands.size() / 2 - 1;
                    break;
                case 3:
                    hands.add(new Point(hands.size() / 2, 1));
                    move.infoX = hands.size() / 2 - 1;
                    move.infoY = 1;
                    break;
            }
        }
    }

    /**
     * Paint map. Saves data to move for unMove method.
     * @param map to paint
     * @param width of map
     * @param move to save unpaint data.
     */
    public void paintMap(int[] map, int width, Move move) {
        for (Point point : hands) {
            int index = indexOf(point.x, point.y, width);
            if (reachable(point, map, width) && inside(index, map) && map[index] == Game.Cell.valueOf(Game.Cell.EMPTY)) {
                map[index] = Game.Cell.valueOf(Game.Cell.PAINTED);
                if (!move.modified.contains(index)) {
                    move.modified.add(index);
                }
            }
        }
    }

    /**
     * Unpaints map. Data provided by move.
     * @param map to unpaint
     * @param move data provider.
     */
    public void unPaintMap(int[] map, Move move) {
        for (Integer i : move.modified) {
            map[i] = Game.Cell.valueOf(Game.Cell.EMPTY);
        }
    }

    /**
     * @param value positive - E (clockwise); negative - Q (counterclockwise)
     */
    public void rotate(int value) {
        if (value < 0) {
            this.rotation = rotation - 1 < 0 ? 3 : rotation - 1;
        } else {
            this.rotation = rotation + 1 > 3 ? 0 : rotation + 1;
        }
        for (Point point : hands) {
            rotatePoint(point, value >= 0 ? 1 : -1);
        }
    }

    /**
     *
     * @param map to check
     * @param widht of map
     * @return true, if any manipulator touches EMPTY cell.
     */
    public boolean isEmptyClose(int[] map, int widht) {
        for (int i = 0; i < 4; i++) {
            Point hand = hands.get(i);
            if (reachable(hand, map, widht)) {
                int index = indexOf(hand.x, hand.y, widht);
                if (inside(index, map) && map[index] != Game.Cell.valueOf(Game.Cell.WALL)) {
                    if (checkCell(hand.x + 1, hand.y, map, widht) || checkCell(hand.x - 1, hand.y, map, widht) ||
                            checkCell(hand.x, hand.y + 1, map, widht) || checkCell(hand.x, hand.y - 1, map, widht)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Helper method for isEmptyClose()
     */
    private boolean checkCell(int offsetX, int offsetY, int[] map, int width) {
        int index = this.x + offsetX + (this.y + offsetY)* width;
        if (inside(index, map) && map[index] == Game.Cell.valueOf(Game.Cell.EMPTY)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks reach of hand. Look at the specification for more info.
     * ONLY WORKS WITH THIS PATTERN OF CONNECTION.
     * @param hand to check
     * @param map with obstacles
     * @param width of map
     * @return true if hand is reachable.
     */
    private boolean reachable(Point hand, int[] map, int width) {
        int dx = hand.x == 1 ? 1 : hand.x == -1 ? -1 : Math.abs(hand.x / 2) + 1;
        int dy = hand.y == 1 ? 1 : hand.y == -1 ? -1 : Math.abs(hand.y / 2) + 1;
        boolean corner = (hand.x + hand.y) % 2 == 0;

        int x = 0;
        int y = 0;
        // Right 1
        if (hand.x == 1) {
            // Up
            if (hand.y > 0) {
                for (int i = 0; i < dy; i++) {
                    if (map[indexOf(x, y++, width)] == Game.Cell.valueOf(Game.Cell.WALL)) {
                        return false;
                    }
                }
                if (!corner) {
                    y--;
                }
                x++;
                for (int i = 0; i < dy; i++) {
                    if (map[indexOf(x, y++, width)] == Game.Cell.valueOf(Game.Cell.WALL)) {
                        return false;
                    }
                }
                return true;
            }
            // Down
            if (hand.y < 0) {
                for (int i = 0; i < dy; i++) {
                    if (map[indexOf(x, y--, width)] == Game.Cell.valueOf(Game.Cell.WALL)) {
                        return false;
                    }
                }
                if (!corner) {
                    y++;
                }
                x++;
                for (int i = 0; i < dy; i++) {
                    if (map[indexOf(x, y--, width)] == Game.Cell.valueOf(Game.Cell.WALL)) {
                        return false;
                    }
                }
                return true;
            }
        }
        // Left 1
        if (hand.x == -1) {
            // Up
            if (hand.y > 0) {
                for (int i = 0; i < dy; i++) {
                    if (map[indexOf(x, y++, width)] == Game.Cell.valueOf(Game.Cell.WALL)) {
                        return false;
                    }
                }
                if (!corner) {
                    y--;
                }
                x--;
                for (int i = 0; i < dy; i++) {
                    if (map[indexOf(x, y++, width)] == Game.Cell.valueOf(Game.Cell.WALL)) {
                        return false;
                    }
                }
                return true;
            }
            // Down
            if (hand.y < 0) {
                for (int i = 0; i < dy; i++) {
                    if (map[indexOf(x, y--, width)] == Game.Cell.valueOf(Game.Cell.WALL)) {
                        return false;
                    }
                }
                if (!corner) {
                    y++;
                }
                x--;
                for (int i = 0; i < dy; i++) {
                    if (map[indexOf(x, y--, width)] == Game.Cell.valueOf(Game.Cell.WALL)) {
                        return false;
                    }
                }
                return true;
            }
        }
        // Up 1
        if (hand.y == 1) {
            // Right
            if (hand.x > 0) {
                for (int i = 0; i < dx; i++) {
                    if (map[indexOf(x++, y, width)] == Game.Cell.valueOf(Game.Cell.WALL)) {
                        return false;
                    }
                }
                if (!corner) {
                    x--;
                }
                y++;
                for (int i = 0; i < dx; i++) {
                    if (map[indexOf(x++, y, width)] == Game.Cell.valueOf(Game.Cell.WALL)) {
                        return false;
                    }
                }
                return true;
            }
            // Left
            if (hand.x < 0) {
                for (int i = 0; i < dx; i++) {
                    if (map[indexOf(x--, y, width)] == Game.Cell.valueOf(Game.Cell.WALL)) {
                        return false;
                    }
                }
                if (!corner) {
                    x++;
                }
                y++;
                for (int i = 0; i < dx; i++) {
                    if (map[indexOf(x--, y, width)] == Game.Cell.valueOf(Game.Cell.WALL)) {
                        return false;
                    }
                }
                return true;
            }
        }
        // Down 1
        if (hand.y == -1) {
            // Right
            if (hand.x > 0) {
                for (int i = 0; i < dx; i++) {
                    if (map[indexOf(x++, y, width)] == Game.Cell.valueOf(Game.Cell.WALL)) {
                        return false;
                    }
                }
                if (!corner) {
                    x--;
                }
                y--;
                for (int i = 0; i < dx; i++) {
                    if (map[indexOf(x++, y, width)] == Game.Cell.valueOf(Game.Cell.WALL)) {
                        return false;
                    }
                }
                return true;
            }
            // Left
            if (hand.x < 0) {
                for (int i = 0; i < dx; i++) {
                    if (map[indexOf(x--, y, width)] == Game.Cell.valueOf(Game.Cell.WALL)) {
                        return false;
                    }
                }
                if (!corner) {
                    x++;
                }
                y--;
                for (int i = 0; i < dx; i++) {
                    if (map[indexOf(x--, y, width)] == Game.Cell.valueOf(Game.Cell.WALL)) {
                        return false;
                    }
                }
                return true;
            }

        }
        return true;
    }

    /**
     * @return index of point by it coordinates.
     */
    private int indexOf(int x, int y, int width) {
        return this.x + x + this.y * width + y * width;
    }

    /**
     * @param point to rotate
     * @param rotation - 1 - E (clockwise); -1 - Q (counterclockwise)
     */
    private void rotatePoint(Point point, int rotation) {
        int buffer = point.y;
        if (rotation == -1) {
            point.y = point.x;
            point.x = -buffer;
        } else {
            point.y = -point.x;
            point.x = buffer;
        }
    }

    /**
     * @return TRUE if inside of map.
     */
    private boolean inside(int index, int[] map) {
        return index >= 0 && index < map.length;
    }

    /**
     *  Container class for manipulator.
     */
    private class Point {
        private Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private int x;
        private int y;
    }
}
