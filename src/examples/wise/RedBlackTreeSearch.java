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

import gov.nasa.jpf.symbc.Debug;
import wise.rbt.RedBlackTree;
import wise.rbt.RedBlackTreeNode;


//import sampling.batch.rbt.RedBlackTree;
//import sampling.batch.rbt.RedBlackTreeNode;

//package edu.berkeley.cs.wise.benchmarks;

//import edu.berkeley.cs.wise.benchmarks.rbtree.RedBlackTree;
//import edu.berkeley.cs.wise.benchmarks.rbtree.RedBlackTreeNode;

//import edu.berkeley.cs.wise.concolic.Concolic;

/**
 * @author Koushik Sen <ksen@cs.berkeley.edu>
 * @author Jacob Burnim <jburnim@cs.berkeley.edu>
 */
public class RedBlackTreeSearch {
    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);

        RedBlackTree tree = new RedBlackTree();

        for (int i = 0; i < N; i++) {
            int data = Debug.makeSymbolicInteger("in"+i);//Concolic.input.Integer();
            tree.treeInsert(new RedBlackTreeNode(data));
        }

        // We only measure the complexity (i.e. path length) of the
        // final search operation.  That is, we count branches only
        // from this point forward in the execution.
        //Concolic.ResetBranchCounting();

        int data = Debug.makeSymbolicInteger("in");//Concolic.input.Integer();
        tree.treeSearch(tree.root(), data);
    }
}
