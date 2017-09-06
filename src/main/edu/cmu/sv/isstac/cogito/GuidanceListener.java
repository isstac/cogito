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

import java.util.Arrays;

import edu.cmu.sv.isstac.cogito.ml.CogitoClassifier;
import edu.cmu.sv.isstac.cogito.ml.DataGenerator;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;
import smile.classification.Classifier;

/**
 * @author Kasper Luckow
 */
public class GuidanceListener extends PropertyListenerAdapter {

  private final DataGenerator dataGenerator;
  private final CogitoClassifier classifier;

  public GuidanceListener(DataGenerator dataGenerator, CogitoClassifier classifier) {
    this.dataGenerator = dataGenerator;
    this.classifier = classifier;
  }

  @Override
  public void choiceGeneratorAdvanced (VM vm, ChoiceGenerator<?> currentCG) {
    ChoiceGenerator<?> cg = vm.getSystemState().getChoiceGenerator();
    if(cg instanceof PCChoiceGenerator) {

      Conditional conditional = Conditional.createFrom(currentCG.getInsn());

      ChoiceGenerator<?> prevCg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);

      //Check for null and create empty path
      Path path = Path.createFrom(prevCg);
      double[] data = dataGenerator.generateFeatures(path);



      //TODO: Assume for now that there are only two classes
      double[] posterior = new double[2];

      int choice = classifier.predict(conditional, data, posterior);
//      System.out.println("Posterior: " + Arrays.toString(posterior));
      cg.select(choice);
    }
  }
}
