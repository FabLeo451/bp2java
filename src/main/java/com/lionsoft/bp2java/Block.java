package com.lionsoft.bp2java;

public class Block {

    private static int sequence = 1;

    private int id;
    private String sourceCode;

    public Block() {
        setId(createNewId());
    }

    public int createNewId() { return sequence ++; }
    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }

    public String getSourceCode() { return this.sourceCode; }
    public void setSourceCode(String s) { this.sourceCode = s; }

};
