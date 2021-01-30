package com.lionsoft.bp2java;

import org.json.simple.JSONObject;

public class BPSequence extends BPNode {

  public BPSequence() {
    super();
    setType (BPNode.SEQUENCE);
  }

  public BPSequence(Blueprint blueprint, JSONObject jo) {
    super(blueprint, jo);

  }

  @Override
  public String toJava() {
      java = "";

      for (int i=0; i<nOut; i++) {
        BPConnector c = getOutputConnector(i);

        if (c.isExec() && c.isConnected()) {
          java += "exec{"+i+"}" + System.lineSeparator();
        }
      }

      //System.out.println(java);

      return(super.toJava());
  }
/*
  public String translate() {
    String code = "";

    // Set exec array
    if (!getSubsequentCode())
      return null;

    for (int i=0; i<nOut; i++) {
      BPConnector c = getOutputConnector(i);

      if (c.isExec() && c.isConnected()) {
        code += exec.get(i);
        code += System.lineSeparator();
      }
    }

    return code;
}*/
 /*
  public String compile() {
    if (super.compile() == null)
      return null;

    return (translate());
  }
*/
};
