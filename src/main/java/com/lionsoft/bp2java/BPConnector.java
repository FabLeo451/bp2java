package com.lionsoft.bp2java;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class BPConnector {

  // Direction
  public final static int INPUT = 0;
  public final static int OUTPUT = 1;
  
  // Type
  public final static int EXEC = 0;
  public final static int INT = 1;
  public final static int FLOAT = 2;
  public final static int STRING = 3;
  public final static int BOOLEAN = 4;
  public final static int JSON = 5;
  public final static int POINTER = 6;
  
  // Dimensions
  /*
  public final static int SCALAR = 0;
  public final static int ARRAY = 1;
  public final static int MATRIX = 2;  */

  BPNode node;
  int direction;
  int id;
  String label;
  Boolean exec;
  int pinType;
  String pinTypeName;
  int dataType;
  String dataTypeName;
  Object value;
  String fixedOutput; // For output connectors, a fixed string to be returned
  int dimensions;
  Reference references;
  
  /*int must_connect;
  int not_null;*/
  //int flags;
  
  /*
   * connectedTo:
   * - For exec: exec connector of next node
   * - For data: data connector of previous node
   */
  BPConnector connectedTo;
  
  int referenceId;
  //BPVariable *references;
  
  //static String strTypes[] = {"_exec_", "int", "float", "String", "Boolean"};
   
  public BPConnector() {
    exec = false;
    value = null;
    connectedTo = null;
    fixedOutput = null;
    references = null;
  }
   
  public BPConnector(int direction, JSONObject jc) {
    this();
    
    setId(((Long)jc.get("id")).intValue());
    setLabel((String)jc.get("label"));
    setDataType(((Long)jc.get("dataType")).intValue());
    setPinType(((Long)jc.get("pinType")).intValue());
    setPinTypeName((String)jc.get("pinTypeName"));
    setExec((Boolean)jc.get("exec"));
    setDimensions(((Long)jc.get("dimensions")).intValue());
    
    setDirection(direction);
    
    if (jc.containsKey("reference_id"))
      setReferenceId(((Long)jc.get("reference_id")).intValue());
    
    if (jc.containsKey("java")) {
      JSONObject java = (JSONObject) jc.get("java");
      
      if (java.containsKey("references")) {
        String varName = null;
        
        JSONObject jref = (JSONObject) java.get("references");
        
        if (jref.containsKey("input")) {
          // Reference an input variable
          references = new Reference(  ((Long)jref.get("input")).intValue());
        }
        else {
          // Reference a new variable
          /*
          varName = "_conn_"+(String)jref.get("name")+"_"+id;
          references = new Reference((String)jref.get("type"), varName);
          setFixedOutput(varName);
          */
          createReferenceLocalVariable (jref);
        }
        
        //setFixedOutput(varName);
        //System.out.println(label+" references "+references.toString());
      }
    }
    
    if (jc.containsKey("value")) {
      switch (dataType) {
        case INT:
        case FLOAT:
        case BOOLEAN:
          value = (Object) jc.get("value");
          break;

        case STRING:
          //value = (Object) ((String)jc.get("value")).replace("\n","\\n").replace("\\","\\\\").replace("\"","\\\"");
          value = (Object) ((String)jc.get("value")).replace("\\","\\\\").replace("\n","\\n").replace("\"","\\\"");
          //System.out.println(label+" = "+(String)value);
          break;

        default:
          break;
      }
    }
  }
  
  public void createReferenceLocalVariable (String type, String name) {
    String varName = null;
    
    varName = "_conn_"+name+"_"+id;
    references = new Reference(type, varName);
    setFixedOutput(varName);
  }
  
  public void createReferenceLocalVariable (JSONObject jref) {
    createReferenceLocalVariable ((String)jref.get("type"), (String)jref.get("name"));
  }

  public String toString() {
    return("BPConnector [id="+id+", type="+dataType+", label="+label+"]");
  }

  public void setReference(Reference r) {
    references = r;
  }
  
  public void setId (int id) {
    this.id = id;
  }
  
  public int getId () {
    return (id);
  }
  
  public void setDataType (int type) {
    this.dataType = type;
  }
  
  public int getDataType () {
    return (dataType);
  }
  
  public void setPinType (int type) {
    this.pinType = type;
  }
  
  public int getPinType () {
    return (pinType);
  }
  
  public void setPinTypeName (String name) {
    this.pinTypeName = name;
  }
  
  public String getPinTypeName () {
    return (pinTypeName);
  }
  
  public void setLabel (String label) {
    this.label = label;
  }
  
  public String getLabel () {
    return (label);
  }
  
  public void setDirection (int direction) {
    this.direction = direction;
  }
  
  public int getDirection () {
    return (direction);
  }
  
  public void setDimensions(int d) {
    dimensions = d;
  }
  
  public int getDimensions() {
    return (dimensions);
  }
    
  public void setExec (Boolean exec) {
    this.exec = exec;
  }
  
  public Boolean getExec () {
    return (exec);
  }
  
  public void setNode (BPNode node) {
    this.node = node;
  }
  
  public BPNode getNode () {
    return (node);
  }
  
  public void setReferenceId (int id) {
    this.referenceId = id;
  }
  
  public int getReferenceId () {
    return (referenceId);
  }

  public void connectTo(BPConnector c) {
    connectedTo = c;
  }

  public BPConnector getConnected() {
    return (connectedTo);
  }

  public BPNode getConnectedNode() {
    return (isConnected() ? connectedTo.getNode() : null);
  }

  public Boolean isConnected() {
    return (connectedTo != null);
  }
  
  public void setFixedOutput(String s) {
    fixedOutput = s;
  }
  
  public String getFixedOutput() {
    return (fixedOutput);
  }
  
  public Reference getReference() {
    return (references);
  }
  
  public void setValue(Object v) {
    value = v;
  }

  public String getValueAsString() {
    String result = "";
    
    if (isConnected() && direction == BPConnector.INPUT) {
      // This node. Input pin connected: go backwards
      result = getConnected().getValueAsString();
    }
    else {
      if (fixedOutput != null) {
        return (fixedOutput);
      } 
      else if (references != null) {
        if (references.getRefType() == Reference.INPUT) {
          result = getNode().getInputConnector(references.inputConnector).getValueAsString();
        }
      }
      else {
        if (value instanceof Long)
          result = Long.toString((Long) value);
        else if (value instanceof Double)
          result = Double.toString((Double) value);
        else if (value instanceof Float)
          result = Float.toString((Float) value);
        else if (value instanceof Boolean)
          result = (Boolean) value ? "Boolean.TRUE" : "Boolean.FALSE";
        else if (value instanceof String) {
          result = "\""+(String) value+"\"";
          //System.out.println(result);
        }
        else {
          if (getNode().getType() == BPNode.ENTRY_POINT)
            result = label;
          else if (getNode().getType() == BPNode.GET)
            result = ((BPGet)getNode()).getVariableName();
          else if (getNode().getType() == BPNode.OPERATOR)
            result = getNode().getCode();
          else {
            if (direction == BPConnector.INPUT && isConnected())
              result = getNode().getCode();
          }
        }
      }
    }
    
    return (result);
  }
};

