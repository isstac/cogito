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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.cmu.sv.isstac.cogito.Conditional;
import smile.classification.Classifier;
import smile.classification.LogisticRegression;
import smile.classification.SoftClassifier;

/**
 * @author Kasper Luckow
 */
public class CogitoClassifier {
  //TODO: This is just a dummy for now

  private Map<Conditional, SoftClassifier<double[]>> classifiers = new HashMap<>();

  public void train(Map<Conditional, DataSet> trainingSet) {
    for(Map.Entry<Conditional, DataSet> entry : trainingSet.entrySet()) {

      //TODO: Fix this ugliness. Do not allow this. Seriously
      List<Integer> classes = new ArrayList<>();
      for(int y : entry.getValue().getYs()) {
        classes.add(y);
      }
      int uniqueClasses = new HashSet<>(classes).size();
      assert uniqueClasses > 0;
      if(uniqueClasses < 2) { //in this case the predictor can simply return the single class found

        final int singleClass = classes.get(0);
        classifiers.put(entry.getKey(), new SoftClassifier<double[]>() {
          @Override
          public int predict(double[] doubles, double[] doubles2) {
            //doubles2 is the posterior prob
            assert doubles2 != null && doubles2.length > 0;
            doubles2[0] = 1.0;
            return singleClass;
          }

          @Override
          public int predict(double[] doubles) {
            return singleClass;
          }
        });
      } else {
        //If there are more than two classes, we will train a logistic regression model
        LogisticRegression lr = new LogisticRegression(
            entry.getValue().getXs(),
            entry.getValue().getYs());

        classifiers.put(entry.getKey(), lr);
      }
//
//      if(entry.getValue().getXs().length > 1) {
//        LogisticRegression lr = new LogisticRegression(entry.getValue().getXs(), entry.getValue().getYs());
//        double[] posterior = new double[2];
//        int y1 = lr.predict(new double[] {1,0,1,0,1,0,0,0}, posterior);//1
//        int y2 = lr.predict(new double[] {1,0,1,0,1,0,0,1});//1
//        int y3 = lr.predict(new double[] {1,0,1,0,1,0,0,2});//1
//        int y4 = lr.predict(new double[] {1,0,1,0,1,0,0,3});//0
//        System.out.println("predict");
//      }
//    }
    }
  }

  public int predict(Conditional conditional, double[] data) {
    SoftClassifier<double[]> classifier = this.classifiers.get(conditional);

    //TODO: Assume for now that there are only two classes
    double[] posterior = new double[2];
    return classifier.predict(data, posterior);
  }
}
