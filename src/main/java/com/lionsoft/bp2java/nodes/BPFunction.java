package com.lionsoft.bp2java;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.regex.Pattern;

public class BPFunction extends BPNode {

  public BPFunction() {
    super();
    setType (BPNode.FUNCTION);
  }

  public BPFunction(Blueprint blueprint, JSONObject jo) {
    super(blueprint, jo);
    setType (BPNode.FUNCTION);
  }
/*
  public String getJava() {
    return java;
  }

  public void setJava(String j) {
    java = j;
  }
  */
  public String translate() {
    String code;

    super.getJava();

    // Set exec array
    if (!getSubsequentCode())
        return null;

    // Set initial code form node
    code = java;

    //System.out.println("java = "+java);

    code = code.replace("{node.id}", Integer.toString(getId()));
    code = code.replace("{count.in}", Integer.toString(nIn));

    // Replace code of input values
    for (int i=0; i<nIn; i++) {
      BPConnector c = getInputConnector(i);

      if (!c.getExec()) {
        //System.out.println(this.name+" connector "+i);
        code = code.replace("in{"+i+"}", c.getValueAsString());
        //System.out.println(c.getValueAsString());
        //code = Pattern.compile("in\\{"+i+"\\}").matcher(code).quoteReplacement(c.getValueAsString());
        //System.out.println("in{"+i+"} -> "+c.getValueAsString());
      }
    }

    // Replace code of output variables (out{2} = 5 -> _code_6 = 5;)
    for (int i=0; i<nOut; i++) {
      BPConnector c = getOutputConnector(i);

      if (!c.getExec()) {
        code = code.replace("out{"+i+"}", c.getValueAsString());
      }
    }

    code += System.lineSeparator();
    /*
    BPConnector c = getOutputConnector(0);

    if (c != null && c.getExec() && c.isConnected())
      code += c.getConnectedNode().compile();
    */
    if (nExec == 1) {
      if (exec.get(0) != null)
        code += exec.get(0);
    }
    else {
      for (int i=0; i<nOut; i++) {
        BPConnector c = getOutputConnector(i);
        //System.out.println("Connector "+c.getNode().getName()+"."+c.getLabel()+" "+ (c.isConnected() ? "[*]" : "[ ]") +" -> "+exec.get(i));

        if (c.getExec()) {
          code = code.replace("exec{"+i+"}", c.isConnected() ? exec.get(i) : "");
        }
      }
    }

    return code;
  }
 /*
  public String compile() {
    System.out.println("Compiling node "+this.name);

    if (super.compile() == null)
      return null;

    return (translate());
  }*/
};
