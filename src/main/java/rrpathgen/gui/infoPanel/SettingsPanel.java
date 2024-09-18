package rrpathgen.gui.infoPanel;

import rrpathgen.Main;
import rrpathgen.data.ProgramProperties;
import rrpathgen.util.SpringUtilities;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.Objects;

public class SettingsPanel extends JPanel {

    private final Main main;

    NumberFormat format = NumberFormat.getInstance();
    NumberFormatter formatter = new NumberFormatter(format);
    private final LinkedList<JTextField> fields = new LinkedList<>();

    private final JComboBox<ProgramProperties.Library> library;
    private final String[] labels = {"Robot Width", "Robot Length", "Resolution", "Import/Export", "Track Width", "Max Velo", "Max Accel", "Max Angular Velo", "Max Angular Accel"};
    private final ProgramProperties robot;
    SettingsPanel(Main main, ProgramProperties properties){
        this.robot = properties;
        this.main = main;
        this.setOpaque(true);
//        this.setPreferredSize(new Dimension((int) Math.floor(30 * main.scale), (int) Math.floor(40 * main.scale)));
        this.setLayout(new SpringLayout());

        library = new JComboBox<>(ProgramProperties.Library.values());
//        set the index to the selected library in the config
        library.setSelectedIndex(Objects.requireNonNull(ProgramProperties.Library.valueOf(robot.prop.getProperty("LIBRARY"))).ordinal());
        JLabel lLibrary = new JLabel("Library: ", JLabel.TRAILING);
        this.add(lLibrary);
        lLibrary.setLabelFor(library);
        this.add(library);

        for (String label : labels) {
            JTextField input;
            if(label.equals(labels[0]) || label.equals(labels[4]))
                input = new JTextField();
            else
                input = new JFormattedTextField(formatter);
            input.setCursor(new Cursor(Cursor.TEXT_CURSOR));
            input.setColumns(10);
//            input.setMaximumSize(new Dimension((int)main.scale*5,10));
            JLabel l = new JLabel(label + ": ", JLabel.TRAILING);
            this.add(l);
            l.setLabelFor(input);
            this.add(input);
            fields.add(input);
        }



        SpringUtilities.makeCompactGrid(this,labels.length+1,2,6,6,6,6);
        this.setVisible(true);

        for (int i = 0; i < fields.size(); i++) {
            JTextField field = fields.get(i);
            int finalI = i;
            field.addActionListener(e -> {
                robot.prop.setProperty(labels[finalI].replaceAll(" ","_").toUpperCase(), field.getText());
                main.reloadConfig();
                main.setState(JFrame.MAXIMIZED_BOTH);
            });
        }

        library.addActionListener(e -> {
            robot.prop.setProperty("LIBRARY", Objects.requireNonNull(ProgramProperties.Library.values()[library.getSelectedIndex()]).name());
            main.reloadConfig();
            main.setState(JFrame.MAXIMIZED_BOTH);
        });
    }

    public void update(){
        for (int i = 0; i < fields.size(); i++) {
            JTextField field = fields.get(i);
            field.setText(robot.prop.getProperty(labels[i].replaceAll(" ","_").toUpperCase()));
        }
        main.saveConfig();
    }


}
