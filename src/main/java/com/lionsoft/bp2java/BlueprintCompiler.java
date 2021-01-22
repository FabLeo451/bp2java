package com.lionsoft.bp2java;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.IOException;
import java.io.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class BlueprintCompiler {

  static boolean format = false;

  public static void main(String[] argv) {
    String filename = argv[0];
    String output = null;
    String depFile = null;
    int resultCode = 0;
    String imageFile = null;

    BPProgram program = new BPProgram();

    int c;
    LongOpt[] longopts = new LongOpt[8];

    longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
    longopts[1] = new LongOpt("blueprint", LongOpt.REQUIRED_ARGUMENT, null, 'b');
    longopts[2] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'O');
    longopts[3] = new LongOpt("program", LongOpt.REQUIRED_ARGUMENT, null, 'P');
    longopts[4] = new LongOpt("manifest", LongOpt.REQUIRED_ARGUMENT, null, 'm');
    longopts[5] = new LongOpt("root", LongOpt.REQUIRED_ARGUMENT, null, 'r');
    longopts[6] = new LongOpt("format", LongOpt.NO_ARGUMENT, null, 'f');
    longopts[7] = new LongOpt("image", LongOpt.REQUIRED_ARGUMENT, null, 'I');
    //longopts[2] = new LongOpt("maximum", LongOpt.OPTIONAL_ARGUMENT, null, 2);

    Getopt g = new Getopt("bp2java", argv, "b:P:m:O:r:hI:", longopts);
    g.setOpterr(false); // We'll do our own error handling

    while ((c = g.getopt()) != -1)
      switch (c) {
          /*
        case 0:
          arg = g.getOptarg();
          System.out.println("Got long option with value '" +
                             (char)(new Integer(sb.toString())).intValue()
                             + "' with argument " +
                             ((arg != null) ? arg : "null"));
          break;
          */

        case 'h':
          help();
          System.exit(0);
          break;

        case 'b':
          if (!program.addBlueprint (g.getOptarg()))
            System.exit(1);
          break;

        case 'P':
          program.setName(g.getOptarg());
          break;

        case 'm':
          program.setManifest(g.getOptarg());
          break;

        case 'r':
          program.setRootDir(g.getOptarg());
          break;

        case 'f':
          format = true;
          break;

        case 'O':
          output = g.getOptarg();
          break;

        case 'I':
            imageFile = g.getOptarg();
            break;

        case ':':
          System.out.println("Doh! You need an argument for option " + (char)g.getOptopt());
          break;

        case '?':
          System.out.println("The option '" + (char)g.getOptopt() + "' is not valid");
          break;

        default:
          System.out.println("getopt() returned " + c);
          break;
     }

    // Convert to Java code
/*
    program.importPackage("gnu.getopt.Getopt");
    program.importPackage("gnu.getopt.LongOpt");

    program.importPackage("org.apache.log4j.Logger");
    program.importPackage("org.apache.log4j.Level");
*/
    String code = program.toJavaCode();

    if (code != null) {

      if (format) {
        if (program.format())
          code = program.getJavaCode();
        else
          resultCode = 1;
      }

      if (output != null) {
        // Write to file
        try {
            FileWriter file = new FileWriter(output);
            file.write(code);
            file.flush();
        } catch (IOException e1) {
            System.out.println(e1.getMessage());
            resultCode = 1;
        }
      }
      /*else
        System.out.println(code);*/

      if (resultCode == 0) {
        // Manifest
        if (program.getManifest() != null) {
          //System.out.println("Creating "+program.getManifest());
          createManifest(program.getManifest());
        }

        // Update dependencies
        if (depFile != null) {
          //JSONParser jsonParser = new JSONParser();

          try {
            //FileReader reader = new FileReader(depFile);

            //JSONArray jdep = (JSONArray) jsonParser.parse(reader);
            JSONArray jdep = new JSONArray();

            //System.out.println("Dependencies to add: "+program.jarList.size());

            for (int k=0; k<program.jarList.size(); k++) {
              //System.out.println("Adding dependency "+program.jarList.get(k));
              jdep.add(program.jarList.get(k));
            }

            // Save dep.java
            FileWriter file = new FileWriter(depFile);

            file.write(jdep.toJSONString());
            file.flush();

          } catch (FileNotFoundException e) {
              e.printStackTrace();
          } catch (IOException e) {
              e.printStackTrace();
          }/* catch (ParseException e) {
              e.printStackTrace();
          }*/
        }

        // Image
        if (imageFile != null) {
            if (program.getBlueprintList().size() > 0 ) {
                Blueprint b = program.getBlueprintList().get(0);
            }
        }
      }
    }
    else
      resultCode = 2;

    System.exit(resultCode);
  }

	public static boolean createManifest(String filename) {
	  String manifest;

	  manifest = "Manifest-version: 1.0" + System.lineSeparator() +
               "Main-Class: Program" + System.lineSeparator();
               //"Class-path: json-simple-1.1.1.jar"

    try {
      FileWriter file = new FileWriter(filename);
      file.write(manifest);
      file.flush();
    } catch (IOException e1) {
      System.out.println(e1.getMessage());
      return false;
    }

    return true;
	}

  static void help() {
    System.out.println("blueprint -b file");
  }
};
