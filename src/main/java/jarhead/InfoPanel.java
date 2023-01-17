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

    InfoPanel(Main main, ProgramProperties props) {
        this.main = main;
        this.setOpaque(true);
        this.settingsPanel = new SettingsPanel(main, props);
        this.editPanel = new EditPanel(main);
        this.setBackground(Color.darkGray.darker());
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(editPanel, BorderLayout.NORTH);
//        this.add(Box.createVerticalStrut((int)main.scale*100));
        this.add(settingsPanel, BorderLayout.SOUTH);
        this.setVisible(true);
    }
}
