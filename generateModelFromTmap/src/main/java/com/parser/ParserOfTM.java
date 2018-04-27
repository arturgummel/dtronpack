package com.parser;

import com.google.gson.Gson;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.*;
import java.util.List;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.yaml.snakeyaml.Yaml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class ParserOfTM {

    private LinkedHashMap<String, LinkedHashMap<String, Object>> topologicalMap = null;

    protected void readTmapFile(File tmapFile) {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        topologicalMap = new LinkedHashMap<String, LinkedHashMap<String, Object>>();

        String[] arrayOfAttributes = {"waypoint:", "edges:", "vertices:"};
        try {
            fileReader = new FileReader(tmapFile);
            bufferedReader = new BufferedReader(fileReader);
            int nodeNumber = 0;
            String nodeName = "";
            String current = "";
            String line = "";

            LinkedHashMap<String, Object> attributeMap = null;
            ArrayList attributeList = null;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.equals("node:")) {
                    attributeMap = new LinkedHashMap<String, Object>();
                    current = "node";
                    nodeNumber += 1;
                    nodeName = "";
                    continue;
                }
                if (Arrays.asList(arrayOfAttributes).contains(line)) {
                    current = line.substring(0, line.length() - 1);
                    attributeList = new ArrayList();
                    continue;
                }
                if (current.equals("node")) {
                    nodeName = line;
                    attributeMap.put("node_no", nodeNumber);
                    nodeNumber += 1;
                    attributeMap.put("node_response", nodeNumber);
                } else {
                    String[] lineData = line.split(",");
                    if(attributeList != null) {
                        attributeList.add(Arrays.asList(lineData));
                        attributeMap.put(current, attributeList);
                    }
                }
                topologicalMap.put(nodeName, attributeMap);
            }
            bufferedReader.close();
            fileReader.close();
        } catch (FileNotFoundException e) {
            System.err.println("File not exists");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(topologicalMap);
        /*
        //model in json format
        JSONObject jsonTM = new JSONObject(topologicalMap);
        System.out.println(jsonTM);

        Gson gson = new Gson();
        System.out.println(gson.toJson(topologicalMap));
        try {
            FileWriter file = new FileWriter("model.json");
            file.write(gson.toJson(topologicalMap));
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    protected void generateUPPAALxml(String fileName) {
        if (topologicalMap == null || topologicalMap.size() == 0) {
            System.err.println("Tmap file is empty!");
            return;
        }
        try {
            if (fileName == null || fileName.equals("")) {
                fileName = "uppaal_model.xml";
            } else {
                fileName += "_model.xml";
            }
            System.out.println("Generating UPPAAL xml file...");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            //root element
            Element rootElement = document.createElement("nta");
            document.appendChild(rootElement);
            //declaration
            Element declaration = document.createElement("declaration");
            declaration.setTextContent("chan i_goto, o_response; int i_goto_state;");
            rootElement.appendChild(declaration);
            //robot map template
            rootElement.appendChild(createRobotMapTemplate(document));
            //sut template
            rootElement.appendChild(createSutTemplate(document));
            //system
            Element elementSystem = document.createElement("system");
            elementSystem.setTextContent("\nProcess1 = robot_map();\n" + "Process2 = sut();\n" + "system Process1, Process2;");
            rootElement.appendChild(elementSystem);
            //queries
            Element elementQueries = document.createElement("queries");
            rootElement.appendChild(elementQueries);

            DOMImplementation domImplementation = document.getImplementation();
            DocumentType doctype = domImplementation.createDocumentType("doctype",
                    "-//Uppaal Team//DTD Flat System 1.1//EN",
                    "http://www.it.uu.se/research/group/darts/uppaal/flat-1_2.dtd");
            document.setXmlStandalone(true);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(fileName));
            transformer.transform(domSource, streamResult);

            System.out.println("File saved as " + fileName);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    protected void generateYamlFile(String fileName) {
        if (topologicalMap == null || topologicalMap.size() == 0) {
            System.err.println("Tmap file is empty!");
            return;
        }
        if (fileName == null || fileName.equals("")) {
            fileName = "model.yaml";
        } else {
            fileName += "_model.yaml";
        }
        System.out.println("Generating nodemap Yaml file...");
        LinkedHashMap<String, LinkedHashMap<String, Object>> yamlMap = new LinkedHashMap<>();
        LinkedHashMap<String, Object> nodeMap = new LinkedHashMap<>();
        LinkedHashMap<String, Object> nodes = new LinkedHashMap<>();

        topologicalMap.forEach((k, v) -> {
            nodeMap.put(v.get("node_no").toString(), k);
            List<List<String>> waypointList = (ArrayList) topologicalMap.get(k).get("waypoint");
            LinkedHashMap<Object, Float> coordninates = new LinkedHashMap<>();
            Float x = Float.parseFloat(waypointList.get(0).get(0));
            Float y = Float.parseFloat(waypointList.get(0).get(1));
            coordninates.put("x", x);
            coordninates.put("y", y);
            nodes.put(k, coordninates);
        });

        yamlMap.put("node_map", nodeMap);
        yamlMap.put("nodes", nodes);
        Yaml yaml = new Yaml();
        FileWriter writer = null;
        try {
            writer = new FileWriter(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        yaml.dump(yamlMap, writer);
        System.out.println("File saved as " + fileName);
    }

    private Element createRobotMapTemplate(Document document) {
        Element robotMapTemplate = document.createElement("template");

        Element robotMapName = document.createElement("name");
        robotMapName.setAttribute("x", "5");
        robotMapName.setAttribute("y", "5");
        robotMapName.setTextContent("robot_map");
        robotMapTemplate.appendChild(robotMapName);

        Element templateDeclaration = document.createElement("declaration");
        templateDeclaration.setTextContent("// Place local declarations here.");
        robotMapTemplate.appendChild(templateDeclaration);

        topologicalMap.forEach((k, v) -> {
            robotMapTemplate.appendChild(locationSend(document, k, v));
            robotMapTemplate.appendChild(locationReceive(document, k, v));
        });

        Element elementInitRef = document.createElement("init");
        elementInitRef.setAttribute("ref", "id1");
        robotMapTemplate.appendChild(elementInitRef);

        topologicalMap.forEach((k, v) -> {
            List<List<String>> edgesList = (ArrayList) topologicalMap.get(k).get("edges");
            for (int i = 0; i < edgesList.size(); i++) {
                robotMapTemplate.appendChild(transitionSend(document, k, v, edgesList.get(i).get(0)));
                robotMapTemplate.appendChild(transitionReceive(document, k, v, edgesList.get(i).get(0)));
            }
        });
        return robotMapTemplate;
    }

    private Element createSutTemplate(Document document) {
        Element elementSutTemplate = document.createElement("template");
        Element elementSutName = document.createElement("name");
        elementSutName.setTextContent("sut");
        Element elemLocation = document.createElement("location");
        elemLocation.setAttribute("id", "id3");
        elemLocation.setAttribute("x", "0");
        elemLocation.setAttribute("y", "0");
        Element elemInit = document.createElement("init");
        elemInit.setAttribute("ref", "id3");

        elementSutTemplate.appendChild(elementSutName);
        elementSutTemplate.appendChild(elemLocation);
        elementSutTemplate.appendChild(elemInit);
        elementSutTemplate.appendChild(sutTransitionSend(document));
        elementSutTemplate.appendChild(sutTransitionReceive(document));
        return elementSutTemplate;
    }

    private Element locationSend(Document document, String key, LinkedHashMap value) {
        Element elementLocationSend = document.createElement("location");
        elementLocationSend.setAttribute("id", "id" + value.get("node_no"));
        if(topologicalMap.get(key).get("waypoint") == null) {
            System.err.println("Tmap file has wrong structure!");
            System.exit(0);
        }
        List<List<String>> waypointList = (ArrayList) topologicalMap.get(key).get("waypoint");
        elementLocationSend.setAttribute("x", String.valueOf((int) Float.parseFloat(waypointList.get(0).get(0))));
        elementLocationSend.setAttribute("y", String.valueOf((int) Float.parseFloat(waypointList.get(0).get(1))));
        Element elementName = document.createElement("name");
        elementName.setTextContent(key);
        elementLocationSend.appendChild(elementName);

        return elementLocationSend;
    }

    private Element locationReceive(Document document, String key, LinkedHashMap value) {
        Element elementLocationReceive = document.createElement("location");
        elementLocationReceive.setAttribute("id", "id" + value.get("node_response"));
        Element itname2 = document.createElement("name");
        itname2.setTextContent(key + "_Res");
        elementLocationReceive.appendChild(itname2);
        return elementLocationReceive;
    }

    private Element transitionSend(Document document, String key, LinkedHashMap value, String state) {
        if(topologicalMap.get(state) == null) {
            System.err.println("Tmap file has wrong structure: " + state + " does not exists");
            System.exit(0);
        }
        String sourceName = "node_no";
        String targetName = "node_response";
        String synchronisation = "i_goto!";
        Element elementTransitionSend = document.createElement("transition");
        Element elementSource = document.createElement("source");
        elementSource.setAttribute("ref", "id" + value.get(sourceName));
        Element elementTarget = document.createElement("target");
        elementTarget.setAttribute("ref", "id" + value.get(targetName));
        Element elementSynch = document.createElement("label");
        elementSynch.setAttribute("kind", "synchronisation");
        elementSynch.setTextContent(synchronisation);
        Element elementAssignment = document.createElement("label");
        elementAssignment.setAttribute("kind", "assignment");
        elementAssignment.setTextContent("i_goto_state=" + topologicalMap.get(state).get("node_no"));
        elementTransitionSend.appendChild(elementSource);
        elementTransitionSend.appendChild(elementTarget);
        elementTransitionSend.appendChild(elementSynch);
        elementTransitionSend.appendChild(elementAssignment);

        return elementTransitionSend;
    }

    private Element transitionReceive(Document document, String key, LinkedHashMap value, String state) {
        if(topologicalMap.get(state) == null) {
            System.err.println("Tmap file has wrong structure: " + state + " does not exists");
            System.exit(0);
        }
        String sourceName = "node_response";
        String targetName = "node_no";
        String synchronisation = "o_response?";
        Element elementTransitionReceive = document.createElement("transition");
        Element elementSource = document.createElement("source");
        elementSource.setAttribute("ref", "id" + value.get(sourceName));
        Element elementTarget = document.createElement("target");
        elementTarget.setAttribute("ref", "id" + topologicalMap.get(state).get(targetName));
        Element elementSynch = document.createElement("label");
        elementSynch.setAttribute("kind", "synchronisation");
        elementSynch.setTextContent(synchronisation);
        elementTransitionReceive.appendChild(elementSource);
        elementTransitionReceive.appendChild(elementTarget);
        elementTransitionReceive.appendChild(elementSynch);
        return elementTransitionReceive;
    }

    private Element sutTransitionSend(Document document) {
        Element elementSutTransitionSend = document.createElement("transition");
        Element elementSource = document.createElement("source");
        elementSource.setAttribute("ref", "id3");
        Element elementTarget = document.createElement("target");
        elementTarget.setAttribute("ref", "id3");
        Element elementLabel = document.createElement("label");
        elementLabel.setTextContent("i_goto?");
        elementLabel.setAttribute("kind", "synchronisation");
        elementLabel.setAttribute("x", "10");
        elementLabel.setAttribute("y", "-93");
        Element elementNail = document.createElement("nail");
        elementNail.setAttribute("x", "-8");
        elementNail.setAttribute("y", "-102");
        Element elementNail2 = document.createElement("nail");
        elementNail2.setAttribute("x", "127");
        elementNail2.setAttribute("y", "-51");
        elementSutTransitionSend.appendChild(elementSource);
        elementSutTransitionSend.appendChild(elementTarget);
        elementSutTransitionSend.appendChild(elementLabel);
        elementSutTransitionSend.appendChild(elementNail);
        elementSutTransitionSend.appendChild(elementNail2);
        return elementSutTransitionSend;
    }

    private Element sutTransitionReceive(Document document) {
        Element elementSutTransitionReceive = document.createElement("transition");
        Element elementSource = document.createElement("source");
        elementSource.setAttribute("ref", "id3");
        Element elementTarget = document.createElement("target");
        elementTarget.setAttribute("ref", "id3");
        Element elementLabel = document.createElement("label");
        elementLabel.setTextContent("o_response!");
        elementLabel.setAttribute("kind", "synchronisation");
        elementLabel.setAttribute("x", "-170");
        elementLabel.setAttribute("y", "68");
        Element elementNail = document.createElement("nail");
        elementNail.setAttribute("x", "8");
        elementNail.setAttribute("y", "119");
        Element elementNail2 = document.createElement("nail");
        elementNail2.setAttribute("x", "-119");
        elementNail2.setAttribute("y", "42");
        elementSutTransitionReceive.appendChild(elementSource);
        elementSutTransitionReceive.appendChild(elementTarget);
        elementSutTransitionReceive.appendChild(elementLabel);
        elementSutTransitionReceive.appendChild(elementNail);
        elementSutTransitionReceive.appendChild(elementNail2);
        return elementSutTransitionReceive;
    }

    public LinkedHashMap<String, LinkedHashMap<String, Object>> getTopologicalMap() {
        return topologicalMap;
    }

    public void setTopologicalMap(LinkedHashMap<String, LinkedHashMap<String, Object>> topologicalMap) {
        this.topologicalMap = topologicalMap;
    }

}
