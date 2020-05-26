package com.lionsoft.bp2java;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BPEntryPoint extends BPNode {
 
  public BPEntryPoint() {
    super();
    setType (ENTRY_POINT);
  }
 
  public BPEntryPoint(JSONObject jo) {
    super(jo);
    setType (ENTRY_POINT);
  }
  
  public String compile() {
    if (getOutputConnector(0).isConnected())
      return (getOutputConnector(0).getConnectedNode().compile());
    else
      return ("");
  }

};

