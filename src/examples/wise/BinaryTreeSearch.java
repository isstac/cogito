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

//package edu.berkeley.cs.wise.benchmarks;

//import edu.berkeley.cs.wise.concolic.Concolic;

/**
 * @author Sudeep Juvekar <sjuvekar@cs.berkeley.edu>
 * @author Jacob Burnim <jburnim@cs.berkeley.edu>
 */
public class BinaryTreeSearch {

    private static class BinaryTree {
        /**
         * Internal class representing a Node in the tree.
         */
        private static class Node {
            int value;
            Node left;
            Node right;

            Node(int v, Node l, Node r) {
                value = v;
                left = l;
                right = r;
            }
        }

        private Node root = null;

        /**
         * Inserts a value in to the tree.
         */
        public void insert(int v) {

            if (root == null) {
                root = new Node(v, null, null);
                return;
            }

            Node curr = root;
            while (true) {
                if (curr.value < v) {
                    if (curr.right != null) {
                        curr = curr.right;
                    } else {
                        curr.right = new Node(v, null, null);
                        break;
                    }
                } else if (curr.value > v) {
                    if (curr.left != null) {
                        curr = curr.left;
                    } else {
                        curr.left = new Node(v, null, null);
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        /**
         * Searches for a value in the tree.
         */
        public boolean search(int v) {
            Node curr = root;
            while (curr != null) {  // N branches
                if (curr.value == v) {  // N-1 branches
                    return true;
                } else if (curr.value < v) {  // N-1 branches
                    curr = curr.right;
                } else {
                    curr = curr.left;
                }
            }
            return false;
        }
    }


    public static void main(String args[]) {
        final int N = Integer.parseInt(args[0]);

        BinaryTree b = new BinaryTree();
        for (int i = 0; i < N; i++) {
            b.insert(Debug.makeSymbolicInteger("in"+i));//Concolic.input.Integer());
        }

        // We only measure the complexity (i.e. path length) of the
        // final search operation.  That is, we count branches only
        // from this point forward in the execution.
        //Concolic.ResetBranchCounting();

        b.search(Debug.makeSymbolicInteger("in"));//Concolic.input.Integer());
    }
}
