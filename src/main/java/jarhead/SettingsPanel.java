package jarhead;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.LinkedList;

public class SettingsPanel extends JPanel {

    private Main main;

    NumberFormat format = NumberFormat.getInstance();
//    NumberFormatter formatter = new NumberFormatter(format);
    private LinkedList<JTextField> fields = new LinkedList<>();
    private String[] labels = {"Robot Width", "Robot Length", "Resolution", "Import/Export"};

    SettingsPanel(Main main){
        this.main = main;
        this.setOpaque(true);
//        this.setPreferredSize(new Dimension((int) Math.floor(30 * main.scale), (int) Math.floor(40 * main.scale)));
        this.setLayout(new SpringLayout());

        for (String label : labels) {
            JTextField input = new JTextField();
            input.setCursor(new Cursor(2));
            input.setColumns(10);
//            input.setMaximumSize(new Dimension((int)main.scale*5,10));
            JLabel l = new JLabel(label + ": ", JLabel.TRAILING);
            this.add(l);
            l.setLabelFor(input);
            this.add(input);
            fields.add(input);
        }

        SpringUtilities.makeCompactGrid(this,labels.length,2,6,6,6,6);
        this.setVisible(true);

        for (int i = 0; i < fields.size(); i++) {
            JTextField field = fields.get(i);
            int finalI = i;
            field.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    main.prop.setProperty(labels[finalI].replaceAll(" ","_").toUpperCase(), field.getText());
                    main.reloadConfig();
                    main.setState(JFrame.MAXIMIZED_BOTH);
                }
            });
        }
    }

    public void update(){
        for (int i = 0; i < fields.size(); i++) {
            JTextField field = fields.get(i);
            field.setText(main.prop.getProperty(labels[i].replaceAll(" ","_").toUpperCase()));
            main.saveConfig();
        }
    }


}
