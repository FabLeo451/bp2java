package com.lionsoft.bp2java;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BPOperator extends BPNode {

  String symbol;
 
  public BPOperator() {
    super();
    setType (BPNode.OPERATOR);
  }
 
  public BPOperator(Blueprint blueprint, JSONObject jo) {
    super(blueprint, jo);
    
    if (jo.containsKey("symbol"))
      symbol = (String) jo.get("symbol");
    /*else
      System.out.println("Error: missing symbol of operator '"+getName()+"'");*/
  }
  
  public String getSymbol() {
    return symbol;
  }
  
  public String translate() {
    String code;
    
    //System.out.println(getName()+": java = "+java);
    
    if (java != null && !java.isEmpty()) {
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
      //System.out.println(getName()+": Using symbol "+symbol);
      
      code = "(" + getInputConnector(0).getValueAsString();
      
      for (int i=1; i<nIn; i++) {
        code += (symbol + getInputConnector(i).getValueAsString());
      }
      
      code += ")";
    }
    
    //System.out.println(getName()+ ": " + code);
    
    return code;
  }
/*  
  public String compile() {
    System.out.println("Compiling operator "+this.name);
    
    if (super.compile() == null)
      return null;
      
    return (translate());
  }*/
};

