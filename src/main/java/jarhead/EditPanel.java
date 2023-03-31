package jarhead;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

public class EditPanel extends JPanel {

    private Main main;

    NumberFormat format = NumberFormat.getInstance();
    NumberFormatter formatter = new NumberFormatter(format);
    public JFormattedTextField x = new JFormattedTextField(formatter);
    public JFormattedTextField y = new JFormattedTextField(formatter);
    public JFormattedTextField splineHeading = new JFormattedTextField(formatter);
    public JFormattedTextField robotHeading = new JFormattedTextField(formatter);
    public JTextField name = new JTextField(10);
    public JComboBox type;

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

        type = new JComboBox(Node.Type.values());
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
            getCurrentNode().setType((Node.Type) type.getItemAt(type.getSelectedIndex()));
            main.drawPanel.repaint();
        });
    }

    public void saveValues(){
        if(main.currentN == -1) return;

        Node node = getCurrentNode();
        main.getCurrentManager().name = name.getText();

        node.x = (Double.parseDouble(x.getText())+72)*main.scale;
        node.y = (72-Double.parseDouble(y.getText()))*main.scale;
        node.splineHeading = Double.parseDouble(splineHeading.getText())-90;
        node.robotHeading = Double.parseDouble(robotHeading.getText())-90;

        node.setType((Node.Type) type.getItemAt(type.getSelectedIndex()));
        main.drawPanel.repaint();
    }
    public Node getCurrentNode(){
        return main.getCurrentManager().get(main.currentN);
    }

    public void updateText() {
        if(main.currentN == -1){
            splineHeading.setText("");
            robotHeading.setText("");
            x.setText("");
            y.setText("");
            type.setSelectedIndex(-1);
            name.setText(main.getCurrentManager().name);
        } else {
            splineHeading.setText(Math.round((getCurrentNode().splineHeading+90)*100)/100.0 + "");
            robotHeading.setText(Math.round((getCurrentNode().robotHeading+90)*100)/100.0 + "");
            x.setText(Math.round(main.toInches(getCurrentNode().x)*100.0)/100.0 + "");
            y.setText(Math.round((-main.toInches(getCurrentNode().y))*100.0)/100.0 + "");
            type.setSelectedItem(getCurrentNode().getType());
            name.setText(main.getCurrentManager().name);
        }
    }
}
