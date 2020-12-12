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
  boolean global = false;
  int referenced = 0;
  Object value; // Initial value
  String valueStr;
  int dimensions;
  BPType type;

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
    this.dimensions = ((Long) jvar.get("dimensions")).intValue();
    this.value = (Object) jvar.get("value");

    this.global = jvar.containsKey("global") ? (Boolean) jvar.get("global") : false;
    this.referenced = jvar.containsKey("referenced") ? ((Long)jvar.get("referenced")).intValue() : 0;

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

  public String toString() {
    return "Variable [id=" + id.toString() + ", name=" + name + ", type=" + typeName + "]";
  }

  public void setValue(Object v) {
    value = v;
  }

  public void setType(BPType type) {
    this.type = type;
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

  public boolean isGlobal() {
    return global;
  }

  public boolean isReferenced() {
    return referenced > 0;
  }

  public String getDeclaration() {
    return typeName + (dimensions == 0 ? "" : (dimensions == 1 ? "[]" : "[][]" ))+" " + name +
           (type != null ? " = " + type.getInitString() : "");
  }

};
