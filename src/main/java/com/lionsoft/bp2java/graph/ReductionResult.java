package com.lionsoft.bp2java;

import java.util.*;

class ReductionResult {
    ExecNode newParent;
    List<ExecNode> startNodes;
    boolean reducted = false;

    public ReductionResult() {
        reducted = false;
    }

    public ReductionResult(ExecNode newParent, List<ExecNode> startNodes) {
        this.newParent = newParent;
        this.startNodes = startNodes;
        reducted = true;
    }

    public boolean hasReduction() {
        return reducted;
    }

    public ExecNode getNewParent() {
        return newParent;
    }

    public List<ExecNode> getStartNodes() {
        return startNodes;
    }
};
