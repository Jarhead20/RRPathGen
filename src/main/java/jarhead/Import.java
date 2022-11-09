package jarhead;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Import {
    //(?:^\s*Trajectory\s+(?:(\w+)\s+\=\s+\w+\.(?:\w|\s)+\((?:\w|\s)+))(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:\(|\,|\s)+(?:\w+\.\w+\()((?:[+-]?(?:\d*\.)?\d+)+)/gm
    //(?:\.((?:\w|\s)+)\((?:\w|\s)+)(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:\(|\,|\s|\))+(?:\w+\.\w+\()?((?:[+-]?(?:\d*\.)?\d+)+)(?:(?:\(|\,|\s|\))+(?:\w+\.\w+\()((?:[+-]?(?:\d*\.)?\d+)+))?

    private final Pattern dataPattern = Pattern.compile("(?:\\.((?:\\w|\\s)+)\\((?:new Pose2d|new Vector2d))(?:(?:\\(|\\,|\\s)+((?:[+-]?(?:\\d*\\.)?\\d+)+))?(?:(?:\\(|\\,|\\s)+((?:[+-]?(?:\\d*\\.)?\\d+)+))?(?:\\(|\\,|\\s|\\))+(?:\\w+\\.\\w+\\()?((?:[+-]?(?:\\d*\\.)?\\d+)+)?(?:(?:\\(|\\,|\\s|\\))+(?:\\w+\\.\\w+\\()((?:[+-]?(?:\\d*\\.)?\\d+)+))?", Pattern.MULTILINE);
    private final Pattern pathName = Pattern.compile("(?:\\s*(Trajectory(?:Sequence)?)\\s+(?:(\\w+)\\s+\\=))");
    private final Pattern displacement = Pattern.compile("(?:\\.(addDisplacementMarker)\\s*\\(\\s*\\(\\)\\s*\\-\\>\\s*)(?:\\{)(.*?)(?=\\}\\s*\\)\\s*\\.)");
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
            if(matcher.group(1).contains("Trajectory")) {
                NodeManager manager = new NodeManager(new ArrayList<>(), managers.size());
                starts.add(matcher.start());
                ends.add(matcher.end());
                manager.name = matcher.group(2);
                managers.add(manager);
            }
        }
        for (int i = 0; i < managers.size(); i++) {
            NodeManager manager = managers.get(i);
            int end = allText.length();
            if(i < managers.size()-1) end = ends.get(i+1);
            Matcher data = dataPattern.matcher(allText.substring(starts.get(i), end));

            while(data.find()){
                for (int j = 0; j < data.groupCount(); j++) {
//                    System.out.println(data.group(j) + " " + manager.name);
                }

                Node node = new Node();
                String type = data.group(1);
                try{
                    node.setType(Node.Type.valueOf(type));
                } catch (IllegalArgumentException e) {
                    //e.printStackTrace();
                    node.setType(Node.Type.splineTo);
                }
//                System.out.println(node.getType());

                switch (node.getType()){
                    case splineTo:
                        System.out.println(data.groupCount());
                        if(data.groupCount() == 5){
                            node.x = (Double.parseDouble(data.group(2))+72)* main.scale;
                            node.y = (72 - (Double.parseDouble(data.group(3))))*main.scale;
                            node.splineHeading = Double.parseDouble(data.group(4))-90;
                            node.robotHeading = node.splineHeading;
                        }
                        break;
                    case splineToSplineHeading:
                        break;
                    case splineToLinearHeading:
                        break;
                    case splineToConstantHeading:
                        break;
                    case displacementMarker:
                        break;
                }
                manager.add(node);
            }
        }
        return managers;
    }
}
//(\.(?:\w|\s)+\((?:\w|\s)+)(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:\(|\,|\s)+(?:\w+\.\w+\()((?:[+-]?(?:\d*\.)?\d+)+)