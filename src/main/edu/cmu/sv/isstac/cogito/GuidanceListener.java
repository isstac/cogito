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

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.cogito.ml.LogisticRegressionClassifier;
import edu.cmu.sv.isstac.cogito.ml.DataGenerator;
import edu.cmu.sv.isstac.cogito.ml.PredictionStatistics;
import edu.cmu.sv.isstac.cogito.structure.Conditional;
import edu.cmu.sv.isstac.cogito.structure.Path;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class GuidanceListener extends PropertyListenerAdapter {

  private static Logger LOGGER = JPFLogger.getLogger(GuidanceListener.class.getName());

  private final DataGenerator dataGenerator;
  private final LogisticRegressionClassifier classifier;
  private final Collection<PredictionFilter> predictionFilters;

  //Statistics
  private final GuidanceStatistics guidanceStatistics = new GuidanceStatistics();

  public GuidanceListener(DataGenerator dataGenerator, LogisticRegressionClassifier classifier) {
    this(dataGenerator, classifier, new ArrayList<>());
  }

  public GuidanceListener(DataGenerator dataGenerator, LogisticRegressionClassifier classifier,
                          Collection<PredictionFilter> predictionFilters) {
    Preconditions.checkNotNull(predictionFilters);

    this.dataGenerator = dataGenerator;
    this.classifier = classifier;
    this.predictionFilters = predictionFilters;
  }

  public GuidanceStatistics getStatistics() {
    return this.guidanceStatistics;
  }

  @Override
  public void choiceGeneratorAdvanced (VM vm, ChoiceGenerator<?> currentCG) {
    ChoiceGenerator<?> cg = vm.getSystemState().getChoiceGenerator();
    if(cg instanceof PCChoiceGenerator) {

      Instruction instruction = currentCG.getInsn();
      Conditional conditional = Conditional.createFrom(instruction);

      //If there is no classifier for the conditional, then exploratiopn degenerates to
      // exhaustive exploration at that choice
      if(this.classifier.hasClassifierFor(conditional)) {

        ChoiceGenerator<?> prevCg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);

        //Check for null and create empty path
        Path path = Path.createFrom(prevCg);

        double[] data = dataGenerator.generateFeatures(path);

        //TODO: Assume for now that there are only two classes
        double[] posterior = new double[2];

        int choice = classifier.predict(conditional, data, posterior);

        //Check if we should skip this prediction
        for(PredictionFilter filter : predictionFilters) {

          if(filter.filterPrediction(choice, conditional, data, posterior)) {
            guidanceStatistics.incrementFilteredPredictedResolutions();
            // Exploration degenerates to exhaustive exploration here
            return;
          }
        }

        LOGGER.fine("Predict: " + choice + " for " + conditional.toString() + ". Probabilities "
            + Arrays.toString(posterior));

        // Select the choice predicted by the classifier
        cg.select(choice);

        // Update some statistics here
        if(Math.abs(posterior[choice] - 1.0) <= 0.00001d) {
          //If this is the case, we regard it as a deterministic resolution

          //TODO: A better way is maybe to check if an instance of DeterministicClassifier was used
          guidanceStatistics.incrementDeterministicResolutions();
        } else {

          // Otherwise we update the predicted resolution statistics
          guidanceStatistics.addPredictionResult(conditional, choice, posterior[choice]);
          guidanceStatistics.incrementPredictedResolutions();
        }

      } else {

        //No resolution could be made. Degenerate to exhaustive here
        guidanceStatistics.incrementNondeterministicResolutions();
      }
    }
  }
}
