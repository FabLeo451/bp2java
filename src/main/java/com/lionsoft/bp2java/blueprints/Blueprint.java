package com.lionsoft.bp2java;

import java.util.*;
import java.io.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Iterator;
import java.awt.image.BufferedImage;
import java.awt.Color;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.traverse.BreadthFirstIterator;
import com.mxgraph.layout.*;
import com.mxgraph.util.mxCellRenderer;
import org.jgrapht.ext.*;
import javax.imageio.ImageIO;

public class Blueprint {
  Code resultCode = Code.SUCCESS;
  String message = "OK";

  JSONObject jbp;

  protected String id;
  protected Integer internalId;
  protected String name, method;
  protected BlueprintType type;

  //Map<Integer, String> types;   // Deprecated
  Map<String, BPType> mapTypes; // Type name is the key

  BPEntryPoint entryPointNode;
  BPReturn returnNode;
  List<BPNode> nodes;
  List<BPVariable> variables;
  List<String> importList;
  List<String> jarList;
  List<String> locals;

  protected BPProgram program;

  protected String declareSection;
  protected String includedJava = "";
  protected String javaSource;

  protected DefaultDirectedGraph<BPNode, DefaultEdge> graph;
  List<Block> blocks = new ArrayList<Block>();

  public Blueprint() {
    nodes = new ArrayList<BPNode>();
    variables = new ArrayList<BPVariable>();
    importList = new ArrayList<String>();
    jarList = new ArrayList<String>();
    locals = new ArrayList<String>();
    name = "myBlueprint";
    method = name;

    graph = new DefaultDirectedGraph<>(DefaultEdge.class);

    // Standard types
    // {"_exec_", "int", "float", "String", "Boolean"};
    /*
    types = new HashMap<Integer, String>();
    types.put(0, "_exec");
    types.put(1, "Integer");
    types.put(2, "Double");
    types.put(3, "String");
    types.put(4, "Boolean");*/

    mapTypes = new HashMap<String, BPType>();
    mapTypes.put("_exec", new BPType(0, "_exec", null));
    mapTypes.put("Integer", new BPType(1, "Integer", null));
    mapTypes.put("Double", new BPType(2, "Double", null));
    mapTypes.put("String", new BPType(3, "String", null));
    mapTypes.put("Boolean", new BPType(4, "Boolean", null));
  }

  public Blueprint(BPProgram program, JSONObject jo) {
    this();
    this.program = program;
    resultCode = createFromJson(jo);

    /*if (result != SUCCESS) {
      System.err.println("Error "+result+" "+message);
    }*/
  }

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

  public Integer getInternalId() {
    return (internalId);
  }

  public String getName() {
    return (name);
  }

  public Code getResult() {
    return (resultCode);
  }

  public String getMessage() {
    return (message);
  }

  public void setResult(Code code, String message) {
    this.resultCode = code;
    this.message = message;
  }

  public BlueprintType getType() {
    return (type);
  }

  public String getMethodName() {
    return (method);
  }

  public List<BPNode> getNodes() {
    return (nodes);
  }

  public void addJar(String jar) {
    if (!jarList.contains(jar))
      jarList.add(jar);
  }

