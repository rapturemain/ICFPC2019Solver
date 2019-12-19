package Logic;

import java.util.LinkedList;
import java.util.List;

/**
 * Move container.
 */

public class Move {
    public Move(Type type, int unMoveBlocked, int whichRobot) {
        this.type = type;
        this.unMoveBlocked = unMoveBlocked;
        this.whichRobot = whichRobot;
        infoX = 0;
        infoY = 0;
    }

    public Move(Type type) {
        this.type = type;
        this.unMoveBlocked = 0;
        whichRobot = 0;
        infoX = 0;
        infoY = 0;
    }

    // Type of move.
    private Type type;
    // Tells unMove method that it is not possible to cancel this move. (due to lack of info and my laziness)
    private int unMoveBlocked;
    // Tells solution saver which robot makes this move.
    private int whichRobot;
    // Tells solution saver coordinates of extra manipulator connection
    int infoX;
    int infoY;

    // Modified cells by moves. Provides data for unpaint methods.
    List<Integer> modified = new LinkedList<Integer>();

    public Type getType() {
        return type;
    }

    public int getWhichRobot() {
        return whichRobot;
    }

    public boolean unMoveBlocked() {
        return unMoveBlocked == 1;
    }

    public enum Type {
        MOVEUP, MOVEDOWN, MOVERIGHT, MOVELEFT, ROTATE_E, ROTATE_Q, APPLY_MANIPULATOR, CREATE_CLONE
    }

    // Provides solution saver move as string.
    @Override
    public String toString() {
        switch (type) {
            case MOVEUP:
                return "W";
            case MOVEDOWN:
                return "S";
            case MOVERIGHT:
                return "D";
            case MOVELEFT:
                return "A";
            case ROTATE_Q:
                return "Q";
            case ROTATE_E:
                return "E";
            case CREATE_CLONE:
                return "C";
            case APPLY_MANIPULATOR:
                return "B" + "(" + infoX + "," + infoY + ")";
        }
        return "Error";
    }
}