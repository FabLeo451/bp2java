package com.lionsoft.bp2java;

import java.util.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;


class RelationshipEdge extends DefaultEdge {
    public static final int BRANCH = 0;
    public static final int FOLLOWS = 1;

    String label;
    BPConnector from;
    int type = BRANCH;

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

    public void setLabel(String label) {
        this.label = label;
    }

    public BPConnector getConnector() {
        return from;
    }

    public Object getTarget() {
        return(super.getTarget());
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isBranch() {
        return type == BRANCH;
    }

    public boolean isFollows() {
        return type == FOLLOWS;
    }

    @Override
    public String toString() {
        return label;
    }
};
