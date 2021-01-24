package com.lionsoft.bp2java;

import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

abstract class BPNode {

  public final static int ENTRY_POINT = 0;
  public final static int RETURN = 1;
  public final static int SEQUENCE = 2;
  public final static int BRANCH = 3;
  public final static int FUNCTION = 4;
  public final static int OPERATOR = 5;
  public final static int GET = 6;
  public final static int SET = 7;
  public final static int BLUEPRINT = 8;
  public final static int FOR_LOOP = 9;
  public final static int EXIT = 10;
  public final static int SWITCH_INTEGER = 12;
  public final static int WHILE_LOOP = 13;
  public final static int EVENT = 14;

  int id;
  int type;
  String blueprintId;

  String name;
  int nIn;
  int nOut;
  int nExecOut; // Available exits (includes not connected)

  String java = "";
  String nodePath = ".";
  String declare = null;

  List<BPConnector> input;
  List<BPConnector> output;
  List<String> exec;

  //public List<String> importList;
  //public List<String> jarList;
  public List<String> includeList;

  private Blueprint blueprint; // Blueprint fo this node

  // Auto-variables referenced by output connectors
  public List<Reference> referenceList;

  // Include an Object array for input values
  private boolean javaInputArray = false;

  Boolean compiled = false;         /* Already compiled (to avoid loops) */
  //int nRef; // Connected exec flows from previous nodes
  Block block;
  List<BPNode> previous = new ArrayList<BPNode>();
  public boolean startsBlock = false;

  public BPNode() {
    input = new ArrayList<BPConnector>();
    output = new ArrayList<BPConnector>();
    //importList = new ArrayList<String>(); // Now read from blueprint
    //jarList = new ArrayList<String>();
    referenceList = new ArrayList<Reference>();
    includeList = new ArrayList<String>();
    nExecOut = 0;
  }

  public BPNode(Blueprint blueprint, JSONObject jn) {
    this();
    set(jn);
    this.blueprint = blueprint;
  }

  public void setId (int id) {
    this.id = id;
  }

  public int getId () {
    return (id);
  }

  public void setType (int type) {
    this.type = type;
  }

  public int getType () {
    return (type);
  }

  public void setName (String name) {
    this.name = name;
  }

  public String getName () {
    return (name);
  }

  //public void ref() { nRef ++; }
  public int getRef() { return previous.size(); }

  public Block getBlock() { return block; }
  public void setBlock(Block block) { this.block = block; }
  public boolean inBlock() { return block != null; }

  public boolean branches() { return nExecOut > 1; }

  public String toString() { return name; }

  public void addPrevious(BPNode node) {
      if (!previous.contains(node))
        previous.add(node);
  }

  public List<BPNode> getPrevious() {
      return previous;
  }

  public boolean checkConnectors() {
    //System.out.println("Checking "+getName()+ " ("+nIn+" connectors)");

    for (int i=0; i<nIn; i++) {
      BPConnector c = getInputConnector(i);

      //System.out.println("  "+i+" "+c.getLabel()+" connected:"+c.isConnected()+" value:"+c.getValue());

      if (c.mustBeConnected() && !c.isConnected()) {
        this.blueprint.setResult(Code.ERR_MUST_CONNECT, "Connector '"+c.getLabel()+"' of node '"+getName()+"' should be connected.");
        return false;
      }
    }

    return true;
  }

  public String getInitialCode() {
    String autoCode = "";

    if (type != OPERATOR)
      autoCode = System.lineSeparator() + System.lineSeparator() + "// Node: "+getName() + System.lineSeparator() + System.lineSeparator();

    if (javaInputArray) {
      String a = "_"+getId()+"_in";

      autoCode += "Object[] "+a+" = new Object["+nIn+"];" + System.lineSeparator() + System.lineSeparator();

      for (int i=0; i<nIn; i++) {
        BPConnector c = getInputConnector(i);

        if (c.getExec())
          autoCode += a+"["+i+"] = null;"+ System.lineSeparator();
        else
          autoCode += a+"["+i+"] = "+c.getValueAsString()+";" + System.lineSeparator();
      }
    }

    //java = autoCode + java;


    return (autoCode);
  }

  public String getJava() {
    return (java);
  }

  public void setJava(String j) {
    java = j;
  }

  public String getDeclare() {
    return (declare);
  }

