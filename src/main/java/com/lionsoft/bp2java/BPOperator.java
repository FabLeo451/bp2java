package com.lionsoft.bp2java;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BPOperator extends BPNode {

  String symbol;
 
  public BPOperator() {
    super();
    setType (BPNode.OPERATOR);
  }
 
  public BPOperator(JSONObject jo) {
    super(jo);
    
    if (jo.containsKey("symbol"))
      symbol = (String) jo.get("symbol");
    /*else
      System.out.println("Error: missing symbol of operator '"+getName()+"'");*/
  }
  
  public String getSymbol() {
    return symbol;
  }
  
  public String getCode() {
    String code;
    
    //System.out.println(getName()+" "+java);
    
    if (java != null) {
      // Update java template
      
      code = java;
      
      // Replace code of input values
      for (int i=0; i<nIn; i++) {
        BPConnector c = getInputConnector(i);

        if (!c.getExec()) {
          code = code.replace("in{"+i+"}", c.getValueAsString());
        }
      }
    }
    else {
      // Concatenate operands with operator symbol
      
      code = "(" + getInputConnector(0).getValueAsString();
      
      for (int i=1; i<nIn; i++) {
        code += (symbol + getInputConnector(i).getValueAsString());
      }
      
      code += ")";
    }
    
    return code;
  }
  
  public String compile() {
    return (getCode());
  }
};

