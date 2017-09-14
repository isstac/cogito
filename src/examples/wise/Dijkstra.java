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
 * @author jburnim@cs.berkeley.edu
 */
public class Dijkstra {

    static final int INFINITY = Integer.MAX_VALUE;

    static int[] runDijkstra(int N, int D[][], int src) {
        // Initialize distances.
        int dist[] = new int[N];
        boolean fixed[] = new boolean[N];
        for (int i = 0; i < N; i++) {  // V+1 branches
            dist[i] = INFINITY;
            fixed[i] = false;
        }
        dist[src] = 0;

        for (int k = 0; k < N; k++) { // V+1 branches
            // Find the minimum-distance, unfixed vertex.
            int min = -1;
            int minDist = INFINITY;
            for (int i = 0; i < N; i++) { // V(V+1) branches
                if (!fixed[i] && (dist[i] < minDist)) { // V^2 + V(V+1)/2
                    min = i;
                    minDist = dist[i];
                }
            }

            // Fix the vertex.
            fixed[min] = true;

            // Process the vertex's outgoing edges.
            for (int i = 0; i < N; i++) { // V(V+1) branches
                // V^2 + V(V-1)/2 branches
                if (!fixed[i] && (dist[min] + D[min][i] < dist[i])) {
                    dist[i] = dist[min] + D[min][i];
                }
            }
            
        }

        // Return the computed distances.
        return dist;
    }

    public static void main(String[] args) {
        final int V = Integer.parseInt(args[0]);

        final int D[][] = new int[V][V];

        for (int i = 0; i < V; i++) {
            for (int j = 0; j < V; j++) {
                if (i ==j) continue;
                D[i][j] = Debug.makeSymbolicInteger("in"+i+j);//Concolic.input.Integer(0, 1000);
            }
        }

        // We only measure the complexity (i.e. path length) of the
        // graph algorithm itself.  That is, we count branches only
        // from this point forward in the execution.
       // Concolic.ResetBranchCounting();

        runDijkstra(V, D, 0);
    }
}
