package com.lionsoft.bp2java;

import org.json.simple.JSONObject;

public class BPGet extends BPNode {

  private String variable;
  
  public BPGet() {
    super();
    setType (BPNode.GET);
  }
 
  public BPGet(JSONObject jo) {
    super(jo);

    variable = getOutputConnector(0).getLabel();
  }
  
  public String getVariableName() {
    return variable;
  }
  
  public String getCode() {
    return variable;
  }
  
  public String compile() {
    return (getCode());
  }

};

