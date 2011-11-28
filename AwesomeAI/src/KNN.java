import java.util.ArrayList;

public abstract class KNN {
  private int k;
  private double threshold; // this is the proportion of votes needed to classify as 1
  ArrayList<Example> trainingExamples = new ArrayList<Example>();

  public KNN(int knum, double majorityThreshold) {
    if (k%2 != 0) {
      System.err.println("Dude, specify an odd k for KNN.");
      System.exit(0);
    }
    k = knum;
    threshold = majorityThreshold;
  }

  public void setTrainingExamples(ArrayList<Example> exampleList) {
    trainingExamples = exampleList;
  }

  /*
   * Get the class of an example.
   * Must be a boolean class: either 1 or 0.
   */
  abstract int getClassType(Example x);

  /*
   * Given an example xprime, checks if we predict the correc type.
   * The boolean is used to control the printing of debugging info.
   */
  abstract boolean checkPrediction(Example x, boolean printstuff);

  /*
   * Returns the distance between two examples.
   */
  abstract double distance(Example x, Example xprime);

  /*
   * Returns the predicted class of xprime.
   * Using boolean classes: 1 or 0
   * O(k)
   */
  public int h(Example xprime) {
    Example[] nn = knn(xprime); // get the k nearest neighbors to xprime
    int countOnes = 0;
    int countZeros = 0;

    for (Example x : nn) {
      int y = getClassType(x);
      if (y == 1) {
        countOnes++;
      } else if (y == 0) {
        countZeros++;
      } else {
        System.err.println("Fffffuuuu! Invalid class type: "+y);
        System.exit(0);
      }
    }
    double proportionOfOnes = ((double)countOnes)/((double)(countOnes+countZeros));

    if (Const.DEBUG_KNN) {
      int result = proportionOfOnes >= threshold ? 1 : 0;
      if (result != getClassType(xprime)) {
        System.err.println("\nNearest neighbors of: " + xprime.getMoveString()
            + " " +xprime.getPlayer1()+" "+xprime.getPlayer2());
        for (Example x : nn) {
          printExample(x);
          System.err.print("\tDid we win? " + getClassType(x) + " " +
              x.getMoveString() + "\tBoard equal? " +
              x.getBoard().equals(xprime.getBoard())+" "+
              x.getPlayer1() + " " + x.getPlayer2()+"\n");
        }
      }
    }

    return proportionOfOnes >= threshold ? 1 : 0;
  }

  private void printExample(Example x) {

  }

  /*
   * Gets the k nearest neighbors of xprime, based on the distance function.
   * O(kn) where n is the number of training examples
   */
  private Example[] knn(Example xprime) {
    Example[] nn = new Example[k]; // nearest neighbors array
    // initialize nn with the first k training examples
    for (int i = 0; i < k; i++) {
      nn[i] = trainingExamples.get(i);
    }

    // keep track of the neighbor farthest from xprime
    int currFarthestIndex = getFarthestIndex(nn, xprime); 

    // iterate through all training examples, throwing away the farthest neighbor
    // each time whenever we find a closer one
    for (Example x : trainingExamples) {
      double newDistance = distance(x, xprime);
      if (newDistance < distance(nn[currFarthestIndex], xprime)) {
        nn[currFarthestIndex] = x;
        currFarthestIndex = getFarthestIndex(nn, xprime);
      }
    }

    return nn;
  }

  /*
   * given an array of k neighbors and a test example (xprime)
   * return the index of the farthest neighbor
   * O(k)
   */
  private int getFarthestIndex(Example[] neighbors, Example xprime) {
    int i = 0;
    int farthestIndex = 0;
    double farthestDistance = distance(neighbors[0], xprime);
    for (Example x : neighbors) {
      double newDistance = distance(x, xprime);
      if (newDistance > farthestDistance) {
        farthestDistance = newDistance;
        farthestIndex = i;
      }
      i++;
    }
    return farthestIndex;
  }
}
