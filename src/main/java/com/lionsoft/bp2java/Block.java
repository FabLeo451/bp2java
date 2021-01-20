package com.lionsoft.bp2java;

public class Block {

    private static int sequence = 1;

    private int id;
    private String sourceCode = "";
    private BPNode start;

    public Block() {
        setId(createNewId());
    }

    public Block(BPNode node) {
        this();
        this.start = node;
    }

    public int createNewId() { return sequence ++; }
    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }

    public String getSourceCode() { return this.sourceCode; }
    public void setSourceCode(String s) { this.sourceCode = s; }
    public void addSourceCode(String s) { this.sourceCode += s; }

    public void append(Block b) { addSourceCode(b.getSourceCode()); }
};
