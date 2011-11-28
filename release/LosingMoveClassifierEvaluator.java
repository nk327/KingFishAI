import java.util.ArrayList;

/*
 * Base class to evaluate a learning algorithm using k-fold
 * cross-validation.
 */
public class LosingMoveClassifierEvaluator {
  int kfold;

  public LosingMoveClassifierEvaluator(int k) {
    this.kfold = k;
  }

  public double evaluate(LosingMoveClassifier learner,
      ArrayList<Example> allExamples) {

    ArrayList<Example> trainingExamples = new ArrayList<Example>();
    ArrayList<Example> testExamples = new ArrayList<Example>();
    double successes = 0.0;
    double failures = 0.0;

    System.err.println("Total of " + allExamples.size() + " examples.");
    for (int k = 0; k < kfold; k++) {
      Util.partitionExamples(k, kfold, allExamples, trainingExamples, testExamples);

      if (Const.DEBUG_KNN) {
        System.err.println("New fold. " + trainingExamples.size() +
            " training examples, and " + testExamples.size() + " test examples.");
      }

      learner.setTrainingExamples(trainingExamples);
      for (Example testx : testExamples) {
        if (learner.checkPrediction(testx, true))
          successes += 1;
        else
          failures += 1;
      }
    }

    double totalPredictions = successes + failures;
    System.err.println("\nSuccesses: "+successes+"\tFailures: "+failures);
    return successes / totalPredictions;
  }

}
