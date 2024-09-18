package rrpathgen.gui;

import rrpathgen.util.Import;
import rrpathgen.Main;
import rrpathgen.data.Marker;
import rrpathgen.data.Node;
import rrpathgen.data.NodeManager;
import rrpathgen.data.ProgramProperties;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

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

    public ButtonPanel(LinkedList<NodeManager> managers, Main main, ProgramProperties props){
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

        exportButton.addActionListener(e -> export());

        flipButton.addActionListener(e -> {
            main.flip();
            main.drawPanel.repaint();

            Node recordOfFlip = new Node();
            recordOfFlip.state = Node.State.FLIP;
            getCurrentManager().undo.add(recordOfFlip);
        });
        undoButton.addActionListener(e -> {
            main.undo(true);
            main.drawPanel.repaint();
        });
        redoButton.addActionListener(e -> {
            main.redo();
            main.drawPanel.repaint();
        });
        clearButton.addActionListener(e -> {
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
            main.drawPanel.getTrajectory().resetPath();

            main.drawPanel.renderBackgroundSplines();
            main.drawPanel.repaint();
        });
        importButton.addActionListener(e -> {
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
            managers.addAll(in);


            main.currentM = managers.size()-1;
            main.currentN = -1;
            main.infoPanel.setManagerName(getCurrentManager().name);
            main.drawPanel.renderBackgroundSplines();
            main.drawPanel.repaint();
        });
    }

    public void export(){
        if(getCurrentManager().size() <= 0) {
            return;
        }
        main.infoPanel.editPanel.saveValues();
        main.infoPanel.markerPanel.saveValues();

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

        String exportString = Main.drawPanel.getTrajectory().constructExportString();
        main.exportPanel.field.setText(exportString);
    }

    private NodeManager getCurrentManager() {
        return main.getManagers().get(main.currentM);
    }
}
