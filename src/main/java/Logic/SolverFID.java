package Logic;

import java.util.*;

/**
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 * Algorithm description:                                                     *
 *                                                                            *
 * If map supports clones: first robot collects and uses all CLONE boosters.  *
 * If robot available to use EXTRA_MANIPULATOR - it does it.                  *
 *                                                                            *
 * Do not use WHEELS.                                                         *
 *                                                                            *
 * TODO: DRILL MAY BE USEFUL. NEED TO THINK ABOUT REALIZATION.                *
 *                                                                            *
 * TODO: TELEPORT IS PRETTY GOOD. NEED TO THINK ABOUT REALIZATION.            *
 *                                                                            *
 *  After all, search for all moves in depth. Select best one node and make   *
 * all it moves.                                                              *
 *                                                                            *
 *  Heuristic is simple:                                                      *
 *      Count EMPTY cells taking into account closes WALLS and PAINTED.       *
 *      If WALL or PAINTED is close: this cell is more expensive.             *
 *                                                                            *
 *      If not EMPTY found near - go to closest EMPTY cell.                   *
 *                                                                            *
 *  This provides that robot tries to go into nooks and crannies in first     *
 *  place. Or at least close to walls and already painted areas.              *
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 */
public class SolverFID implements Solver {
    public SolverFID() {
        closed = new HashSet<Game.GameContainer>(500);
        moves = new ArrayList<Move>();
    }

    private final int DEPTH = 5;
    private Game game;
    private HashSet<Game.GameContainer> closed;
    public List<Move> moves;
    // SAME MOVES PROTECTION
    private ArrayList<int[]> prev = new ArrayList<int[]>();

    public void solve(Game game) {
        closed.clear();
        this.game = game;
        int emp = countEmpty();
        int counter = 0;

        while (emp != 0) {
            int movesPrev = this.moves.size();
            nextMove(game);

            if (game.getWhichMoves() < 0) {
                game.nextRobot();
                counter = 0;
            }
            if (counter > 10) {
                game.nextRobot();
                counter = 0;
            }
            counter++;

            emp = countEmpty();
            System.out.println(emp);
            if (this.moves.size() == movesPrev) {
                System.out.println("SOMETHING WENT WRONG. NO MOVE DONE");
                return;
            }
        }
    }

    public int nextMove(Game game) {
        this.game = game;
        if (prev.size() < game.robotCount()) {
            prev.add(new int[3]);
            prev.get(prev.size() - 1)[0] = 0;
            prev.get(prev.size() - 1)[0] = 1;
            prev.get(prev.size() - 1)[0] = 2;
        }
        // CLONING
        if (game.getMaxClones() > 0 && game.getWhichMoves() == 0) {
            if (game.robotCount() <= game.getMaxClones()) {
                List<Move> moves;
                // Search for X_MARK
                if (game.getCloneBoosterAvailable() > 0) {
                    moves = distanceClosestBooster(Game.Booster.X_POINT, DEPTH);
                    if (moves.size() == 0) {
                        Move m = new Move(Move.Type.CREATE_CLONE, 1, 0);
                        game.makeMove(m);
                        this.moves.add(m);
                        return 1;
                    } else {
                        for (Move m : moves) {
                            game.makeMove(m);
                        }
                        this.moves.addAll(moves);
                        return moves.size();
                    }
                }
                // Search for CLONE
                else {
                    moves = distanceClosestBooster(Game.Booster.CLONE, DEPTH);
                    for (Move m : moves) {
                        game.makeMove(m);
                    }
                    this.moves.addAll(moves);
                    return moves.size();
                }
            }
        }
        // EXTRA MANIPULATOR
        if (game.robotCount() == 1 && game.getExtraManipulatorOnMapCount() > 0) {
            List<Move> moves = distanceClosestBooster(Game.Booster.EXTRA_MANIPULATOR, 40);
            if (moves.size() < 20) {
                for (Move m : moves) {
                    game.makeMove(m);
                }
                this.moves.addAll(moves);
                return moves.size();
            }
        }
        // DEFAULT SEARCH
            // Init
                // Same nodes protection.
        closed.clear();
                // Same moves protection
        prev.get(game.getWhichMoves())[0] = prev.get(game.getWhichMoves())[1];
        prev.get(game.getWhichMoves())[1] = prev.get(game.getWhichMoves())[2];
        prev.get(game.getWhichMoves())[2] = game.getIndex();
        boolean useTable = prev.get(game.getWhichMoves())[0] != prev.get(game.getWhichMoves())[2];
                // Depth initiation.
        ArrayList<Node> nodes = new ArrayList<Node>();
        double bestEvaluation = Double.MAX_VALUE;
        Node bestNode = null;
        nodes.add(new Node(null, null));

                // DEPTH SEARCH
        for (int i = 0; i < (DEPTH + (useTable ? 0 : 2)); i++) {
            int size = nodes.size();
            for (int j = 0; j < size; j++) {
                nodes.get(0).makeMoves(game);
                for (Move move : game.getMoves()) {
                    game.makeMove(move);
                    if (move.unMoveBlocked()) {
                        this.moves.addAll(nodes.get(0).getAllMoves());
                        this.moves.add(move);
                        return this.moves.size();
                    }
                    Game.GameContainer gc = i < 5 ? game.getGC() : null;
                    if (i >= 5 || !closed.contains(gc)) {
                        if (i < 5) {
                            closed.add(gc);
                        }
                        nodes.add(new Node(move, nodes.get(0)));
                        if (bestEvaluation > (useTable ? evaluationWithMoves(i + 1) : countEmpty())) {
                            bestEvaluation = (useTable ? evaluationWithMoves(i + 1) : countEmpty());
                            bestNode = nodes.get(nodes.size() - 1);
                        }
                    }
                    game.unMove(move);
                }
                nodes.get(0).unMoves(game);
                nodes.remove(0);
            }
        }
        if (bestNode == null) {
            closed.clear();
            return nextMove(game);
        } else {
            this.moves.addAll(bestNode.getAllMoves());
            return bestNode.makeMovesEnd(game);
        }
    }

