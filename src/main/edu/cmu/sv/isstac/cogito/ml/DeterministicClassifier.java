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

import java.util.HashMap;
import java.util.Map;

import smile.classification.SoftClassifier;

/**
 * @author Kasper Luckow
 */
public class DeterministicClassifier implements SoftClassifier<double[]> {

  private static final Map<Integer, DeterministicClassifier> cache = new HashMap<>();

  public static DeterministicClassifier create(int deterministicChoice) {
    DeterministicClassifier classifier = cache.get(deterministicChoice);
    if(classifier == null) {
      classifier = new DeterministicClassifier(deterministicChoice);
      cache.put(deterministicChoice, classifier);
    }
    return classifier;
  }

  private final int deterministicChoice;

  private DeterministicClassifier(int deterministicChoice) {
    this.deterministicChoice = deterministicChoice;
  }

  @Override
  public int predict(double[] instance, double[] posteriori) {
    Preconditions.checkNotNull(posteriori);
    Preconditions.checkArgument(posteriori.length - 1 >= deterministicChoice);

    // The probability of the deterministic choice we have selected is 1.0; for all others 0.0
    for(int i = 0; i < posteriori.length; i++) {
      if(i == deterministicChoice) {
        posteriori[deterministicChoice] = 1.0;
      } else {
        posteriori[deterministicChoice] = 0.0;
      }
    }

    return deterministicChoice;
  }

  @Override
  public int predict(double[] instance) {
    return deterministicChoice;
  }
}