  // DEPRECATED
  /*
  public boolean getSubsequentCode_old () {
    for (int i=0; i<nOut; i++) {
      BPConnector c = getOutputConnector(i);

      if (c != null && c.getExec() && c.isConnected()) {
        Block b = c.getConnectedNode().compile();

        if (b == null)
          return false;

        exec.set(i, b.getSourceCode());
      }
    }

    return true;
  }
*/
  public boolean getSubsequentCode () {
    for (int i=0; i<nOut; i++) {
      BPConnector c = getOutputConnector(i);

      if (c != null && c.getExec() && c.isConnected()) {
        //System.out.println(name+" -> "+c.getConnectedNode().getName());
        if (c.getConnectedNode().getRef() == 1) {
            if (c.getConnectedNode().getBlock() == block)
                exec.set(i, c.getConnectedNode().toJava());
            else
                exec.set(i, c.getConnectedNode().getBlock().toJava());
        }
        else
            exec.set(i, "");
      }
    }

    return true;
  }

  public int getInputParamsCount() {
    return (nIn);
  }

  public int getOutputParamsCount() {
    return (nOut);
  }

  public boolean returns() {
    int n = 0;

    for (int i=0; i<nOut; i++) {
      BPConnector c = getOutputConnector(i);

      if (!c.getExec())
        n ++;
    }

    return (n > 0);
  }

  public int set (JSONObject jn) {
    //String path = ".";

    setId(((Long)jn.get("id")).intValue());
    setName((String) jn.get("name"));
    setType(((Long)jn.get("type")).intValue());

    //System.out.println("* Node "+name);

    if (jn.containsKey("data")) {
      JSONObject jdata = (JSONObject) jn.get("data");
      nodePath = jdata.containsKey("path") ? (String) jdata.get("path") : ".";
    }

    if (jn.containsKey("declare")) {
      declare = ((String) jn.get("declare")).replace("{node.id}", Integer.toString(id));
    }

    if (jn.containsKey("options")) {
      JSONObject joptions = (JSONObject) jn.get("options");

      // Create Object[] for input parameters
      javaInputArray = joptions.containsKey("javaInputArray") ? (Boolean) joptions.get("javaInputArray") : false;

      // Include section into program
      JSONArray jinclude = joptions.containsKey("include") ? (JSONArray) joptions.get("include") : null;

      for (int i=0; jinclude != null && i < jinclude.size(); i++) {
        System.out.println("Adding for inclusion: "+(String) jinclude.get(i));
        includeList.add((String) jinclude.get(i));
      }
    }

    //System.out.println("options.javaInputArray = "+javaInputArray);


    JSONArray jConnectorArray = (JSONArray) jn.get("input");

    nIn = jConnectorArray.size();

    for (int i = 0; i < nIn; i++) {
      JSONObject jc = (JSONObject) jConnectorArray.get(i);
      BPConnector c = new BPConnector(this, BPConnector.INPUT, jc);
      //c.setNode(this);
      input.add (c);
    }

    jConnectorArray = (JSONArray) jn.get("output");

    nOut = jConnectorArray.size();

    exec = new ArrayList<String>(nOut);

    for (int i = 0; i < nOut; i++) {
      JSONObject jc = (JSONObject) jConnectorArray.get(i);
      BPConnector c = new BPConnector(this, BPConnector.OUTPUT, jc);
      //c.setNode(this);

      if (c.getReference() != null) {
        referenceList.add(c.getReference());
      }

      output.add (c);

      // Initializze every exec item to null. Will be filled in BPNode.getSubsequentCode()
      exec.add(null);

      if (c.getExec())
        nExecOut ++;
    }

    if (jn.containsKey("java"))
      java = (String) jn.get("java");
    /*else
      System.out.println("Warning: node '"+getName()+"' has no Java code");*/

    return (0);
  }

  public BPConnector getInputConnector(int position) {
    return (input.get(position));
  }

  public BPConnector getOutputConnector(int position) {
    return (position < nOut ? output.get(position) : null);
  }

  public BPConnector getConnectorById(int id) {
    BPConnector c;

    for (int i = 0; i < input.size(); i++) {
      c = input.get(i);
      //System.out.println (getName()+"."+c.getId() + " " + c.getLabel() + " is "+id+" ? " + (c.getId() == id ? "YES" : "NO"));
      if (c.getId() == id)
        return c;
    }

    for (int i = 0; i < output.size(); i++) {
      c = output.get(i);
      //System.out.println (getName()+"."+c.getId() + " " + c.getLabel() + " is "+id+" ? " + (c.getId() == id ? "YES" : "NO"));
      if (c.getId() == id)
        return c;
    }

    return null;
  }

