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

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public class Path {
  private LinkedList<Integer> store = new LinkedList<>();

  public static Path createFrom(ChoiceGenerator<?> cg) {
    Path p = new Path();
    if(cg != null) {
      for(ChoiceGenerator<?> c : cg.getAll()) {
        p.addChoice(c);
      }
    }
    return p;
  }

  public Path() {

  }

  public void addChoice(ChoiceGenerator<?> cg) {
    int choice = getCurrentChoiceOfCG(cg);
    assert choice >= 0;
    addChoice(choice);
  }

  //A bit expensive since store is a linked list at the moment
  public int getChoice(int index) {
    return this.store.get(index);
  }

  public boolean isPrefix(Path other) {
    if(other.length() > this.length()) {
      return false;
    }
    for(int i = 0; i < other.length(); i++) {
      if(!store.get(i).equals(other.store.get(i))) {
        return false;
      }
    }
    return true;
  }

  public int length() {
    return store.size();
  }

  public void addChoice(int choice) {
    store.add(choice);
  }

  public int removeLast() {
    return store.removeLast();
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
    return Objects.hashCode(store);
  }
  
  @Override
  public boolean equals(Object other) {
    if(other == null) return false;
    if(getClass() != other.getClass()) return false;
    Path otherPath = (Path) other;
    return Objects.equal(this.store, otherPath.store);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    Iterator<Integer> iter = store.iterator();
    while(iter.hasNext()) {
      sb.append(iter.next());
      if(iter.hasNext())
        sb.append(",");
    }
    sb.append("]");
    return sb.toString();
  }
}
