package jarhead;

import javax.swing.*;
import java.awt.*;

public class ExportPanel extends JPanel {

    JTextArea field = new JTextArea();
    JScrollPane scroll = new JScrollPane(field);
    Main main;
    private final int MULTIPLIER = 3;

    ExportPanel(Main main) {
        field.setText("Export text area");

        this.main = main;
        this.setOpaque(true);
        this.setBackground(Color.darkGray.darker());
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

//        field.setAutoscrolls(true);
//        field.setLineWrap(true);

        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.add(scroll);
        this.setVisible(true);
    }

    @Override
    public Dimension getPreferredSize() {
//        field.setColumns((int) main.scale*MULTIPLIER);
//        field.setRows((int) (main.scale*MULTIPLIER));
        return super.getPreferredSize();
    }


}
