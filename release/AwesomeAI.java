import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class AwesomeAI extends Player {
  public static boolean oldversion = true;

	public static int specialMarblesToAdd = 0;
	public static ArrayList<Cell> defaultSpecialMarbles = new ArrayList<Cell>();
	public static int[][] initialBoard = new int[Const.BOARD_HEIGHT][Const.BOARD_WIDTH];
	State currentState;
	boolean opponentMadeAMove;
	int gameProgress = Const.UNKNOWN;
	ArrayList<Move> aStarWinningMoveList = new ArrayList<Move>();
  AStarBlackBox aStarBlackBox;
	AB_BlackBox ABSearch;
	static float horz = 1;
	static float vert = 1;
	static float straggler = 1;
	static float chain = 1;
	static float interact = 9;

  public static LosingMoveClassifier moveClassifier;

	public AwesomeAI(Scanner scanner) {
		super(scanner);
		ABSearch = new AB_BlackBox(getMyturn(), horz, vert, straggler, chain);
		aStarBlackBox = new AStarBlackBox(getMyturn());
		aStarBlackBox.setMoveCutoff(5);

    if (Const.LEARN_LIKE_A_BOSS) {
      // LEARN LIKE AN EFFING BOSS.
      // opponents who we don't always beat but we don't always lose to either
      String[] unpredictableOpponents = {
        "droptable",
        "artificialwintelligence"
      };

      DataLoader dataLoader = new DataLoader(unpredictableOpponents);
      dataLoader.loadData();
      ArrayList<Example> allFishyExamples = dataLoader.getKingFishExamples();
      allFishyExamples = dataLoader.removeTypicalExamples(allFishyExamples);
      if (Const.DEBUG_KNN) {
        dataLoader.printExamples(allFishyExamples);
      }
      moveClassifier = new LosingMoveClassifier(5, 0.5);

      LosingMoveClassifierEvaluator evaluator = new LosingMoveClassifierEvaluator(5);
      double successRate = evaluator.evaluate(moveClassifier, allFishyExamples);
      System.err.println("Success rate: " + successRate);

      moveClassifier.setTrainingExamples(allFishyExamples);
      if (Const.DEBUG_KNN) {
        System.exit(0);
      }
    }
	}

	@Override
	public String think() {
		if (opponentMadeAMove) { // if we are the first move then this will be false at first
			Move opponentMove = getOpponentsMove(currentState.reconstructBoardArray(), Util.getArrayFromBoard(getBoard()));
			currentState = new State(opponentMove, currentState);
		}

		gameProgress = Util.progress(getBoard(), gameProgress, interact);
		Util.printGameProgress(gameProgress);

		Move m = null;
		//below is code to edit to make your own behavior\\
		if (gameProgress == Const.OPENING || (gameProgress == Const.CLOSING && Const.USE_ASTAR_CLOSING) || (gameProgress == Const.INTERACTING && Const.USE_ASTAR_CLOSING)) {
      if (gameProgress == Const.CLOSING) {
				aStarBlackBox.useClosingHeuristic(true);
        aStarBlackBox.setMoveCutoff(5);
      } else {
				aStarBlackBox.useClosingHeuristic(false);
			}
			m = getAStarMove();
		} else {
			aStarWinningMoveList.clear();
			System.err.println("Using AlphaBastard...");
			m = getAlphaBetaMove();
		}

  	//standard end of think() method----always use the code below
		// perform the move before sending it
		board.move(m);
		currentState = new State(m, currentState);
		
		if(m == null){
			System.out.println("Holy shit!");
			System.exit(0);
		}
		Util.checkStateConsistency(currentState, getBoard());
		System.err.println("My move: " + m.r1+" "+m.c1+" "+m.r2+" "+m.c2 + " "+ m.r3+" "+ m.c3);
		return m.r1+" "+m.c1+" "+m.r2+" "+m.c2 + " "+ m.r3+" "+ m.c3;
	}

	//Nikita's alpha/beta
	public Move getAlphaBetaMove() {
		Move m = null;

		AB_BlackBox abbox=new AB_BlackBox(getMyturn(), horz, vert, straggler, chain);
		AB_BlackBox.Message output=ABSearch.gimmeAMove(getBoard(), 6);

		if(output==AB_BlackBox.Message.NEED_TO_RECOMPUTE){
			System.err.println("oh no, ab search not finding a move. sending null move");
		}else{
			m=output.getMove();
		}
		return m;
	}
		
	// Pablo's A*
	public Move getAStarMove() {
		Move m = null;
		if (!aStarWinningMoveList.isEmpty()) {
			m = aStarWinningMoveList.remove(0);
		}

		if (m == null) {
			System.err.println("Empty move list happened. Calculating new sequence.");
			aStarWinningMoveList = aStarBlackBox.aStarSearch(currentState);
			m = aStarWinningMoveList.remove(0);
		}
		if (!board.validateSimpleMove(m.r1,m.c1,m.r2,m.c2,m.r3,m.c3,getMyturn())) {
			System.err.println("Invalid move happened. There were still " +
					(aStarWinningMoveList.size()) + " moves left. Calculating new sequence.");
			aStarWinningMoveList = aStarBlackBox.aStarSearch(currentState);
			m = aStarWinningMoveList.remove(0);
		}
		//System.err.println("Is move valid? " + board.validateSimpleMove(m.r1,m.c1,m.r2,m.c2,m.r3,m.c3,getMyturn()));
		return m;
	}

	/*
	 * Get the opponent's move by comparing the old board with the new board to see what happened.
	 */
	public Move getOpponentsMove(int[][] board1, int[][] board2) {
		int r1 = -1;
		int c1 = -1;
		int r2 = -1;
		int c2 = -1;
		int r3 = -1;
		int c3 = -1;

		int oppTurn = getOpponentTurn();

		for (int i = 0; i < 17; i++) {
			for (int j = 0; j < 25; j++) {
				if (board1[i][j] == oppTurn && board2[i][j] == 0) {
					r1 = i;
					c1 = j;
				}
				if (board1[i][j] == 0  && board2[i][j] == oppTurn) {
					r2 = i;
					c2 = j;
				}
				if (board1[i][j] == 0 && board2[i][j] == 3) {
					r3 = i;
					c3 = j;
				}
			}
		}

//		System.err.println("Opponent's move: "+r1+" "+c1+" "+r2+" "+c2+" "+r3+" "+c3);

		return new Move(0, 0, 0, r1, c1, r2, c2, r3, c3);
	}

	public int getOpponentTurn() {
		return Util.flipTurn(getMyturn());
  }

  public static void main(String args[]){
    if (args.length ==5){
      System.err.println("THINGS ARE WORKING");
      horz = Float.parseFloat(args[0]);
      vert = Float.parseFloat(args[1]);
      straggler = Float.parseFloat(args[2]);
      chain = Float.parseFloat(args[3]);
      interact = Float.parseFloat(args[4]);	
    }

    if (Const.USE_AB_TESTING) {
      int version = 0;
      try {
        File file = new File("abtest.txt");
        Scanner sc = new Scanner(file);
        version = Integer.parseInt(sc.nextLine());
        AwesomeAI.oldversion = version == 1 ? true : false;
        sc.close();
      } catch (FileNotFoundException e) {
        System.err.println(e.toString());
      }
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter("abtest.txt"));
        String result = version == 1 ? "0" : "1";
        out.write(result);
        out.close();
      } catch (IOException e) {
        System.err.println(e.toString());
      }
    }
    Const.AB_TRY_NEW_WEIGHTS = AwesomeAI.oldversion ? false : true;
    System.err.println("AB_TRY_NEW_WEIGHTS: " + Const.AB_TRY_NEW_WEIGHTS);

    if (AwesomeAI.oldversion) {
      System.err.println("Using old weights.");
    } else {
      System.err.println("Using new weights.");
    }

		int turn = 1;
		AwesomeAI p = new AwesomeAI(new Scanner(System.in));

		p.currentState = new State(null, null); // first state: no move, no parent
		System.err.println("Init state");
		Util.printBoard(p.getBoard());
		for (int i = 0; i < Const.BOARD_HEIGHT; i++) {
			for (int j = 0; j < Const.BOARD_WIDTH; j++) {
				AwesomeAI.initialBoard[i][j] = p.getBoard().at(i, j);
				if (p.getBoard().at(i, j) == 3) {
					AwesomeAI.defaultSpecialMarbles.add(new Cell(i, j));
				}
			}
		}
		AwesomeAI.specialMarblesToAdd = p.getBoard().getSpecials(1) + p.getBoard().getSpecials(2);

		while (true) {
			System.err.println("turn = "+turn+"   myturn = "+p.getMyturn());	
			if (turn == p.getMyturn()){
				System.err.println("It is my turn and I am thinking");	
				System.out.println(p.think());
				int status = p.getStatus();
				if(status<0){
					System.err.println("I lost. Status: " + status);
					break;
				}
				else if(status>0){
					Util.printBoard(p.getBoard());
					System.err.println("I won");
					break;
				}
			} else {
				int res = p.getOpponentMove();

				if (res == -1)
					System.err.println("The server is messed up");
				else if (res == 1) {
					System.err.println("The other player won.");
					break;
				} else { 
					System.err.println("OK lemme think...");
					p.opponentMadeAMove = true;
				}

			}
			System.err.println(p.getBoard().toString('*'));
			turn = 3-turn;
		}
	}
}
