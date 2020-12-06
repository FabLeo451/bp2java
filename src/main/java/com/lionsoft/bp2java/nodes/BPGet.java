package com.lionsoft.bp2java;

import org.json.simple.JSONObject;

public class BPGet extends BPNode {

  private String variable;
  
  public BPGet() {
    super();
    setType (BPNode.GET);
  }
 
  public BPGet(Blueprint blueprint, JSONObject jo) {
    super(blueprint, jo);

    variable = getOutputConnector(0).getLabel();
  }
  
  public String getVariableName() {
    return variable;
  }
  
  public String translate() {
    return variable;
  }
  
  public String compile() {
    if (super.compile() == null)
      return null;
      
    return (translate());
  }

};

