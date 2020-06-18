package com.lionsoft.bp2java;

import org.json.simple.JSONObject;

public class BPSequence extends BPNode {
  
  public BPSequence() {
    super();
    setType (BPNode.SEQUENCE);
  }
 
  public BPSequence(JSONObject jo) {
    super(jo);
  }
  
  public String getCode() {
    String code = "";
    
    // Set exec array
    getSubsequentCode ();
  
    for (int i=0; i<nOut; i++) {
      BPConnector c = getOutputConnector(i);

      if (c.getExec() && c.isConnected()) {
        code += exec.get(i);
        code += System.lineSeparator();
      }
    }
    
    return code;
  }
  
  public String compile() {
    return (getCode());
  }

};