  public Code createFromJson(JSONObject jbp) {
    id = (String) jbp.get("id");
    internalId = (Integer) ((Long) jbp.get("internalId")).intValue();
    name = (String) jbp.get("name");
    //type = ((Long) jbp.get("type")).intValue();
    type = BlueprintType.valueOf((String) jbp.get("type"));
    method = jbp.containsKey("method") ? (String) jbp.get("method") : name.replace(" ", "_");

		// Import list
		if (jbp.containsKey("import")) {
      JSONArray jImportArray = (JSONArray) jbp.get("import");

      for (int i=0; i < jImportArray.size(); i++) {
				    String item = (String) jImportArray.get(i);

            if (!importList.contains(item)) {
              importList.add(item);
              //System.out.println("Blueprint "+name+" import: "+item);
            }
      }
    }

		// Jar list
		if (jbp.containsKey("jar")) {
      JSONArray jJarArray = (JSONArray) jbp.get("jar");

      for (int i=0; i < jJarArray.size(); i++)
        addJar((String) jJarArray.get(i));
    }

    // Types: convert json array into <1,Integer> <2;FLoat> ecc...

    if (jbp.containsKey("types")) {
      JSONArray jTypesArray = (JSONArray) jbp.get("types");

      for (int i = 0; i < jTypesArray.size(); i++) {
        // Deprecated list (id is the key)

        JSONObject jtype = (JSONObject) jTypesArray.get(i);
/*        int id = ((Long)jtype.get("id")).intValue();

        types.put(id, (String)jtype.get("name"));*/

        // New list (name is the key)

        mapTypes.put((String) jtype.get("name"),
                     new BPType(0, // Numeric id is deprecated
                                (String) jtype.get("name"),
                                jtype.containsKey("init") ? (String) jtype.get("init") : null));
      }
    }

    // Variables

    JSONArray jVarArray = (JSONArray) jbp.get("variables");

    declareSection = "";

    for (int i = 0; i < jVarArray.size(); i++) {
      JSONObject jvar = (JSONObject) jVarArray.get(i);

      BPVariable v = new BPVariable(jvar);

      if (!v.isReferenced())
        continue;

      v.setType(mapTypes.get(v.getTypeName()));

      if (v.isGlobal()) {
        //System.out.println("Adding global "+v.toString());
        program.addGlobal(v);
      } else {
        variables.add(v);
        declareSection += v.getDeclaration() + ";" + System.lineSeparator();
      }
    }

    // Nodes

    JSONArray jnodeArray = (JSONArray) jbp.get("nodes");

    //System.out.println("Blueprint has "+jnodeArray.size()+" nodes");

    //BPNode node;

    for (int i = 0; i < jnodeArray.size(); i++) {
      JSONObject jnode = (JSONObject) jnodeArray.get(i);

      String name = (String) jnode.get("name");
      int type = ((Long)jnode.get("type")).intValue();

      //System.out.println(this.name+"."+name+" "+type);

      BPNode node = null;

      switch (type) {
        case BPNode.ENTRY_POINT:
          entryPointNode = new BPEntryPoint(this, jnode);
          nodes.add(entryPointNode);
          node = (BPNode) entryPointNode;
          break;

        case BPNode.RETURN:
          returnNode = new BPReturn(this, jnode);
          nodes.add(returnNode);
          node = (BPNode) returnNode;

          /*if (returnNode.returnsValue())
            System.out.println("Returns "+returnNode.getReturnTypeName());*/
          break;

        case BPNode.SEQUENCE:
          nodes.add(new BPSequence(this, jnode));
          break;

        case BPNode.BRANCH:
          nodes.add(new BPBranch(this, jnode));
          break;

        case BPNode.OPERATOR:
          BPOperator o = new BPOperator(this, jnode);
          nodes.add(o);
/*
          for (int k=0; k<o.importList.size(); k++) {
            if (!importList.contains(o.importList.get(k)))
              importList.add(o.importList.get(k));
          }
*/
/*
          for (int k=0; k<o.jarList.size(); k++) {
            if (!jarList.contains(o.jarList.get(k)))
              jarList.add(o.jarList.get(k));
          }
*/

          for (int k=0; k<o.referenceList.size(); k++) {
            if (o.referenceList.get(k).getRefType() == Reference.LOCAL)
              locals.add(o.referenceList.get(k).getDeclaration());
          }

          break;

        case BPNode.FUNCTION:
          BPFunction f = new BPFunction(this, jnode);
          nodes.add(f);
          node = (BPNode) f;
/*
          for (int k=0; k<f.importList.size(); k++) {
            if (!importList.contains(f.importList.get(k)))
              importList.add(f.importList.get(k));
          }
*/
/*
          for (int k=0; k<f.jarList.size(); k++) {
            if (!jarList.contains(f.jarList.get(k)))
              jarList.add(f.jarList.get(k));
          }
*/
          for (int k=0; k<f.referenceList.size(); k++) {
            if (f.referenceList.get(k).getRefType() == Reference.LOCAL)
              locals.add(f.referenceList.get(k).getDeclaration());
          }

          break;

        case BPNode.BLUEPRINT:
          BPBlueprint bp = new BPBlueprint(this, jnode);
          bp.setBlueprintList(program.getBlueprintList());
          nodes.add(bp);

          for (int k=0; k<bp.referenceList.size(); k++) {
            if (bp.referenceList.get(k).getRefType() == Reference.LOCAL)
              locals.add(bp.referenceList.get(k).getDeclaration());
          }
          break;

        case BPNode.GET:
          nodes.add(new BPGet(this, jnode));
          break;

        case BPNode.SET:
          nodes.add(new BPSet(this, jnode));
          break;

        case BPNode.WHILE_LOOP:
          nodes.add(new BPWhileLoop(this, jnode));
          break;

        case BPNode.SWITCH_INTEGER:
          nodes.add(new BPSwitchInteger(this, jnode));
          break;

        case BPNode.FOR_LOOP:
          BPForLoop fl = new BPForLoop(this, jnode);
          nodes.add(fl);

          for (int k=0; k<fl.referenceList.size(); k++) {
            if (fl.referenceList.get(k).getRefType() == Reference.LOCAL)
              locals.add(fl.referenceList.get(k).getDeclaration());
          }
          break;

        case BPNode.EXIT:
          nodes.add(new BPExit(this, jnode));
          break;

        case BPNode.EVENT:
          BPEvent event = new BPEvent(this, jnode);
          nodes.add(event);
          //eventNodes.put(EventType.values()[event.getType()], event);
          break;

        default:
          System.out.println("Unknown node type: "+type);
          break;
      }

      if (node != null) {
        for (int k=0; k<node.includeList.size(); k++) {
          String filename = program.getRootDir()+"/"+node.includeList.get(k).replace("{path}", node.nodePath);
          //System.out.println("Including "+filename);

          try {
            includedJava += new String (Files.readAllBytes(Paths.get(filename)));
          } catch (IOException e) {
            System.err.println("Can't include "+filename+": "+e.getMessage());
          }
        }

        if (node.getDeclare() != null)
          program.appendToGlobals(node.getDeclare() + System.lineSeparator());
      }
    }

    // Create graph
    for (BPNode node : nodes) {
      graph.addVertex(node);
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
        graph.addEdge(c1.getNode(), c2.getNode());
      }
      else {
        // Data connection (next points to previous)
        c2.connectTo(c1);
        // graph.addEdge(c2.getNode(), c1.getNode()); // Don't add data edges since introduces cycles
      }
    }

