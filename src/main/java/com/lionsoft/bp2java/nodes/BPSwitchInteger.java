package com.lionsoft.bp2java;

import org.json.simple.JSONObject;

public class BPSwitchInteger extends BPNode {

  public BPSwitchInteger() {
    super();
    setType (BPNode.SWITCH_INTEGER);
  }

  public BPSwitchInteger(Blueprint blueprint, JSONObject jo) {
    super(blueprint, jo);
    setType (BPNode.SWITCH_INTEGER);

    java = "";

    for (int i=1; i<getOutputParamsCount(); i++) {
        if (i > 1)
            java +="else ";

        java += "if (in{1} == "+getOutputConnector(i).getValueAsString()+") {" + System.lineSeparator() + " exec{"+i+"} }" + System.lineSeparator();
    }

    if (getOutputParamsCount() > 0)
        java += "else { exec{0} }" + System.lineSeparator();
    else
        java = "exec{0}";
  }
/*
  public String translate() {
    String code = "";

    // Set exec array
    if (!getSubsequentCode())
      return null;

    if (getOutputParamsCount() == 1) {
      // Only default
      code = exec.get(0);
    }
    else {
      for (int i=1; i<getOutputParamsCount(); i++) {

          code = "if (" + getInputConnector(1).getValueAsString() + " == "+getOutputConnector(i).getValueAsString()+") {" + System.lineSeparator();

          if (getOutputConnector(i).isConnected())
            code += exec.get(i) + System.lineSeparator();

          code += "}";
      }

      code += "else {" + exec.get(0) + "}" + System.lineSeparator();
    }

    return code;
} */

 /*
  public String compile() {
    return (translate());
  }
*/
};
