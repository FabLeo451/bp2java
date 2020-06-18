package com.lionsoft.bp2java;

import java.util.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Iterator;

public class BlueprintEvents extends Blueprint {

  public BlueprintEvents() {
    super();
    type = BlueprintType.EVENTS;
  }

  public BlueprintEvents(JSONObject jo) {
    super(jo);
  }

  // Overrides
  public String toJavaCode() {
    String functionCode, scope, returnType, header, parameters = "", body = "";
    
    //scope = (getType() == Blueprint.MAIN) ? "public static" : "public";
    scope = "public static";
    //returnType = returnNode.returnsValue() ? /*BPConnector.typeToString(returnNode.getReturnType())*/ (String) types.get(returnNode.getReturnType()) : "void";
    returnType = returnNode.returnsValue() ? returnNode.getReturnTypeName() : "void";
    
    //System.out.println("nIn = "+entryPointNode.getInputParamsCount());
    for (int i=1; i<entryPointNode.getOutputParamsCount(); i++) {
      if (i > 1)
        parameters += ", ";
        
      String dim;
      
      switch (entryPointNode.getOutputConnector(i).getDimensions()) {
        case BPVariable.ARRAY:
          dim = "[]";
          break;
          
        case BPVariable.MATRIX:
          dim = "[][]";
          break;
          
        default:
          dim = "";
          break;
      }
        
      parameters += /*(String) types.get(entryPointNode.getOutputConnector(i).getDataType())*/ entryPointNode.getOutputConnector(i).getDataTypeName() + dim + " " + entryPointNode.getOutputConnector(i).getLabel();
    }
    
    header = scope + " " + returnType + " " + getMethodName() + "("+parameters+") throws ExitException ";
      
    for (int k=0; k<locals.size(); k++)
      declareSection += locals.get(k) + System.lineSeparator();
  /*
    for (int i=0; i<nodes.size(); i++) {
      System.out.println(nodes.get(i).getCode());
    }*/
    

    body = entryPointNode.compile();

    functionCode = header + " {" + System.lineSeparator() +
                   declareSection + System.lineSeparator() +
                   body + System.lineSeparator() +
                   "}" + System.lineSeparator();
    
    return functionCode;
  }

};

