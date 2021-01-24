package com.lionsoft.bp2java;

import org.json.simple.JSONObject;

public class BPForLoop extends BPFunction {

  public BPForLoop() {
    super();
    setType (BPNode.FOR_LOOP);
  }

  public BPForLoop(Blueprint blueprint, JSONObject jo) {
    super(blueprint, jo);
    setType (BPNode.FOR_LOOP);
  }
};
