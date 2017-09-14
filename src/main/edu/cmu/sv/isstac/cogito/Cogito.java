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

import java.util.*;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.cogito.cost.CostModel;
import edu.cmu.sv.isstac.cogito.ml.FullPathDataGenerator;
import edu.cmu.sv.isstac.cogito.ml.LogisticRegressionClassifier;
import edu.cmu.sv.isstac.cogito.ml.DataSet;
import edu.cmu.sv.isstac.cogito.ml.DataGenerator;
import edu.cmu.sv.isstac.cogito.ml.PredictionStatistics;
import edu.cmu.sv.isstac.cogito.structure.Conditional;
import edu.cmu.sv.isstac.cogito.structure.Path;
import edu.cmu.sv.isstac.cogito.visualization.ResultsOutputter;
import edu.cmu.sv.isstac.cogito.visualization.Visualizer;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class Cogito implements JPFShell {

  private static final Logger LOGGER = JPFLogger.getLogger(Cogito.class.getName());
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
    LOGGER.info("Setting symbolic.optimizechoices=false");

    CostModel costModel = config.getInstance(Options.COST_MODEL,
        CostModel.class, Options.DEFAULT_COST_MODEL);

    // Gather all analysis event observers
    Collection<AnalysisEventObserver> analysisObservers = new HashSet<>();

    if(config.hasValue(Options.EVENT_OBSERVER)) {
      Collection<AnalysisEventObserver> observers = config.getInstances(Options.EVENT_OBSERVER,
          AnalysisEventObserver.class);
      analysisObservers.addAll(observers);
    }

    // Check if we want to visualize the results in a chart + function fitting
    if(config.getBoolean(Options.VISUALIZE, Options.DEFAULT_VISUALIZE)) {
      LOGGER.info("Visualizing results and applying function fitting");
      Visualizer visualizer = new Visualizer(costModel.getCostName());
      analysisObservers.add(visualizer);
    }

    // Check if we want to output all the data to csv files
    if(config.hasValue(Options.OUTPUT_RESULTS)) {
      LOGGER.info("Outputting results to CSV file");
      String outputPath = config.getString(Options.OUTPUT_RESULTS);
      String tgtName = config.getString("target");
      ResultsOutputter outputter = new ResultsOutputter(outputPath, tgtName);
      analysisObservers.add(outputter);
    }

    // Notify observers
    analysisObservers.forEach(e -> e.analysisStart(config));

    WorstCasePathListener.Factory wcpListenerFactory =
        new WorstCasePathListener.Factory(costModel);


    /*
     * Phase 1: Collect all max paths for input sizes lower-upper.
     */

    Collection<Path> maxPaths = new ArrayList<>();

    int[] trainingInputSizes = config.getIntArray(Options.TRAINING_TARGET_ARGS);
    int lower = trainingInputSizes[0], upper = trainingInputSizes[1];

    analysisObservers.forEach(e -> e.trainingStart());
    for(int inputSize = lower; inputSize <= upper; inputSize++) {
      int finalInputSize = inputSize;
      // Notify observers
      analysisObservers.forEach(e -> e.trainingInputSizeStart(finalInputSize));

      WorstCasePathListener wcpListener = wcpListenerFactory.build();

      config.setProperty("target.args", inputSize + "");
      JPF jpf = new JPF(config);
      jpf.addListener(wcpListener);
      jpf.run();

      Set<Path> paths = wcpListener.getMaxPaths();
      maxPaths.addAll(paths);

      // Notify observers
      analysisObservers.forEach(e -> e.trainingInputSizeDone(finalInputSize, paths, wcpListener
          .getMaxCost(), jpf));
    }
    analysisObservers.forEach(e -> e.trainingDone());

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

    analysisObservers.forEach(e -> e.guidanceStart());
    for(int inputSize = 1; inputSize <= maxInputSize; inputSize++) {
      int finalInputSize = inputSize;
      // Notify observers
      analysisObservers.forEach(e -> e.guidanceInputSizeStart(finalInputSize));

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

      // Notify observers
      analysisObservers.forEach(e -> e.guidanceInputSizeDone(finalInputSize, wcpListener.getMaxPaths(),
          maxCost, guidanceListener.getStatistics(), guidedJPF));

      xs[idx] = inputSize;
      ys[idx] = maxCost;
      idx++;
    }
    analysisObservers.forEach(e -> e.guidanceDone());

    // Notify observers
    analysisObservers.forEach(e -> e.analysisDone());
  }

}
