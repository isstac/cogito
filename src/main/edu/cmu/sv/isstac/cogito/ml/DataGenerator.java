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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cmu.sv.isstac.cogito.structure.Conditional;
import edu.cmu.sv.isstac.cogito.structure.Decision;
import edu.cmu.sv.isstac.cogito.structure.Path;

/**
 * @author Kasper Luckow
 */
public class DataGenerator {

  //TODO: This class must be refactored

  private Map<Conditional, Integer> dec2idx = new HashMap<>();
  private int nextIndex = 0;

  private Set<Conditional> conditionals = new HashSet<>();

  public Map<Conditional, DataSet> generateTrainingData(Collection<Path> paths) {
    for(Path p : paths) {
      conditionals.addAll(p.getUniqueConditionals());
    }

    Map<Conditional, DataSet> datasets = new HashMap<>();

    for(Path path : paths) {
      double[] data = new double[conditionals.size() * 2];
      for(Decision decision : path) {
        DataSet decisionTrainingData = datasets.get(decision.getCond());
        if(decisionTrainingData == null) {
          decisionTrainingData = new DataSet();
          datasets.put(decision.getCond(), decisionTrainingData);
        }
        Instance instance = new Instance(data, decision.getChoice());
        decisionTrainingData.add(instance);

        double[] newData = new double[conditionals.size() * 2];
        java.lang.System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;

        int idx = getIdx(decision);
        data[idx] = data[idx] + 1;
      }
    }

    return datasets;
  }

  public double[] generateFeatures(Path path) {

    double[] data = new double[conditionals.size() * 2];
    for(Decision decision : path) {
      int idx = getIdx(decision);
      data[idx] = data[idx] + 1;
    }

    return data;
  }

  private int getIdx(Decision decision) {

    int conditionalIdx;
    if(dec2idx.containsKey(decision.getCond())) {
      conditionalIdx = dec2idx.get(decision.getCond());
    } else {
      conditionalIdx = nextIndex;
      dec2idx.put(decision.getCond(), conditionalIdx);
      nextIndex++;
    }

    int decisionIdx = toDecisionIdx(conditionalIdx, decision.getChoice());
    return decisionIdx;
  }

  private int toDecisionIdx(int conditionalIdx, int choice) {
    //Todo: We constrain ourselves to binary decisions now
    Preconditions.checkArgument(choice == 0 || choice == 1);

    return conditionalIdx * 2 + choice;
  }
}
