package com.lionsoft.bp2java;

import org.json.simple.JSONObject;

public class BPSet extends BPNode {

  private String variable;

  public BPSet() {
    super();
    setType (BPNode.SET);
  }

  public BPSet(Blueprint blueprint, JSONObject jo) {
    super(blueprint, jo);

    variable = getInputConnector(1).getLabel();
    getOutputConnector(1).setFixedOutput(variable);

    java = variable + " = " + getInputConnector(1).getValueAsString() + ";" + System.lineSeparator();
    //java += "exec{0}" + System.lineSeparator();

  }

  public String getVariable() {
    return variable;
  }
/*
  @Override
  public String toJava() {

    String code;

    code = variable + " = " + getInputConnector(1).getValueAsString() + ";" + System.lineSeparator();

    BPConnector c = getOutputConnector(0);

    if (c != null && c.getExec() && c.isConnected())
      code += c.getConnectedNode().compile().getSourceCode();

    return code;

    return(super.toJava());
  }
  */
/*
  public String compile() {
    if (super.compile() == null)
      return null;

    return (translate());
  }
*/
};
