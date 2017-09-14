/*
 * MIT License
 *
 * Copyright (c) 2017 Carnegie Mellon University.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.cmu.sv.isstac.cogito.ml;

import java.util.HashMap;
import java.util.Map;

import edu.cmu.sv.isstac.cogito.structure.Conditional;
import smile.classification.LogisticRegression;
import smile.classification.SoftClassifier;

/**
 * @author Kasper Luckow
 */
public class LogisticRegressionClassifier implements CogitoClassifier {

  // TODO: There is a lot to refactor here: We should hoist the generation of deterministic
  // classifiers. Any instance of CogitoClassifier should only be responsible for training
  // classifiers for predicting "non-deterministic" choices.
  private Map<Conditional, SoftClassifier<double[]>> classifiers = new HashMap<>();

  // TODO: Prediction statistics. We should probably hoist this into a decorator
  private Map<Conditional, PredictionStatistics> predictionStatistics = new HashMap<>();

  @Override
  public void train(Map<Conditional, DataSet> trainingSet) {
    for(Map.Entry<Conditional, DataSet> entry : trainingSet.entrySet()) {

      int uniqueClasses = entry.getValue().getClasses().size();
      assert uniqueClasses > 0;

      if(uniqueClasses < 2) {

        //In this case the predictor can simply return the single class found
        final int singleClass = entry.getValue().getClasses().iterator().next();
        classifiers.put(entry.getKey(), DeterministicClassifier.create(singleClass));
      } else {

        //If there are more than 1 class, we will train a logistic regression model
        LogisticRegression lr = new LogisticRegression(
            entry.getValue().getXs(),
            entry.getValue().getYs());

        classifiers.put(entry.getKey(), lr);
      }
    }
  }

  @Override
  public int predict(Conditional conditional, double[] data, double[] posterior) {
    SoftClassifier<double[]> classifier = this.classifiers.get(conditional);
    int choice = classifier.predict(data, posterior);
    // So ugly passing return values through the parameters here...
    PredictionStatistics statistics = predictionStatistics.get(conditional);
    if(statistics == null) {
      statistics = new PredictionStatistics(conditional);
      predictionStatistics.put(conditional, statistics);
    }
    statistics.addPredictionData(choice, posterior[choice]);

    return choice;
  }

  @Override
  public boolean hasClassifierFor(Conditional conditional) {
    return this.classifiers.containsKey(conditional);
  }

  public Map<Conditional, PredictionStatistics> getStatistics() {
    return this.predictionStatistics;
  }
}
