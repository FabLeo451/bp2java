package com.lionsoft.bp2java;

import java.util.UUID;
import org.json.simple.JSONObject;

class BPVariable {

  public final static int SCALAR = 0;
  public final static int ARRAY = 1;
  public final static int MATRIX = 2;

  UUID id;
  //int id;
  //int type;
  String typeName;
  String name;
  Object value; // Initial value
  String valueStr; 
  int dimensions;
 
  public BPVariable() {
    value = null;
    dimensions = SCALAR;
    name = "unnamed";
  }
/* 
  public BPVariable(int id, int type, int dimensions, String name) {
    this();
    
    this.id = id;
    this.type = type;
    this.dimensions = dimensions;
    this.name = name;
  }
 
  public BPVariable(int id, int type, int dimensions, String name, Object value) {
    this(id, type, dimensions, name);
    setValue(value);
  }
*/ 
  public BPVariable(JSONObject jvar) {
    this();
    
    //this.id = ((Long)jvar.get("id")).intValue();
    this.id = UUID.fromString((String)jvar.get("id"));
    //this.type = ((Long)jvar.get("type")).intValue();
    this.typeName = (String) jvar.get("typeName");
    this.name = (String) jvar.get("name");
    this.value = (Object) jvar.get("value");
/*      
    switch (this.type) {
      case BPConnector.INT:
        valueStr = Long.toString((Long) this.value);
        break;
        
      case BPConnector.FLOAT:
        valueStr = Double.toString((Double) this.value);
        break;
        
      case BPConnector.BOOLEAN:
        valueStr = (Boolean) this.value ? "Boolean.TRUE" : "Boolean.FALSE";
        break;
        
      case BPConnector.STRING:
        valueStr = "\"" + ((String) jvar.get("value")).replace("\n","\\n").replace("\"","\\\"") + "\"";
        break;
        
      default:
        valueStr = "null";
        break;
    }
*/
    if (this.value != null) {
      if (typeName.equals("Integer"))
        valueStr = Long.toString((Long) this.value);
      else if (typeName.equals("Boolean"))
        valueStr = (Boolean) this.value ? "Boolean.TRUE" : "Boolean.FALSE";
      else if (typeName.equals("Double"))
        valueStr = Double.toString((Double) this.value);
      else if (typeName.equals("String"))
        valueStr = "\"" + ((String) jvar.get("value")).replace("\n","\\n").replace("\"","\\\"") + "\"";
      else 
        valueStr = "null";
    }   
  }
  
  public void setValue(Object v) {
    value = v;
  }
  
  public Object getValue() {
    return (value);
  }
  
  public Object getValueStr() {
    return (valueStr);
  }
  
  public String getName() {
    return (name);
  }
/*  
  public int getType() {
    return (type);
  }
*/  
  public String getTypeName() {
    return (typeName);
  }
  
  public int getDimensions() {
    return (dimensions);
  }

};

