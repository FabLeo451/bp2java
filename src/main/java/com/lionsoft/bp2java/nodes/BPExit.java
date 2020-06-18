package com.lionsoft.bp2java;

import org.json.simple.JSONObject;

public class BPExit extends BPFunction {

  public BPExit() {
    super();
    setType (BPNode.EXIT);
  }
  
  public BPExit(JSONObject jo) {
    super(jo);
    setType (BPNode.EXIT);
    
    // Do nothing: Java code is in node definition
  }
};


