package com.lionsoft.bp2java;

import java.util.*;

public class NullNode extends BPNode {

    public NullNode() {
        super();
        setType (NULL_NODE);

        nIn = 1;

        BPConnector c = new BPConnector();
        c.setExec(true);
        c.setNode(this);
        input.add(c);
    }

    @Override
    public String toJava() {
        return("");
    }

    public BPConnector getInputConnector() {
        return(input.get(0));
    }
};
