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

import com.google.common.base.Objects;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public class Path implements Iterable<Decision> {

  private LinkedList<Decision> decisions = new LinkedList<>();
  private Set<Conditional> uniqueConditionals = new HashSet<>();

  public static Path createFrom(ChoiceGenerator<?> cg) {
    Path p = new Path();
    if(cg != null) {
      for(ChoiceGenerator<?> c : cg.getAll()) {
        p.addDecision(c);
      }
    }
    return p;
  }

  private void addDecision(ChoiceGenerator<?> cg) {
    int choice = getCurrentChoiceOfCG(cg);
    assert choice >= 0;

    Conditional cond = Conditional.createFrom(cg.getInsn());
    uniqueConditionals.add(cond);

    Decision dec = Decision.createFrom(cond, choice);

    addDecision(dec);
  }

  public Decision getDecision(int index) {
    return this.decisions.get(index);
  }

  public boolean isPrefix(Path other) {
    if(other.length() > this.length()) {
      return false;
    }
    for(int i = 0; i < other.length(); i++) {
      if(!decisions.get(i).equals(other.decisions.get(i))) {
        return false;
      }
    }
    return true;
  }

  public Set<Conditional> getUniqueConditionals() {
    return uniqueConditionals;
  }

  public int length() {
    return decisions.size();
  }

  public void addDecision(Decision decision) {
    decisions.add(decision);
  }

  private static int getCurrentChoiceOfCG(ChoiceGenerator<?> cg) {
    //BIG FAT WARNING:
    //This is in general UNSAFE to do,
    //because there is NO guarantee that choices are selected
    //incrementally! However, there does not seem to be another
    //way of obtaining a lightweight representation of the path
    //i.e. a sequence of decisions (represented by ints)
    //I think it is safe for ThreadChoiceFromSet (currently our only nondeterministic choice)
    //and PCChoiceGenerator
    return cg.getProcessedNumberOfChoices() - 1;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(decisions);
  }
  
  @Override
  public boolean equals(Object other) {
    if(other == null) return false;
    if(!(other instanceof Path)) return false;

    Path otherPath = (Path) other;
    return Objects.equal(this.decisions, otherPath.decisions);
  }

  public String toSimplePathString() {
    return stringifyPath(dec -> String.valueOf(dec.getChoice()));
  }

  @Override
  public String toString() {
    return stringifyPath(dec -> dec.toString());
  }

  private String stringifyPath(Function<Decision, String> mapper) {
    String result = decisions
        .stream()
        .map(mapper)
        .collect(Collectors.joining(", ", "[", "]"));

    return result;
  }

  @Override
  public Iterator<Decision> iterator() {
    return decisions.iterator();
  }
}
