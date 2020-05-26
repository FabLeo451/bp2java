package com.lionsoft.bp2java;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BPReturn extends BPNode {

  Boolean returnsValueFlag;
  int returnType;
 
  public BPReturn() {
    super();
    setType (RETURN);
    returnsValueFlag = false;
  }
 
  public BPReturn(JSONObject jo) {
    super(jo);
    returnsValueFlag = nIn > 1; // Exec is always present
    
    if (returnsValueFlag)
      returnType = getInputConnector(1).getDataType();
  }
  
  public Boolean returnsValue() {
    return (returnsValueFlag);
  }
  
  public int getReturnType() {
    return (returnType);
  }
  
  public String getCode() {
    String code = "return";
    
    if (nIn > 1) {
      code += "(";
      code += getInputConnector(1).getValueAsString();
      code += ")";
    }
    
    code += ";";
    
    return code;
  }  
  
  public String compile() {
    return (getCode());
  }
};

