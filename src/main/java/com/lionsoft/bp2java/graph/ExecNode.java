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
        String follows = "";
        Set<RelationshipEdge> edges = tree.getOutgoinEdges(this);
        Iterator<RelationshipEdge> it = edges.iterator();

        while (it.hasNext()) {
            RelationshipEdge edge = it.next();
            //System.out.println(node.getName()+"  Connector "+ (edge.getConnector() != null ? edge.getConnector().getIndex() : "null"));
            ExecNode branch = (ExecNode) edge.getTarget();

            switch (edge.getType()) {
                case RelationshipEdge.BRANCH:
                    if (edge.getConnector() != null) {
                        int i = edge.getConnector().getIndex();
                        //System.out.println("  Getting Java from "+branch.toString());
                        node.setExec(i, branch.toJava());
                    }
                    break;

                case RelationshipEdge.FOLLOWS:
                    //node.setExec(0, "");
                    follows = branch.toJava();
                    break;

                default:
                    break;
            }
        }

        return(node.toJava() + follows);
    }

    public RelationshipEdge getEdgeByConnector(BPConnector c) {
        Set<RelationshipEdge> edges = tree.getOutgoinEdges(this);
        Iterator<RelationshipEdge> it = edges.iterator();

        while (it.hasNext()) {
            RelationshipEdge edge = it.next();

            if (edge.getConnector() == c)
                return edge;
        }

        return null;
    }

    /**
     * Reduce the subtree starting at this node
     */
    public ReductionResult reduce() {
        // Nodes with an iteration connector (For and While loop) must be treated separately
        if (node.getType() == BPNode.WHILE_LOOP) {
            RelationshipEdge edgeIter = getEdgeByConnector(node.getOutputConnector(0));
            ExecNode startIter = (ExecNode) edgeIter.getTarget();
            ReductionResult r = startIter.reduce();

            if (r.hasReduction())
                return r;

            RelationshipEdge edgeCompl = getEdgeByConnector(node.getOutputConnector(1));

            if (edgeCompl != null) {
                ExecNode startCompl = (ExecNode) edgeCompl.getTarget();
                //return(completedIter.reduce());

                tree.detach(startCompl);
                tree.attachFollows(this, startCompl);
            }
        } else if (node.getType() == BPNode.FOR_LOOP) {

        } else {
            // Reduce children
            Set<RelationshipEdge> edges = tree.getOutgoinEdges(this);
            Iterator<RelationshipEdge> it = edges.iterator();

            while (it.hasNext()) {
                RelationshipEdge edge = it.next();
                ExecNode child = (ExecNode) edge.getTarget();
                ReductionResult r = child.reduce();

                if (r.hasReduction())
                    return r;
            }
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
        List<ExecNode> startNodes = findCommonTail(sequences);

        if (startNodes != null && startNodes.size() > 0) {
            System.out.println("  Found common tail starting at "+startNodes.get(0).getNode().toString());

            return(new ReductionResult(this, startNodes));
        }

        return(new ReductionResult());
    }

    public RelationshipEdge getFollowsEdge() {
        Set<RelationshipEdge> edges = tree.getOutgoinEdges(this);
        Iterator<RelationshipEdge> it = edges.iterator();

        while (it.hasNext()) {
            RelationshipEdge e = it.next();

            if (e.isFollows())
                return e;
        }

        return null;
    }

    public boolean hasFollows() {
        return(getFollowsEdge() != null);
    }

    /**
     * Get the sequences of all children
     */
    public List<List<ExecNode>> getSequences() {
        List<List<ExecNode>> list = new ArrayList<List<ExecNode>>();

        Set<RelationshipEdge> edges = tree.getOutgoinEdges(this);

        if (edges.size() == 0) {
            // Return an empty list
            List<ExecNode> sequence = new ArrayList<ExecNode>();
            list.add(sequence);
        } else {
            // If this node has a 'follows' edge (it's been already reduced) use only the follows edge
            if (hasFollows()) {
                ExecNode startFollows = (ExecNode) getFollowsEdge().getTarget();
                List<List<ExecNode>> sequences = startFollows.getSequences();
                for (List<ExecNode> s: sequences) {
                    s.add(0, startFollows);
                    list.add(s);
                }
            } else {
                // Iterate children
                Iterator<RelationshipEdge> it = edges.iterator();
                while (it.hasNext()) {
                    RelationshipEdge edge = it.next();
                    ExecNode child = (ExecNode) edge.getTarget();

                    // Get children sequences
                    List<List<ExecNode>> sequences = child.getSequences();

                    // Prepend the starting node to all subsequences
                    for (List<ExecNode> s: sequences) {
                        s.add(0, child);
                        list.add(s);
                    }
                }
            }
        }

        return list;
    }

    /**
     * Check if list of sequences have a common tail
     - Node: 3 Compare integer
       5 Compare integer  6 Print String  8 Print String  10 Print String  2 Return
       10 Print String  2 Return
       4 Print String  10 Print String  2 Return
     Found common tail starting at 10 Print String
     */
    public List<ExecNode> findCommonTail(List<List<ExecNode>> sequences) {
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

            // Check node m in all sequences at position i

            boolean all = true;

            for (int k=1; k<sequences.size(); k++) {
                List<ExecNode> targetSeq = sequences.get(k);

                if (targetSeq.size() - 1 < i) {
                    all = false;
                    break;
                }

                ExecNode t = targetSeq.get(i);

                System.out.println(m.getNode() +" "+ t.getNode() +": "+(m.getNode() != t.getNode()));

                if (m.getNode() != t.getNode()){
                    all = false;
                    break;
                }
            }

            if (all)
                startPos = i;

            if (startPos == -1)
                break;
        }

        List<ExecNode> result = new ArrayList<ExecNode>();

        if (startPos > -1) {
            for (List<ExecNode> seq: sequences) {
                result.add(seq.get(startPos));
            }
        }

        //BPNode start = startPos > -1 ? masterSeq.get(startPos).getNode() : null;

        // Reverse sequences again
        for (List<ExecNode> seq: sequences) {
            Collections.reverse(seq);
        }

        return(result);
    }
};
