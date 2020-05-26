package com.lionsoft.bp2java;

public class Reference {

  public static int LOCAL = 0; // Create a new local variable
  public static int INPUT = 1; // Reference an input connector
  
  private int refType;
  public int inputConnector;
  private String type;
  private String name;
  
  public Reference(String t, String n) {
    refType = LOCAL;
    type = t;
    name = n;
  }
  
  public Reference(int i) {
    refType = INPUT;
    inputConnector = i;
  }
  
  public String getDeclaration() {
    return (type+" "+name+" = null;");
  }
  
  public int getRefType() {
    return (refType);
  }
};


