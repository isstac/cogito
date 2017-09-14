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
public class BellmanFord {

    static final int INFINITY = 1000000;

    static int[] runBellmanFord(int N, int D[][], int src) {
        // Initialize distances.
        int dist[] = new int[N];
        boolean infinite[] = new boolean[N];
        for (int i = 0; i < N; i++) {  // V+1 branches
            dist[i] = INFINITY;
            infinite[i] = true;
        }
        dist[src] = 0;
        infinite[src] = false;

        // Keep relaxing edges until either:
        //  (1) No more edges need to be updated.
        //  (2) We have passed through the edges N times.
        int k;
        for (k = 0; k < N; k++) {  // V+1 branches
            boolean relaxed = false;
            for (int i = 0; i < N; i++) {  // V(V+1) branches
                for (int j = 0; j < N; j++) {  // V^2(V+1) branches
                    if (i == j) continue;  // V^3 branches
                    if (!infinite[i]) {  // V^2(V-1) branches
                        if (dist[j] > dist[i] + D[i][j]) {  // V^2(V-1) branches
                            dist[j] = dist[i] + D[i][j];
                            infinite[j] = false;
                            relaxed = true;
                        }
                    }
                }
            }
            if (!relaxed)  // V branches
                break;
        }

        // Check for negative-weight egdes.
        if (k == N) {  // 1 branch
            // We relaxed during the N-th iteration, so there must be
            // a negative-weight cycle.
        }

        // Return the computed distances.
        return dist;
    }

    public static void main(String[] args) {
        final int V = Integer.parseInt(args[0]);
    	// final int V = 3;

        final int D[][] = new int[V][V];

        for (int i = 0; i < V; i++) {
            for (int j = 0; j < V; j++) {
                if (i ==j) continue;
                D[i][j] = Debug.makeSymbolicInteger("in"+i+j);//Concolic.input.Integer(-1000, 1000);
            }
        }

        // We only measure the complexity (i.e. path length) of the
        // graph algorithm itself.  That is, we count branches only
        // from this point forward in the execution.
        //Concolic.ResetBranchCounting();

        runBellmanFord(V, D, 0);
    }
}
