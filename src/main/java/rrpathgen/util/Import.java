package rrpathgen.util;

import rrpathgen.Main;
import rrpathgen.data.Marker;
import rrpathgen.data.Node;
import rrpathgen.data.NodeManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Import {
    //(?:^\s*Trajectory\s+(?:(\w+)\s+\=\s+\w+\.(?:\w|\s)+\((?:\w|\s)+))(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:\(|\,|\s)+(?:\w+\.\w+\()((?:[+-]?(?:\d*\.)?\d+)+)/gm
    //(?:\.((?:\w|\s)+)\((?:\w|\s)+)(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:\(|\,|\s|\))+(?:\w+\.\w+\()?((?:[+-]?(?:\d*\.)?\d+)+)(?:(?:\(|\,|\s|\))+(?:\w+\.\w+\()((?:[+-]?(?:\d*\.)?\d+)+))?

    private final Pattern dataPattern = Pattern.compile("(?:\\.((?:\\w|\\s)+)\\((?:new Pose2d|new Vector2d))\\s*(?:\\()(.*)(?=\\)\\s*\\))");
    private final Pattern trajectoryPattern = Pattern.compile("(\\w+)\\s*\\=\\s*(?:\\s*\\w+(.trajectory(?:Sequence)?Builder))(?:\\s*\\(\\s*)((.|\\r\\n|\\r|\\n)*?)(?=\\.build\\(\\)\\;)");
    private final Pattern markerPattern = Pattern.compile("(?:\\.(UNSTABLE_addTemporalMarkerOffset)\\s*\\(((?:[+-]?(?:\\d*\\.)?\\d+)+),\\s*\\(\\)\\s*\\-\\>\\s*)(?:\\{)((?:.|\\r\\n|\\r|\\n)*?)(?=\\}\\s*\\)\\s*)");
    private final Pattern numberPattern = Pattern.compile("((?:[+-]?(?:\\d*\\.)?\\d+)+)");
    //latest regex to match pose/vector2d
    //(?:\.((?:\w|\s)+)\((?:new Pose2d|new Vector2d))\s*(?:\()(.*)(?=\)\s*\))

    //match any number
    //((?:[+-]?(?:\d*\.)?\d+)+)

    //ignore comments
    //^(?!\s*\/\/).*

    //new regex with commenting
    //(\w+)\s*\=\s*(?:\s*\w+(.trajectory(?:Sequence)?Builder))(?:\s*\(\s*)((?:.|\r\n|\r|\n)*?)(?=\.build\(\)\;)


    private Main main;
    public Import(Main main){
        this.main = main;
    }


    /**
     * Reads the file and generates a list of node managers
     * @param file
     * @return LinkedList<NodeManager>
     */
    public LinkedList<NodeManager> read(File file){
        String allText = "";
        LinkedList<NodeManager> managers = new LinkedList<>();
        try{
            Scanner reader = new Scanner(file);
            while(reader.hasNextLine()){
                String line = reader.nextLine();
                if(line.matches("^(?!\\s*\\/\\/).*")) //lines will only be added if they aren't commented
                    allText += line + "\n";
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//        allText.replaceAll("\n", "");

        Matcher matcher = trajectoryPattern.matcher(allText);
        LinkedList<Integer> starts = new LinkedList<>();
        LinkedList<Integer> ends = new LinkedList<>();
//        Set<String> data = new HashSet<>();
        while(matcher.find()) {
            NodeManager manager = new NodeManager(new ArrayList<>(), managers.size());
            starts.add(matcher.start());
            ends.add(matcher.end());
            manager.name = matcher.group(1);
            managers.add(manager);
        }
        for (int i = 0; i < managers.size(); i++) {

            NodeManager manager = managers.get(i);
            //matches all the data within the trajectory builder
            Matcher data = dataPattern.matcher(allText.substring(starts.get(i), ends.get(i)));
            while(data.find()){
                Node node = new Node();
                String type = data.group(1);
                try{
                    node.setType(Node.Type.valueOf(type));
                } catch (IllegalArgumentException e) {
                    //e.printStackTrace();
                    node.setType(Node.Type.splineTo);
                }
                node.x = main.scale*72;
                node.y = main.scale*72;
                node.robotHeading = 90;
                node.robotHeading = 90;
                Matcher numbers = numberPattern.matcher(data.group(2));

                List<Double> nlist = new LinkedList<>();
                while(numbers.find()){
                    try{
                        nlist.add(Double.parseDouble(numbers.group(1)));
                    } catch (Exception e){
                        e.printStackTrace();
                        nlist.add(0.0);
                    }
                }
                try{
                    node.x = (nlist.get(0)+72.0)*main.scale;
                    node.y = (72.0 - nlist.get(1))*main.scale;
                    switch (node.getType()){
                        case splineTo:
                        case splineToConstantHeading:
                            node.splineHeading = nlist.get(2)-90.0;
                            node.robotHeading = node.splineHeading;
                            break;
                        case splineToSplineHeading:
                        case splineToLinearHeading:
                            node.splineHeading = nlist.get(3)-90.0;
                            node.robotHeading = nlist.get(2)-90.0;
                            break;
                        case lineTo:
                        case lineToConstantHeading:
                            break;
                        case lineToSplineHeading:
                        case lineToLinearHeading:
                            node.robotHeading = nlist.get(2)-90.0;
                            break;
                        default:


                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                    node.x = main.scale*72;
                    node.y = main.scale*72;
                    node.robotHeading = 90;
                    node.robotHeading = 90;
                }
                manager.add(node);
            }
            Matcher markers = markerPattern.matcher(allText.substring(starts.get(i), ends.get(i)));
            while(markers.find()){
                Marker marker = new Marker(Double.parseDouble(markers.group(2)), markers.group(3).trim(), Marker.Type.addTemporalMarker);
                manager.add(marker);
            }
        }
        return managers;
    }
}
//(\.(?:\w|\s)+\((?:\w|\s)+)(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:\(|\,|\s)+(?:\w+\.\w+\()((?:[+-]?(?:\d*\.)?\d+)+)