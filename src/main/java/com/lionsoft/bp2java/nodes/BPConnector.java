package com.lionsoft.bp2java;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class BPConnector {

  // Direction
  public final static int INPUT = 0;
  public final static int OUTPUT = 1;

  // Type
  /*
  public final static int EXEC = 0;
  public final static int INT = 1;
  public final static int FLOAT = 2;
  public final static int STRING = 3;
  public final static int BOOLEAN = 4;
  public final static int JSON = 5;
  public final static int POINTER = 6;
  */

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
  //int pinType;
  //String pinTypeName; // Can be EXEC
  //int dataType;
  String dataTypeName;
  Object value;
  String fixedOutput; // For output connectors, a fixed string to be returned
  int dimensions;
  Reference references;
  BPType type; // This is data type
  //Boolean mustConnect;
  Boolean nullable = false; // If true, can be disconnected

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

  Block outBlock;

  //static String strTypes[] = {"_exec_", "int", "float", "String", "Boolean"};

  public BPConnector() {
    exec = false;
    value = null;
    connectedTo = null;
    fixedOutput = null;
    references = null;
    //mustConnect = false;
  }

  public BPConnector(BPNode node, int direction, JSONObject jc) {
    this();

    this.node = node;

    setId(((Long) jc.get("id")).intValue());
    setLabel((String) jc.get("label"));
    //setDataType(((Long) jc.get("dataType")).intValue());
    //setDataTypeName((String) jc.get("dataTypeName"));
    //setPinType(((Long) jc.get("pinType")).intValue());
    //setPinTypeName((String) jc.get("pinTypeName"));
    setDataTypeName((String) jc.get("dataType"));
    setExec((Boolean) jc.get("exec"));
    setDimensions(((Long) jc.get("dimensions")).intValue());

    //System.out.println("Connector "+getId()+" "+node.getName()+"."+getLabel());

    //type = new BPType((String) jc.get("dataTypeName"));
    type = new BPType((String) jc.get("dataType"));

    if (jc.containsKey("nullable"))
      setNullable((Boolean)jc.get("nullable"));

    setDirection(direction);

    if (jc.containsKey("reference_id"))
      setReferenceId(((Long)jc.get("reference_id")).intValue());

    if (jc.containsKey("java")) {
      JSONObject java = (JSONObject) jc.get("java");

      if (java.containsKey("references")) {
        String varName = null;

        JSONObject jref = (JSONObject) java.get("references");

        if (jref.containsKey("input")) {
          // Reference an input connector
          references = new Reference(((Long)jref.get("input")).intValue());
        }
        else if (jref.containsKey("variable")) {
          // Reference a local variable to be declared
          // "references": {"variable":"varName"}
          String arrayAttr = dimensions == 0 ? "" : (dimensions == 1 ? "[]" : "[][]");
          createReferenceLocalVariable (type.getName() + arrayAttr, (String) jref.get("variable"));
          //System.out.println("Referencing "+type.getName() + arrayAttr+" "+(String) jref.get("variable"));
        }
        else if (jref.containsKey("object")) {
          // Reference an object
          setFixedOutput((String) jref.get("object"));
          //System.out.println(getLabel()+" references "+fixedOutput);
        }
        else {
          System.out.println("'references' without target object");
        }

        //setFixedOutput(varName);
        //System.out.println(label+" references "+references.toString());
      }
    }

    if (jc.containsKey("value")) {
      /*
      switch (dataType) {
        case INT:
        case BOOLEAN:
          value = (Object) jc.get("value");
          break;

        case FLOAT:
          value = jc.get("value") instanceof Long ? ((Long)jc.get("value")).doubleValue() : jc.get("value");
          break;

        case STRING:
          //value = (Object) ((String)jc.get("value")).replace("\n","\\n").replace("\\","\\\\").replace("\"","\\\"");
          value = (Object) ((String)jc.get("value")).replace("\\","\\\\").replace("\n","\\n").replace("\"","\\\"");
          //System.out.println(label+" = "+(String)value);
          break;

        default:
          break;
      }
      */

      if (dataTypeName.equals("Integer") || dataTypeName.equals("Boolean"))
        value = (Object) jc.get("value");
      else if (dataTypeName.equals("Double"))
        value = jc.get("value") instanceof Long ? ((Long)jc.get("value")).doubleValue() : jc.get("value");
      else if (dataTypeName.equals("String"))
        value = (Object) ((String)jc.get("value")).replace("\\","\\\\").replace("\n","\\n").replace("\"","\\\"");
      else {

      }
    }

    //System.out.println("Created "+this.toString());
  }

  public void createReferenceLocalVariable (String decl, String name) {
    String varName = null;

    varName = "_conn_"+name+"_"+id;
    references = new Reference(decl, varName);
    setFixedOutput(varName);

    //System.out.println("Local var: "+decl+" "+varName);
  }

  public void createReferenceLocalVariable (JSONObject jref) {
    createReferenceLocalVariable ((String)jref.get("type"), (String)jref.get("name"));
  }

  public String toString() {
    return("BPConnector [id="+id+", type="+dataTypeName+", label="+label+", dimensions="+dimensions+"]");
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
/*
  public void setDataType (int type) {
    this.dataType = type;
  }

  public int getDataType () {
    return (dataType);
  }
*/

  public String getDataTypeName () {
    return (dataTypeName);
  }

  public void setDataTypeName (String name) {
    this.dataTypeName = name;
  }
/*
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
*/
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

  public boolean mustBeConnected() {
    return (!getExec() && getValue() == null && !getNullable());
  }

  public void setNullable (Boolean b) {
    this.nullable = b;
  }

  public Boolean getNullable () {
    return (this.nullable);
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

    if (getExec())
        c.getNode().addPrevious(getNode());
  }

  public BPConnector getConnected() {
    return (connectedTo);
  }

  public BPNode getConnectedNode() {
    return (isConnected() ? connectedTo.getNode() : null);
  }

  public boolean isConnected() {
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

  public Object getValue() {
    return(value);
  }

  public String getValueAsString() {
    String result = "null";

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
            //result = getNode().getJava();
            result = getNode().toJava();
          else {
            if (direction == BPConnector.INPUT && isConnected())
              //result = getNode().getJava();
              result = getNode().toJava();
          }
        }
      }
    }

    return (result);
  }
};
