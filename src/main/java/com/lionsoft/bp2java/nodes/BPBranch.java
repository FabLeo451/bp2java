package com.lionsoft.bp2java;

import org.json.simple.JSONObject;

public class BPBranch extends BPNode {
  
  public BPBranch() {
    super();
    setType (BPNode.BRANCH);
  }
 
  public BPBranch(JSONObject jo) {
    super(jo);
    setType (BPNode.BRANCH);
  }
  
  public String getCode() {
    String code, bt = "", bf = "";
    
    BPConnector ct = getOutputConnector(0); // True connector
    BPConnector cf = getOutputConnector(1); // False connector

    if (ct != null && ct.getExec() && ct.isConnected())
      bt = ct.getConnectedNode().compile();

    if (cf != null && cf.getExec() && cf.isConnected())
      bf = cf.getConnectedNode().compile();
    
    code = "if (" + getInputConnector(1).getValueAsString() + ") {" + System.lineSeparator();
    code += bt + System.lineSeparator();
    code += "}" + System.lineSeparator() + "else {" + System.lineSeparator();
    code += bf + System.lineSeparator() + "}" + System.lineSeparator();
    
    return code;
  }
  
  public String compile() {
    return (getCode());
  }

};

