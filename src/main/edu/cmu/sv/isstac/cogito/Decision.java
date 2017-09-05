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

import java.util.Iterator;
import java.util.Map;

/**
 * @author Kasper Luckow
 */
public class Decision {

  private final Conditional cond;
  private final int choice;

  public static Decision createFrom(Conditional cond, int choice) {
    //Maybe we should cache the objects. Profile this
    return new Decision(cond, choice);
  }

  private Decision(Conditional cond, int choice) {
    this.cond = cond;
    this.choice = choice;
  }

  public Conditional getCond() {
    return cond;
  }

  public int getChoice() {
    return choice;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(cond, choice);
  }

  @Override
  public boolean equals(Object other) {
    if(other == null) return false;
    if(!(other instanceof Decision)) return false;

    Decision otherDec = (Decision)other;
    return Objects.equal(this.cond.equals(otherDec), this.choice) &&
           Objects.equal(this.choice, otherDec.choice);
  }

  @Override
  public String toString() {
    return cond.toString() + ":" + choice;
  }

}
