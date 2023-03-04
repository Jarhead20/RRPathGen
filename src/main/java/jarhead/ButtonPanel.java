package jarhead;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ButtonPanel extends JPanel {

    private final JButton exportButton = new JButton("Export");
    private final JButton importButton = new JButton("Import");
    public final JButton flipButton = new JButton("Flip");
    private final JButton clearButton = new JButton("Clear");
    private final JButton undoButton = new JButton("Undo");
    private final JButton redoButton = new JButton("Redo");
    private LinkedList<NodeManager> managers;
    private Main main;
    private ProgramProperties robot;

    ButtonPanel(LinkedList<NodeManager> managers, Main main, ProgramProperties props){
        this.robot = props;
        this.main = main;
        this.managers = managers;
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

        this.setVisible(true);

        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                export();
            }
        });

        flipButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Node un = new Node(-2,-2);
                un.state = 3;
                for (int i = 0; i < getCurrentManager().size(); i++) {
                    Node node = getCurrentManager().get(i);
                    node.y = 144*main.scale-node.y;
                    node.splineHeading = 180-node.splineHeading;
                    node.robotHeading = 180-node.robotHeading;
                    getCurrentManager().set(i, node);
                }
                main.drawPanel.repaint();
            }
        });
        undoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                main.undo(true);
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
                main.infoPanel.editPanel.updateText();
                main.drawPanel.resetPath();

                main.drawPanel.renderBackgroundSplines();
                main.drawPanel.repaint();
            }
        });
        importButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file;
                if(robot.importPath.matches("")){
                    JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView());
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("Java Files", "java");
                    chooser.setFileFilter(filter);
                    int r = chooser.showOpenDialog(null);
                    if(r != JFileChooser.APPROVE_OPTION) return;
                    robot.importPath = chooser.getSelectedFile().getPath();
                    robot.prop.setProperty("IMPORT/EXPORT", robot.importPath);
                    main.saveConfig();
                    main.infoPanel.settingsPanel.update();
                    file = chooser.getSelectedFile();
                } else {
                    main.saveConfig();
                    file = new File(robot.importPath);
                }
                Import importer = new Import(main);
                LinkedList<NodeManager> in = importer.read(file);
                if(getCurrentManager().size() < 1)
                    managers.remove(getCurrentManager());
                in.forEach((m) -> {
                    managers.add(m);
                });


                main.currentM = managers.size()-1;
                main.currentN = -1;
                main.infoPanel.editPanel.name.setText(getCurrentManager().name);
                main.drawPanel.renderBackgroundSplines();
                main.drawPanel.repaint();
            }
        });
    }

    public void export(){
        if(getCurrentManager().size() > 0){
            main.infoPanel.editPanel.saveValues();
            main.infoPanel.markerPanel.saveValues();
            Node node = getCurrentManager().getNodes().get(0);
            double x = main.toInches(node.x);
            double y = main.toInches(node.y);
            if(!robot.importPath.matches("")){
                File outputFile = new File(robot.importPath.substring(0,robot.importPath.length()-4) + "backup.java");
                System.out.println(outputFile.getPath());
                try {
                    outputFile.createNewFile();
                    FileWriter writer = new FileWriter(outputFile);
                    Scanner reader = new Scanner(new File(robot.importPath));

                    writer.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

            }

            StringBuilder sb = new StringBuilder();
            if(main.exportPanel.addDataType) sb.append("TrajectorySequence ");
            sb.append(String.format("%s = drive.trajectorySequenceBuilder(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)))%n",getCurrentManager().name, x, -y, (node.robotHeading +90)));
            //sort the markers
            List<Marker> markers = getCurrentManager().getMarkers();
            markers.sort((n1, n2) -> ((Double) n1.displacement).compareTo(n2.displacement));
            for (int i = 0; i < markers.size(); i++) {
                Marker marker = markers.get(i);
                sb.append(String.format(".UNSTABLE_addTemporalMarkerOffset(%.2f,() -> {%s})%n", marker.displacement, marker.code));
            }
            boolean prev = false;
            for (int i = 0; i < getCurrentManager().size(); i++) {
                node = getCurrentManager().get(i);
                if(node.equals(getCurrentManager().getNodes().get(0))) {
                    if(node.reversed != prev){
                        sb.append(String.format(".setReversed(%s)%n", node.reversed));
                        prev = node.reversed;
                    }
                    continue;
                }
                x = main.toInches(node.x);
                y = main.toInches(node.y);


                switch (node.getType()){
                    case splineTo:
                        sb.append(String.format(".splineTo(new Vector2d(%.2f, %.2f), Math.toRadians(%.2f))%n", x, -y, (node.splineHeading +90)));
                        break;
                    case splineToSplineHeading:
                        sb.append(String.format(".splineToSplineHeading(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)), Math.toRadians(%.2f))%n", x, -y, (node.robotHeading +90), (node.splineHeading +90)));
                        break;
                    case splineToLinearHeading:
                        sb.append(String.format(".splineToLinearHeading(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)), Math.toRadians(%.2f))%n", x, -y, (node.robotHeading +90), (node.splineHeading +90)));
                        break;
                    case splineToConstantHeading:
                        sb.append(String.format(".splineToConstantHeading(new Vector2d(%.2f, %.2f), Math.toRadians(%.2f))%n", x, -y, (node.splineHeading +90)));
                        break;
                    case lineTo:
                        sb.append(String.format(".lineTo(new Vector2d(%.2f, %.2f))%n", x, -y));
                        break;
                    case lineToSplineHeading:
                        sb.append(String.format(".lineToSplineHeading(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)))%n", x, -y, (node.robotHeading +90)));
                        break;
                    case lineToLinearHeading:
                        sb.append(String.format(".lineToLinearHeading(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)))%n", x, -y, (node.robotHeading +90)));
                        break;
                    case lineToConstantHeading:
                        sb.append(String.format(".lineToConstantHeading(new Vector2d(%.2f, %.2f))%n", x, -y, (node.splineHeading +90)));
                        break;
                    case addTemporalMarker:
                        break;
                    default:
                        sb.append("couldn't find type");
                        break;
                }
                if(node.reversed != prev){
                    sb.append(String.format(".setReversed(%s)%n", node.reversed));
                    prev = node.reversed;
                }
            }
            sb.append(String.format(".build();%n"));
            if(main.exportPanel.addPoseEstimate) sb.append(String.format("drive.setPoseEstimate(%s.start());", getCurrentManager().name));
            main.exportPanel.field.setText(sb.toString());
        }
    }

    private NodeManager getCurrentManager() {
        return main.getManagers().get(main.currentM);
    }
}
