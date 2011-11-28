import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class DataLoader {
  // not all games will be useful, so we only want to learn from some players
  public String[] goodPlayers = {
    "droptable",
    "progamers",
    "eigenbot",
    "deepred",
    "artificialwintelligence",
    "ccplus",
    "googolhex",
    "robotdowneyjr"
  };


  private ArrayList<Example> allExamples = new ArrayList<Example>();
  private HashSet<String> learningPlayerSet = new HashSet<String>(); // the players from which we want to learn

  public DataLoader() {
    for (String s : goodPlayers) {
      learningPlayerSet.add(s.toLowerCase());
    }
  }

  public DataLoader(String[] players) {
    for (String s : players) {
      learningPlayerSet.add(s.toLowerCase());
    }
  }

  public void loadData() {
    int numFilesUsed = 0;
    long startTime = System.nanoTime();
    int numfiles = new File("parsedgames").listFiles().length;
    try {
      for (int k = 0; k < numfiles; k++) {
        File file = new File("parsedgames/game_"+k+".txt");
        Scanner sc = new Scanner(file);
        String line = sc.nextLine();
        long exampleTime = Long.parseLong(line);
        line = sc.nextLine();
        String p1 = line.toLowerCase();
        line = sc.nextLine();
        String p2 = line.toLowerCase();

        if (learningPlayerSet.contains(p1) || learningPlayerSet.contains(p2)) {
          numFilesUsed++;
          int moveNum = 0;
          int t = 1;
          while (sc.hasNextLine()) {
            line = sc.nextLine();
            String[] parts = line.split(" ");
            int turn = Integer.parseInt(parts[0]);
            int t1 = Integer.parseInt(parts[2]);
            int t2 = Integer.parseInt(parts[3]);
            int r1  = Integer.parseInt(parts[4]);
            int c1  = Integer.parseInt(parts[5]);
            int r2  = Integer.parseInt(parts[6]);
            int c2  = Integer.parseInt(parts[7]);
            int r3  = Integer.parseInt(parts[8]);
            int c3  = Integer.parseInt(parts[9]);

            Move m = new Move(0, t1, t2, r1, c1, r2, c2, r3, c3);
            String board = parts[10];
            int winner = Integer.parseInt(parts[11]);
            Example x = new Example(exampleTime, turn, winner, m, board, p1, p2);

            if (t == 1) moveNum++;
            x.setMoveNum(moveNum);
            allExamples.add(x);
            t = 3-t;
          }
        }
      }
    } catch(FileNotFoundException e) {
      System.err.println(e.toString());
    }
    long endTime = System.nanoTime();
    long totalTime = endTime - startTime;
    System.err.println("Loading examples took: " + (totalTime*Math.pow(10, -9))
        + " seconds.\nUsed "+numFilesUsed+" out of "+numfiles+" files.");
  }

  public ArrayList<Example> getAllExamples() {
    return allExamples;
  }

  // get all the examples containing moves done by our fishy AI
  public ArrayList<Example> getKingFishExamples() {
    ArrayList<Example> fishlist = new ArrayList<Example>();
    for (Example x : allExamples) {
      if (x.isKingFishMove())
        fishlist.add(x);
    }
    return fishlist;
  }

  // typical examples are the ones that involve moves down the main
  // area of the board. They are useless to learning because they happen
  // so often that they will evenly be spread between losing and winning games
  public ArrayList<Example> removeTypicalExamples(ArrayList<Example> examples) {
    System.err.println("Old size: " + examples.size());
    ArrayList<Example> newlist = new ArrayList<Example>();
    for (Example x : examples) {
      Move m = x.getMove();
      if (!Util.isTypicalMove(m)) {
        newlist.add(x);
      }
    }
    System.err.println("New size: " + newlist.size());
    return newlist;
  }

  public void printExamples(ArrayList<Example> fishlist) {
    System.err.println("All " + fishlist.size() + " examples to learn from." +
        " Viva the fish.");
    for (Example x : fishlist) {
      System.err.println("Move number: "+x.getMoveNum()+"\tDid we win? " +
          x.didWeWin() + "\t"+x.getMoveString());
    }
  }

  public void printAllExamples() {
    System.err.println("All " + allExamples.size() + " examples to learn from:");
    for (Example x : allExamples) {
      System.err.println("Move number: "+x.getMoveNum()+"\tDid we win? " +
          x.didWeWin() + "\t"+x.getMoveString());
    }
  }

}
