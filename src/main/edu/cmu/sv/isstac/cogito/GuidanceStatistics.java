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

/**
 * @author Kasper Luckow
 */
public class GuidanceStatistics {

  private int predictedResolutions = 0;
  private int deterministicResolutions = 0;
  private int nondeterministicResolutions = 0;
  private int filteredPredictedResolutions = 0;

  public int getPredictedResolutions() {
    return predictedResolutions;
  }

  public int getDeterministicResolutions() {
    return deterministicResolutions;
  }

  public int getFilteredPredictedResolutions() {
    return filteredPredictedResolutions;
  }

  public int getNondeterministicResolutions() {
    return nondeterministicResolutions;
  }

  public void incrementPredictedResolutions() {
    predictedResolutions++;
  }

  public void incrementDeterministicResolutions() {
    deterministicResolutions++;
  }

  public void incrementNondeterministicResolutions() {
    nondeterministicResolutions++;
  }

  public void incrementFilteredPredictedResolutions() {
    filteredPredictedResolutions++;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Predicted resolutions: ").append(this.predictedResolutions).append('\n')
        .append("Deterministic resolutions: ").append(this.deterministicResolutions).append('\n')
        .append("Filtered resolutions (nondeterministic): ")
            .append(this.filteredPredictedResolutions).append('\n')
        .append("Non-deterministic resolutions: ")
            .append(this.nondeterministicResolutions).append('\n');
    return sb.toString();
  }
}
