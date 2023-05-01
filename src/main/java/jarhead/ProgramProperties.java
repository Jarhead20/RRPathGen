package jarhead;

import java.io.*;
import java.util.Properties;

public class ProgramProperties {
    public double robotWidth;
    public double robotLength;
    public double resolution;
    public String importPath;
    public double trackWidth;
    public double maxVelo;
    public double maxAccel;
    public double maxAngVelo;
    public double maxAngAccel;
    public Properties prop;
    private final File file;

    public ProgramProperties(File file) {
        this.file = file;
        if(!file.exists()) {
            generateFile(file);
        }
        prop = new Properties();
        readFile(file);

        reload();
    }

    public void reload(){
        try{
            robotLength = Double.parseDouble(prop.getProperty("ROBOT_LENGTH"));
            robotWidth = Double.parseDouble(prop.getProperty("ROBOT_WIDTH"));
            resolution = Double.parseDouble(prop.getProperty("RESOLUTION"));
            importPath = prop.getProperty("IMPORT/EXPORT");
            trackWidth = Double.parseDouble(prop.getProperty("TRACK_WIDTH"));
            maxVelo = Double.parseDouble(prop.getProperty("MAX_VELO"));
            maxAccel = Double.parseDouble(prop.getProperty("MAX_ACCEL"));
            maxAngVelo = Double.parseDouble(prop.getProperty("MAX_ANGULAR_VELO"));
            maxAngAccel = Double.parseDouble(prop.getProperty("MAX_ANGULAR_ACCEL"));
        } catch (NullPointerException e){
            prop = new Properties();
            generateFile(file);
        }

    }

    private void generateFile(File file) {
        try {
            file.getParentFile().mkdir();
            file.createNewFile();
            FileWriter writer = new FileWriter(file, false);
            writer.write(
                    """
                            ROBOT_WIDTH=18
                            ROBOT_LENGTH=18
                            RESOLUTION=0.1
                            IMPORT/EXPORT=
                            TRACK_WIDTH=15
                            MAX_VELO=60
                            MAX_ACCEL=60
                            MAX_ANGULAR_VELO=60
                            MAX_ANGULAR_ACCEL=60"""
            );
            writer.close();
            readFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFile(File file){
        try (FileInputStream in = new FileInputStream(file)){
            prop.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(){
        try (FileOutputStream out = new FileOutputStream(file)){
            prop.store(out, "V1.3");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
