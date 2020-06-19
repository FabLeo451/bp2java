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

  public BlueprintEvents(JSONObject jo) {
    super(jo);
  }

  public void addEventNode(BPEvent node) {
    eventNodes.put(node.getEvent(), node);
  }

  public Map<EventType, BPEvent> getEventNodes() {
    return(eventNodes);
  }

  // Overrides
  public String toJavaCode() {
    String functionCode, scope, returnType, header, parameters = "", body = "";
    
    scope = "public static";
    returnType = "void";
    parameters = "BPEventType event";
    header = scope + " " + returnType + " " + getMethodName() + "("+parameters+") throws ExitException ";

		body = "if (event == BPEventType.BEGIN) {"+ System.lineSeparator() +
           "} else if (event == BPEventType.EXCEPTION) {"+ System.lineSeparator() +
           "} else if (event == BPEventType.END) {"+ System.lineSeparator() +
           "} else { }"+ System.lineSeparator();

/*
     for (int k=0; k<locals.size(); k++)
      declareSection += locals.get(k) + System.lineSeparator();

    body = entryPointNode.compile();
*/
    functionCode = header + " {" + System.lineSeparator() +
                   declareSection + System.lineSeparator() +
                   body + System.lineSeparator() +
                   "}" + System.lineSeparator();
    
    return functionCode;
  }

};

