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

public class Blueprint {

  public final static int GENERIC = 0;
  public final static int MAIN = 1;
  
  public final static int SUCCESS = 0;
  public final static int ERR_FILE_NOT_FOUND = 1;
  public final static int ERR_IO = 2;
  public final static int ERR_JSON_PARSING = 3;

  JSONObject jbp;
  
  private String id;
  private String name, method;
  private int type;
  
  Map types;
  BPEntryPoint entryPointNode;
  BPReturn returnNode;
  List<BPNode> nodes;
  List<BPVariable> variables;
  List<String> importList;
  List<String> jarList;
  List<String> locals;
  
  private BPProgram program;
  
  private String declareSection;
 
  public Blueprint() {
    nodes = new ArrayList<BPNode>();
    variables = new ArrayList<BPVariable>();
    importList = new ArrayList<String>();
    jarList = new ArrayList<String>();
    locals = new ArrayList<String>();
    name = "myBlueprint";
    method = name;
    
    // Standard types
    // {"_exec_", "int", "float", "String", "Boolean"};
    types = new HashMap();
    types.put(0, "_exec");
    types.put(1, "Integer");
    types.put(2, "Double");
    types.put(3, "String");
    types.put(4, "Boolean");
  }
/* 
  public Blueprint(String filename) {
    this();
    
    int result = load (filename);
    
    if (result != SUCCESS)
      System.out.println("Error "+result);
  }*/
  
  public BPConnector getConnectorById(int id) {
    BPNode n;
    BPConnector c;

    for (int i = 0; i < nodes.size(); i++) {
      n = nodes.get(i);
      c = n.getConnectorById(id);
      
      if (c != null)
        return (c);
    }

    return null;
  }
  
  public String getId() {
    return (id);
  }
  
  public String getName() {
    return (name);
  }
  
  public int getType() {
    return (type);
  }
  
  public String getMethodName() {
    return (method);
  }
  