    return (Code.SUCCESS);
  }

  public String getJavaSource() {
    return(javaSource);
  }

  public boolean checkGraph() {
    // Check if Return node is reachable
    if (entryPointNode != null && returnNode != null) {
      ConnectivityInspector<BPNode, DefaultEdge> ci = new ConnectivityInspector<BPNode, DefaultEdge>(graph);

      if (!ci.pathExists(entryPointNode, returnNode))
        System.out.println("Warning: blueprint "+this.name+": Return node not reachable");
    }

    // Check cycles
    CycleDetector<BPNode, DefaultEdge> cd = new CycleDetector<BPNode, DefaultEdge>(graph);

    if (cd.detectCycles()) {
      setResult(Code.ERR_CYCLES, "Cycle detected");
      return false;
    }

    return true;
  }

  /**
   * Find nodes that start blocks
   */
  public void findBlocks() {
      blocks.add(new Block(entryPointNode));

      BreadthFirstIterator iterator = new BreadthFirstIterator<>(graph);
      while (iterator.hasNext()) {
          BPNode node = (BPNode) iterator.next();
          //System.out.println(node.toString());

          //if (node.branches() || node.getRef() > 1)
          if (graph.outgoingEdgesOf(node).size() > 1 || graph.incomingEdgesOf(node).size() > 1)
              blocks.add(new Block(node));
      }

      for (Block b: blocks) {
          System.out.println(b.toString());
      }
  }

  /**
   * Assign blocks to all nodes
   */
  public void propagateBlocks() {
      for (Block b: blocks) {
          BPNode start = b.getStart();
          start.propagateBlock();
      }
  }

  /**
   * Link blocks
   */
  public void linkBlocks() {
      for (Block b: blocks) {
          BPNode start = b.getStart();

          if (start.getPrevious() != null) {
              Block prev = start.getPrevious().get(0).getBlock();

              for (BPNode n: start.getPrevious()) {

              }
          }
      }
  }

  public String compile() {
    String /*functionCode,*/ scope, returnType, header, parameters = "", body = "";

    if (!checkGraph())
      return null;

    findBlocks();
    propagateBlocks();

    javaSource = "";

    //scope = (getType() == Blueprint.MAIN) ? "public static" : "public";
    scope = "public static";
    //returnType = returnNode.returnsValue() ? /*BPConnector.typeToString(returnNode.getReturnType())*/ (String) types.get(returnNode.getReturnType()) : "void";
    returnType = (returnNode.returnsValue() ? returnNode.getReturnTypeName() : "void") +
                 (returnNode.getReturnArray() == 1 ? "[]" : "");

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

      parameters += "@BPConnector(id="+entryPointNode.getOutputConnector(i).getId()+")" + entryPointNode.getOutputConnector(i).getDataTypeName() + dim + " " + entryPointNode.getOutputConnector(i).getLabel();
    }

    header = scope + " " + returnType + " " + getMethodName() + "("+parameters+") throws ExitException ";

    for (int k=0; k<locals.size(); k++)
      declareSection += locals.get(k) + System.lineSeparator();
  /*
    for (int i=0; i<nodes.size(); i++) {
      System.out.println(nodes.get(i).getCode());
    }*/


    Block block = entryPointNode.compile();

    if (body == null)
      return null;

    javaSource = "@Blueprint(id=\""+id+"\", internalId="+internalId+", name=\""+name+"\", type=\""+type+"\")" +
                   (returnNode.returnsValue() ?
                      "@BPConnector(id="+returnNode.getInputConnector(1).getId()+", "+
                                   "label=\""+returnNode.getInputConnector(1).getLabel()+"\")" :
                      "") +
                   header + " {" + System.lineSeparator() +
                   declareSection + System.lineSeparator() +
                   block.getSourceCode() + System.lineSeparator() +
                   //(returnNode.returnsValue() ? returnNode.getCode(): "") +
                   "}" + System.lineSeparator();

    return javaSource;
  }

    public void setProgram(BPProgram p) {
        program = p;
    }

    public String getIncludedJava() {
        return includedJava != null ? includedJava : "";
    }

    public boolean createImage(String imageFile) {

/*
        try {
            imgFile.createNewFile();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }*/
/*
        JGraphXAdapter<String, DefaultEdge> graphAdapter =  new JGraphXAdapter<String, DefaultEdge>(graph);
        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());*/


        JGraphXAdapter<BPNode, DefaultEdge> graphAdapter = new JGraphXAdapter<BPNode, DefaultEdge>(graph);
        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());
        BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, new Color(0f,0f,0f,.5f), true, null);
        File file = new File(imageFile);

        try {
            ImageIO.write(image, "PNG", file);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
        //ListenableGraph<BPNode, DefaultEdge> g = new DefaultListenableGraph<>(graph);

        return true;
    }
/*
  public void addNodeToGraph(BPNode node) {
    graph.addVertex(node);
  }

  public void addEdgeToGraph(BPNode node1, BPNode node2) {
    graph.addEdge(node1, node2);
  }*/
};
