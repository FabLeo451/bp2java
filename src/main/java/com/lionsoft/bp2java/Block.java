package com.lionsoft.bp2java;

import java.util.*;

public class Block {

    private static int sequence = 1;

    private int id;
    private String sourceCode = "";
    private BPNode start;
    private Block root, next;
    private List<Block> branches = new ArrayList<Block>();

    public Block() {
        setId(createNewId());
        root = next = null;
    }

    public Block(BPNode node) {
        this();
        this.start = node;
        node.setBlock(this);
        node.startsBlock = true;
    }

    public Block(BPNode node, Block root) {
        this(node);
        setRoot(root);
    }

    public int createNewId() { return sequence ++; }
    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }

    public String getSourceCode() { return this.sourceCode; }
    public void setSourceCode(String s) { this.sourceCode = s; }
    public void addSourceCode(String s) { this.sourceCode += s; }

    public Block getRoot() { return root; }
    public void setRoot(Block block) {
        this.root = block;

    }

    public Block getNext() { return next; }
    public void setNext(Block block) { this.next = block; }

    public List<Block> getBranches() { return branches; }
    public void addBranch(Block block) {
        if (!branches.contains(block))
            branches.add(block); 
    }

    public void append(Block b) { addSourceCode(b.getSourceCode()); }

    public BPNode getStart() { return start; }

    public String toString() { return "Block [id="+id+", start="+start.getName()+", root="+(root != null ? root.start.getName() : "")+"]"; }
};