    private double evaluation(int moves) {
        double value = 0;
        value += countEmptyWithWalls() * 5000;
        value += distanceClosestEmpty();
        return value;
    }

    private double evaluationWithMoves(int moves) {
        return evaluation(moves);
    }

    private boolean solved() {
        return countEmpty() == 0;
    }

    private int countEmpty() {
        int count = 0;
        for (int i = 0; i < game.getIntMap().length; i++) {
            if (game.getIntMap()[i] == Game.Cell.valueOf(Game.Cell.EMPTY)) {
                count++;
            }
        }
        return count;
    }

    private int countEmptyWithWalls() {
        int count = 0;
        int countAdd = 0;
        for (int i = 0; i < game.getIntMap().length; i++) {
            if (game.getIntMap()[i] == Game.Cell.valueOf(Game.Cell.EMPTY)) {
                count++;
                int c = 0;
                // Up
                if (game.getIntMap()[i + game.getWidth()] == Game.Cell.valueOf(Game.Cell.WALL)) {
                    c += 2;
                }
                if (game.getIntMap()[i + game.getWidth()] == Game.Cell.valueOf(Game.Cell.PAINTED)) {
                    c++;
                }
                // Down
                if (game.getIntMap()[i - game.getWidth()] == Game.Cell.valueOf(Game.Cell.WALL)) {
                    c += 2;
                }
                if (game.getIntMap()[i - game.getWidth()] == Game.Cell.valueOf(Game.Cell.PAINTED)) {
                    c++;
                }
                // Right
                if (game.getIntMap()[i + 1] == Game.Cell.valueOf(Game.Cell.WALL)) {
                    c += 2;
                }
                if (game.getIntMap()[i + 1] == Game.Cell.valueOf(Game.Cell.PAINTED)) {
                    c++;
                }
                // Left
                if (game.getIntMap()[i - 1] == Game.Cell.valueOf(Game.Cell.WALL)) {
                    c += 2;
                }
                if (game.getIntMap()[i - 1] == Game.Cell.valueOf(Game.Cell.PAINTED)) {
                    c++;
                }

                switch (c) {
                    case 7:
                        countAdd += 60;
                        break;
                    case 6:
                        countAdd += 55;
                        break;
                    case 5:
                        countAdd += 23;
                        break;
                    case 4:
                        countAdd += 20;
                        break;
                    case 3:
                        countAdd += 4;
                        break;
                    case 2:
                        countAdd += 3;
                        break;
                    case 1:
                        countAdd += 1;
                        break;
                }
            }
        }
        return count + countAdd;
    }

