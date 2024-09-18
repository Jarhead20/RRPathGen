package rrpathgen.gui.infoPanel;

import rrpathgen.Main;
import rrpathgen.data.Node;
import rrpathgen.util.SpringUtilities;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;

public class EditPanel extends JPanel {

    private final Main main;

    private final NumberFormat format = NumberFormat.getInstance();
    private final NumberFormatter formatter = new NumberFormatter(format);
    private final JFormattedTextField x = new JFormattedTextField(formatter);
    private final JFormattedTextField y = new JFormattedTextField(formatter);
    private final JFormattedTextField splineHeading = new JFormattedTextField(formatter);
    private final JFormattedTextField robotHeading = new JFormattedTextField(formatter);
    protected JTextField name = new JTextField(10);
    private final JComboBox<Node.Type> type;

    EditPanel(Main main){
        this.main = main;
        this.setOpaque(true);
        this.setLayout(new SpringLayout());
        JLabel lX = new JLabel("X: ", JLabel.TRAILING);
        JLabel lY = new JLabel("Y: ", JLabel.TRAILING);
        JLabel lSplineHeading = new JLabel("Spline Heading: ", JLabel.TRAILING);
        JLabel lRobotHeading = new JLabel("Robot Heading: ", JLabel.TRAILING);
        JLabel lName = new JLabel("Name: ", JLabel.TRAILING);
        JLabel lType = new JLabel("Type: ", JLabel.TRAILING);


        type = new JComboBox<>();
//        type = new JComboBox(Arrays.stream(Node.Type.values()).filter(i -> i.toString().contains("line")).toArray());

        type.setSelectedIndex(-1);

        this.add(lName);
        lName.setLabelFor(name);
        this.add(name);

        this.add(lX);
        lX.setLabelFor(x);
        this.add(x);

        this.add(lY);
        lY.setLabelFor(y);
        this.add(y);

        this.add(lSplineHeading);
        lSplineHeading.setLabelFor(splineHeading);
        this.add(splineHeading);

        this.add(lRobotHeading);
        lRobotHeading.setLabelFor(robotHeading);
        this.add(robotHeading);

        this.add(lType);
        lType.setLabelFor(type);
        this.add(type);

        SpringUtilities.makeCompactGrid(this,6,2,6,6,6,6);
        this.setVisible(true);

        name.addActionListener(e -> {
            main.getCurrentManager().name = name.getText();
            main.drawPanel.repaint();
        });

        x.addActionListener(e -> {
            if(main.currentN == -1) return;
            getCurrentNode().x = (Double.parseDouble(x.getText())+72)*main.scale;
            main.drawPanel.repaint();
        });

        y.addActionListener(e -> {
            if(main.currentN == -1) return;
            getCurrentNode().y = (72-Double.parseDouble(y.getText()))*main.scale;
            main.drawPanel.repaint();
        });

        splineHeading.addActionListener(e -> {
            if(main.currentN == -1) return;
            getCurrentNode().splineHeading = Double.parseDouble(splineHeading.getText())-90;
            main.drawPanel.repaint();
        });

        robotHeading.addActionListener(e -> {
            if(main.currentN == -1) return;
            getCurrentNode().robotHeading = Double.parseDouble(robotHeading.getText())-90;
            main.drawPanel.repaint();
        });

        type.addActionListener(e -> {
            if(main.currentN == -1) return;
            getCurrentNode().setType(type.getItemAt(type.getSelectedIndex()));
            main.drawPanel.repaint();
        });
    }

    //TOOD move into main
    public void saveValues(){
        if(main.currentN == -1) return;

        Node node = getCurrentNode();
        main.getCurrentManager().name = name.getText();

        node.x = (Double.parseDouble(x.getText())+72)*main.scale;
        node.y = (72-Double.parseDouble(y.getText()))*main.scale;
        node.splineHeading = Double.parseDouble(splineHeading.getText())-90;
        node.robotHeading = Double.parseDouble(robotHeading.getText())-90;

        node.setType(type.getItemAt(type.getSelectedIndex()));
        main.drawPanel.repaint();
    }
    private Node getCurrentNode(){
        return main.getCurrentManager().get(main.currentN);
    }

    public void updateText() {
        if(main.currentN == -1){
            splineHeading.setText("");
            robotHeading.setText("");
            x.setText("");
            y.setText("");
            type.setSelectedIndex(-1);
        } else {
            splineHeading.setText(Math.round((getCurrentNode().splineHeading+90)*100)/100.0 + "");
            robotHeading.setText(Math.round((getCurrentNode().robotHeading+90)*100)/100.0 + "");
            x.setText(Math.round(main.toInches(getCurrentNode().x)*100.0)/100.0 + "");
            y.setText(Math.round((-main.toInches(getCurrentNode().y))*100.0)/100.0 + "");
            type.setSelectedItem(getCurrentNode().getType());
        }
        name.setText(main.getCurrentManager().name);
    }

    public void updateNodeTypes(){
        type.removeAllItems();
        for(Node.Type t : Main.drawPanel.getTrajectory().getValidNodeTypes()){
            type.addItem(t);
        }
    }
}
