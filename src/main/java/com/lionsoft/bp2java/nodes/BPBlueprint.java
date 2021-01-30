package com.lionsoft.bp2java;

import org.json.simple.JSONObject;
import java.util.*;

public class BPBlueprint extends BPFunction {

  //String method = null;
  String blueprintId = null;
  int blueprintInternalId = -1;
  private List<Blueprint> blueprintList;

  public BPBlueprint() {
    super();
    setType (BPNode.BLUEPRINT);
  }

  public BPBlueprint(Blueprint blueprint, JSONObject jo) {
    super(blueprint, jo);
    setType (BPNode.BLUEPRINT);

    //method = (String) jo.get("method");
    blueprintId = (String) jo.get("blueprintId");
    blueprintInternalId = ((Long) jo.get("blueprintInternalId")).intValue();

    String javaCode = "";

    if (returns())
      javaCode += "out{1} = ";

    javaCode += "{method}(";

    int nParams = 0;

    for (int i=0; i<nIn; i++) {
      BPConnector c = getInputConnector(i);

      if (!c.isExec()) {
        if (nParams > 0)
          javaCode += ", ";

        javaCode += "in{"+i+"}";
        nParams ++;
      }
    }

    javaCode += ");";

    if (returns()) {
      BPConnector returnConn = getOutputConnector(1);
      //System.out.println("BPBlueprint: "+returnConn.toString());
      returnConn.createReferenceLocalVariable (returnConn.getDataTypeName() + (returnConn.getDimensions() == 0 ? "" : "[]"), "bp_out");
      referenceList.add(new Reference(returnConn.getDataTypeName() + (returnConn.getDimensions() == 0 ? "" : "[]"), returnConn.getFixedOutput()));
    }

    setJava(javaCode);
  }

  public void setBlueprintList(List<Blueprint> l) {
    blueprintList = l;
  }

  public String toJava() {
    //String source = super.translate();

    // Search the blueprint to be called by internal id

    for (Blueprint b: blueprintList) {
      //System.out.println(blueprintId+" -> "+b.getId()+" "+b.getMethodName());

      if (b.getInternalId() == blueprintInternalId) {
        java = java.replace("{method}", b.getMethodName());
        break;
      }
    }

    return (super.toJava());
  }
};
