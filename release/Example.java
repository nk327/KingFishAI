
public class Example {
  // exampleTime: the time at which the parsed file was last modified (serves as a good way of finding out the most recent games)
  private long exampleTime; 
  private int turn; // whose move it was (player 1 or 2?)
  private int winner; // who ended up winning the game
  private int didwewin; // 1 means yes, 0 means no
  private int moveNum; // in the game, which move number was it?
  private Move m;
  private String board; // board state BEFORE the move was applied
  private String player1, player2; // the players in this game (their actual names)

  public Example(Move m) {
    this.m = m;
  }

  public Example(long exampleTime, int turn, int winner, Move m, String board, String p1, String p2) {
    this.exampleTime = exampleTime;
    this.turn = turn;
    this.winner = winner;
    this.m = m;
    this.board = board;
    this.player1 = p1;
    this.player2 = p2;

    if (!player1.toLowerCase().equals("kingfish") && !player2.toLowerCase().equals("kingfish")) {
      System.err.println("Tried to load an example where neither player was KingFish. Dumbass.");
      Const.LEARN_LIKE_A_BOSS = false;
    }
    int ourPlayer = player1.toLowerCase().equals("kingfish") ? 1 : 2;
    this.didwewin = ourPlayer == winner ? 1 : 0;
  }

  public int didWeWin() { return didwewin; }
  public long getTime() { return exampleTime; }
  public int getTurn() { return turn; }
  public int getWinner() { return winner; }
  public Move getMove() { return m; }
  public int getMoveNum() { return moveNum; }
  public String getBoard() { return board; }
  public String getPlayer1() { return player1; }
  public String getPlayer2() { return player2; }

  public void setMoveNum(int n) {
    this.moveNum = n;
  }

  public boolean isKingFishMove() {
    int ourPlayer = player1.toLowerCase().equals("kingfish") ? 1 : 2;
    return ourPlayer == turn ? true : false;
  }

  public String getMoveString() {
    return Util.moveToString(m);
  }
}
