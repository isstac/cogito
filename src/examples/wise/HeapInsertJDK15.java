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

package wise;

//package edu.berkeley.cs.wise.benchmarks;

import gov.nasa.jpf.symbc.Debug;
import wise.java15.util.PriorityQueue;


//import edu.berkeley.cs.wise.benchmarks.java15.util.PriorityQueue;

//import edu.berkeley.cs.wise.concolic.Concolic;

/**
 * @author Jacob Burnim <jburnim@cs.berkeley.edu>
 * @author Koushik Sen <ksen@cs.berkeley.edu>
 */
public class HeapInsertJDK15 {

    public static void main(String[] args) {
        final int N = Integer.parseInt(args[0]);

        PriorityQueue<SimpleObject> Q = new PriorityQueue<SimpleObject>(N);

        for (int i = 1; i < N; i++) {
            Q.addMask(new SimpleObject(Debug.makeSymbolicInteger("in"+i)));//Concolic.input.Integer()));
            
        }

        //Debug.printPC("before add");
        // We only measure the complexity (i.e. path length) of the
        // final insert operation.  That is, we count branches only
        // from this point forward in the execution.
       // Concolic.ResetBranchCounting();

        Q.add(new SimpleObject(Debug.makeSymbolicInteger("in")));//Concolic.input.Integer()));
        //Debug.printPC("after add");
    }
}
