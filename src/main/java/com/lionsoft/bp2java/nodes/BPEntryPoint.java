package com.lionsoft.bp2java;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BPEntryPoint extends BPNode {

  public BPEntryPoint() {
    super();
    setType (ENTRY_POINT);
  }

  public BPEntryPoint(Blueprint blueprint, JSONObject jo) {
    super(blueprint, jo);
    setType (ENTRY_POINT);
  }

  public String translate() {
    return null;
  }

  public Block compile() {
/*
    Block block = new Block(this);

    if (compiled)
      block.setSourceCode(java);*/

    compiled = true;

    if (getOutputConnector(0).isConnected())
      return(getOutputConnector(0).getConnectedNode().compile());
    /*else
      java = "";*/

    return(block);
  }

};
