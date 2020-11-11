package com.lionsoft.bp2java;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BPEvent extends BPNode {

  EventType event;
 
  public BPEvent() {
    super();
    setType (EVENT);
  }
 
  public BPEvent(JSONObject jo) {
    super(jo);
    setType (EVENT);
    //System.out.println("BPEvent "+getName()+" "+(String) jo.get("event"));
    event = EventType.valueOf((String) jo.get("event"));
  }

  public EventType getEvent() {
    return(event);
  }
  
  public String compile() {
    if (getOutputConnector(0).isConnected())
      return (getOutputConnector(0).getConnectedNode().compile());
    else
      return ("");
  }

};

