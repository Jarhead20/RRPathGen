import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DrawPanel extends JPanel {

    private static final Pattern p = Pattern.compile("[+-]?(\\d*\\.)?\\d+");

    private final NodeManager nodeM;

    private final JPopupMenu menu = new JPopupMenu("Menu");

    private final JMenuItem open = new JMenuItem("Delete");
    private final JMenuItem cut = new JMenuItem("Cut");
    private final JMenuItem copy = new JMenuItem("Copy");
    private final JMenuItem paste = new JMenuItem("Paste");

    private final JButton exportButton = new JButton("Export");
    private final JButton importButton = new JButton("Import");
    private final JButton flipButton = new JButton("Flip");
    private final JButton clearButton = new JButton("Clear");
    private final JButton undoButton = new JButton("Undo");
    AffineTransform tx = new AffineTransform();
    int[] xPoly = {-5, 0, 5};
    int[] yPoly = {-5, 0, -5};
    Polygon poly = new Polygon(xPoly, yPoly, xPoly.length);
    private final double SCALE = Main.getSCALE();

    DrawPanel(NodeManager nodeM) {
        this.nodeM = nodeM;
        setPreferredSize(new Dimension((int) Math.floor(144 * SCALE + 4), (int) Math.floor(144 * SCALE + (30))));
        JPanel buttons = new JPanel(new GridLayout(1, 4, 1, 1));

        menu.add(open);
        menu.add(cut);
        menu.add(copy);
        menu.add(paste);
        add(menu);

        buttons.add(exportButton);
        buttons.add(importButton);
        buttons.add(flipButton);
        buttons.add(clearButton);
        buttons.add(undoButton);
        add(Box.createRigidArea(new Dimension((int) Math.floor(144 * SCALE), (int) Math.floor(144 * SCALE))));
        add(buttons);

        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Node node = nodeM.get(0);
                System.out.println("drive.trajectorySequenceBuilder(new Pose2d(" + node.x + "," + -node.y + ", Math.toRadians(" + (node.heading+90) + ")))");
                for (int i = 1; i < nodeM.size(); i++) {
                    node = nodeM.get(i);
                    switch (node.getType()){
                        case SPLINE:
                            System.out.println(".splineToSplineHeading(new Pose2d(" + node.x + "," + -node.y + ",Math.toRadians(" + (node.heading+90) + ")),Math.toRadians(" + (node.heading+90) + "))");
                            break;
                        case MARKER:
                            System.out.println(".splineToSplineHeading(new Pose2d(" + node.x + "," + -node.y + ",Math.toRadians(" + (node.heading+90) + ")),Math.toRadians(" + (node.heading+90) + "))");
                            System.out.println(".addDisplacementMarker(() -> {})");
                            break;
                        default:
                            System.out.println("what");
                            break;
                    }
                }
                System.out.println(".build()");
            }
        });
        flipButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < nodeM.size(); i++) {
                    Node node = nodeM.get(i);
                    node.y *= -1;
                    nodeM.set(i, node);
                }
                repaint();
            }
        });
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nodeM.clear();
                repaint();
            }
        });
        importButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    File file = new File(Main.class.getResource("/import.txt").toURI());
                    Scanner reader = new Scanner(file);
                    while (reader.hasNextLine()) {
                        String line = reader.nextLine();
                        if(line.contains(".splineTo(new Vector2d(")){
                            Matcher m = p.matcher(line);
                            Node node = new Node();
                            String[] data = new String[4];
                            for (int i = 0; m.find(); i++) {
                                data[i]=m.group(0);
                            }
                            node.x = Double.parseDouble(data[1]);
                            node.y = -Double.parseDouble(data[2]);
                            node.heading = Double.parseDouble(data[3])-90;

                            nodeM.add(node);
                        } else if(line.contains(".addDisplacementMarker(")){
                            (nodeM.get(nodeM.size()-1)).setType(Node.Type.MARKER);
                        }
                    }
                } catch (URISyntaxException | FileNotFoundException uriSyntaxException) {
                    uriSyntaxException.printStackTrace();
                }
                repaint();
            }
        });
        menu.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                repaint();
            }

            public void popupMenuCanceled(PopupMenuEvent e) {
                repaint();
            }
        });

        open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                nodeM.remove(nodeM.editIndex);
                repaint();
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(new ImageIcon(Main.class.getResource("/field-2021-adi-dark.png")).getImage(), 0, 0, (int) Math.floor(144 * SCALE), (int) Math.floor(144 * SCALE), null);

        for (int i = 0; i < nodeM.size(); i++) {
            Node p1 = nodeM.get(i);
            if (i < nodeM.size() - 1) {
                Node p2 = nodeM.get(i + 1);
                g.setColor(Color.white);
                g.drawLine((int) (SCALE * (p1.x + 72)), (int) (SCALE * (p1.y + 72)), (int) (SCALE * (p2.x + 72)), (int) (SCALE * (p2.y + 72)));
                Node mid = p1.mid(p2);
                g.setColor(Color.green);
                g.fillOval((int) Math.floor((SCALE * (mid.x + 72)) - (0.25 * SCALE * SCALE)), (int) Math.floor((SCALE * (mid.y + 72)) - (0.25 * SCALE * SCALE)), (int) Math.floor(0.5 * SCALE * SCALE), (int) Math.floor(0.5 * SCALE * SCALE));

            }

            double angle = 180 - ( nodeM.get(i)).heading;
            tx.setToIdentity();
            tx.translate((SCALE * (p1.x + 72)), (SCALE * (p1.y + 72)));
            tx.rotate(Math.toRadians(angle));
            tx.scale (SCALE, SCALE);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setTransform(tx);

            g2.setColor(Color.blue);
            g2.fillOval((int) Math.floor(-0.25 * SCALE), (int) Math.floor(-0.25 * SCALE), (int) Math.floor(0.5 * SCALE), (int) Math.floor(0.5 * SCALE));

            switch (p1.getType()) {
                case MARKER:
                    g2.setColor(Color.ORANGE);
                    break;
                case SPLINE:
                    g2.setColor(Color.white);
                    break;
                default:
                    g2.setColor(Color.PINK);
                    break;
            }
            g2.fill(poly);

            g2.dispose();
        }
    }
    public JPopupMenu getMenu(){
        return menu;
    }

}