  /*public String getIncludedJava() {
    return includedJava != null ? includedJava : "";
  }*/
    public void propagateBlock() {
        //System.out.println("  Propagating block from "+toString());

        // Iterate all exits
        for (int i=0; i<nOut; i++) {
            BPConnector c = getOutputConnector(i);

            if (c != null && c.getExec() && c.isConnected()) {
                // Connected
                BPNode connected = c.getConnectedNode();

                // If following node doesn't start a block, propagate
                //if (!connected.inBlock() /*&& !connected.startsBlock*/) {
                if (!connected.inBlock() /*&& !connected.startsBlock*/) {
                    // Propagate
                    connected.setBlock(getBlock());
                    connected.propagateBlock();
                } else {
                    if (branches()) {
                        //block.setBranchNode(this);
                        // Start of a block
                        //System.out.println(connected.getName()+ " starts a block ref = "+connected.getRef());
                        connected.getBlock().setRoot(this.getBlock(), this);

                        //System.out.println("  Found branch "+connected.getBlock().toString());

                        if (connected.getRef() == 1) {
                            // One only input. the block follows current
                            //System.out.println(block.toString()+" -> "+connected.getBlock().toString());
                            getBlock().addBranch(connected.getBlock());
                        }
                        else {
                                //getBlock().setNext(connected.getBlock());
                                connected.getBlock().addIncoming(this.block);

                        }
                    } else {
                        if (this.getBlock().getRoot() != null) {
                            //System.out.println(" Branch "+this.getBlock().getRootNode().getName()+" type = "+this.getBlock().getRootNode().getType());
                            if (this.getBlock().getRootNode().getType() == BPNode.SEQUENCE)
                                this.getBlock().setNext(connected.getBlock());
                            else {
                                /*
                                if (connected.getBlock().getPrev() != null && connected.getBlock().getPrev().isDescendantOf(block)) {
                                    connected.getBlock().getPrev().setNext(null);
                                }

                                if (!this.block.followedByRecurs(connected.getBlock()))
                                    this.getBlock().getRoot().setNext(connected.getBlock());
                                    */
                                connected.getBlock().addIncoming(this.block.getRoot() != null ? this.block.getRoot() : this.block);

                            }
                        }
                    }
                }
            }
        }
    }

    public String translate() { return ""; }

    // DEPRECATED
    public Block compile() {
        Block block = new Block(this);
        return(compile(block));
    }

    // DEPRECATED
    public Block compile(Block block) {
        if (compiled)
            return(block);

        compiled = true;

        //System.out.println("Compiling "+blueprint.getName()+"."+this.name);

        // Check connectors
        if (!checkConnectors())
          return null;

        // Compile nodes backwards where input come from (eg. operators)
        for (int i=0; i<nIn; i++) {
            BPConnector c = getInputConnector(i);

            if (!c.getExec() && c.isConnected()) {
                if (c.getConnected().getNode().compile() == null)
                    return null;
            }
        }

        block.addSourceCode(getInitialCode());
        //System.out.println("Initial code: "+block.getSourceCode());
        block.addSourceCode(translate());
        //System.out.println("Translated: "+block.getSourceCode());

        return(block);
    }

    public String toJava() {
        //System.out.println("Translating "+getName());
        String actualSource = getInitialCode();
        actualSource += java; // Add original code to be processed

        // Set java code on exec exits
        if (!getSubsequentCode())
            return null;

        actualSource = actualSource.replace("{node.id}", Integer.toString(getId()));
        actualSource = actualSource.replace("{count.in}", Integer.toString(nIn));

        // Replace code of input values
        for (int i=0; i<nIn; i++) {
          BPConnector c = getInputConnector(i);

          if (!c.getExec()) {
            //System.out.println("  Input data "+i);
            actualSource = actualSource.replace("in{"+i+"}", c.getValueAsString());
          }
        }

        // Replace code of output variables (out{2} = 5 -> _code_6 = 5;)
        for (int i=0; i<nOut; i++) {
          BPConnector c = getOutputConnector(i);

          if (!c.getExec()) {
            actualSource = actualSource.replace("out{"+i+"}", c.getValueAsString());
          }
        }

        actualSource += System.lineSeparator();

        if (nExecOut == 1) {
          if (exec.get(0) != null)
            actualSource += exec.get(0);
            //System.out.println(" exec.get(0) = "+ exec.get(0));

        }
        else {
          for (int i=0; i<nOut; i++) {
            BPConnector c = getOutputConnector(i);
            //System.out.println("Connector "+c.getNode().getName()+"."+c.getLabel()+" "+ (c.isConnected() ? "[*]" : "[ ]") +" -> "+exec.get(i));

            if (c.getExec()) {
              actualSource = actualSource.replace("exec{"+i+"}", c.isConnected() ? exec.get(i) : "");
            }
          }
        }

        return actualSource;
    }
};
