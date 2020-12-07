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

//import com.lionsoft.bp2java.BlueprintType;

public class BPProgram {

  private List<String> importList;
  public List<String> jarList;
  private List<Blueprint> blueprintList;
  private Map<String, BPVariable> globals;

  private String rootDir = ".";
  private String name;
  private String manifest;
  private String code;
  private String importSection;
  private String globalSection = "";

  public BPProgram() {
    importList = new ArrayList<String>();
    jarList = new ArrayList<String>();
    blueprintList = new ArrayList<Blueprint>();
    globals = new HashMap<String, BPVariable>();
    name = "Program";
    manifest = null;
    code = "";
  }

  public void setRootDir(String d) {
    this.rootDir = d;
  }

  public String getRootDir() {
    return (this.rootDir);
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

  public boolean addBlueprint (String filename) {
    JSONObject jbp;
    JSONParser jsonParser = new JSONParser();
    BlueprintType type;

    //System.out.println("Loading "+filename);
    // https://crunchify.com/how-to-read-json-object-from-file-in-java/
    try {
      FileReader reader = new FileReader(filename);

      jbp = (JSONObject) jsonParser.parse(reader);
      type = BlueprintType.valueOf((String) jbp.get("type"));

    } catch (FileNotFoundException e) {
        e.printStackTrace();
        return false;
    } catch (IOException e) {
        e.printStackTrace();
        return false;
    } catch (ParseException e) {
        e.printStackTrace();
        return false;
    }

    switch (type) {
      case EVENTS:
        BlueprintEvents be = new BlueprintEvents (this, jbp);
        //be.setProgram(this);
        blueprintList.add(be);

        for (BPNode n: be.getNodes()) {
          if (n.getType() == BPNode.EVENT)
            be.addEventNode((BPEvent) n);
        }
        break;

      default:
        Blueprint b = new Blueprint (this, jbp);

				if (b.getResult() != Code.SUCCESS) {
          System.err.println(b.getMessage());
          return false;
        }

        //b.setProgram(this);
        blueprintList.add(b);
        break;
    }

    return true;
  }

  public void addGlobal (BPVariable v) {
    if (!globals.containsKey(v.getName())) {
      globals.put(v.getName(), v);
    }
  }

  public void appendToGlobals (String s) {
    if (!globalSection.contains(s))
      globalSection += s;
  }

  public void importPackage (String p) {
    /*for (String s : importList) {
      if (s.equals(p))
        return;
    }*/

		if (!importList.contains(p))
      importList.add (p);
  }

  public String getJavaCode() {
    return (code);
  }

  public String toJavaCode () {
    String template = "", importSection = "", /*globalSection = "", */ includeSection = "";

    // Compile blueprints
    
    for (int i = 0; i < blueprintList.size(); i++) {
      Blueprint b = blueprintList.get(i);
      
      //System.out.println("Compiling "+b.getName());
      
      if (b.compile() == null) {
        System.err.println("Error: Blueprint "+b.getName()+": "+b.getMessage());
        return null;
      }
    }    

    // Load template

    try {
      InputStream inputStream = getClass().getResourceAsStream("/templates/template.java");
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      template = reader.lines().collect(Collectors.joining(System.lineSeparator()));
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Build program
    
    for (int i = 0; i < blueprintList.size(); i++) {
      //System.out.println("Blueprint "+blueprintList.get(i).getName());

      Blueprint b = blueprintList.get(i);

      String source = b.getJavaSource();
/*      
      if (source == null) {
        System.err.println(b.getMessage());
        return null;
      }*/
        
      code += source;

      for (int k=0; k<b.importList.size(); k++)
        //importList.add(b.importList.get(k));
        importPackage(b.importList.get(k));

      for (int k=0; k<b.jarList.size(); k++)
        jarList.add(b.jarList.get(k));

      // Include section
      includeSection += b.getIncludedJava();
    }

    // Import section
    for (int i = 0; i < importList.size(); i++) {
      importSection += "import "+importList.get(i)+";" + System.lineSeparator();
    }

    // Global section
    for (Map.Entry<String, BPVariable> entry : globals.entrySet()) {
     globalSection += "static "+entry.getValue().getDeclaration()+";" + System.lineSeparator();
    }

    //System.out.println("Updating template...");

    template = template
               .replace("{import}", importSection)
               .replace("{globals}", globalSection)
               .replace("{include}", includeSection)
               .replace("{user-functions}", code)
               .replace("{className}", "Program")
               .replace("{programName}", getName());

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
