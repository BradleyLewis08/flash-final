package org.snlab.flash.ModelManager;

import java.math.BigInteger;

import jdd.bdd.BDD;

class TrieCode {
    int result;
    TrieCode left, right, skip; // left: 1, right: 0

    public TrieCode(int result) {
        this.result = result;
        this.left = this.right = null;
    }

    public TrieCode buildLeft(BDD bdd, int var) {
        if (this.left == null) {
            this.left = new TrieCode(bdd.ref(bdd.and(result, var)));
        }
        return this.left;
    }

    public TrieCode buildRight(BDD bdd, int nVar) {
        if (this.right == null) {
            this.right = new TrieCode(bdd.ref(bdd.and(result, nVar)));
        }
        return this.right;
    }

    public TrieCode buildSkip() {
        if (this.skip == null)
            this.skip = new TrieCode(BDDEngine.BDDTrue);
        return this.skip;
    }
}

/**
 * All BDD operations should be encapsulated in this Class.
 */
public final class BDDEngine {
    public final static int BDDFalse = 0;
    public final static int BDDTrue = 1;

    private final BDD bdd;
    private final int[] vars, nVars, svars, snVars;
    private final TrieCode dst, src;
    private int size;

    public double opCnt;

    // bdd variable array is from high bit to low bit
    public BDDEngine(int size) {
        this.opCnt = 0;
        this.size = size;
        this.bdd = new BDD(1000, 10000);
        this.vars = new int[size];
        this.nVars = new int[size];
        for (int i = 0; i < size; i++) {
            vars[i] = bdd.createVar();
            nVars[i] = bdd.ref(bdd.not(vars[i]));
        }
        this.svars = new int[8];
        this.snVars = new int[8];
        for (int i = 0; i < 8; i++) {
            svars[i] = bdd.createVar();
            snVars[i] = bdd.ref(bdd.not(svars[i]));
        }
        this.dst = new TrieCode(BDDTrue);
        this.src = new TrieCode(BDDTrue);
    }

    public void printVars() {
        for (int i = 0; i < size; i++) {
            System.out.println("var " + i + ": " + vars[i]);
            System.out.println("nvar " + i + ": " + nVars[i]);
        }
    }

    public int encodeIpv4(BigInteger ip, int prefix) {
        TrieCode ret = dst;
        for (int i = 0; i < prefix; i++) {
            if (ip.testBit(size - 1 - i)) {
                ret = ret.buildLeft(this.bdd, vars[i]);
            } else {
                ret = ret.buildRight(this.bdd, nVars[i]);
            }
        }
        return bdd.ref(ret.result);
    }

    public int encodeIpv4(BigInteger ip, int prefix, int srcIp, int srcSuffix) {
        TrieCode ret = dst;
        for (int i = 0; i < prefix; i++) {
            if (ip.testBit(size - 1 - i)) {
                ret = ret.buildLeft(this.bdd, vars[i]);
            } else {
                ret = ret.buildRight(this.bdd, nVars[i]);
            }
        }

        TrieCode tmp = src;
        for (int i = 0; i < srcSuffix; i++) {
            if (((srcIp >> i) & 1) == 1) {
                tmp = tmp.buildLeft(this.bdd, svars[i]);
            } else {
                tmp = tmp.buildRight(this.bdd, snVars[i]);
            }
        }

        return bdd.ref(bdd.and(ret.result, tmp.result));
    }

    public int convertDestToPredicate(BigInteger destIp) {
        TrieCode current = dst; // Start from the root of the source IP Trie
        TrieCode bestMatch = null; // To keep track of the best match

        // Iterate over each bit of the srcIp, starting from the highest order bit
        for (int i = size - 1; i >= 0; i--) {
            // Check if the current bit in srcIp is set (1) or not (0)
            if (destIp.testBit(i)) {
                // If the bit is set, move to the left child in the Trie
                if (current.left != null) {
                    current = current.left;
                    bestMatch = current; // Update best match
                } else {
                    break; // No further match possible
                }
            } else {
                // If the bit is not set, move to the right child in the Trie
                if (current.right != null) {
                    current = current.right;
                    bestMatch = current; // Update best match
                } else {
                    break; // No further match possible
                }
            }
        }

        // Return the TrieCode node that represents the best match
        return bestMatch.result;
    }

    public boolean checkIntersection(int a, int b) {
        // Calculate the intersection of the first predicate and the negation of the
        // second predicate
        int intersection = bdd.ref(bdd.and(a, b));

        // Check if the intersection is false
        boolean isContained = intersection == BDDTrue;

        // Don't forget to dereference BDD nodes when you're done with them to prevent
        // memory leaks
        bdd.deref(intersection);

        return isContained;
    }

    public static int refCnt = 0, defCnt = 0;
    public static double tot;

    public int not(int a) {
        opCnt++;
        return bdd.ref(bdd.not(a));
    }

    public int and(int a, int b) {
        opCnt++;
        return bdd.ref(bdd.and(a, b));
    }

    public int or(int a, int b) {
        opCnt++;
        return bdd.ref(bdd.or(a, b));
    }

    public int diff(int a, int b) {
        opCnt += 2;
        int tmp = bdd.ref(bdd.not(b));
        int ret = bdd.ref(bdd.and(a, tmp));
        bdd.deref(tmp);
        return ret;
    }

    public int ref(int a) {
        return bdd.ref(a);
    }

    public void deRef(int a) {
        bdd.deref(a);
    }

    public int xor(int a, int b) {
        opCnt++;
        return bdd.ref(bdd.xor(a, b));
    }

    public BDD getBdd() {
        return bdd;
    }
}