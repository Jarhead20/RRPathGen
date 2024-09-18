package rrpathgen.gui.infoPanel;

import rrpathgen.*;
import rrpathgen.data.ProgramProperties;

import javax.swing.*;
import java.awt.*;

public class InfoPanel extends JPanel {

    public SettingsPanel settingsPanel;
    public EditPanel editPanel;
    public MarkerPanel markerPanel;

    public InfoPanel(Main main, ProgramProperties props) {
        this.setOpaque(true);
        this.settingsPanel = new SettingsPanel(main, props);
        this.editPanel = new EditPanel(main);
        this.markerPanel = new MarkerPanel(main);
        this.setBackground(Color.darkGray.darker());
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(editPanel, BorderLayout.NORTH);
        this.add(markerPanel, BorderLayout.NORTH);
        markerPanel.setVisible(false);
//        this.add(Box.createVerticalStrut((int)main.scale*100));
        this.add(settingsPanel, BorderLayout.SOUTH);
        this.setVisible(true);
    }

    public void changePanel(boolean marker){
        if(!markerPanel.isVisible() && marker){
            markerPanel.updateText();
            markerPanel.setVisible(true);
            editPanel.setVisible(false);
        } else if(!editPanel.isVisible() && !marker) {
            editPanel.updateText();
            editPanel.setVisible(true);
            markerPanel.setVisible(false);
        }
    }

    public void setManagerName(String name) {
        editPanel.name.setText(name);
    }

}
