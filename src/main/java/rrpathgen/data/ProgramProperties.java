package rrpathgen.data;

import java.io.*;
import java.util.Properties;

public class ProgramProperties {
    public enum Library {
        RROLD,
        RRNEW
    }
    public Library lib;
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
            lib = Library.valueOf(prop.getProperty("LIBRARY"));
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
            e.printStackTrace();
            prop = new Properties();
            System.out.println("Generating new file");
            generateFile(file);
        }

    }

    private void generateFile(File file) {
        try {
            file.getParentFile().mkdir();
            file.createNewFile();
            FileWriter writer = new FileWriter(file, false);
            writer.write(
                    "LIBRARY=RROLD\n" +
                    "ROBOT_WIDTH=18\n" +
                    "ROBOT_LENGTH=18\n" +
                    "RESOLUTION=0.1\n" +
                    "IMPORT/EXPORT=\n" +
                    "TRACK_WIDTH=15\n" +
                    "MAX_VELO=60\n" +
                    "MAX_ACCEL=60\n" +
                    "MAX_ANGULAR_VELO=60\n" +
                    "MAX_ANGULAR_ACCEL=60"
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
            prop.store(out, "V1.7");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
