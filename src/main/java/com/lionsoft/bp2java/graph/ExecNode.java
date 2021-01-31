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

        if (edges.size() == 1) {
            List<ExecNode> sequence = new ArrayList<ExecNode>();
            sequence.add(this);
            list.add(sequence);
        } else {
            Iterator<RelationshipEdge> it = edges.iterator();
            while (it.hasNext()) {
                List<ExecNode> sequence = new ArrayList<ExecNode>();
                RelationshipEdge edge = it.next();
                ExecNode child = (ExecNode) edge.getTarget();
            }
        }

        return list;
    }

};