  // https://crunchify.com/how-to-read-json-object-from-file-in-java/
  public int load(String filename) {
    JSONParser jsonParser = new JSONParser();
    
    //System.out.println("Loading "+filename);
     
    try {
      FileReader reader = new FileReader(filename);
      
      jbp = (JSONObject) jsonParser.parse(reader);


    } catch (FileNotFoundException e) {
        e.printStackTrace();
        return ERR_FILE_NOT_FOUND;
    } catch (IOException e) {
        e.printStackTrace();
        return ERR_IO;
    } catch (ParseException e) {
        e.printStackTrace();
        return ERR_JSON_PARSING;
    }
    
    id = (String) jbp.get("id");
    name = (String) jbp.get("name");
    type = ((Long) jbp.get("type")).intValue();
    method = jbp.containsKey("method") ? (String) jbp.get("method") : name.replace(" ", "_");
    
    // Types
    
    if (jbp.containsKey("types")) {
      JSONArray jTypesArray = (JSONArray) jbp.get("types");
    
      for (int i = 0; i < jTypesArray.size(); i++) {
        JSONObject jtype = (JSONObject) jTypesArray.get(i);
        int key = ((Long)jtype.get("id")).intValue();
        
        types.put(key, (String)jtype.get("name"));
        
        System.out.println(key +" "+ (String)types.get(key));
      }
    }
        
    // Variables
    
    JSONArray jVarArray = (JSONArray) jbp.get("variables");
    
    declareSection = "";
    
    for (int i = 0; i < jVarArray.size(); i++) {
      JSONObject jvar = (JSONObject) jVarArray.get(i);

      BPVariable v = new BPVariable(jvar);
      variables.add(v);
      
      declareSection += v.getTypeName() + " " + v.getName() + " = " + v.getValueStr() + ";" + System.lineSeparator();
    }
        
    // Nodes
    
    JSONArray jnodeArray = (JSONArray) jbp.get("nodes");
    
    //System.out.println("Blueprint has "+jnodeArray.size()+" nodes");
    
    //BPNode node;

    for (int i = 0; i < jnodeArray.size(); i++) {
      JSONObject jnode = (JSONObject) jnodeArray.get(i);
      
      String name = (String) jnode.get("name");
      int type = ((Long)jnode.get("type")).intValue();
      
      //System.out.println(name+" "+type);
      
      switch (type) {
        case BPNode.ENTRY_POINT:
          entryPointNode = new BPEntryPoint(jnode);
          nodes.add(entryPointNode);
          break;
          
        case BPNode.RETURN:
          returnNode = new BPReturn(jnode);
          nodes.add(returnNode);
          
          if (returnNode.returnsValue())
            System.out.println("Returns "+returnNode.getReturnType()+" "+(String) types.get(returnNode.getReturnType()));
          break;
          
        case BPNode.SEQUENCE:
          nodes.add(new BPSequence(jnode));
          break;
          
        case BPNode.BRANCH:
          nodes.add(new BPBranch(jnode));
          break;
          
        case BPNode.OPERATOR:
          BPOperator o = new BPOperator(jnode);
          nodes.add(o);
          
          for (int k=0; k<o.importList.size(); k++) {
            if (!importList.contains(o.importList.get(k)))
              importList.add(o.importList.get(k));
          }
          
          for (int k=0; k<o.jarList.size(); k++) {
            if (!jarList.contains(o.jarList.get(k)))
              jarList.add(o.jarList.get(k));
          }
          
          for (int k=0; k<o.referenceList.size(); k++) {
            if (o.referenceList.get(k).getRefType() == Reference.LOCAL)
              locals.add(o.referenceList.get(k).getDeclaration());
          }
          
          break;
          
        case BPNode.FUNCTION:
          BPFunction f = new BPFunction(jnode);
          nodes.add(f);
          
          for (int k=0; k<f.importList.size(); k++) {
            if (!importList.contains(f.importList.get(k)))
              importList.add(f.importList.get(k));
          }
          
          for (int k=0; k<f.jarList.size(); k++) {
            if (!jarList.contains(f.jarList.get(k)))
              jarList.add(f.jarList.get(k));
          }
          
          for (int k=0; k<f.referenceList.size(); k++) {
            if (f.referenceList.get(k).getRefType() == Reference.LOCAL)
              locals.add(f.referenceList.get(k).getDeclaration());
          }
            
          break;
          
        case BPNode.BLUEPRINT:
          BPBlueprint bp = new BPBlueprint(jnode);
          bp.setBlueprintList(program.getBlueprintList());
          nodes.add(bp);
          
          for (int k=0; k<bp.referenceList.size(); k++) {
            if (bp.referenceList.get(k).getRefType() == Reference.LOCAL)
              locals.add(bp.referenceList.get(k).getDeclaration());
          }
          break;
          
        case BPNode.GET:
          nodes.add(new BPGet(jnode));
          break;
          
        case BPNode.SET:
          nodes.add(new BPSet(jnode));
          break;
          
        case BPNode.WHILE_LOOP:
          nodes.add(new BPWhileLoop(jnode));
          break;
          
        case BPNode.SWITCH_INTEGER:
          nodes.add(new BPSwitchInteger(jnode));
          break;
          
        case BPNode.FOR_LOOP:
          BPForLoop fl = new BPForLoop(jnode);
          nodes.add(fl);
          
          for (int k=0; k<fl.referenceList.size(); k++) {
            if (fl.referenceList.get(k).getRefType() == Reference.LOCAL)
              locals.add(fl.referenceList.get(k).getDeclaration());
          }
          break;
          
        case BPNode.EXIT:
          nodes.add(new BPExit(jnode));
          break;
          
        default:
          System.out.println("Unknown node type: "+type);
          break;
      }
    }
    
    // Edges
    
    int from, to;
    BPConnector c1, c2;
    JSONArray jEdgeArray = (JSONArray) jbp.get("edges");
    
    for (int i = 0; i < jEdgeArray.size(); i++) {
      JSONObject je = (JSONObject) jEdgeArray.get(i);
      
      from = ((Long)je.get("from")).intValue();
      to = ((Long)je.get("to")).intValue();
      
      //System.out.println("Connecting "+from+" and "+to);

      c1 = getConnectorById (from);
      c2 = getConnectorById (to);

      if (c1.getExec()) {
        // Execution flow connection (previous points to next)
        c1.connectTo(c2);
      }
      else {
        // Data connection (next points to previous)
        c2.connectTo(c1);
      }
    }
    
    
    for (BPNode node : nodes) {
      //BPNode node = (BPNode)
      node.initCode();
    }

    
    return (SUCCESS);
  }
  
  public String toJavaCode() {
    String functionCode, scope, returnType, header, parameters = "", body;
    
    //scope = (getType() == Blueprint.MAIN) ? "public static" : "public";
    scope = "public static";
    returnType = returnNode.returnsValue() ? /*BPConnector.typeToString(returnNode.getReturnType())*/ (String) types.get(returnNode.getReturnType()) : "void";
    
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
        
      parameters += /*BPConnector.typeToString(entryPointNode.getOutputConnector(i).getDataType())*/(String) types.get(entryPointNode.getOutputConnector(i).getDataType()) + dim + " " + entryPointNode.getOutputConnector(i).getLabel();
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
  
  public void setProgram(BPProgram p) {
    program = p;
  }
};

