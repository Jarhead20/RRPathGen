package jarhead;

import javax.lang.model.type.UnknownTypeException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Import {
    //(?:^\s*Trajectory\s+(?:(\w+)\s+\=\s+\w+\.(?:\w|\s)+\((?:\w|\s)+))(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:\(|\,|\s)+(?:\w+\.\w+\()((?:[+-]?(?:\d*\.)?\d+)+)/gm
    //(?:\.((?:\w|\s)+)\((?:\w|\s)+)(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:\(|\,|\s|\))+(?:\w+\.\w+\()?((?:[+-]?(?:\d*\.)?\d+)+)(?:(?:\(|\,|\s|\))+(?:\w+\.\w+\()((?:[+-]?(?:\d*\.)?\d+)+))?

    private final Pattern dataPattern = Pattern.compile("(?:\\.((?:\\w|\\s)+)\\((?:new Pose2d|new Vector2d))(?:(?:\\(|\\,|\\s)+((?:[+-]?(?:\\d*\\.)?\\d+)+))?(?:(?:\\(|\\,|\\s)+((?:[+-]?(?:\\d*\\.)?\\d+)+))?(?:\\(|\\,|\\s|\\))+(?:\\w+\\.\\w+\\()?((?:[+-]?(?:\\d*\\.)?\\d+)+)?(?:(?:\\(|\\,|\\s|\\))+(?:\\w+\\.\\w+\\()((?:[+-]?(?:\\d*\\.)?\\d+)+))?", Pattern.MULTILINE);
    private final Pattern pathName = Pattern.compile("(\\w+)\\s*\\=\\s*(?:\\s*\\w+(.trajectory(?:Sequence)?Builder))(?:\\s*\\(\\s*)(.*?)(?=\\.build\\(\\)\\;)");
    private final Pattern displacement = Pattern.compile("(?:\\.(addDisplacementMarker)\\s*\\(\\s*\\(\\)\\s*\\-\\>\\s*)(?:\\{)(.*?)(?=\\}\\s*\\)\\s*\\.)");
    //latest regex to match pose/vector2d
    //(?:\.((?:\w|\s)+)\((?:new Pose2d|new Vector2d))\s*(?:\()(.*)(?=\)\s*\))

    //match any number
    //((?:[+-]?(?:\d*\.)?\d+)+)

    //ignore comments
    //^(?!\s*\/\/).*

    //new regex to match whole trajectory builder without dotall
    //(\w+)\s*\=\s*(?:\s*\w+(.trajectory(?:Sequence)?Builder))(?:\s*\(\s*)((.|\r\n|\r|\n)*?)(?=\.build\(\)\;)

    //new regex with commenting
    //^(?!\s*\/\/).*(\w+)\s*\=\s*(?:\s*\w+(.trajectory(?:Sequence)?Builder))(?:\s*\(\s*)((?:.|\r\n|\r|\n)*?)(?=\.build\(\)\;)
    private Main main;
    public Import(Main main){
        this.main = main;
    }

    public LinkedList<NodeManager> read(File file){
        String allText = "";
        LinkedList<NodeManager> managers = new LinkedList<>();
        try{
            Scanner reader = new Scanner(file);
            while(reader.hasNextLine()){
                allText += reader.nextLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        allText.replaceAll("\n", "");

        Matcher matcher = pathName.matcher(allText);
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
            int end = allText.length();
            if(i < managers.size()-1) end = ends.get(i+1);
            Matcher data = dataPattern.matcher(allText.substring(starts.get(i), end));

            while(data.find()){
                boolean discard = false;
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
                try{
                    node.x = (Double.parseDouble(data.group(2))+72)* main.scale;
                    node.y = (72 - (Double.parseDouble(data.group(3))))*main.scale;

                    switch (node.getType()){
                        case splineTo:
                            node.splineHeading = Double.parseDouble(data.group(4))-90;
                            node.robotHeading = node.splineHeading;
                            break;
                        case splineToSplineHeading:
                        case splineToLinearHeading:
                            node.splineHeading = Double.parseDouble(data.group(5))-90;
                            node.robotHeading = Double.parseDouble(data.group(4))-90;
                            break;
                        case splineToConstantHeading:
                            node.splineHeading = Double.parseDouble(data.group(4))-90;
                            node.robotHeading = node.splineHeading;
                            break;
                        default:
                            //TODO: fix importing
                            Matcher displace = displacement.matcher(allText.substring(data.start(), data.end()));
                            while(displace.find()){
                                System.out.println(displace.group(0));
                                System.out.println(displace.group(2));
                                manager.get(manager.size()-1).code = displace.group(2);
                                discard = true;
                            }

                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                    node.x = main.scale*72;
                    node.y = main.scale*72;
                    node.robotHeading = 90;
                    node.robotHeading = 90;
                }
                if(!discard)
                    manager.add(node);

            }
        }
        return managers;
    }
}
//(\.(?:\w|\s)+\((?:\w|\s)+)(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:\(|\,|\s)+(?:\w+\.\w+\()((?:[+-]?(?:\d*\.)?\d+)+)