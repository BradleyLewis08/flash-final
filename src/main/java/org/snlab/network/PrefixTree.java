package org.snlab.network;

import java.math.BigInteger;
import java.util.ArrayList;

class TreeNode {
    private BigInteger prefix;
    private int predicate;
    public TreeNode left;
    public TreeNode right;

    public TreeNode(BigInteger prefix, int predicate) {
        this.prefix = prefix;
        this.predicate = predicate;
        this.left = null;
        this.right = null;
    }

    public TreeNode(BigInteger prefix, int predicate, TreeNode left, TreeNode right) {
        this.prefix = prefix;
        this.predicate = predicate;
        this.left = left;
        this.right = right;
    }

    // Returns the fill 32 bit representation of the prefix
    public BigInteger getPrefix() {
        return this.prefix.shiftLeft(32 - this.prefix.bitLength());
    }

    public int getPredicate() {
        return this.predicate;
    }
}

public class PrefixTree {

    // Constructor

    // Add a node to the tree
    public void addNode(BigInteger prefix, int predicate) {
        // Preprocess the prefix to be 32 bits
        prefix = prefix.shiftLeft(32 - prefix.bitLength());

        // Traverse to the current best parent
        ArrayList<TreeNode> path = this.getPath(prefix);
        TreeNode parent = path.get(path.size() - 1);

        if (parent.getPrefix().equals(prefix)) {
            // If the prefix already exists, update the predicate
            return;
        }

        // Set the node to the left or right of the parent based on the next bit

        // Set the child
    }

    // Remove a node from the tree -- likely never used byt still useful
    public void removeNode(BigInteger prefix) {
        // Traverse the tree to the parent of the current node

        // Set the parent's new left or right child to the current node's child
    }

    // Traverse the tree and return the best match predicate
    public int getPredicate(BigInteger prefix) {
        // Traverse the tree and return the best match predicate

        return 0;
    }

    // Traverse the tree and return the list of paths to the curent node
    public ArrayList<TreeNode> getPath(BigInteger prefix) {
        ArrayList<TreeNode> path = new ArrayList<TreeNode>();
        // Traverse the tree and return the list of paths to the curent node

        return path;
    }
}

