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
        /*
        String actualSource = node.getInitialCode() + node.getJava();

        actualSource = actualSource.replace("{node.id}", Integer.toString(node.getId()));
        actualSource = actualSource.replace("{count.in}", Integer.toString(node.getInputParamsCount()));

        // Replace code of input values
        for (int i=0; i<node.getInputParamsCount(); i++) {
          BPConnector c = node.getInputConnector(i);

          if (c.isData()) {
            //System.out.println("  Input data "+i);
            actualSource = actualSource.replace("in{"+i+"}", c.getValueAsString());
          }
        }

        // Replace code of output variables (out{2} = 5 -> _code_6 = 5;)
        for (int i=0; i<node.getOutputParamsCount(); i++) {
          BPConnector c = node.getOutputConnector(i);

          if (c.isData()) {
            //System.out.println("Node "+name+" out{"+i+"}");
            actualSource = actualSource.replace("out{"+i+"}", c.getValueAsString());
          }
        }

        actualSource += System.lineSeparator();

        if (node.getExitsCount() == 1)
          actualSource += "exec{0}";

        for (int i=0; i<node.getOutputParamsCount(); i++) {
            BPConnector c = node.getOutputConnector(i);
            //System.out.println("Connector "+c.getNode().getName()+"."+c.getLabel()+" "+ (c.isConnected() ? "[*]" : "[ ]") +" -> "+exec.get(i));

            if (c.isExec()) {
              //actualSource = actualSource.replace("exec{"+i+"}", c.isConnected() ? exec.get(i) : "");
            }
        }
*/
        // Get subsequent code
        /*
        for (int i=0; i<node.getOutputParamsCount(); i++) {
          BPConnector c = node.getOutputConnector(i);

          if (c != null && c.isExec() && c.isConnected()) {
                //BPNode connected = c.getConnectedNode();
                ExecNode branch = null;

                Set<RelationshipEdge> edges = tree.getOutgoinEdges(this);
                Iterator<RelationshipEdge> it = edges.iterator();
                while (it.hasNext()) {
                    if (it.next().getConnector() == c) {
                        branch = (ExecNode) it.next().getTarget();
                        break;
                    }
                }

                if (branch != null)
                  node.setExec(i, branch.toJava());
          }
      }*/


        Set<RelationshipEdge> edges = tree.getOutgoinEdges(this);
        Iterator<RelationshipEdge> it = edges.iterator();
        while (it.hasNext()) {
            RelationshipEdge edge = it.next();
            System.out.println(node.getName()+"  Connector "+ (edge.getConnector() != null ? edge.getConnector().getIndex() : "null"));
            if (edge.getConnector() != null) {
                int i = edge.getConnector().getIndex();
                ExecNode branch = (ExecNode) edge.getTarget();
                System.out.println("  Getting Java from "+branch.toString());
                node.setExec(i, branch.toJava());
            }
        }

        return node.toJava();
    }

};
