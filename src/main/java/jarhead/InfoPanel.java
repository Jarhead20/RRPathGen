package jarhead;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.LinkedList;

public class InfoPanel extends JPanel {

    private Main main;
    public SettingsPanel settingsPanel;
    public EditPanel editPanel;

    InfoPanel(Main main) {
        this.main = main;
        this.setOpaque(true);
        this.settingsPanel = new SettingsPanel(main);
        this.editPanel = new EditPanel(main);
        this.setBackground(Color.darkGray.darker());
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(editPanel, BorderLayout.NORTH);
        this.add(Box.createVerticalStrut((int)main.scale*100));
        this.add(settingsPanel, BorderLayout.SOUTH);
        this.setVisible(true);
//        GridBagConstraints c = new GridBagConstraints();
//        c.fill = GridBagConstraints.PAGE_START;
//        c.gridx = 0;
//        c.gridy = 0;
//        c.gridheight=1;
//        c.weightx = 0.5;
//        this.add(editPanel, c);
//        c.fill = GridBagConstraints.PAGE_END;
//        c.gridx = 0;
//        c.gridy = 4;
//        c.gridheight=1;
//        this.add(settingsPanel, c);
    }
}
