package com.lionsoft.bp2java;

import org.json.simple.JSONObject;

public class BPSet extends BPNode {

  private String variable;
  
  public BPSet() {
    super();
    setType (BPNode.SET);
  }
 
  public BPSet(JSONObject jo) {
    super(jo);

    variable = getInputConnector(1).getLabel();
    getOutputConnector(1).setFixedOutput(variable);
  }
  
  public String getVariable() {
    return variable;
  }
  
  public String getCode() {
    String code;
    
    code = variable + " = " + getInputConnector(1).getValueAsString() + ";" + System.lineSeparator();
    
    BPConnector c = getOutputConnector(0);

    if (c != null && c.getExec() && c.isConnected())
      code += c.getConnectedNode().compile();
          
    return code;
  }
  
  public String compile() {
    return (getCode());
  }

};

