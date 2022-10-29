package jarhead;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Import {
    //(?:^\s*Trajectory\s+(?:(\w+)\s+\=\s+\w+\.(?:\w|\s)+\((?:\w|\s)+))(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:(?:\(|\,|\s)+((?:[+-]?(?:\d*\.)?\d+)+))(?:\(|\,|\s)+(?:\w+\.\w+\()((?:[+-]?(?:\d*\.)?\d+)+)/gm

    private static final Pattern numberPattern = Pattern.compile("(?:\\W)(([+-]?(\\d*\\.)?\\d+))");
    private static final Pattern pathName = Pattern.compile("(?:^\\s*Trajectory\\s+(\\w*))");

    public Set<String[]> read(File file){
        Set<String[]> set = new HashSet<>();
        try{
            Scanner reader = new Scanner(file);
            while(reader.hasNextLine()){
                String line = reader.nextLine();
                Matcher matcher = numberPattern.matcher(line);
                Set<String> data = new HashSet<>();
                while(matcher.find()) {
                    data.add(matcher.group(0));
                }
                set.add((String[])data.toArray());

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return set;
    }
}
