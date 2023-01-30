package jarhead;

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

    MarkerPanel(Main main){
        this.main = main;
        this.setOpaque(true);
        this.setLayout(new SpringLayout());
        JLabel lDisplacement = new JLabel("Displacement: ", JLabel.TRAILING);
        JLabel lCode = new JLabel("Code: ", JLabel.TRAILING);

        this.add(lDisplacement);
        lDisplacement.setLabelFor(displacement);
        this.add(displacement);

        this.add(lCode);
        lCode.setLabelFor(code);
        this.add(code);



        SpringUtilities.makeCompactGrid(this,6,2,6,6,6,6);
        this.setVisible(true);


        displacement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(main.currentMarker != -1) getCurrentMarker().displacement = Double.parseDouble(displacement.getText());

                main.drawPanel.repaint();
            }
        });



        code.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(main.currentN != -1) {
                    System.out.println(code.getText());
//                    getCurrentNode().code = code.getText();
                }
                main.drawPanel.repaint();
            }
        });
    }

    public void saveValues(){
        if(main.currentN != -1){
            Marker marker = getCurrentMarker();
            marker.code = code.getText();
            marker.displacement = Double.parseDouble(displacement.getText());
            main.drawPanel.repaint();
        }
    }
    public Marker getCurrentMarker(){
        return (Marker) main.getCurrentManager().markers.get(main.currentMarker);
    }

    public void update() {
        if(main.currentN == -1){
            code.setText("");
            displacement.setText("0");
        } else {
            code.setText(getCurrentMarker().code);
            displacement.setText(getCurrentMarker().displacement + "");
        }
    }
}
