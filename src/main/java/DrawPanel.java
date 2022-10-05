import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.path.Path;
import com.acmerobotics.roadrunner.path.PathSegment;
import com.acmerobotics.roadrunner.path.QuinticSpline;

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

    private final double robotWidth = 10.7;
    private final double robotLength = 13;

    private static final Pattern p = Pattern.compile("[+-]?(\\d*\\.)?\\d+");

    private final NodeManager nodeM;
    private Path path;
    private Main main;
    private final JPopupMenu menu = new JPopupMenu("Menu");

    private final JMenuItem delete = new JMenuItem("Delete");
    private final JMenuItem makeDisplace = new JMenuItem("Make Displacement Marker");
    private final JMenuItem makeSpline = new JMenuItem("Make Spline");

    private final JButton exportButton = new JButton("Export");
    private final JButton importButton = new JButton("Import");
    public final JButton flipButton = new JButton("Flip");
    private final JButton clearButton = new JButton("Clear");
    private final JButton undoButton = new JButton("Undo");
    private final JButton redoButton = new JButton("Redo");
    AffineTransform tx = new AffineTransform();
    AffineTransform outLine = new AffineTransform();
    int[] xPoly = {0, -2, 0, 2};
    int[] yPoly = {0, -4, -3, -4};
    Polygon poly = new Polygon(xPoly, yPoly, xPoly.length);

    DrawPanel(NodeManager nodeM, NodeManager undo, NodeManager redo, Main main) {
        this.nodeM = nodeM;
        this.main = main;
        setPreferredSize(new Dimension((int) Math.floor(144 * main.scale + 4), (int) Math.floor(144 * main.scale + (30))));
        JPanel buttons = new JPanel(new GridLayout(1, 4, 1, 1));

        menu.add(delete);
        menu.add(makeDisplace);
        menu.add(makeSpline);
        add(menu);

        buttons.add(exportButton);
        buttons.add(importButton);
        buttons.add(flipButton);
        buttons.add(clearButton);
        buttons.add(undoButton);
        buttons.add(redoButton);
        add(Box.createRigidArea(new Dimension((int) Math.floor(144 * main.scale), (int) Math.floor(144 * main.scale))));

        add(buttons);

        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(nodeM.size() > 0){
                    Node node = nodeM.get(0);
                    double x = main.toInches(node.x);
                    double y = main.toInches(node.y);
                    System.out.printf("drive.trajectorySequenceBuilder(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)))%n", x, -y, (node.heading+90));
                    for (int i = 1; i < nodeM.size(); i++) {
                        node = nodeM.get(i);
                        switch (node.getType()){
                            case SPLINE:
                                System.out.printf(".splineTo(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)))%n", x, -y, (node.heading+90));
                                break;
                            case MARKER:
                                System.out.printf(".splineTo(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)))%n", x, -y, (node.heading+90));
                                System.out.println(".addDisplacementMarker(() -> {})");
                                break;
                            default:
                                System.out.println("what");
                                break;
                        }
                    }
                    System.out.println(".build()");
                }
            }
        });
        flipButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Node un = new Node(-2,-2);
                un.state = 3;
                undo.add(un);
                for (int i = 0; i < nodeM.size(); i++) {
                    Node node = nodeM.get(i);
                    node.y *= -1;
                    node.heading = 180-node.heading;
                    nodeM.set(i, node);
                }
                repaint();
            }
        });
        undoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                main.undo();
                repaint();
            }
        });
        redoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                main.redo();
                repaint();
            }
        });
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //todo: add undo for this
                undo.clear();
                redo.clear();
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

        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Node n = nodeM.get(nodeM.editIndex);
                n.index = nodeM.editIndex;
                n.state = 1;
                undo.add(n);
                nodeM.remove(nodeM.editIndex);
                repaint();
            }
        });
        makeDisplace.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nodeM.get(nodeM.editIndex).setType(Node.Type.MARKER);
            }
        });
        makeSpline.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nodeM.get(nodeM.editIndex).setType(Node.Type.SPLINE);
            }
        });
    }

    Color cyan = new Color(104, 167, 157);
    Color darkPurple = new Color(105,81,121);
    Color lightPurple = new Color(147, 88, 172);

    private void renderSplines(Graphics g, Path path, Color color) {
        for (int i = 0; i < path.length(); i++) {
            Pose2d pose1 = path.get(i-1);
            Pose2d pose2 = path.get(i);
            int x1 = (int) pose1.getX();
            int y1 = (int) pose1.getY();
            int x2 = (int) pose2.getX();
            int y2 = (int) pose2.getY();

            g.setColor(color);
            g.drawLine(x1,y1,x2,y2);
        }
    }

    private void renderRobotPath(Graphics g, Path path, Color color) {
        for (int i = 0; i < path.length(); i++) {
            Pose2d pose1 = path.get(i-1);
            int x1 = (int) pose1.getX();
            int y1 = (int) pose1.getY();
            double rX = robotWidth*main.scale;
            double rY = robotLength*main.scale;

            outLine.setToIdentity();
            outLine.translate(x1, y1);
            outLine.rotate(pose1.getHeading());

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(color);
            g2.setTransform(outLine);
            g2.fillRect((int) Math.floor(-rX/2),(int) Math.floor(-rY/2),(int) Math.floor(rX),(int) Math.floor(rY));
//            g2.fillOval((int) Math.floor(-rX/2),(int) Math.floor(-rY/2), (int) Math.floor(rX),(int) Math.floor(rY));
            g2.dispose();

//            g.setColor(new Color(0,255,0));
//            double theta1 = pose1.getHeading() - Math.toRadians(90);
//            double theta2 = pose2.getHeading() - Math.toRadians(90);
//            System.out.println(Math.cos(0.78));
//            g.fillRect((int) (x1+(Math.cos(theta1)*rX/2)), (int) (y1+(Math.sin(theta1)*rY/2)),(int)(Math.cos(theta2)*rX*2),(int)(Math.sin(theta2)*rY*2));
//            g.drawLine((int) (x1+(Math.cos(theta1)*rX)), (int) (y1+(Math.sin(theta1)*rY)), (int) (x2+(Math.cos(theta2)*rX)), (int) (y2+(Math.sin(theta2)*rY)));
//            g.drawLine((int) (x1-(Math.cos(theta1)*rX)), (int) (y1-(Math.sin(theta1)*rY)), (int) (x2-(Math.cos(theta2)*rX)), (int) (y2-(Math.sin(theta2)*rY)));
        }
    }

    private void renderPoints(Graphics g, Path path, Color c1, int ovalScale){
        path.getSegments().forEach(pathSegment -> {
            Pose2d mid = pathSegment.get(pathSegment.length()/2);
            g.setColor(c1);
            g.fillOval((int) Math.floor(mid.getX() - (ovalScale*main.scale)), (int) Math.floor(mid.getY() - (ovalScale*main.scale)), (int) Math.floor(2*ovalScale*main.scale), (int) Math.floor(2*ovalScale*main.scale));
        });
    }

    @Override
    public void paintComponent(Graphics g) {


        super.paintComponent(g);
        g.drawImage(new ImageIcon(Main.class.getResource("/field-2022-kai-dark.png")).getImage(), 0, 0, (int) Math.floor(144 * main.scale), (int) Math.floor(144 * main.scale), null);

        if(nodeM.size() > 0) {
            java.util.List<PathSegment> segments = new ArrayList<>();

            Node node = nodeM.get(0);
            for (int i = 1; i < nodeM.size(); i++) {
                final Node prevNode = node;
                node = nodeM.get(i);
                double currentX = node.x;
                double currentY = node.y;
                double prevX = prevNode.x;
                double prevY = prevNode.y;
                final double derivMag = Math.hypot(currentX - prevX, currentY - prevY);
                final double prevHeading = Math.toRadians(-prevNode.heading - 90);
                final double heading = Math.toRadians(-node.heading - 90);
                segments.add(new PathSegment(new QuinticSpline(
                        new QuinticSpline.Knot(prevX, prevY, derivMag * Math.cos(prevHeading), derivMag * Math.sin(prevHeading)),
                        new QuinticSpline.Knot(currentX, currentY, derivMag * Math.cos(heading), derivMag * Math.sin(heading)),
                        0.25, 1, 4
                )));
            }
            path = new Path(segments);
            renderRobotPath(g, path, darkPurple);
            renderSplines(g, path, cyan);
            renderPoints(g, path, cyan, 1);
            renderArrows(g, nodeM, 1);
        }
    }

    private void renderArrows(Graphics g, NodeManager nodeM, int ovalScale) {
        for (int i = 0; i < nodeM.size(); i++) {
            Node node = nodeM.get(i);
            tx.setToIdentity();
            tx.translate(node.x, node.y);
            tx.rotate(Math.toRadians(-node.heading+180));
            tx.scale (main.scale, main.scale);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setTransform(tx);

            g2.setColor(cyan);
            g2.fillOval(-ovalScale,-ovalScale, 2*ovalScale, 2*ovalScale);
            switch (node.getType()){
                case SPLINE:
                    g2.setColor(lightPurple);
                    break;
                case MARKER:
                    g2.setColor(cyan);
                    break;
            }
            g2.fill(poly);
            g2.dispose();
        }
    }

    public JPopupMenu getMenu(){
        return menu;
    }

    public Path getPath(){
        return path;
    }

}