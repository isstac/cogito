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

import java.awt.*;
import java.util.*;

import edu.cmu.sv.isstac.cogito.cost.CostModel;
import edu.cmu.sv.isstac.cogito.fitting.DataSeries;
import edu.cmu.sv.isstac.cogito.fitting.FunctionFitter;
import edu.cmu.sv.isstac.cogito.ml.FullPathDataGenerator;
import edu.cmu.sv.isstac.cogito.ml.LogisticRegressionClassifier;
import edu.cmu.sv.isstac.cogito.ml.DataSet;
import edu.cmu.sv.isstac.cogito.ml.DataGenerator;
import edu.cmu.sv.isstac.cogito.ml.PredictionStatistics;
import edu.cmu.sv.isstac.cogito.structure.Conditional;
import edu.cmu.sv.isstac.cogito.structure.Path;
import edu.cmu.sv.isstac.cogito.visualization.Chart;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFShell;

/**
 * @author Kasper Luckow
 */
public class Cogito implements JPFShell {

  //TODO: Refactor this class...

  private final Config config;

  public Cogito(Config config) {
    this.config = config;
  }

  @Override
  public void start(String[] args) {
    if(!Options.valid(config)) {
      Options.printConfigurations();
      return;
    }

    //Enforce no optimization of PC cg's
    config.put("symbolic.optimizechoices", "false");

    Collection<Path> maxPaths = new ArrayList<>();

    CostModel costModel = config.getInstance(Options.COST_MODEL,
        CostModel.class, Options.DEFAULT_COST_MODEL);

    WorstCasePathListener.Factory wcpListenerFactory =
        new WorstCasePathListener.Factory(costModel);

    /*
     * Phase 1: Collect all max paths for input sizes lower-upper.
     */
    int[] trainingInputSizes = config.getIntArray(Options.TRAINING_TARGET_ARGS);
    int lower = trainingInputSizes[0], upper = trainingInputSizes[1];
    for(int inputSize = lower; inputSize <= upper; inputSize++) {
      WorstCasePathListener wcpListener = wcpListenerFactory.build();

      config.setProperty("target.args", inputSize + "");
      JPF jpf = new JPF(config);
      jpf.addListener(wcpListener);
      jpf.run();

      Set<Path> paths = wcpListener.getMaxPaths();
      maxPaths.addAll(paths);
    }

    // Generate training data
    DataGenerator dataGenerator = new FullPathDataGenerator();
    Map<Conditional, DataSet> dataSets = dataGenerator.generateTrainingData(maxPaths);

    LogisticRegressionClassifier classifier = new LogisticRegressionClassifier();

    //Train classifier
    classifier.train(dataSets);

    Collection<PredictionFilter> predictionFilters = new ArrayList<>();
    if(config.hasValue(Options.CONFIDENCE)) {
      double confidence = config.getDouble(Options.CONFIDENCE);
      predictionFilters.add(new ConfidenceFilter(confidence));
    }

    /*
     * Phase 2: Use classifier to resolve decisions
     */
    int maxInputSize = config.getInt(Options.PREDICTION_TARGET_ARGS);

    double[] xs = new double[maxInputSize];
    double[] ys = new double[maxInputSize];
    int idx = 0;

    Map<Integer, GuidanceStatistics> guidanceStatistics = new HashMap<>();

    for(int inputSize = 1; inputSize <= maxInputSize; inputSize++) {
      config.setProperty("target.args", inputSize + "");
      WorstCasePathListener wcpListener = wcpListenerFactory.build();
      GuidanceListener guidanceListener = new GuidanceListener(dataGenerator,
          classifier, predictionFilters);

      JPF guidedJPF = new JPF(config);
      guidedJPF.addListener(guidanceListener);
      guidedJPF.addListener(wcpListener);

      guidedJPF.run();
      long maxCost = wcpListener.getMaxCost();

      // Store guidance statistics
      guidanceStatistics.put(inputSize, guidanceListener.getStatistics());

      xs[idx] = inputSize;
      ys[idx] = maxCost;
      idx++;
    }

    // Generate chart
    Chart.ChartBuilder chartBuilder = new Chart.ChartBuilder("Costs per input size",
        "Input Size",
        costModel.getCostName());

    Collection<DataSeries> predictionSeries = FunctionFitter.computePredictionSeries(xs, ys,
        (int)(xs.length * 1.5));

    for(DataSeries series : predictionSeries) {
      chartBuilder.addSeries(series);
    }

    DataSeries rawSeries = new DataSeries("Raw", xs.length);
    for(int i = 0; i < xs.length; i++) {
      rawSeries.add(xs[i], ys[i]);
    }
    chartBuilder.setRawSeries(rawSeries);

    Chart chart = chartBuilder.build();
    chart.setPreferredSize(new Dimension(1024, 768));
    chart.pack();
    chart.setVisible(true);

    //TODO: We should write all these statistics to a file instead
    System.out.println("--------------------------------------------------------");
    System.out.println("Classifier Statistics:");
    for(PredictionStatistics classifierStatistics : classifier.getStatistics().values()) {
      System.out.println(classifierStatistics.toString());
    }
    System.out.println("--------------------------------------------------------");

    System.out.println("Guidance Statistics: ");
    //TODO: fix this ugliness
    java.util.List<Map.Entry<Integer, GuidanceStatistics>> entries =
        new LinkedList<>(guidanceStatistics.entrySet());
    // Sort by input size
    Collections.sort(entries, (o1, o2) -> o1.getKey() - o2.getKey());

    for(Map.Entry<Integer, GuidanceStatistics> statisticsEntry : entries) {
      System.out.println("Input size: " + statisticsEntry.getKey());
      System.out.println(statisticsEntry.getValue().toString());
    }
  }
}
