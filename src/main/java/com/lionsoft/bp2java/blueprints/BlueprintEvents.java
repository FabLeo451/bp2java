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

  protected Map<EventType, BPEvent> eventNodes = new HashMap<EventType, BPEvent>();

  public BlueprintEvents() {
    super();
    type = BlueprintType.EVENTS;
  }

  public BlueprintEvents(BPProgram program, JSONObject jo) {
    super(program, jo);
  }

  public void addEventNode(BPEvent node) {
    eventNodes.put(node.getEvent(), node);
  }

  public Map<EventType, BPEvent> getEventNodes() {
    return(eventNodes);
  }

  // Overrides
  public String compile() {
    String /*functionCode,*/ header, declareSection="", body;
    String begin="", exception="", end="";

    javaSource = "";

    if (!checkGraph())
      return null;

    if (!checkNodes())
      return null;
/*
    List<Block> blocksBegin = findBlocks(eventNodes.get(EventType.BEGIN));
    Block beginBl = startBlock;
    propagateBlocks(blocksBegin);
    List<Block> blocksException = findBlocks(eventNodes.get(EventType.EXCEPTION));
    Block exceptionBl = startBlock;
    propagateBlocks(blocksException);
    List<Block> blocksEnd = findBlocks(eventNodes.get(EventType.END));
    Block endBl = startBlock;
    propagateBlocks(blocksEnd);
*/
    ExecutionTree beginTree = new ExecutionTree(graph, eventNodes.get(EventType.BEGIN));
    ExecutionTree exceptionTree = new ExecutionTree(graph, eventNodes.get(EventType.EXCEPTION));
    ExecutionTree endTree = new ExecutionTree(graph, eventNodes.get(EventType.END));

    header = "public static void " + getMethodName() + "(EventType event, String message) throws ExitException ";

     for (int k=0; k<locals.size(); k++)
      declareSection += locals.get(k) + System.lineSeparator();
/*
    begin = eventNodes.get(EventType.BEGIN).compile().getSourceCode();
    exception = eventNodes.get(EventType.EXCEPTION).compile().getSourceCode();
    end = eventNodes.get(EventType.END).compile().getSourceCode();
*/
    begin = beginTree.toJava();
    exception = exceptionTree.toJava();
    end = endTree.toJava();

    if (begin == null || exception == null || end == null)
      return null;

		body = "if (event == EventType.BEGIN) {"+ System.lineSeparator() +
		       begin +
           "} else if (event == EventType.EXCEPTION) {"+ System.lineSeparator() +
           exception +
           "} else if (event == EventType.END) {"+ System.lineSeparator() +
           end +
           "} else { }"+ System.lineSeparator();


    javaSource = header + " {" + System.lineSeparator() +
                   declareSection + System.lineSeparator() +
                   body + System.lineSeparator() +
                   "}" + System.lineSeparator();

    return javaSource;
  }

};
