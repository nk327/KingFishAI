import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * This class is given a game.log file and it then parses it to produce a file
 * with all of the moves, whose turn it was, the board state at each move
 * (i.e. the board before the move is applied), and which team won.
 * Each line will follow this order:
 *            <turn> <move> <board> <winner>
 * Inputs:
 *  - The filename of the game log file.
 *      Must be in the format: <player1> <player2> <anything>.txt
 *  Outputs:
 *  - game_k.txt where k is the index of the game.
 *  (it will be stored in the parsedgames directory)
 *  First line is the date file was last modified (provided file was never modified,
 *  it equals the date file was created. Lets us know which are the most recent games.)
 *  Second and third lines are player1 and player2 respectively.
 *  Everything that follows are the parsed moves and board states.
 */
public class GameLogParser {
	Scanner sc;
  String outputname, player1, player2;
  int winner;
  long dateLastModified;

  int[][] board = new int[17][25];
	private static final String initialBoard[] = new String[] {
		"            2            ",
		"           2 2           ",
		"          2 2 2          ",
		"         2 2 2 2         ",
		"0 0 0 0 0 0 0 0 0 0 0 0 0",
		" 0 0 0 0 0 0 0 0 0 0 0 0 ",
		"  0 0 0 0 0 0 0 0 0 0 0  ",
		"   0 0 0 0 0 0 0 0 0 0   ",
		"    0 0 0 0 0 0 0 0 0    ",
		"   0 0 0 0 0 0 0 0 0 0   ",
		"  0 0 0 0 0 0 0 0 0 0 0  ",
		" 0 0 0 0 0 0 0 0 0 0 0 0 ",
		"0 0 0 0 0 0 0 0 0 0 0 0 0",
		"         1 1 1 1         ",
		"          1 1 1          ",
		"           1 1           ",
		"            1            "
	};

	public GameLogParser(String gamelogname) {
    int numfiles = new File("parsedgames").listFiles().length;
    outputname = "parsedgames/game_"+numfiles+".txt";

    // set up initial board
    for (int i = 0; i < 17; i++) {
      String line = initialBoard[i];
      for (int j = 0; j < 25; j++) {
        if (line.charAt(j) == ' ') {
          board[i][j] = -1;
        } else {
          board[i][j] = line.charAt(j)-'0';
        }
      }
    }

		try {
      File file = new File(gamelogname);
      dateLastModified = file.lastModified();
      String[] fileparts = file.getName().split(" |\\.");
      this.player1 = fileparts[0];
      this.player2 = fileparts[1];
			sc = new Scanner(new File(gamelogname));
		} catch(FileNotFoundException e) {
			System.out.println(e.toString());
		}
	}

	public void parse() {
		ArrayList<Move> moveList = new ArrayList<Move>();
		String line;
		boolean ignoreInitialLines = true;
    winner = 2;
		while (sc.hasNextLine()) {
			line = sc.nextLine();
			if (ignoreInitialLines && line.length() != 0 && line.charAt(0) == '#') {
				ignoreInitialLines = false;
			}
			if (line.length() == 0 || line.charAt(0) == '#' || ignoreInitialLines) {
				continue;
			}

      winner = 3-winner;
      String[] moveparts = line.split(" ");
      int t1 = Integer.parseInt(moveparts[1]);
      int t2 = Integer.parseInt(moveparts[2]);
      int r1 = Integer.parseInt(moveparts[3]);
      int c1 = Integer.parseInt(moveparts[4]);
      int r2 = Integer.parseInt(moveparts[5]);
      int c2 = Integer.parseInt(moveparts[6]);
      int r3 = Integer.parseInt(moveparts[7]);
      int c3 = Integer.parseInt(moveparts[8]);
      Move m = new Move(0, t1, t2, r1, c1, r2, c2, r3, c3);
			moveList.add(m);
		}
		sc.close();

		try {
      BufferedWriter out = new BufferedWriter(new FileWriter(outputname));
      out.write(moveListToParsedString(moveList));
      out.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}

    System.out.println("Wrote the parsed game to " + outputname);
	}

	private String moveListToParsedString(ArrayList<Move> moveList) {
    int turn = 1;
    String str = dateLastModified + "\n" + player1 + "\n" + player2 + "\n";
    for (Move m : moveList) {
      str += turn+" "+moveToString(m)+" "+boardToString(board)+" "+winner+"\n";
      turn = 3-turn;
      applyMove(m);
    }
    return str;
	}

  private String boardToString(int[][] b) {
    StringBuilder sb = new StringBuilder("");
    for (int i = 0; i < 17; i++) {
      for (int j = 0; j < 25; j++) {
        if (b[i][j] != -1) {
          sb.append(b[i][j]);
        }
      }
    }
    return sb.toString();
  }

  private String moveToString(Move m) {
    return "0 "+m.t1+" "+m.t2+" "+m.r1+" "+m.c1+" "+m.r2+" "+m.c2+" "+m.r3+" "+m.c3;
  }

	private void applyMove(Move m) {
		if (m.r3!=-1 && m.c3 !=-1){
			board[m.r3][m.c3] = 3;
		}
		board[m.r2][m.c2] = board[m.r1][m.c1];
		board[m.r1][m.c1] = 0;		
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("You must specify a game log filename in the required format: <player1>_<player2>_<anything>.txt");
			System.exit(0);
		} else if (args.length > 1) {
			System.out.println("Too many arguments. You must specify only a game log filename in the required format: <player1>_<player2>_<anything>.txt");
			System.exit(0);
		}

		GameLogParser gameParser = new GameLogParser(args[0]);
		gameParser.parse();
	}
}
