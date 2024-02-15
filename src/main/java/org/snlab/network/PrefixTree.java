package org.snlab.network;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

class PredicateReference {
    private int predicate;

    public PredicateReference(int predicate) {
        this.predicate = predicate;
    }

    public int getPredicate() {
        return this.predicate;
    }

    public void setPredicate(int predicate) {
        this.predicate = predicate;
    }
}

class TreeNode {
    private BigInteger prefix;
    private PredicateReference predicate;
    public TreeNode left;
    public TreeNode right;

    public TreeNode(BigInteger prefix, PredicateReference predicate) {
        this.prefix = prefix;
        this.predicate = predicate;
        this.left = null;
        this.right = null;
    }

    public TreeNode(BigInteger prefix, PredicateReference predicate, TreeNode left, TreeNode right) {
        this.prefix = prefix;
        this.predicate = predicate;
        this.left = left;
        this.right = right;
    }

    // Returns the fill 32 bit representation of the prefix
    public BigInteger getPrefix() {
        return this.prefix;
    }

    public BigInteger getPrefixAllBits() {
        return this.prefix.shiftRight(32 - this.prefix.bitLength());
    }

    public int getPredicate() {
        return this.predicate.getPredicate();
    }

    public void setPredicate(int predicate) {
        this.predicate.setPredicate(predicate);
    }
}

public class PrefixTree {
    private TreeNode root;
    private HashMap<Integer, PredicateReference> predicatesToReferences;

    // Constructor
    public PrefixTree(int defaultPredicate) {
        PredicateReference predicate = new PredicateReference(defaultPredicate);
        this.root = new TreeNode(BigInteger.ZERO, predicate);
    }

    public void updatePredicateReference(int predicate, int newPredicate) {
        if (!this.predicatesToReferences.containsKey(predicate)) {
            PredicateReference reference = new PredicateReference(predicate);
            this.predicatesToReferences.put(predicate, reference);
        } else {
            PredicateReference reference = this.predicatesToReferences.get(predicate);
            reference.setPredicate(newPredicate);
            predicatesToReferences.remove(predicate);
            predicatesToReferences.put(newPredicate, reference);
        }
    }

    // Update an existing node of the tree, or add a new one if it doesn't exist
    public boolean updateNode(BigInteger prefix, int predicate) {
        // Prefix is too long
        if (prefix.bitLength() > 32) {
            return false;
        }
        
        // Preprocess the prefix to be 32 bits
        prefix = prefix.shiftLeft(32 - prefix.bitLength());

        // Traverse to the current best parent
        ArrayList<TreeNode> path = this.getPath(prefix);
        TreeNode parent = path.get(path.size() - 1);

        // Node already exists, update instead
        if (parent.getPrefixAllBits().equals(prefix)) {
            parent.setPredicate(predicate);
            return false;
        }

        // Set the node to the left or right of the parent based on the next bit
        PredicateReference predicateReference;
        // if predicate not in the map
        if (!this.predicatesToReferences.containsKey(predicate)) {
            predicateReference = new PredicateReference(predicate);
            this.predicatesToReferences.put(predicate, predicateReference);
        } else {
            predicateReference = this.predicatesToReferences.get(predicate);
        }

        TreeNode child = new TreeNode(prefix, predicateReference);

        this.insertNode(child, parent);

        return true;
    }

    // Remove a node from the tree -- likely never used byt still useful
    public void removeNode(BigInteger prefix) {
        // Traverse the tree to the parent of the current node

        // Set the parent's new left or right child to the current node's child
    }

    // Traverse the tree and return the best match predicate
    public int getPredicate(BigInteger prefix) {
        // Preprocessing
        prefix = prefix.shiftLeft(32 - prefix.bitLength());
        
        // Traverse the tree and return the best match predicate
        TreeNode temp = this.root;
        int predicateMatch = temp.getPredicate();

        for (int i = 31; i >= 0; i--) {
            // current node doesn't exist or doesn't match
            if (temp == null || !temp.getPrefixAllBits().and(prefix).equals(temp.getPrefixAllBits())) {
                return predicateMatch;
            }

            // Update the best match predicate
            predicateMatch = temp.getPredicate();

            if (prefix.testBit(i)) {
                temp = temp.right;
            } else {
                temp = temp.left;
            }
        }

        return predicateMatch;
    }

    // Traverse the tree and return the list of paths to the curent node
    public ArrayList<TreeNode> getPath(BigInteger prefix) {
        ArrayList<TreeNode> path = new ArrayList<TreeNode>();
        prefix = prefix.shiftLeft(32 - prefix.bitLength());

        TreeNode temp = this.root;

        for (int i = 31; i >= 0; i--) {
            if (temp == null) {
                return path;
            }
            
            path.add(temp);

            if (prefix.testBit(i)) {
                temp = temp.right;
            } else {
                temp = temp.left;
            }
        }

        
        return path;
    }

    // 0 is left, 1 is right
    private void insertNode(TreeNode child, TreeNode parent) {
        // Get the next bit of the parent prefix for the direction of the child insertion
        boolean insertionDirection = child.getPrefix().testBit(parent.getPrefix().bitLength());
        
        // If right insertion
        if (insertionDirection) {
            // Check for children
            if (parent.right == null) {
                parent.right = child;
                return;
            }
            
            // Make the proper insertion
            boolean childDirection = parent.right.getPrefix().testBit(child.getPrefix().bitLength());

            if (childDirection) {
                child.right = parent.right;
                parent.right = child;
            } else {
                child.left = parent.right;
                parent.right = child;
            }
        } else {
            // Check for children
            if (parent.left == null) {
                parent.left = child;
                return;
            }

            // Make the proper insertion
            boolean childDirection = parent.left.getPrefix().testBit(child.getPrefix().bitLength());

            if (childDirection) {
                child.right = parent.left;
                parent.left = child;
            } else {
                child.left = parent.left;
                parent.left = child;
            }

        }
    }
}