    private int distanceClosestEmpty() {
        if (game.isEmptyClose()) {
            return 1;
        }
        int index = game.getIndex();
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        ArrayList<Integer> moves = new ArrayList<Integer>();
        HashSet<Integer> closed = new HashSet<Integer>();
        indexes.add(index);
        moves.add(0);
        while (indexes.size() > 0) {
            int point = indexes.get(0);
            int m = moves.get(0);
            if (closed.contains(point)) {
                indexes.remove(0);
                moves.remove(0);
                continue;
            }
            if (game.getIntMap()[point] == Game.Cell.valueOf(Game.Cell.EMPTY)) {
                return m;
            }
            if (game.getIntMap()[point] != Game.Cell.valueOf(Game.Cell.WALL)) {
                indexes.add(point + 1);
                indexes.add(point - 1);
                indexes.add(point + game.getWidth());
                indexes.add(point - game.getWidth());
                moves.add(m + 1);
                moves.add(m + 1);
                moves.add(m + 1);
                moves.add(m + 1);
            }
            closed.add(point);
            indexes.remove(0);
            moves.remove(0);
        }
        return 0;
    }

    private List<Move> distanceClosestBooster(Game.Booster booster, int depth) {
        int index = game.getIndex();
        ArrayList<NodeWidth> indexes = new ArrayList<NodeWidth>();
        HashSet<Integer> closed = new HashSet<Integer>();
        indexes.add(new NodeWidth(index, 0, null));
        while (indexes.size() > 0) {
            int point = indexes.get(0).getIndex();
            if (closed.contains(point)) {
                indexes.remove(0);
                continue;
            }
            for (int[] b : game.getBoosters()) {
                if (b[0] == Game.Booster.valueOf(booster)) {
                    if (point % game.getWidth() == b[1] && point / game.getWidth() == b[2]) {
                        return indexes.get(0).getMoves(depth);
                    }
                }
            }
            if (game.getIntMap()[point + game.getWidth()] != Game.Cell.valueOf(Game.Cell.WALL)) {
                indexes.add(new NodeWidth(point + game.getWidth(), 1, indexes.get(0)));
            }
            if (game.getIntMap()[point + 1] != Game.Cell.valueOf(Game.Cell.WALL)) {
                indexes.add(new NodeWidth(point + 1, 2, indexes.get(0)));
            }
            if (game.getIntMap()[point - game.getWidth()] != Game.Cell.valueOf(Game.Cell.WALL)) {
                indexes.add(new NodeWidth(point - game.getWidth(), 3, indexes.get(0)));
            }
            if (game.getIntMap()[point - 1] != Game.Cell.valueOf(Game.Cell.WALL)) {
                indexes.add(new NodeWidth(point - 1, 4, indexes.get(0)));
            }
            closed.add(point);
            indexes.remove(0);
        }
        return null;
    }

    private class Node {
        private Node(Move move, Node parent) {
            this.move = move;
            this.parent = parent;
        }

        private Move move;
        private Node parent;

        public Node getParent() {
            return parent;
        }

        public Move getMove() {
            return move;
        }

        public void unMoves(Game game) {
            if (move != null) {
                game.unMove(move);
                parent.unMoves(game);
            }
        }

        public void makeMoves(Game game) {
            if (move != null) {
                List<Move> moves = getAllMoves();
                for (Move move : moves) {
                    game.makeMove(move);
                }
            }
        }

        public int makeMovesEnd(Game game) {
            int movesCount = 0;
            if (move != null) {
                List<Move> moves = getAllMoves();
                for (Move move : moves) {
                    game.makeMove(move);
                    movesCount++;
                    if (solved()) {
                        return movesCount;
                    }
                }
            }
            return movesCount;
        }

        private List<Move> getAllMoves() {
            if (move != null) {
                List<Move> moves = new LinkedList<Move>(parent.getAllMoves());
                moves.add(move);
                return moves;
            } else {
                return new LinkedList<Move>();
            }
        }

        public Move getFirstMove() {
            Node node = this;
            Move move = null;
            while (node.getParent() != null) {
                move = node.getMove();
                node = node.getParent();
            }
            return move;
        }
    }

    private class NodeWidth {
        NodeWidth(int index, int move, NodeWidth parent) {
            this.index = index;
            this.move = move;
            this.parent = parent;
        }

        private NodeWidth parent;
        private int index;
        private int move;

        public NodeWidth getParent() {
            return parent;
        }

        public int getIndex() {
            return index;
        }

        public int getMove() {
            return move;
        }

        public List<Move> getMoves(int limit) {
            List<Move> moves = new LinkedList<Move>();
            if (parent == null) {
                return moves;
            }
            moves.addAll(parent.getMoves(limit));
            if (moves.size() < limit) {
                Move move = new Move(this.move == 1 ? Move.Type.MOVEUP : this.move == 2 ? Move.Type.MOVERIGHT :
                        this.move == 3 ? Move.Type.MOVEDOWN : Move.Type.MOVELEFT, 0, game.getWhichMoves());
                moves.add(move);
            }
            return moves;
        }
    }
}
