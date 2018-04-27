package com.parser;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
/*
* This program could parse topological map with structure:
* node:
	WayPoint
	waypoint:
		17.000000,17.000000,0.010200,0.000000,0.000000,-0.841202,0.540721
	edges:
		 WayPoint1, move_base
	vertices:
		0.250000,0.690000
		-0.287000,0.690000
		-0.690000,0.287000
		-0.690000,-0.287000
		-0.287000,-0.690000
		0.250000,-0.690000
   node:
        ..
* */
public class GenerateModelFromTmap {
    public static void main(String[] args) {
        if(args.length == 1) {
            File file = new File(args[0]);
            if(file.exists() && !file.isDirectory()) {
                if (FilenameUtils.getExtension(args[0]).equals("tmap")) {
                    ParserOfTM parserOfTM = new ParserOfTM();
                    parserOfTM.readTmapFile(file);
                    if(parserOfTM.getTopologicalMap().size() != 0) {
                        parserOfTM.generateUPPAALxml(FilenameUtils.removeExtension(args[0]));
                        System.out.println();
                        parserOfTM.generateYamlFile(FilenameUtils.removeExtension(args[0]));
                    } else {
                        System.err.println("Tmap file is empty!");
                    }
                } else {
                    System.err.println("File must be of tmap type!");
                }
            } else {
                System.err.println("File " + args[0] + " does not exist!");
            }
        } else {
            System.err.println("Type as follows: java -jar generateModelFromTmap.jar input_file.tmap");
        }
    }
}
