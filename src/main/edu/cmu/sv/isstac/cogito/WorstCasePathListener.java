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

import java.util.HashSet;
import java.util.Set;

import edu.cmu.sv.isstac.cogito.cost.CostModel;
import edu.cmu.sv.isstac.cogito.cost.DepthCostModel;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Transition;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class WorstCasePathListener extends PropertyListenerAdapter {

  private final CostModel costModel;
  private long maxCost = -1;

  private Set<Path> maxPaths = new HashSet<>();

  public WorstCasePathListener(Config config) {
    this.costModel = config.getInstance(Options.COST_MODEL, CostModel.class,
        DepthCostModel.class.getName());
  }

  @Override
  public void objectCreated(VM vm, ThreadInfo currentThread, ElementInfo newObject) {
    this.costModel.objectCreated(vm, currentThread, newObject);
  }


  @Override
  public void objectReleased(VM vm, ThreadInfo currentThread, ElementInfo releasedObject) {
    this.costModel.objectReleased(vm, currentThread, releasedObject);
  }

  @Override
  public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {
    this.costModel.instructionExecuted(vm, currentThread, nextInstruction, executedInstruction);
  }

  @Override
  public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {
    this.costModel.choiceGeneratorAdvanced(vm, currentCG);
  }

  @Override
  public void stateBacktracked(Search search) {
    this.costModel.stateBacktracked(search);
  }

  @Override
  public void stateAdvanced(Search search) {
    if(search.isEndState()) {
      long cost = this.costModel.getCost(search);
      //TODO: update cost to be Comparable instead
      if(cost >= maxCost) {
        if(cost > maxCost) {
          maxPaths.clear();
        }
        maxCost = cost;
        Path maxPath = Path.createFrom(search.getVM().getChoiceGenerator());
        maxPaths.add(maxPath);
      }
    }
  }

  public long getMaxCost() {
    return maxCost;
  }

  public Set<Path> getMaxPaths() {
    return maxPaths;
  }
}
