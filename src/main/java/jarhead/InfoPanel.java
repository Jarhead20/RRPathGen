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

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(editPanel, BorderLayout.NORTH);
        this.add(Box.createVerticalStrut((int)main.scale*80));
        this.add(settingsPanel, BorderLayout.SOUTH);

        this.setBackground(Color.darkGray.darker());
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.red),
                this.getBorder()));
//        this.setMaximumSize(new Dimension(100,100));
        this.setVisible(true);
    }
}
