package com.lionsoft.bp2java;

public class Block {

    private static int sequence = 1;

    private int id;
    private String sourceCode = "";
    private BPNode start;
    private Block next;
    private int level = 1;

    public Block() {
        setId(createNewId());
    }

    public Block(BPNode node) {
        this();
        this.start = node;
        node.setBlock(this);
    }

    public int createNewId() { return sequence ++; }
    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }

    public int getLevel() { return this.level; }
    public void setLevel(int level) { this.level = level; }

    public String getSourceCode() { return this.sourceCode; }
    public void setSourceCode(String s) { this.sourceCode = s; }
    public void addSourceCode(String s) { this.sourceCode += s; }

    public void append(Block b) { addSourceCode(b.getSourceCode()); }

    public BPNode getStart() { return start; }

    public String toString() { return "Block [id="+id+", level="+level+", start="+start.getName()+"]"; }
};
