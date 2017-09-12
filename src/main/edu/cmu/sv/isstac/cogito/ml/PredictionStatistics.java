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

import com.google.common.base.Preconditions;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import edu.cmu.sv.isstac.cogito.structure.Conditional;

/**
 * @author Kasper Luckow
 */
public class PredictionStatistics {

  //TOOD: maybe we don't need this
  private final Conditional conditional;

  // TODO: We assume only two choices for now...
  private SummaryStatistics[] probabilities = new SummaryStatistics[] {
      new SummaryStatistics(),
      new SummaryStatistics()};

  public PredictionStatistics(Conditional conditional) {
    this.conditional = conditional;
  }

  public void addPredictionData(int choice, double probability) {
    //TODO: We restrict ourselves to binary decisions...
    Preconditions.checkArgument(choice >= 0 && choice <= 1);

    probabilities[choice].addValue(probability);
  }

  public double getChoiceCounts(int choice) {
    //TODO: We restrict ourselves to binary decisions...
    Preconditions.checkArgument(choice >= 0 && choice <= 1);
    //The number of probabilties we added, correspond to how many times we made that prediction.
    return probabilities[choice].getN();
  }

  public double getAvgProbability(int choice) {
    //TODO: We restrict ourselves to binary decisions...
    Preconditions.checkArgument(choice >= 0 && choice <= 1);
    return probabilities[choice].getMean();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(conditional.toString()).append(":\n");
    for(int i = 0; i < probabilities.length; i++) {
      sb.append("Choice[").append(i).append("]: ");
      sb.append(probabilities[i].toString());
      if(i < probabilities.length - 1) {
        sb.append("\n");
      }
    }
    return sb.toString();
  }
}
