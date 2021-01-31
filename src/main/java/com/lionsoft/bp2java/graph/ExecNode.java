package com.lionsoft.bp2java;

import java.util.*;

class ExecNode {
    protected BPNode node;
    ExecutionTree tree;
    BPConnector from;

    String java = "";

    public ExecNode(ExecutionTree tree, BPNode node, BPConnector from) {
        this.tree = tree;
        this.node = node;
        this.from = from;
    }

    public ExecNode(ExecutionTree tree, BPNode node) {
        this(tree, node, null);
    }

    public BPNode getNode() {
        return node;
    }

    public String toString() {
        return(node.toString());
    }

    public String toJava() {
        // Set subsequent source
        Set<RelationshipEdge> edges = tree.getOutgoinEdges(this);
        Iterator<RelationshipEdge> it = edges.iterator();
        while (it.hasNext()) {
            RelationshipEdge edge = it.next();
            //System.out.println(node.getName()+"  Connector "+ (edge.getConnector() != null ? edge.getConnector().getIndex() : "null"));
            if (edge.getConnector() != null) {
                int i = edge.getConnector().getIndex();
                ExecNode branch = (ExecNode) edge.getTarget();
                //System.out.println("  Getting Java from "+branch.toString());
                node.setExec(i, branch.toJava());
            }
        }

        return node.toJava();
    }

    public List<List<ExecNode>> getSequences() {
        List<List<ExecNode>> list = new ArrayList<List<ExecNode>>();

        Set<RelationshipEdge> edges = tree.getOutgoinEdges(this);

        if (edges.size() == 0) {
            List<ExecNode> sequence = new ArrayList<ExecNode>();
            //sequence.add(this);
            list.add(sequence);
        } else {
            // Iterate children
            Iterator<RelationshipEdge> it = edges.iterator();
            while (it.hasNext()) {
                RelationshipEdge edge = it.next();
                ExecNode child = (ExecNode) edge.getTarget();

                // Get children sequences
                List<List<ExecNode>> sequences = child.getSequences();

                // Prepend current node to all subsequences
                for (List<ExecNode> s: sequences) {
                    s.add(0, child);
                    list.add(s);
                }
            }
        }

        return list;
    }

    public void reduce() {
        // Reduce children
        Set<RelationshipEdge> edges = tree.getOutgoinEdges(this);
        Iterator<RelationshipEdge> it = edges.iterator();
        while (it.hasNext()) {
            RelationshipEdge edge = it.next();
            ExecNode child = (ExecNode) edge.getTarget();
            child.reduce();
        }

        // Get sequences of this node
        List<List<ExecNode>> sequences = getSequences();

        System.out.println("- "+node.toString());

        for (List<ExecNode> seq: sequences) {
            String s = "";

            for (ExecNode e: seq) {
                s += "  "+e.getNode().toString();
            }

            System.out.println(s);
        }

        // Search common tail
        BPNode start = findCommonTail(sequences);

        if (start != null)
            System.out.println("Found common tail starting at "+start.toString());
    }

    /**
     * Check if list of sequences have a common tail
     - 3 Compare integer
       5 Compare integer  6 Print String  8 Print String  10 Print String  2 Return
       10 Print String  2 Return
       4 Print String  10 Print String  2 Return
     Found common tail starting at 10 Print String
     */
    public BPNode findCommonTail(List<List<ExecNode>> sequences) {
        if (sequences.size() < 2)
            return null;

        // Reverse sequences
        for (List<ExecNode> seq: sequences) {
            Collections.reverse(seq);
        }

        // Get the first sequence as source
        List<ExecNode> masterSeq = sequences.get(0);
        int startPos = -1;

        for (int i=0; i<masterSeq.size(); i++) {
            ExecNode m = masterSeq.get(i);

            boolean all = true;

            for (int k=1; k<sequences.size(); k++) {
                List<ExecNode> targetSeq = sequences.get(k);

                if (targetSeq.size() - 1 < i) {
                    all = false;
                    break;
                }

                ExecNode t = targetSeq.get(i);

                if (m.getNode() != t.getNode()){
                    all = false;
                    break;
                }
            }

            if (all)
                startPos = i;
        }

        BPNode start = startPos > -1 ? masterSeq.get(startPos).getNode() : null;

        // Reverse sequences again
        for (List<ExecNode> seq: sequences) {
            Collections.reverse(seq);
        }

        return(start);
    }
};
