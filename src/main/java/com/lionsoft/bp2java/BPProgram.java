package com.lionsoft.bp2java;

import java.util.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.io.*;
import java.nio.file.Files;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Iterator;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;

public class BPProgram {

  private List<String> importList;
  public List<String> jarList;
  private List<Blueprint> blueprintList;
  
  private String name;
  private String manifest;
  private String code;
  private String importSection;
 
  public BPProgram() {
    importList = new ArrayList<String>();
    jarList = new ArrayList<String>();
    blueprintList = new ArrayList<Blueprint>();
    name = "Program";
    manifest = null;
    code = "";
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return (this.name);
  }
  
  public void setManifest(String manifest) {
    this.manifest = manifest;
  }
  
  public String getManifest() {
    return (this.manifest);
  }
  
  public List<Blueprint> getBlueprintList() {
    return (blueprintList);
  }

  public void addBlueprint (String filename) {
    Blueprint b = new Blueprint ();
    b.setProgram(this);
    
    if (b.load(filename) != Blueprint.SUCCESS)
      System.err.println("Error loading blueprint "+filename);

    blueprintList.add(b);
  }

  public void importPackage (String p) {
    for (String s : importList) {
      if (s.equals(p))
        return;
    }
            
    importList.add (p);
  }
  
  public String getJavaCode() {
    return (code);
  }

  public String toJavaCode () {
    String template = "", importSection = "", globals = "";
    
    // Load template
    
    try {
      InputStream inputStream = getClass().getResourceAsStream("/templates/template.java");
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      template = reader.lines().collect(Collectors.joining(System.lineSeparator()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  
    //code += "public class "+getName()+" {" + System.lineSeparator();
    
    for (int i = 0; i < blueprintList.size(); i++) {
      //System.out.println("Blueprint "+blueprintList.get(i).getName());
      
      Blueprint b = blueprintList.get(i);
      
      code += b.toJavaCode();
      
      for (int k=0; k<b.importList.size(); k++)
        importList.add(b.importList.get(k));
      
      for (int k=0; k<b.jarList.size(); k++)
        jarList.add(b.jarList.get(k));
    }
     
    for (int i = 0; i < importList.size(); i++) {
      importSection += "import "+importList.get(i)+";" + System.lineSeparator();
    }
    
    //System.out.println("Updating template...");
    
    template = template
               .replace("{import}", importSection)
               .replace("{globals}", globals)
               .replace("{className}", "Program")
               .replace("{programName}", getName())
               .replace("{code}", code);
               
    code = template;

    return code;
  }
  
  public boolean format() {
    //System.out.println("Formatting...");
    
    try {
      code = new Formatter().formatSource(code);
    } 
    catch (FormatterException e) {
      System.err.println(e.getMessage());
      return false;
    }
    
    return true;
  }
};

