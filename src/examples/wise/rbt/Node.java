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

/**
 * This code originally by Tuomo Saarni.  Obtained from:
 *
 *     http://users.utu.fi/~tuiisa/Java/index.html
 *
 * under the following license:
 *
 *     Here's some java sources I've made. Most codes are free to
 *     download. If you use some of my sources just remember give me
 *     the credits.
 */

//package edu.berkeley.cs.wise.benchmarks.rbtree;

package wise.rbt;

/**
 * A <code>Node</code> object is a node of search tree
 * including key data and satellite object.
 * <p/>
 * It can be used with binary search tree as well as with
 * red black tree or with any other search tree.
 *
 * @author Tuomo Saarerni
 * @version 1.2, 08/16/01
 */

public class Node {
    /**
     * The key of the node.
     *
     * @see #key()
     * @see #keyTo
     */
    protected int key;

    /**
     * The satellite data in the node.
     *
     * @see #object()
     * @see #objectTo
     */
    protected Object data;                // Refers to the satellite data

    /**
     * Constructs a new node. The satellite data is set to <code>null>/code>.
     *
     * @param _key The key of the node.
     */
    public Node(int _key) {
        key = _key;
        data = null;
    }

    /**
     * Constructs a new node.
     *
     * @param _key The key of the node.
     * @param dat  The satellite data of the node, type <code>Object</code>.
     */
    public Node(int _key, Object dat) {
        this(_key);
        Object data = dat;
    }

    /**
     * Returns the key of the node.
     *
     * @return The key of the node.
     */
    public int key() {
        return this.key;
    }

    /**
     * Returns the satellite data of the node.
     *
     * @return The satellite object of the node.
     */
    public Object object() {
        return this.data;
    }

    /**
     * Returns the node.
     *
     * @return The node as a <code>String</code>.
     */
    public String toString() {
        return new String("Key: " + this.key);
    }

    /**
     * Sets the key to _key.
     *
     * @param _key The new key of the node.
     */
    public void keyTo(int _key) {
        this.key = _key;
    }

    /**
     * Sets the data to o.
     *
     * @param o The new data of the node.
     */
	public void objectTo(Object o)
	{
		this.data = o;
	}

} // End class Node
