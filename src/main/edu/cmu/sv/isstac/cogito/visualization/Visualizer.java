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

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import edu.cmu.sv.isstac.cogito.AnalysisEventObserver;
import edu.cmu.sv.isstac.cogito.GuidanceStatistics;
import edu.cmu.sv.isstac.cogito.fitting.DataSeries;
import edu.cmu.sv.isstac.cogito.fitting.FunctionFitter;
import edu.cmu.sv.isstac.cogito.ml.PredictionStatistics;
import edu.cmu.sv.isstac.cogito.structure.Conditional;
import edu.cmu.sv.isstac.cogito.structure.Path;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

/**
 * @author Kasper Luckow
 */
public class Visualizer implements AnalysisEventObserver {

  private final String costmodelName;
  private final ArrayList<Double> xsList = new ArrayList<>();
  private final ArrayList<Double> ysList = new ArrayList<>();

  public Visualizer(String costmodelName) {
    this.costmodelName = costmodelName;
  }

  @Override
  public void trainingInputSizeStart(int inputSize) {
    // We only visualize for guided search, so ¯\_(ツ)_/¯
  }

  @Override
  public void trainingInputSizeDone(int inputSize, Set<Path> maxPaths, long maxCost, JPF jpf) {
    // We only visualize for guided search, so ¯\_(ツ)_/¯
  }

  @Override
  public void trainingStart() {

  }

  @Override
  public void trainingDone() {

  }

  @Override
  public void guidanceStart() {

  }

  @Override
  public void guidanceInputSizeStart(int inputSize) {
  }

  @Override
  public void guidanceInputSizeDone(int inputSize, Set<Path> maxPaths, long maxCost,
                                    GuidanceStatistics guidanceStats, JPF jpf) {
    xsList.add((double)inputSize);
    ysList.add((double)maxCost);
  }

  @Override
  public void guidanceDone() {

  }

  @Override
  public void analysisStart(Config config) {

  }

  @Override
  public void analysisDone() {
    double xs[] = toDoubleArray(xsList);
    double ys[] = toDoubleArray(ysList);

    // Generate chart
    Chart.ChartBuilder chartBuilder = new Chart.ChartBuilder("Costs per input size",
        "Input Size",
        costmodelName);
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
  }


  // come on...
  public static double[] toDoubleArray(ArrayList<Double> list) {
    double[] out = new double[list.size()];
    for (int i = 0; i < out.length; i++) {
      out[i] = list.get(i).doubleValue();
    }
    return out;
  }
}
