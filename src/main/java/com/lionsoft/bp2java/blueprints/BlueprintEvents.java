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
    
    header = "public static void " + getMethodName() + "(EventType event, String message) throws ExitException ";

     for (int k=0; k<locals.size(); k++)
      declareSection += locals.get(k) + System.lineSeparator();

    begin = eventNodes.get(EventType.BEGIN).compile();
    exception = eventNodes.get(EventType.EXCEPTION).compile();
    end = eventNodes.get(EventType.END).compile();

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

