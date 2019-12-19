package Logic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Automatic solver. Uses ready algorithm. Solution saver.
 * If solution for map already exist then skips the map.
 */

public class Main {
    public static void main(String[] args) throws IOException {
        for (int i = 0; i < 300; i++) {
            String name = "src\\main\\resources\\prob-";
            if (i < 9) {
                name += "00";
            } else {
                if (i < 99) {
                    name += "0";
                }
            }
            File file = new File(name + Integer.toString(i + 1) + ".sol");
            if (!file.exists()) {
                System.out.println(name + Integer.toString(i + 1) + ".desc");
                Game game = Game.loadFromFileString(name + Integer.toString(i + 1) + ".desc");
                SolverFID solver = new SolverFID();
                solver.solve(game);
                file.createNewFile();
                ArrayList<String> strings = new ArrayList<String>();
                for (int j = 0; j < game.robotCount(); j++) {
                    StringBuilder sb = new StringBuilder();
                    for (int k = 0; k < solver.moves.size(); k++) {
                        if (solver.moves.get(k).getWhichRobot() == j) {
                            sb.append(solver.moves.get(k).toString());
                        }
                    }
                    strings.add(sb.toString());
                }
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                for (int j = 0; j < strings.size(); j++) {
                    bw.write(strings.get(j));
                    if (j != strings.size() - 1) {
                        bw.write("#");
                    }
                }
                bw.close();
            } else {
                System.out.println("EXIST " + Integer.toString(i + 1));
            }
        }
    }
}
