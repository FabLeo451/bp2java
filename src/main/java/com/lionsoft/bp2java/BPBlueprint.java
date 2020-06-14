package com.lionsoft.bp2java;

import org.json.simple.JSONObject;
import java.util.*;

public class BPBlueprint extends BPFunction {

  //String method = null;
  String blueprintId = null;
  private List<Blueprint> blueprintList;

  public BPBlueprint() {
    super();
    setType (BPNode.BLUEPRINT);
  }
  
  public BPBlueprint(JSONObject jo) {
    super(jo);
    setType (BPNode.BLUEPRINT);
    
    //method = (String) jo.get("method");
    blueprintId = (String) jo.get("blueprintId");

    String javaCode = "";

    if (returns())
      javaCode += "out{1} = ";
   
    javaCode += "{method}(";
    
    int nParams = 0;
    
    for (int i=0; i<nIn; i++) {
      BPConnector c = getInputConnector(i);

      if (!c.getExec()) {
        if (nParams > 0)
          javaCode += ", ";
          
        javaCode += "in{"+i+"}";
        nParams ++;
      }
    }
    
    javaCode += ");";
 
    if (returns()) {
      BPConnector returnConn = getOutputConnector(1);
      System.out.println("BPBlueprint: "+returnConn.toString());
      returnConn.createReferenceLocalVariable (returnConn.getDataTypeName(), "bp_out");
      referenceList.add(new Reference(returnConn.getDataTypeName(), returnConn.getFixedOutput()));
    }
    
    setJava(javaCode);
  }
  
  public void setBlueprintList(List<Blueprint> l) {
    blueprintList = l;
  }

  public String getCode() {
    String source = super.getCode();
    
    for (Blueprint b: blueprintList) {
      //System.out.println(blueprintId+" -> "+b.getId()+" "+b.getMethodName());
      
      if (b.getId().equals(blueprintId)) {
        source = source.replace("{method}", b.getMethodName());
        break;
      }
    }
    
    return (source);
  }
};


