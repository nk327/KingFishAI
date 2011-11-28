
public class Const {
	public final static int BOARD_HEIGHT = 17;
	public final static int BOARD_WIDTH = 25;
	public final static int NUM_VALID_CELLS = 121;
	public final static String BOARD_FILE = "initboard.txt";

	// Game progress constants
	public static final int UNKNOWN=0;
	public static final int OPENING = 1;
	public static final int INTERACTING = 2;
	public static final int CLOSING = 3;

	// testing constants
	public static final boolean AB_USE_MOVE_ORDERING = true;
	public static final boolean AB_USE_HASHING = true;
  public static final boolean USE_ASTAR_CLOSING = false;
	public static final boolean USE_ASTAR_CLOSING_HEURISTIC = false;
  public static boolean LEARN_LIKE_A_BOSS = true;
  public static boolean AB_TRY_NEW_WEIGHTS = true;
  public static final boolean USE_AB_TESTING = false; // test between old and new weights
  public static final boolean DEBUG_KNN = false;
}
