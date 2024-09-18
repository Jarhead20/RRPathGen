package rrpathgen.gui.infoPanel;

import rrpathgen.Main;
import rrpathgen.data.Marker;
import rrpathgen.data.Node;
import rrpathgen.util.SpringUtilities;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;

public class MarkerPanel extends JPanel {

    private final Main main;

    private final NumberFormat format = NumberFormat.getInstance();
    private final NumberFormatter formatter = new NumberFormatter(format);
    private final JFormattedTextField displacement = new JFormattedTextField(formatter);
    private final JTextField code = new JTextField(10);
    private final JTextField name = new JTextField(10);

    private final JComboBox<Node.Type> type = new JComboBox<>();

    //TODO: add the trajectory name
    MarkerPanel(Main main){
        this.main = main;
        type.setSelectedIndex(-1);
        this.setOpaque(true);
        this.setLayout(new SpringLayout());
        JLabel lDisplacement = new JLabel("Displacement: ", JLabel.TRAILING);
        JLabel lCode = new JLabel("Code: ", JLabel.TRAILING);
        JLabel lType = new JLabel("Type: ", JLabel.TRAILING);
        JLabel lName = new JLabel("Name: ", JLabel.TRAILING);

        this.add(lName);
        lName.setLabelFor(name);
        this.add(name);

        this.add(lDisplacement);
        lDisplacement.setLabelFor(displacement);
        this.add(displacement);

        this.add(lCode);
        lCode.setLabelFor(code);
        this.add(code);
        this.add(lType);
        lType.setLabelFor(type);
        this.add(type);

        SpringUtilities.makeCompactGrid(this,4,2,6,6,6,6);

        this.setVisible(true);


        displacement.addActionListener(e -> {
            if(main.currentMarker != -1) getCurrentMarker().displacement = Double.parseDouble(displacement.getText());
            main.drawPanel.repaint();
        });

        name.addActionListener(e -> {
            main.getCurrentManager().name = name.getText();
        });

        code.addActionListener(e -> {
            if(main.currentMarker != -1) {
                getCurrentMarker().code = code.getText();
            }
            main.drawPanel.repaint();
        });
    }

    public void saveValues(){
        if(main.currentMarker == -1) return;

        Marker marker = getCurrentMarker();
        main.getCurrentManager().name = name.getText();
        marker.code = code.getText();
        marker.displacement = Double.parseDouble(displacement.getText());
        main.drawPanel.repaint();
    }
    private Marker getCurrentMarker(){
        return main.getCurrentManager().getMarkers().get(main.currentMarker);
    }

    public void updateText() {
        upateNodeTypes();
        if(main.currentMarker == -1){
            code.setText("");
            displacement.setText("0");
            name.setText(main.getCurrentManager().name);
        } else {
            code.setText(getCurrentMarker().code);
            displacement.setText(String.format("%.2f",getCurrentMarker().displacement));
            name.setText(main.getCurrentManager().name);
        }
    }

    public void upateNodeTypes(){
        type.removeAllItems();
        for(Node.Type t : Main.drawPanel.getTrajectory().getValidMarkerTypes()){
            type.addItem(t);
        }
    }
}
