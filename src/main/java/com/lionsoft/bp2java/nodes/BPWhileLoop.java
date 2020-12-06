package com.lionsoft.bp2java;

import org.json.simple.JSONObject;

public class BPWhileLoop extends BPNode {
  
  public BPWhileLoop() {
    super();
    setType (BPNode.WHILE_LOOP);
  }
 
  public BPWhileLoop(Blueprint blueprint, JSONObject jo) {
    super(blueprint, jo);
  }
  
  public String translate() {
    String code, iteration = "";
    
    BPConnector ci = getOutputConnector(0); // Iteration connector

    if (ci != null && ci.getExec() && ci.isConnected())
      iteration = ci.getConnectedNode().compile();
    
    code = "while ("+ getInputConnector(1).getValueAsString() + ") {" + System.lineSeparator();
    code += iteration + System.lineSeparator();
    code += "}" + System.lineSeparator();

    BPConnector c = getOutputConnector(1); // Completed
    
    if (c != null && c.getExec() && c.isConnected())
      code += c.getConnectedNode().compile();
    
    return code;
  }
  
  public String compile() {
    if (super.compile() == null)
      return null;
      
    return (translate());
  }

};

