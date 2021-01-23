package com.lionsoft.bp2java;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BPReturn extends BPNode {

  Boolean returnsValueFlag;
  //int returnType;
  String returnTypeName;
  int returnDim = 0;

  public BPReturn() {
    super();
    setType (RETURN);
    returnsValueFlag = false;
  }

  public BPReturn(Blueprint blueprint, JSONObject jo) {
    super(blueprint, jo);
    returnsValueFlag = nIn > 1; // Exec is always present

    if (returnsValueFlag) {
      //returnType = getInputConnector(1).getDataType();
      returnTypeName = getInputConnector(1).getDataTypeName();
      returnDim = getInputConnector(1).getDimensions();
    }
  }

  public Boolean returnsValue() {
    return (returnsValueFlag);
  }
/*
  public int getReturnType() {
    return (returnType);
  }
*/
  public String getReturnTypeName() {
    return (returnTypeName);
  }

  public int getReturnArray() {
    return (returnDim);
  }

  @Override
  public String toJava() {
    String code = "return";

    if (nIn > 1) {
      code += "(";
      code += getInputConnector(1).getValueAsString();
      code += ")";
    }

    code += ";";

    return code;
  }
/*
  public String compile() {
    if (super.compile() == null)
      return null;

    return (translate());
  }*/
};
