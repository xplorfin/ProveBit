package org.provebit.merkle;

import java.lang.reflect.Array;

public class BTree<T> {
    
    private T[] tree;
    private int totalNodes;
    private int height; // Tree of 1 node height 0 (root at height 0)
    
    /**
     * Constructor that allocates space for a tree of specified size
     * @param t - type of elements in tree
     * @param size - total number of nodes to allocate for tree
     */
    @SuppressWarnings("unchecked")
    public BTree(Class<T> t, int size) {
        tree = (T[]) Array.newInstance(t, size);
        for (int i = 0; i < size; i++) {
            tree[i] = null;
        }
        
        totalNodes = size;
        height = (int) logBase2((int)size);
    }
    
    /**
     * Computes log base 2 of the argument value
     * @param value - double to compute log_2 of
     * @return log_2(value)
     */
    private double logBase2(double value) {
        return (Math.log(value)/Math.log(2.0));
    }
    
    
}
