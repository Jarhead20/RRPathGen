package jarhead;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ButtonPanel extends JPanel {
    private static final Pattern numberPattern = Pattern.compile("[+-]?(\\d*\\.)?\\d+");
    private static final Pattern pathName = Pattern.compile("(?:^\\s*Trajectory\\s+(\\w*))");

    private final JButton exportButton = new JButton("Export");
    private final JButton importButton = new JButton("Import");
    public final JButton flipButton = new JButton("Flip");
    private final JButton clearButton = new JButton("Clear");
    private final JButton undoButton = new JButton("Undo");
    private final JButton redoButton = new JButton("Redo");
    private LinkedList<NodeManager> managers;
    private double scale;
    private Main main;

    ButtonPanel(LinkedList<NodeManager> managers, Main main){
        this.main = main;
        this.managers = managers;
        this.scale = main.scale;
        this.setMinimumSize(new Dimension(0,20));
        this.setLayout(new GridLayout(1, 4, 1, 1));



        exportButton.setFocusable(false);
        importButton.setFocusable(false);
        flipButton.setFocusable(false);
        clearButton.setFocusable(false);
        undoButton.setFocusable(false);
        redoButton.setFocusable(false);
        this.add(exportButton);
        this.add(importButton);
        this.add(flipButton);
        this.add(clearButton);
        this.add(undoButton);
        this.add(redoButton);
        this.setMaximumSize(new Dimension((int) Math.floor(144 * scale),(int)scale*4));
        this.setVisible(true);

        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(getCurrentManager().size() > 0){
                    main.infoPanel.editPanel.saveValues();
                    Node node = getCurrentManager().get(0);
                    double x = main.toInches(node.x);
                    double y = main.toInches(node.y);
                    System.out.printf("Trajectory %s = drive.trajectoryBuilder(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)))%n",getCurrentManager().name, x, -y, (node.splineHeading +90));
                    for (int i = 1; i < getCurrentManager().size(); i++) {
                        node = getCurrentManager().get(i);
                        x = main.toInches(node.x);
                        y = main.toInches(node.y);
                        switch (node.getType()){
                            case splineTo:
                                System.out.printf(".splineTo(new Vector2d(%.2f, %.2f), Math.toRadians(%.2f))%n", x, -y, (node.splineHeading +90));
                                break;
                            case displacementMarker:
                                System.out.printf(".splineTo(new Vector2d(%.2f, %.2f), Math.toRadians(%.2f))%n", x, -y, (node.splineHeading +90));
                                System.out.printf(".addDisplacementMarker(() -> {%s})%n", node.code);
                                break;
                            case splineToSplineHeading:
                                System.out.printf(".splineToSplineHeading(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)), Math.toRadians(%.2f))%n", x, -y, (node.robotHeading +90), (node.splineHeading +90));
                                break;
                            case splineToLinearHeading:
                                System.out.printf(".splineToLinearHeading(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)), Math.toRadians(%.2f))%n", x, -y, (node.robotHeading +90), (node.splineHeading +90));
                                break;
                            case splineToConstantHeading:
                                System.out.printf(".splineToConstantHeading(new Vector2d(%.2f, %.2f), Math.toRadians(%.2f))%n", x, -y, (node.splineHeading +90));
                                break;
                            default:
                                System.out.println("what");
                                break;
                        }
                    }
                    System.out.println(".build();");
                }
            }
        });

        flipButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Node un = new Node(-2,-2);
                un.state = 3;
                for (int i = 0; i < getCurrentManager().size(); i++) {
                    Node node = getCurrentManager().get(i);
                    node.y = 144*scale-node.y;
                    node.splineHeading = 180-node.splineHeading;
                    getCurrentManager().set(i, node);
                }
                main.drawPanel.repaint();
            }
        });
        undoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                main.undo();
                main.drawPanel.repaint();
            }
        });
        redoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                main.redo();
                main.drawPanel.repaint();
            }
        });
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //todo: add undo for this
                getCurrentManager().undo.clear();
                getCurrentManager().redo.clear();
                getCurrentManager().clear();
                int id = main.currentM;
                for (int i = id; i < managers.size()-1; i++) {
                    managers.set(i, managers.get(i+1));
                }
                if(managers.size() > 1)
                    managers.removeLast();
                else main.currentM = 0;
                if(main.currentM > 0)
                    main.currentM--;
                main.currentN = -1;
                main.infoPanel.editPanel.update();
                main.drawPanel.resetPath();

                main.drawPanel.renderBackgroundSplines();
                main.drawPanel.repaint();
            }
        });
        importButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                NodeManager manager = null;
                try {
                    InputStream stream = getClass().getClassLoader().getResourceAsStream("import.java");
                    Scanner reader = new Scanner(stream);
                    boolean discard = true;
                    while (reader.hasNextLine()) {
                        String line = reader.nextLine();

                        if (line.contains("trajectoryBuilder")){
                            discard = false;
                            Matcher matcher = pathName.matcher(line);
                            matcher.find();
                            String name = matcher.group(1).trim();
                            if(getCurrentManager().size() > 0)
                                manager = new NodeManager(new ArrayList<>(), managers.size(), name);
                            else {
                                manager = getCurrentManager();
                                manager.name = name;
                            }
                            if(line.contains("true")) manager.reversed = true;
                            managers.add(manager);
                        }
                        if(!discard){
                            if(line.contains("new Vector2d(") || line.contains("new Pose2d(")){
                                Matcher m = numberPattern.matcher(line);
                                Node node = new Node();
                                String[] data = new String[10];
                                int i;
                                for (i = 0; m.find(); i++) {
                                    data[i]=m.group(0);
                                }
                                String substring = line.trim().substring(1, line.trim().indexOf("("));
                                System.out.println(substring);
                                try{
                                    int j = 0;
                                    System.out.println(substring.matches(".*\\d.*"));
                                    if(substring.matches(".*\\d.*"))
                                        j++;
                                    node.x = (Double.parseDouble(data[1+j])+72)* scale;
                                    node.y = (-Double.parseDouble(data[2+j])+72)* scale;
                                    System.out.println(i);
                                    if(i > 4 && j==0){
                                        node.robotHeading = Double.parseDouble(data[3+j])-90;
                                        node.splineHeading = Double.parseDouble(data[4+j])-90;
                                    } else {
                                        node.splineHeading = Double.parseDouble(data[3+j])-90;
                                        node.robotHeading = node.splineHeading;
                                    }
                                } catch (Exception error){
                                    error.printStackTrace();
                                    node.x = 72* scale;
                                    node.y = 72* scale;
                                    node.splineHeading = 270;
                                    node.robotHeading = 270;
                                }

                                try {
                                    node.setType(Node.Type.valueOf(substring));
                                } catch (IllegalArgumentException error){
                                    error.printStackTrace();
                                    node.setType(Node.Type.splineTo);
                                }

                                if(manager.reversed && manager.size() == 0) node.splineHeading += 180;
                                manager.add(node);
                            } else if(line.contains(".addDisplacementMarker(")){
                                (manager.get(manager.size()-1)).setType(Node.Type.displacementMarker);
                            } else {
                                discard = true;
                            }
                        }
                    }
                    stream.close();
                } catch (FileNotFoundException uriSyntaxException) {
                    uriSyntaxException.printStackTrace();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                main.currentM = managers.size()-1;
                main.infoPanel.editPanel.name.setText(getCurrentManager().name);
                main.drawPanel.renderBackgroundSplines();
                main.drawPanel.repaint();
            }
        });
    }

    private NodeManager getCurrentManager() {
        return main.getManagers().get(main.currentM);
    }
}
