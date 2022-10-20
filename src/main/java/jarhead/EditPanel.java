package jarhead;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

public class EditPanel extends JPanel {

    private Main main;

    NumberFormat format = NumberFormat.getInstance();
    NumberFormatter formatter = new NumberFormatter(format);
    public JFormattedTextField x = new JFormattedTextField(formatter);
    public JFormattedTextField y = new JFormattedTextField(formatter);
    public JFormattedTextField heading = new JFormattedTextField(formatter);
    public JTextField name = new JTextField(10);
    public JTextField code = new JTextField(10);
    public JComboBox type;

    EditPanel(Main main){
        this.main = main;
        this.setOpaque(true);
        this.setLayout(new SpringLayout());
        JLabel lX = new JLabel("X: ", JLabel.TRAILING);
        JLabel lY = new JLabel("Y: ", JLabel.TRAILING);
        JLabel lHeading = new JLabel("Heading: ", JLabel.TRAILING);
        JLabel lName = new JLabel("Name: ", JLabel.TRAILING);
        JLabel lType = new JLabel("Type: ", JLabel.TRAILING);
        JLabel lCode = new JLabel("Code: ", JLabel.TRAILING);

        type = new JComboBox(Node.Type.values());

        this.add(lName);
        lName.setLabelFor(name);
        this.add(name);

        this.add(lX);
        lX.setLabelFor(x);
        this.add(x);

        this.add(lY);
        lY.setLabelFor(y);
        this.add(y);

        this.add(lHeading);
        lHeading.setLabelFor(heading);
        this.add(heading);

        this.add(lType);
        lType.setLabelFor(type);
        this.add(type);

        this.add(lCode);
        lCode.setLabelFor(code);
        this.add(code);

        SpringUtilities.makeCompactGrid(this,6,2,6,6,6,6);
        this.setVisible(true);

        name.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                main.getCurrentManager().name = name.getText();
                main.drawPanel.repaint();
            }
        });

        x.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(main.currentN != -1) getCurrentNode().x = (Double.parseDouble(x.getText())+72)*main.scale;
                main.drawPanel.repaint();
            }
        });


        y.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(main.currentN != -1) getCurrentNode().y = (Double.parseDouble(y.getText())+72)*main.scale;
                main.drawPanel.repaint();
            }
        });


        heading.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(main.currentN != -1) getCurrentNode().splineHeading = Double.parseDouble(heading.getText());
                main.drawPanel.repaint();
            }
        });

        type.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(main.currentN != -1) getCurrentNode().setType((Node.Type) type.getItemAt(type.getSelectedIndex()));
                main.drawPanel.repaint();
            }
        });

        code.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(main.currentN != -1) {
                    System.out.println(code.getText());
                    getCurrentNode().code = code.getText();
                }
                main.drawPanel.repaint();
            }
        });
    }

    public void saveValues(){
        if(main.currentN != -1){
            Node node = getCurrentNode();
            main.getCurrentManager().name = name.getText();
            node.x = (Double.parseDouble(x.getText())+72)*main.scale;
            node.y = (Double.parseDouble(y.getText())+72)*main.scale;
            node.splineHeading = Double.parseDouble(heading.getText());
            node.setType((Node.Type) type.getItemAt(type.getSelectedIndex()));
            node.code = code.getText();
            main.drawPanel.repaint();
        }
    }
    public Node getCurrentNode(){
        return main.getCurrentManager().get(main.currentN);
    }

    public void update() {
        if(main.currentN == -1){
            heading.setText("");
            x.setText("");
            y.setText("");
            type.setSelectedIndex(-1);
            name.setText(main.getCurrentManager().name);
            code.setText("");
        } else {
            heading.setText(Math.round((getCurrentNode().splineHeading)*100)/100.0 + "");
            x.setText(Math.round(main.toInches(getCurrentNode().x)*100)/100.0 + "");
            y.setText(Math.round(main.toInches(getCurrentNode().y)*100)/100.0 + "");
            type.setSelectedItem(getCurrentNode().getType());
            name.setText(main.getCurrentManager().name);
            code.setText(getCurrentNode().code);
        }
    }
}
