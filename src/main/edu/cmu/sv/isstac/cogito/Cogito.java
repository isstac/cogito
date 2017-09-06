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

package edu.cmu.sv.isstac.cogito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import edu.cmu.sv.isstac.cogito.ml.CogitoClassifier;
import edu.cmu.sv.isstac.cogito.ml.DataSet;
import edu.cmu.sv.isstac.cogito.ml.DataGenerator;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFShell;
import smile.classification.LogisticRegression;

/**
 * @author Kasper Luckow
 */
public class Cogito implements JPFShell {

  private final Config config;

  public Cogito(Config config) {
    this.config = config;
  }

  @Override
  public void start(String[] args) {

    Collection<Path> maxPaths = new ArrayList<>();
    int[] inputSizes = config.getIntArray("cogito.training.target.args");

    for(int inputSize = inputSizes[0]; inputSize <= inputSizes[1]; inputSize++) {
      WorstCasePathListener worstCasePathListener = new WorstCasePathListener(config);

      config.setProperty("target.args", inputSize + "");
      JPF jpf = new JPF(config);
      jpf.addListener(worstCasePathListener);
      jpf.run();

      maxPaths.addAll(worstCasePathListener.getMaxPaths());
    }


    DataGenerator dataGenerator = new DataGenerator();
    Map<Conditional, DataSet> dataSets = dataGenerator.generateTrainingData(maxPaths);

    CogitoClassifier classifier = new CogitoClassifier();
    classifier.train(dataSets);

    //TODO: put this into the option object
    int maxInputSize = config.getInt("cogito.predict.target.args");
    long[] costs = new long[maxInputSize];
    for(int i = 1; i < maxInputSize; i++) {
      config.setProperty("target.args", i + "");
      WorstCasePathListener guidedWcListener = new WorstCasePathListener(config);
      GuidanceListener guidanceListener = new GuidanceListener(dataGenerator, classifier);
      JPF guidedJPF = new JPF(config);
      guidedJPF.addListener(guidanceListener);
      guidedJPF.addListener(guidedWcListener);

      guidedJPF.run();

      costs[i] = guidedWcListener.getMaxCost();
    }

    String costStr = Arrays.toString(costs).replace(", ", "\n");

    System.out.println(costStr);
    System.out.println("Done");
  }
}
