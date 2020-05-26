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

  int id;
  int type;
  String blueprintId;

  String name;
  int nIn;
  int nOut;
  int nExec;

  String java = "";
  
  List<BPConnector> input;
  List<BPConnector> output;
  List<String> exec;
  
  public List<String> importList;
  public List<String> jarList;
  
  // Auto-variables referenced by output connectors
  public List<Reference> referenceList;
  
  // Include an Object array for input values
  private boolean javaInputArray = false;

  Boolean compiled;         /* Already compiled (to avoid loops) */
   
  public BPNode() {
    input = new ArrayList<BPConnector>();
    output = new ArrayList<BPConnector>();
    importList = new ArrayList<String>();
    jarList = new ArrayList<String>();
    referenceList = new ArrayList<Reference>();
    nExec = 0;
  }
  
  public BPNode(JSONObject jn) {
    this();
    set(jn);
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
  
  public String initCode () {
    //System.out.println("[getCode] javaInputArray = "+javaInputArray);
    String autoCode = "";
    
    if (type != OPERATOR)
      autoCode = "// Node: "+getName() + System.lineSeparator() + System.lineSeparator();
    
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
    
    //System.out.println("[getCode] java = "+java);
    
    return (java);
  }
  
  public String getCode () {
    return (java);
  }
  
  public void getSubsequentCode () {
    for (int i=0; i<nOut; i++) {
      BPConnector c = getOutputConnector(i);
      
      if (c != null && c.getExec() && c.isConnected()) {
        exec.set(i, c.getConnectedNode().compile());
      }
    }
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
    setId(((Long)jn.get("id")).intValue());
    setName((String) jn.get("name"));
    setType(((Long)jn.get("type")).intValue());
    
    if (jn.containsKey("options")) {
      JSONObject joptions = (JSONObject) jn.get("options");
      
      javaInputArray = joptions.containsKey("javaInputArray") ? (Boolean) joptions.get("javaInputArray") : false; 
    }
    
    //System.out.println("options.javaInputArray = "+javaInputArray);
    
    if (jn.containsKey("import")) {
      JSONArray ja = (JSONArray) jn.get("import");
      
      for (int i=0; i < ja.size(); i++) {
        importList.add((String)ja.get(i));
      }
    }
    
    if (jn.containsKey("jar")) {
      JSONArray ja = (JSONArray) jn.get("jar");
      
      for (int i=0; i < ja.size(); i++) {
        jarList.add((String)ja.get(i));
      }
    }
    
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
  
  public abstract String compile();     
};

