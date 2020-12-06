package com.lionsoft.bp2java;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BPEvent extends BPNode {

  EventType event;
 
  public BPEvent() {
    super();
    setType (EVENT);
  }
 
  public BPEvent(Blueprint blueprint, JSONObject jo) {
    super(blueprint, jo);
    setType (EVENT);
    //System.out.println("BPEvent "+getName()+" "+(String) jo.get("event"));
    event = EventType.valueOf((String) jo.get("event"));
  }

  public EventType getEvent() {
    return(event);
  }
  
  public String translate() { 
    return null;
  }
  
  public String compile() {
    /*if (super.compile() == null)
      return null;*/
      
    if (compiled)
      return(java);
      
    compiled = true;
    
    if (getOutputConnector(0).isConnected())
      java = getOutputConnector(0).getConnectedNode().compile();
    else
      java = "";
      
    return(java);
  }

};

