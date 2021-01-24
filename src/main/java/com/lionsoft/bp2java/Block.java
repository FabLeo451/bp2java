package com.lionsoft.bp2java;

import java.util.*;

public class Block {

    private static int sequence = 1;

    private int id;
    private String sourceCode = "";
    private BPNode start, branchNode;
    private Block root, next, prev;
    private List<Block> incoming = new ArrayList<Block>();
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
    /*
        public Block(BPNode node, Block root) {
            this(node);
            setRoot(root);
        }*/

    public int createNewId() {
        return sequence++;
    }
    public int getId() {
        return this.id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getSourceCode() {
        return this.sourceCode;
    }
    public void setSourceCode(String s) {
        this.sourceCode = s;
    }
    public void addSourceCode(String s) {
        this.sourceCode += s;
    }

    public Block getRoot() {
        return root;
    }
    public void setRoot(Block block, BPNode n) {
        this.root = block;
        branchNode = n;
    }

    public BPNode getRootNode() {
        return branchNode;
    }
    public void setRootNode(BPNode n) {
        this.branchNode = n;
    }

    public Block getNext() {
        return next;
    }

    public void setNext(Block block) {
        this.next = block;
        block.setPrev(this);
        //System.out.println(toString() + " (next)-> " + block.toString());
    }

    public Block getPrev() {
        return prev;
    }

    public void setPrev(Block block) {
        this.prev = block;
        //System.out.println(toString() + " (next)-> " + block.toString());
    }

    public boolean followedBy(Block block) {
        return(next == block);
    }

    public boolean followedByRecurs(Block block) {
        if (followedBy(block))
            return true;

        if (root == null)
            return false;

        return(root.followedByRecurs(block));
    }

    public boolean isDescendantOf(Block block) {
        if (root == block)
            return true;

        if (root == null)
            return false;

        return(root.isDescendantOf(block));
    }

    public List < Block > getBranches() {
        return branches;
    }

    public void addBranch(Block block) {
        if (!branches.contains(block))
            branches.add(block);
    }

    public List < Block > getIncoming() {
        return incoming;
    }

    public void addIncoming(Block block) {
        if (!incoming.contains(block))
            incoming.add(block);
    }

    public void append(Block b) {
        addSourceCode(b.getSourceCode());
    }

    public BPNode getStart() {
        return start;
    }

    public String toString() {
        return "Block [id=" + id + ", start=" + start.getName() + ", from=" + (branchNode != null ? branchNode.getName() : "") + ", root="+(root != null ? root.getId() : "" )+"]";
    }

    public String toJava() {
        if (start == null)
            return "";

        //System.out.println("Translating " + toString());
        sourceCode = start.toJava();

        if (next != null) {
            //System.out.println("Followed by " + next.toString());
            sourceCode += next.getStart().toJava();
        }

        return (sourceCode);
    }
};
