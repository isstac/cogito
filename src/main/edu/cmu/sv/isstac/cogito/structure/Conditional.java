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

package edu.cmu.sv.isstac.cogito.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import gov.nasa.jpf.jvm.bytecode.IfInstruction;

import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;

/**
 * @author Kasper Luckow
 */
public class Conditional {

  private final String mnemonic;
  private final int index;
  private final int lineNumber;
  private final String methodName;
  private final String className;

  private static Map<String, Map<String, Map<Integer, Conditional>>> cache = new HashMap<>();

  //TODO: Maybe this is premature optimization...
  // Sadly, we cannot enforce isntances of IfInstructions, because some PCChoiceGenerators have
  // INVOKESTATIC associated with them.
  public static Conditional createFrom(Instruction instruction) {
    String cl = getClassName(instruction);
    String mt = getMethodName(instruction);
    int index = getIndex(instruction);

    Map<String, Map<Integer, Conditional>> meth2cond = cache.get(cl);
    if(meth2cond == null) {
      meth2cond = new HashMap<>();
      cache.put(cl, meth2cond);
    }

    Map<Integer, Conditional> index2cond = meth2cond.get(mt);
    if(index2cond == null) {
      index2cond = new HashMap<>();
      meth2cond.put(mt, index2cond);
    }

    Conditional cond = index2cond.get(index);
    if(cond == null) {
      String mnemonic = getMnemonic(instruction);
      int lineNumer = getLineNumber(instruction);
      cond = new Conditional(mnemonic, cl, mt, index, lineNumer);
      index2cond.put(index, cond);
    }

    assert cond != null;
    return cond;

  }

  private Conditional(String mnemonic, String className,
                      String methodName, int index, int lineNumber) {
    this.mnemonic = mnemonic;
    this.className = className;
    this.methodName = methodName;
    this.index = index;
    this.lineNumber = lineNumber;
  }

  private static String getMnemonic(Instruction instr) {
    return instr.getMnemonic();
  }

  private static String getClassName(Instruction instr) {
    return instr.getMethodInfo().getClassName();
  }

  private static String getMethodName(Instruction instr) {
    return normalizeJPFMethodName(instr.getMethodInfo());
  }

  private static int getIndex(Instruction instr) {
    return instr.getInstructionIndex();
  }

  private static int getLineNumber(Instruction instr) {
    return instr.getLineNumber();
  }

  private static String normalizeJPFMethodName(MethodInfo methInfo) {
    int methBeginIdx = methInfo.getBaseName().lastIndexOf('.') + 1;
    String fullName = methInfo.getFullName();
    return fullName.substring(methBeginIdx, fullName.length());
  }

  public String getMnemonic() {
    return mnemonic;
  }

  public String getClassName() {
    return className;
  }

  public int getInstructionIndex() {
    return index;
  }

  public String getMethodName() {
    return methodName;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  @Override
  public int hashCode() {
    return Objects.hash(index, lineNumber, mnemonic, className, methodName);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof Conditional)) {
      return false;
    }
    Conditional cond = (Conditional) o;
    return index == cond.index &&
        lineNumber == cond.lineNumber &&
        mnemonic.equals(cond.mnemonic) &&
        className.equals(cond.className) &&
        methodName.equals(cond.methodName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("l:" + this.getLineNumber())
        .append("(o:").append(this.getInstructionIndex()).append(")");
    return sb.toString();
  }

}
