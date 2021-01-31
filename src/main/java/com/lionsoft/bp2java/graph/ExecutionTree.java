package com.lionsoft.bp2java;

import java.util.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import com.mxgraph.layout.*;
import com.mxgraph.util.mxCellRenderer;
import org.jgrapht.ext.*;
import javax.imageio.ImageIO;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import javax.swing.SwingConstants;

class ExecutionTree {
    ExecNode root;
    protected DefaultDirectedGraph<ExecNode, RelationshipEdge> tree;
    DefaultDirectedGraph<BPNode, RelationshipEdge> graph;
    int indent = 0;
/*
    public ExecutionTree(DefaultDirectedGraph<BPNode, RelationshipEdge> graph, BPNode startNode) {
      fromGraph(graph, startNode);
    }
*/
    public ExecutionTree(Blueprint blueprint, BPNode startNode) {
      fromBlueprint(blueprint, startNode);
    }

    ExecNode getRoot() {
      return root;
    }
/*
    public void fromGraph(DefaultDirectedGraph<BPNode, RelationshipEdge> graph, BPNode startNode) {
      tree = new DefaultDirectedGraph<>(RelationshipEdge.class);
      root = new ExecNode(this, startNode);
      tree.addVertex(root);
      this.graph = graph;

      goGraph(root);
    }

    public void goGraph(ExecNode start) {
        Set<RelationshipEdge> edges = graph.outgoingEdgesOf(start.getNode());
        Iterator<RelationshipEdge> it = edges.iterator();

        if (edges.size() == 0)
            return;

        while (it.hasNext()) {
            RelationshipEdge edge = it.next();
            ExecNode node = new ExecNode(this, (BPNode) edge.getTarget(), edge.getConnector());
            tree.addVertex(node);
            tree.addEdge(start, node, new RelationshipEdge(edge.getConnector()));
            go(node);
        }
    }
*/
    public void fromBlueprint(Blueprint blueprint, BPNode startNode) {
      tree = new DefaultDirectedGraph<>(RelationshipEdge.class);
      root = new ExecNode(this, startNode);
      tree.addVertex(root);
      this.graph = graph;

      go(root);
    }

    public void go(ExecNode start) {
        List<BPConnector> exec = start.getNode().getExecConnectors();

        if (exec.size() == 0)
            return;

        for (BPConnector c: exec) {
            ExecNode node = new ExecNode(this, c.getConnectedNode(), c);
            tree.addVertex(node);
            tree.addEdge(start, node, new RelationshipEdge(c));
            go(node);
        }
    }

    public Set<RelationshipEdge> getOutgoinEdges(ExecNode node) {
        return(tree.outgoingEdgesOf(node));
    }

    public String toJava() {
        return(root.toJava());
    }

    public void print() {
        indent = 0;
        print(root);
    }

    public void print(ExecNode node) {
        String spaces = "";

        for (int i=0; i<indent; i++)
          spaces += " ";

        System.out.println(spaces + "- "+node.toString());

        Set<RelationshipEdge> edges = tree.outgoingEdgesOf(node);
        Iterator<RelationshipEdge> it = edges.iterator();

        if (edges.size() == 1) {
            print((ExecNode) it.next().getTarget());
        } else if (edges.size() > 1) {
            indent += 4;
            while (it.hasNext()) {
                print((ExecNode) it.next().getTarget());
            }
            indent -= 4;
        }
    }
};
