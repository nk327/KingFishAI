
/*
 * Given a move, tells us if the move is a losing or a winning move.
 * Uses k-nearest neighbors.
 */

public class LosingMoveClassifier extends KNN {
  public LosingMoveClassifier(int k, double majorityThreshold) {
    super(k, majorityThreshold);
  }

  // assumes that we are only using examples of games that involved KingFish
  public int getClassType(Example x) {
    return x.didWeWin();
  }

  public boolean checkPrediction(Example x, boolean printstuff) {
    // get its real class
    int realClass = getClassType(x);
    // get prediction class
    int predictedClass = h(x);
    if (Const.DEBUG_KNN) {
      if (printstuff) {
        System.err.print("\nDid we win: "+realClass+"\tPrediction: "+predictedClass);
        if (realClass != predictedClass)
          System.err.print("\t WRONG PREDICTION");
      }
    }
    return realClass == predictedClass;
  }

  /*
   * The board state and the move being performed should be enough
   * to determine how similar to examples are.
   */
  public double distance(Example x, Example xprime) {
    String b1 = x.getBoard();
    Move m1 = x.getMove();
    String b2 = xprime.getBoard();
    Move m2 = xprime.getMove();

    /*
    Pari2i dest1 = new Pair2i(m1.r2, m1.c2);
    Pari2i dest2 = new Pair2i(m2.r2, m2.c2);
    */

    double dist = 0.0;
    // only care about target destination, not origin

    int colDist = 2*Math.abs(m2.c2 - m1.c2); // col distance has more weight. more important.
    int rowDist = Math.abs(m2.r2 - m1.r2);
    dist = colDist + rowDist;

//    dist = (double)Util.dist(m1.r2, m1.c2, m2.r2, m2.c2);
//    dist = b1.equals(b2) ? dist : 2*dist; // if board states weren't equal, they are farther. play with this.

    return dist;
  }

  class Pair2i {
    int a, b;
    public Pair2i(int a, int b) {
      this.a = a;
      this.b = b;
    }

    @Override
    public boolean equals(Object o) {
      Pair2i p = (Pair2i)o;
      return a == p.a && b == p.b;
    }
  }
}
