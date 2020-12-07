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
  int nExec;

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
  //String message;

  public BPNode() {
    input = new ArrayList<BPConnector>();
    output = new ArrayList<BPConnector>();
    //importList = new ArrayList<String>(); // Now read from blueprint
    //jarList = new ArrayList<String>();
    referenceList = new ArrayList<Reference>();
    includeList = new ArrayList<String>();
    nExec = 0;
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
/*
  public String getMessage () {
    return (message);
  }*/

  public boolean checkConnectors () {
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

  public String initCode() {
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

    java = autoCode + java;


    return (java);
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

  public boolean getSubsequentCode () {
    for (int i=0; i<nOut; i++) {
      BPConnector c = getOutputConnector(i);

      if (c != null && c.getExec() && c.isConnected()) {
        String s = c.getConnectedNode().compile();
        
        if (s == null)
          return false;
          
        exec.set(i, s);
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
        /*String filename = path+"/"+(String)jinclude.get(i);

        System.out.println("Including "+filename);

        try {
          includedJava = new String (Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
          System.err.println("Can't include "+filename+": "+e.getMessage());
          includedJava = null;
        }*/
      }
    }

    //System.out.println("options.javaInputArray = "+javaInputArray);
/* 
    // Now read from blueprint
    if (jn.containsKey("import")) {
      JSONArray ja = (JSONArray) jn.get("import");

      for (int i=0; i < ja.size(); i++) {
        importList.add((String)ja.get(i));
      }
    }
*/

    /*
    // Done by JLogic when compiling
    if (jn.containsKey("jar")) {
      JSONArray ja = (JSONArray) jn.get("jar");

      // If path is present maybe we have "jar":"{path}/jarname.jar"
      String path = null;

      if (jn.containsKey("data")) {
        JSONObject jdata = (JSONObject) jn.get("data");
        path = jdata.containsKey("path") ? (String) jdata.get("path") : null;
      }

      //System.out.println(getName()+" path: "+path);

      for (int i=0; i < ja.size(); i++) {
        String jar = (String)ja.get(i);

        if (path != null)
          jar = jar.replace("{path}", path);

        jarList.add(jar);
      }
    }
    */

    JSONArray jConnectorArray = (JSONArray) jn.get("input");

    nIn = jConnectorArray.size();

    for (int i = 0; i < nIn; i++) {
      JSONObject jc = (JSONObject) jConnectorArray.get(i);
      BPConnector c = new BPConnector(BPConnector.INPUT, jc);
      c.setNode(this);
      input.add (c);
    }

    jConnectorArray = (JSONArray) jn.get("output");

    nOut = jConnectorArray.size();

    exec = new ArrayList<String>(nOut);

    for (int i = 0; i < nOut; i++) {
      JSONObject jc = (JSONObject) jConnectorArray.get(i);
      BPConnector c = new BPConnector(BPConnector.OUTPUT, jc);
      c.setNode(this);

      if (c.getReference() != null) {
        referenceList.add(c.getReference());
      }

      output.add (c);

      // Initializze every exec item to null. Will be filled in BPNode.getSubsequentCode()
      exec.add(null);

      if (c.getExec())
        nExec ++;
    }

    if (jn.containsKey("java"))
      java += (String) jn.get("java");
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

  public abstract String translate();
  
  public String compile() {
    if (compiled)
      return(java);
    
    compiled = true;

    //System.out.println("Compiling "+blueprint.getName()+"."+this.name);
      
    // Check connectors
    if (!checkConnectors())
      return null;
      
    // Compile nodes where input come (eg. operators)
    for (int i=0; i<nIn; i++) {
      BPConnector c = getInputConnector(i);

      if (!c.getExec() && c.isConnected()) {
        if (c.getConnected().getNode().compile() == null)
          return null;
      }
    }
    
    initCode();

    java = translate();
    
    
    
    return(java);
  }
};
