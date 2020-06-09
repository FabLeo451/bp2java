package com.lionsoft.bp2java;

class BPType {

  public final static int SCALAR = 0;
  public final static int ARRAY = 1;
  public final static int MATRIX = 2;

  int id = 0;
  String name = null;
  String init = null;
 
  public BPType() {
  }
 
  public BPType(int id, String name, String init) {
    this();
    
    this.id = id;
    this.name = name;
    this.init = init;
  }
  
  public String toString() {
    return("BPType [id="+this.id+", name="+name+", init="+init+"]");
  }
  
  public int getId() {
    return(this.id);
  }

  public void setId(int id) {
    this.id = id;
  }
  
  public String getName() {
    return (name);
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getInit() {
    return (init);
  }
  
  public void setInit(String init) {
    this.init = init;
  }
  
  public String getInitString() {
    return (init != null ? init : "null");
  }

};

