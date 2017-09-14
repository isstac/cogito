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

package edu.cmu.sv.isstac.cogito.visualization;

import com.google.common.base.Stopwatch;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.cogito.AnalysisEventObserver;
import edu.cmu.sv.isstac.cogito.GuidanceStatistics;
import edu.cmu.sv.isstac.cogito.fitting.DataSeries;
import edu.cmu.sv.isstac.cogito.fitting.FunctionFitter;
import edu.cmu.sv.isstac.cogito.ml.PredictionStatistics;
import edu.cmu.sv.isstac.cogito.structure.Conditional;
import edu.cmu.sv.isstac.cogito.structure.Path;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class ResultsOutputter implements AnalysisEventObserver {
  // Not the prettiest code...

  private final Logger LOGGER = JPFLogger.getLogger(ResultsOutputter.class.getName());

  private final String outputPath;
  private final String baseFileName;

  private final Stopwatch analysisStopwatch = Stopwatch.createUnstarted();
  private final Stopwatch trainingStopwatch = Stopwatch.createUnstarted();
  private final Stopwatch guidedSearchStopwatch = Stopwatch.createUnstarted();

  private StringBuilder trainingString;
  private StringBuilder guidanceString;

  private static final NumberFormat decimalFormatter = new DecimalFormat("#0.0000");

  public ResultsOutputter(String outputPath, String sutName) {
    this.outputPath = outputPath;
    this.baseFileName = sutName;
  }

  @Override
  public void analysisStart(Config config) {
    analysisStopwatch.start();
  }

  @Override
  public void analysisDone() {
    LOGGER.info("Total analysis time: " + analysisStopwatch.elapsed(TimeUnit.SECONDS) + "s");
    analysisStopwatch.stop();
  }

  @Override
  public void trainingStart() {
    this.trainingString = new StringBuilder();

    //Create csv header
    this.trainingString
        .append("inputSize,")
        .append("maxCost,")
        .append("#maxPaths,")
        .append("#paths,")
        .append("analysisTime")
        .append('\n');

  }

  @Override
  public void trainingInputSizeStart(int inputSize) {
    trainingStopwatch.start();
  }

  @Override
  public void trainingInputSizeDone(int inputSize, Set<Path> maxPaths, long maxCost, JPF jpf) {
    long analysisTime = trainingStopwatch.elapsed(TimeUnit.SECONDS);
    trainingStopwatch.stop();
    this.trainingString
        .append(inputSize).append(",")
        .append(maxCost).append(",")
        .append(maxPaths.size()).append(",")
        .append(jpf.getReporter().getStatistics().endStates).append(",")
        .append(analysisTime)
        .append('\n');
  }

  @Override
  public void trainingDone() {
    File f = new File(outputPath,baseFileName + "_training.csv");
    try(FileWriter fw = new FileWriter(f)) {
      fw.write(trainingString.toString());
    } catch (IOException e) {
      LOGGER.severe(e.getMessage());
    }
    LOGGER.info("Wrote training data to " + f.getAbsolutePath());
  }

  @Override
  public void guidanceStart() {
    this.guidanceString = new StringBuilder();

    //Create csv header
    this.guidanceString
        .append("inputSize,")
        .append("maxCost,")
        .append("#maxPaths,")
        .append("#paths,")
        .append("analysisTime,")
        .append("#predictedResolutions,")
        .append("#deterministicResolutions,")
        .append("#nondeterministicResolutions,")
        .append("#filteredPredictedResolutions,")
        .append("#overallPredictionProbability")
        .append('\n');
  }

  @Override
  public void guidanceInputSizeStart(int inputSize) {
    guidedSearchStopwatch.start();
  }

  @Override
  public void guidanceInputSizeDone(int inputSize, Set<Path> maxPaths, long maxCost,
                                    GuidanceStatistics guidanceStats,
                                    JPF jpf) {
    long analysisTime = guidedSearchStopwatch.elapsed(TimeUnit.SECONDS);
    guidedSearchStopwatch.stop();

    //Maybe this is not really an interesting stat
    Map<Conditional, PredictionStatistics> predStats = guidanceStats.getPredictionStatistics();
    double probabilitySum = 0.0;
    int count = 0;
    for(PredictionStatistics ps : predStats.values()) {
      for(int i = 0; i <= 1; i++) {
        if(ps.getChoiceCounts(i) > 0) {
          probabilitySum += ps.getAvgProbability(i);
          count++;
        }
      }
    }

    double probabilityAvg = probabilitySum / count;

    this.guidanceString
        .append(inputSize).append(",")
        .append(maxCost).append(",")
        .append(maxPaths.size()).append(",")
        .append(jpf.getReporter().getStatistics().endStates).append(",")
        .append(analysisTime).append(",")
        .append(guidanceStats.getPredictedResolutions()).append(",")
        .append(guidanceStats.getDeterministicResolutions()).append(",")
        .append(guidanceStats.getNondeterministicResolutions()).append(",")
        .append(guidanceStats.getFilteredPredictedResolutions()).append(",")
        .append(decimalFormatter.format(probabilityAvg))
        .append('\n');
  }

  @Override
  public void guidanceDone() {
    File f = new File(outputPath,baseFileName + "_guidance.csv");
    try(FileWriter fw = new FileWriter(f)) {
      fw.write(guidanceString.toString());
    } catch (IOException e) {
      LOGGER.severe(e.getMessage());
    }
    LOGGER.info("Wrote guidance data to " + f.getAbsolutePath());
  }
}
