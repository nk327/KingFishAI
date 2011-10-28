import java.util.HashSet;

public class Util {

	/*
	 * Helpful board array functions
	 * A lot of these functions have similar functionality to some of
	 * the Board class functions, but they can now be applied straight
	 * on a board array, instead of having to create a whole new Board class
	 * instance, which is slower.
	 */
	public static int[][] copyBoardArray(int[][] board) {
		int[][] newBoard = new int[Const.BOARD_HEIGHT][Const.BOARD_WIDTH];
		for (int i = 0; i < Const.BOARD_HEIGHT; i++) {
			for (int j = 0; j < Const.BOARD_WIDTH; j++) {
				newBoard[i][j] = board[i][j];
			}
		}
		return newBoard;
	}

	public static void performMoveOnBoard(Move m, int[][] board) {
		if (m.r3!=-1 && m.c3 !=-1){
			board[m.r3][m.c3] = 3;
		}
		board[m.r2][m.c2] = board[m.r1][m.c1];
		board[m.r1][m.c1] = 0;		
	}

	public static int[][] getArrayFromBoard(Board board) {
		int[][] newBoard = new int[Const.BOARD_HEIGHT][Const.BOARD_WIDTH];
		for (int i = 0; i < Const.BOARD_HEIGHT; i++) {
			for (int j = 0; j < Const.BOARD_WIDTH; j++) {
				newBoard[i][j] = board.at(i, j);
			}
		}
		return newBoard;
	}

	public static int euclideanDistSq(int r1, int c1, int r2,int c2){
		return (r2-r1)*(r2-r1)+(c2-c1)*(c2-c1);
	}

	public static int dist(int r1, int c1, int r2,int c2){
		int dr = r2 - r1;
		int ds = (r1+c1-r2-c2)/2;
		if (dr*ds>0){
			return Math.abs(dr)+ Math.abs(ds);
		}
		else{
			return Math.max(Math.abs(dr), Math.abs(ds));
		}
	}
	public static void getBoardLegalMoves(int r, int c, HashSet<Integer> moves, int[][] board){
		moves.clear();
		/* Immediate moves */
		int[][] immediate = new int[][] {{r,c-2},{r-1,c-1},{r-1,c+1},{r,c+2},{r+1,c+1},{r+1,c-1}};		
		/* First add the immediate moves */
		for(int i=0; i<immediate.length; i++){
			int r2 = immediate[i][0];
			int c2 = immediate[i][1];
			if(0<=r2 && r2<17 && 0<=c2 && c2<25 && board[r2][c2]==0)
				moves.add(25*r2+c2);
		}
		/* Now add all jumps recursively */
		jump(r,c,moves, board);
	}

	private static void jump(int r, int c, HashSet<Integer> moves, int[][] board){
		/* Jump moves */
		int[][] jumps = new int[][] {{r,c-2,r,c-4},{r-1,c-1,r-2,c-2},{r-1,c+1,r-2,c+2},{r,c+2,r,c+4},{r+1,c+1,r+2,c+2},{r+1,c-1,r+2,c-2}};
		for(int i=0; i<jumps.length; i++){
			int r1=jumps[i][0];
			int c1=jumps[i][1];
			int r2=jumps[i][2];
			int c2=jumps[i][3];
			if(0<=r2 && r2<17 && 0<=c2 && c2<25 && board[r1][c1]>0 && board[r2][c2]==0 && !moves.contains(25*r2+c2)){
				moves.add(25*r2+c2);
				jump(r2,c2,moves, board);
			}
		}
	}

	/*
	 * Other helpful functions
	 */
	public static int flipTurn(int turn) {
		if (turn == 1)
			return 2;
		else
			return 1;
	}
	/*
	 * Helpful print functions for great justice.
	 */
	public static void printState(State state) {
		printBoard(state.reconstructBoard());
	}

	public static void printBoard(Board board) {
		System.err.println(board.toString('0'));
	}

	public static void printStateConsistency(State state, Board board) {
		System.err.println("\n************************************");
		checkStateConsistency(state, board);
		System.err.println("Current board:");
		printBoard(board);
		System.err.println("Current state representation:");
		printBoard(state.reconstructBoard());
		System.err.println("************************************\n");
	}

	// Checks if the given state is a correct representation of the given board
	public static void checkStateConsistency(State state, Board board) {
		System.err.println("Is state consistent? " + state.equalsBoard(board));
	}

	public static void printMove(Move m) {
		System.err.println("Move: "+m.r1+" "+m.c1+" "+m.r2+" "+m.c2+ " "+m.r3+" "+m.c3);
	}
}
