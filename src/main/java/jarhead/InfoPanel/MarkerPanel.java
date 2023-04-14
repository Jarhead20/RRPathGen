package jarhead.InfoPanel;

import jarhead.Main;
import jarhead.Marker;
import jarhead.SpringUtilities;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

public class MarkerPanel extends JPanel {

    private Main main;

    NumberFormat format = NumberFormat.getInstance();
    NumberFormatter formatter = new NumberFormatter(format);
    public JFormattedTextField displacement = new JFormattedTextField(formatter);
    public JTextField code = new JTextField(10);
    public JTextField name = new JTextField(10);
    public JComboBox type;
    //TODO: add the trajectory name
    MarkerPanel(Main main){
        this.main = main;
        type = new JComboBox(Marker.Type.values());
        type.setSelectedIndex(0);
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


        displacement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(main.currentMarker != -1) getCurrentMarker().displacement = Double.parseDouble(displacement.getText());
                main.drawPanel.repaint();
            }
        });

        name.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                main.getCurrentManager().name = name.getText();
                main.drawPanel.repaint();
            }
        });

        code.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(main.currentMarker != -1) {
                    getCurrentMarker().code = code.getText();
                }
                main.drawPanel.repaint();
            }
        });
    }

    public void saveValues(){
        if(main.currentMarker != -1){
            Marker marker = getCurrentMarker();
            main.getCurrentManager().name = name.getText();
            marker.code = code.getText();
            marker.displacement = Double.parseDouble(displacement.getText());
            main.drawPanel.repaint();
        }
    }
    public Marker getCurrentMarker(){
        return main.getCurrentManager().getMarkers().get(main.currentMarker);
    }

    public void updateText() {
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
}
