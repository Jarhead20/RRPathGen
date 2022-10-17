package jarhead;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.LinkedList;

public class EditPanel extends JPanel {

    private Main main;

    NumberFormat format = NumberFormat.getInstance();
    NumberFormatter formatter = new NumberFormatter(format);
    private LinkedList<JTextField> fields = new LinkedList<>();

    EditPanel(Main main){
        this.main = main;
        this.setOpaque(true);

        this.setBackground(Color.darkGray.darker());
        this.setLayout(new SpringLayout());
        String[] labels = {"Name", "X", "Y", "Heading", "Type", "Code"};
        for (String label : labels) {
            JTextField input = new JFormattedTextField(formatter);
            input.setForeground(Color.lightGray);
            input.setBackground(Color.darkGray.darker());
            input.setCursor(new Cursor(2));
            input.setCaretColor(Color.lightGray);
            input.setColumns(10);
//            input.setText(main.prop.getProperty(label.replaceAll(" ","_").toUpperCase()));
//            input.setMaximumSize(new Dimension((int)main.scale*5,10));
            JLabel l = new JLabel(label + ": ", JLabel.TRAILING);
            l.setForeground(Color.lightGray);
            this.add(l);
            l.setLabelFor(input);
            this.add(input);
            fields.add(input);
        }

        SpringUtilities.makeCompactGrid(this,labels.length,2,6,6,6,6);
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.red),
                this.getBorder()));
        this.setVisible(true);

//        for (int i = 0; i < fields.size(); i++) {
//            JTextField field = fields.get(i);
//            int finalI = i;
//            field.addActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    main.prop.setProperty(labels[finalI].replaceAll(" ","_").toUpperCase(), field.getText());
//                    double oldScale = main.scale;
//                    main.loadConfig();
//                    double newScale = main.scale;
//                    if(finalI == 0){
//                        double d = newScale/oldScale;
//                        main.getManagers().forEach(nodeManager -> {
//                            scale(nodeManager, d);
//                            scale(nodeManager.undo, d);
//                            scale(nodeManager.redo, d);
//                        });
//                    }
//
//
//                    main.remove(main.drawPanel);
//                    DrawPanel drawPanel = new DrawPanel(main.getManagers(), main);
//                    main.drawPanel = drawPanel;
//                    main.getContentPane().add(drawPanel, BorderLayout.WEST);
//                    main.pack();
//                }
//            });
//        }



    }
    private void scale(NodeManager manager, double d){
        for (int j = 0; j < manager.size(); j++) {
            Node n = manager.get(j);
            n.x *= d;
            n.y *= d;
        }
    }



}
