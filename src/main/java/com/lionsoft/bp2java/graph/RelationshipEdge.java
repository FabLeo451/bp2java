package com.lionsoft.bp2java;

import java.util.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;


class RelationshipEdge extends DefaultEdge {
    private String label;
    private BPConnector from;

    public RelationshipEdge(String label, BPConnector from) {
        super();
        this.label = label;
        this.from = from;
    }

    public RelationshipEdge(String label) {
        super();
        this.label = label;
    }

    public RelationshipEdge(BPConnector from) {
        super();
        this.from = from;
    }

    public RelationshipEdge() {
        this("");
    }

    public String getLabel() {
        return label;
    }

    public BPConnector getConnector() {
        return from;
    }

    public Object getTarget() {
        return(super.getTarget());
    }

    @Override
    public String toString() {
        return label;
    }
};
