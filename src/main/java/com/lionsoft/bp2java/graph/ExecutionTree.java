package com.lionsoft.bp2java;

import java.util.*;
import java.io.*;

import java.awt.image.BufferedImage;
import java.awt.Color;

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

    DefaultDirectedGraph<ExecNode, RelationshipEdge> getGraph() {
      return tree;
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
      reduce();
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

    /**
     * Reduce the tree
     */
    public void reduce() {
        int n = 0;

        saveAsImage("/media/data/Source/JLogic-all/tree-before.png");

        while(true) {
            ReductionResult result = root.reduce();

            if (!result.hasReduction())
                break;

            n ++;

            // Detach all the common tails
            for (ExecNode node: result.getStartNodes()) {
                detach(node);
            }

            // Attach new parent node to one tail with an edge of type FOLLOWS
            RelationshipEdge followsEdge = new RelationshipEdge();
            followsEdge.setType(RelationshipEdge.FOLLOWS);
            followsEdge.setLabel("FOLLOWS");
            tree.addEdge(result.getNewParent(), result.getStartNodes().get(0), followsEdge);
            System.out.println(result.getNewParent().getNode().toString()+" -> " + result.getStartNodes().get(0).getNode().toString());
        }

        System.out.println("Reductions: " + n);
        saveAsImage("/media/data/Source/JLogic-all/tree.png");
    }

    /**
     * Remove all incoming edges of a node
     */
    public void detach(ExecNode node) {
        Set<RelationshipEdge> edges = tree.incomingEdgesOf(node);
        Iterator<RelationshipEdge> it = edges.iterator();

        // Avoid java.util.ConcurrentModificationException
        List<RelationshipEdge> toBeremoved = new ArrayList<RelationshipEdge>();

        while (it.hasNext())
            toBeremoved.add(it.next());

        for (RelationshipEdge e: toBeremoved)
            tree.removeEdge(e);
    }

    public boolean saveAsImage(String imageFile) {
        JGraphXAdapter<ExecNode, RelationshipEdge> graphAdapter = new JGraphXAdapter<ExecNode, RelationshipEdge>(tree);
        mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
        ((mxHierarchicalLayout)layout).setOrientation(SwingConstants.WEST);
        layout.execute(graphAdapter.getDefaultParent());

        BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        File file = new File(imageFile);

        try {
            ImageIO.write(image, "PNG", file);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
        //ListenableGraph<BPNode, DefaultEdge> g = new DefaultListenableGraph<>(graph);

        return true;
    }
};
