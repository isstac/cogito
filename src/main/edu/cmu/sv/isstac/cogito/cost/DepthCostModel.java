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

package edu.cmu.sv.isstac.cogito.cost;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.cogito.Options;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.ClassFile;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class DepthCostModel implements CostModel {
  private static final Logger LOGGER = JPFLogger.getLogger(DepthCostModel.class.getName());

  // If we are just targeting symbolic.method, then we simply use the
  // depth obtained from the vm object. If there is a measured method, then we iterate over the
  // CGs until we reach one that is
  private interface DepthComputation {
    long compute(Search search);
    void methodEntered(VM vm, ThreadInfo currentThread, MethodInfo enteredMethod);
  }

  //This is the "standard" depth computation that just relies on search object's depth
  private static class JPFDepthComputation implements DepthComputation {
    @Override
    public long compute(Search search) {
      return search.getDepth();
    }

    @Override
    public void methodEntered(VM vm, ThreadInfo currentThread, MethodInfo enteredMethod) {
      // Ignore
    }
  }

  private static class MeasuredMethodDepthComputation implements DepthComputation {

    private final Set<String> measuredMethods;
    private static final int DEPTH_NOT_SET = -1;
    private int startDepth = DEPTH_NOT_SET;

    public MeasuredMethodDepthComputation(Set<String> measuredMethods) {
      this.measuredMethods = measuredMethods;
    }

    @Override
    public long compute(Search search) {
      if(startDepth == DEPTH_NOT_SET) {
        String msg = "Start depth has not been set. " +
            "Maybe measured method has incorrectly been set?";
        LOGGER.severe(msg);
        throw new RuntimeException(msg);
      }

      int realDepth = search.getDepth() - startDepth;

      return realDepth;
    }

    @Override
    public void methodEntered(VM vm, ThreadInfo currentThread, MethodInfo enteredMethod) {
      String meth = enteredMethod.getBaseName();
      if(measuredMethods.contains(meth)) {
        int currentDepth = vm.getSearch().getDepth();
        if(this.startDepth != DEPTH_NOT_SET) {
          LOGGER.fine("Start depth is being set again. Was " + this.startDepth + " will be set " +
              "to " + currentDepth);
        }
        this.startDepth = currentDepth;
      }
    }
  }


  private final DepthComputation depthComputation;

  public DepthCostModel(Config jpfConfig) {
    if (jpfConfig.hasValue(Options.MEASURED_METHOD)) {
      String[] measMeth = jpfConfig.getStringArray(Options.MEASURED_METHOD);
      Set<String> measuredMethods = extractSimpleMethodNames(measMeth);
      this.depthComputation = new MeasuredMethodDepthComputation(measuredMethods);
    } else {
      //Just default to JPF's notion of depth
      this.depthComputation = new JPFDepthComputation();
    }
  }

  private static Set<String> extractSimpleMethodNames(String[] jpfMethodSpecs) {

    //TODO: This also means that we do not distinguish between overloaded methods
    String[] processedMethods = new String[jpfMethodSpecs.length];
    System.arraycopy(jpfMethodSpecs, 0, processedMethods, 0, jpfMethodSpecs.length);
    for (int i = 0; i < jpfMethodSpecs.length; i++) {
      String meth = jpfMethodSpecs[i];
      int sigBegin = meth.indexOf('(');
      if (sigBegin >= 0)
        processedMethods[i] = meth.substring(0, sigBegin);
    }
    return new HashSet<>(Arrays.asList(processedMethods));
  }

  @Override
  public void methodEntered(VM vm, ThreadInfo currentThread, MethodInfo enteredMethod) {
    // Forward event to the actual depth computation
    this.depthComputation.methodEntered(vm, currentThread, enteredMethod);
  }

  @Override
  public long getCost(Search search) {
    return depthComputation.compute(search);
  }

  @Override
  public String getCostName() {
    return "Depth";
  }

  @Override
  public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {

  }

  @Override
  public void objectCreated(VM vm, ThreadInfo currentThread, ElementInfo newObject) {

  }

  @Override
  public void objectReleased(VM vm, ThreadInfo currentThread, ElementInfo releasedObject) {

  }

  @Override
  public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {

  }

  @Override
  public void stateBacktracked(Search search) {

  }

  @Override
  public void methodExited(VM vm, ThreadInfo currentThread, MethodInfo exitedMethod) {

  }
}
