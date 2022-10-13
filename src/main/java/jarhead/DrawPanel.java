package jarhead;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.path.Path;
import com.acmerobotics.roadrunner.path.PathSegment;
import com.acmerobotics.roadrunner.path.QuinticSpline;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DrawPanel extends JPanel {

    private static final Pattern numberPattern = Pattern.compile("[+-]?(\\d*\\.)?\\d+");
    private static final Pattern pathName = Pattern.compile("(?:^\\s*Trajectory\\s+(\\w*))");

    private LinkedList<NodeManager> managers;

    private Path path;
    private Main main;
    private final JPopupMenu menu = new JPopupMenu("Menu");

    private final JMenuItem delete = new JMenuItem("Delete");
    private final JMenuItem makeDisplace = new JMenuItem("Make Displacement Marker");
    private final JMenuItem makeSpline = new JMenuItem("Make Spline");
    private final JMenuItem setXY = new JMenuItem("Set X, Y");

    JTextField codeField = new JTextField("");
    NumberFormat format = NumberFormat.getInstance();
//    NumberFormatter formatter = new NumberFormatter(format);
//    JTextField fX = new JFormattedTextField(formatter);
//    JTextField fY = new JFormattedTextField(formatter);
    JTextField fX = new JTextField("");
    JTextField fY = new JTextField("");



    private final JButton exportButton = new JButton("Export");
    private final JButton importButton = new JButton("Import");
    public final JButton flipButton = new JButton("Flip");
    private final JButton clearButton = new JButton("Clear");
    private final JButton undoButton = new JButton("Undo");
    private final JButton redoButton = new JButton("Redo");
    private BufferedImage preRenderedSplines;
    AffineTransform tx = new AffineTransform();
    AffineTransform outLine = new AffineTransform();
    int[] xPoly = {0, -2, 0, 2};
    int[] yPoly = {0, -4, -3, -4};
    Polygon poly = new Polygon(xPoly, yPoly, xPoly.length);

    DrawPanel(LinkedList<NodeManager> managers, Main main) {
        this.managers = managers;
        this.main = main;
        preRenderedSplines = new BufferedImage((int) Math.floor(144*main.scale), (int) Math.floor(144*main.scale), BufferedImage.TYPE_4BYTE_ABGR);
        setPreferredSize(new Dimension((int) Math.floor(144 * main.scale + 4), (int) Math.floor(144 * main.scale + (30))));
        JPanel buttons = new JPanel(new GridLayout(1, 4, 1, 1));

        menu.add(delete);
        menu.add(makeDisplace);
        menu.add(makeSpline);
        menu.add(setXY);
        add(menu);

        exportButton.setFocusable(false);
        importButton.setFocusable(false);
        flipButton.setFocusable(false);
        clearButton.setFocusable(false);
        undoButton.setFocusable(false);
        redoButton.setFocusable(false);

        buttons.add(exportButton);
        buttons.add(importButton);
        buttons.add(flipButton);
        buttons.add(clearButton);
        buttons.add(undoButton);
        buttons.add(redoButton);
        add(Box.createRigidArea(new Dimension((int) Math.floor(144 * main.scale), (int) Math.floor(144 * main.scale))));
        add(buttons);
        add(codeField);
        add(fX);
        add(fY);
        fX.setFocusable(false);
        fY.setFocusable(false);
        codeField.setFocusable(false);
        fX.setVisible(false);
        fY.setVisible(false);
        codeField.setVisible(false);

        codeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                escape(e);
            }
        });

        fX.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                escape(e);
            }
        });

        fY.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                escape(e);
            }
        });



        codeField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Node node = getCurrentManager().get(getCurrentManager().editIndex);
                node.setType(Node.Type.MARKER);
                node.code = codeField.getText();
                codeField.setVisible(false);
                codeField.setFocusable(false);
                setFocusable(true);
                grabFocus();
                repaint();
            }
        });

        fX.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fY.grabFocus();
            }
        });

        fY.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Node node = getCurrentManager().get(getCurrentManager().editIndex);

                double x = node.x;
                double y = node.y;
                if(fY.getText().length() > 0)
                    y = (Double.parseDouble(fY.getText())+72)* main.scale;
                if(fX.getText().length() > 0)
                    x = (Double.parseDouble(fX.getText())+72)* main.scale;
                if(x != node.x || y != node.y){
                    Node temp = node.copy();
                    temp.state = 4;
                    getCurrentManager().undo.add(temp);
                    node.x = x;
                    node.y = y;

                }
                fX.setVisible(false);
                fY.setVisible(false);
                fX.setFocusable(false);
                fY.setFocusable(false);
                setFocusable(true);
                grabFocus();
                repaint();
            }
        });

        setXY.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Node node = getCurrentManager().get(getCurrentManager().editIndex);
                fX.setBounds((int)node.x, (int)node.y, 40,20);
                fY.setBounds((int)node.x + 50, (int)node.y, 40,20);
                fX.setVisible(true);
                fY.setVisible(true);
                fX.setFocusable(true);
                fY.setFocusable(true);
                setFocusable(false);
                fX.grabFocus();
                fX.setText("");
                fY.setText("");
            }
        });

        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(getCurrentManager().size() > 0){
                    Node node = getCurrentManager().get(0);
                    double x = main.toInches(node.x);
                    double y = main.toInches(node.y);
                    System.out.printf("Trajectory %s = drive.trajectoryBuilder(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)))%n",getCurrentManager().name, x, -y, (node.heading+90));
                    for (int i = 1; i < getCurrentManager().size(); i++) {
                        node = getCurrentManager().get(i);
                        x = main.toInches(node.x);
                        y = main.toInches(node.y);
                        switch (node.getType()){
                            case SPLINE:
                                System.out.printf(".splineTo(new Vector2d(%.2f, %.2f), Math.toRadians(%.2f))%n", x, -y, (node.heading+90));
                                break;
                            case MARKER:
                                System.out.printf(".splineTo(new Vector2d(%.2f, %.2f), Math.toRadians(%.2f))%n", x, -y, (node.heading+90));
                                System.out.printf(".addDisplacementMarker(() -> {%s})%n", node.code);
                                break;
                            default:
                                System.out.println("what");
                                break;
                        }
                    }
                    System.out.println(".build();");
                }
            }
        });
        flipButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Node un = new Node(-2,-2);
                un.state = 3;
                for (int i = 0; i < getCurrentManager().size(); i++) {
                    Node node = getCurrentManager().get(i);
                    node.y = 144*main.scale-node.y;
                    node.heading = 180-node.heading;
                    getCurrentManager().set(i, node);
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
                getCurrentManager().undo.clear();
                getCurrentManager().redo.clear();
                getCurrentManager().clear();
                int id = main.currentM;
                for (int i = id; i < managers.size()-1; i++) {
                    managers.set(i, managers.get(i+1));
                }
                if(managers.size() > 1)
                    managers.removeLast();
                else main.currentM = 0;
                if(main.currentM > 0)
                    main.currentM--;
                resetPath();

                renderBackgroundSplines();
                repaint();
            }
        });
        importButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                NodeManager manager = null;
                try {
                    File file = new File(Main.class.getResource("/import.java").toURI());
                    Scanner reader = new Scanner(file);
                    boolean discard = true;
                    while (reader.hasNextLine()) {
                        String line = reader.nextLine();

                        if (line.contains("trajectoryBuilder")){
                            discard = false;
                            Matcher matcher = pathName.matcher(line);
                            matcher.find();
                            String name = matcher.group(1).trim();
                            if(getCurrentManager().size() > 0)
                                manager = new NodeManager(new ArrayList<>(), managers.size(), name);
                            else manager = getCurrentManager();
                            if(line.contains("true")) manager.reversed = true;
                            managers.add(manager);
                        }
                        if(!discard){
                            if(line.contains("new Vector2d(") || line.contains("new Pose2d(")){
                                Matcher m = numberPattern.matcher(line);
                                Node node = new Node();
                                String[] data = new String[4];
                                for (int i = 0; m.find(); i++) {
                                    data[i]=m.group(0);
                                }
                                try{
                                    node.x = (Double.parseDouble(data[1])+72)* main.scale;
                                    node.y = (-Double.parseDouble(data[2])+72)* main.scale;
                                    node.heading = Double.parseDouble(data[3])-90;
                                } catch (Exception error){
//                                    error.printStackTrace();
                                    node.x = 72* main.scale;
                                    node.y = 72* main.scale;
                                    node.heading = 270;
                                }
                                if(manager.reversed && manager.size() == 0) node.heading += 180;
                                manager.add(node);
                            } else if(line.contains(".addDisplacementMarker(")){
                                (manager.get(manager.size()-1)).setType(Node.Type.MARKER);
                            } else {
                                discard = true;
                            }
                        }
                    }
                } catch (URISyntaxException | FileNotFoundException uriSyntaxException) {
                    uriSyntaxException.printStackTrace();
                }
                main.currentM = managers.size()-1;
                renderBackgroundSplines();
                repaint();
            }
        });
        menu.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { repaint(); }
            public void popupMenuCanceled(PopupMenuEvent e) { repaint(); }
        });

        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Node n = getCurrentManager().get(getCurrentManager().editIndex);
                n.index = getCurrentManager().editIndex;
                n.state = 1;
                getCurrentManager().undo.add(n);
                getCurrentManager().remove(getCurrentManager().editIndex);
                repaint();
            }
        });
        makeDisplace.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Node node = getCurrentManager().get(getCurrentManager().editIndex);
                codeField.setBounds((int)node.x, (int)node.y, 100,20);
                codeField.setText("");
                codeField.setVisible(true);
                codeField.setFocusable(true);
                setFocusable(false);
                codeField.grabFocus();

            }
        });
        makeSpline.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getCurrentManager().get(getCurrentManager().editIndex).setType(Node.Type.SPLINE);
            }
        });
    }



    private void renderSplines(Graphics g, Path path, Color color) {
        for (double i = 0; i < path.length(); i+=main.resolution) {
            Pose2d pose1 = path.get(i-main.resolution);
            Pose2d pose2 = path.get(i);
            int x1 = (int) pose1.getX();
            int y1 = (int) pose1.getY();
            int x2 = (int) pose2.getX();
            int y2 = (int) pose2.getY();

            g.setColor(color);
            g.drawLine(x1,y1,x2,y2);
        }
    }

    private void renderRobotPath(Graphics2D g, Path path, Color color, float transparency) {
        BufferedImage image = new BufferedImage((int) Math.floor(144 * main.scale), (int) Math.floor(144 * main.scale), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        for (double i = 0; i < path.length(); i+=main.resolution) {
            Pose2d pose1 = path.get(i-1);
            int x1 = (int) pose1.getX();
            int y1 = (int) pose1.getY();
            double rX = main.robotLength*main.scale;
            double rY = main.robotWidth*main.scale;

            outLine.setToIdentity();
            outLine.translate(x1, y1);
            outLine.rotate(pose1.getHeading());

            g2.setColor(color);
            g2.setTransform(outLine);
//            if(i == 0 || i >= path.length()-1)
            g2.fillRect((int) Math.floor(-rX/2),(int) Math.floor(-rY/2),(int) Math.floor(rX),(int) Math.floor(rY));
//            else
//                g2.fillOval((int) Math.floor(-rX/2),(int) Math.floor(-rY/2), (int) Math.floor(rX),(int) Math.floor(rY));

//            g.setColor(new Color(0,255,0));
//            double theta1 = pose1.getHeading() - Math.toRadians(90);
//            double theta2 = pose2.getHeading() - Math.toRadians(90);
//            System.out.println(Math.cos(0.78));
//            g.fillRect((int) (x1+(Math.cos(theta1)*rX/2)), (int) (y1+(Math.sin(theta1)*rY/2)),(int)(Math.cos(theta2)*rX*2),(int)(Math.sin(theta2)*rY*2));
//            g.drawLine((int) (x1+(Math.cos(theta1)*rX)), (int) (y1+(Math.sin(theta1)*rY)), (int) (x2+(Math.cos(theta2)*rX)), (int) (y2+(Math.sin(theta2)*rY)));
//            g.drawLine((int) (x1-(Math.cos(theta1)*rX)), (int) (y1-(Math.sin(theta1)*rY)), (int) (x2-(Math.cos(theta2)*rX)), (int) (y2-(Math.sin(theta2)*rY)));
        }
        Composite comp = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        g.drawImage(image, 0,0,null);
        g.setComposite(comp);
        g2.dispose();
    }

    private void renderPoints(Graphics g, Path path, Color c1, int ovalScale){
        path.getSegments().forEach(pathSegment -> {
            Pose2d mid = pathSegment.get(pathSegment.length()/2);
            g.setColor(c1);
            g.fillOval((int) Math.floor(mid.getX() - (ovalScale*main.scale)), (int) Math.floor(mid.getY() - (ovalScale*main.scale)), (int) Math.floor(2*ovalScale*main.scale), (int) Math.floor(2*ovalScale*main.scale));
        });
    }


    Color cyan = new Color(104, 167, 157);
    Color darkPurple = new Color(124, 78, 158);
    Color lightPurple = new Color(147, 88, 172);
    Color dLightPurple = lightPurple.darker();
    Color dCyan = cyan.darker();
    Color dDarkPurple = darkPurple.darker();

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(new ImageIcon(Main.class.getResource("/field-2022-kai-dark.png")).getImage(), 0, 0, (int) Math.floor(144 * main.scale), (int) Math.floor(144 * main.scale), null);
        g.drawImage(preRenderedSplines, 0,0,null);
        if(getCurrentManager().size() > 0) {
            java.util.List<PathSegment> segments = new ArrayList<>();

            Node node = getCurrentManager().get(0);
            for (int i = 1; i < getCurrentManager().size(); i++) {
                final Node prevNode = node;
                node = getCurrentManager().get(i);
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

            renderRobotPath((Graphics2D) g, path, lightPurple, 0.5f);
            renderSplines(g, path, cyan);
            renderPoints(g, path, cyan, 1);
            renderArrows(g, getCurrentManager(), 1, darkPurple, lightPurple, cyan);
        }
    }

    public void renderBackgroundSplines(){
        preRenderedSplines = new BufferedImage(preRenderedSplines.getWidth(), preRenderedSplines.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = preRenderedSplines.getGraphics();
        for (NodeManager manager : managers){
            if(!manager.equals(getCurrentManager())){
                if(manager.size() > 0) {

                    java.util.List<PathSegment> segments = new ArrayList<>();

                    Node node = manager.get(0);
                    for (int i = 1; i < manager.size(); i++) {
                        final Node prevNode = node;
                        node = manager.get(i);
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
                    Path path = new Path(segments);

                    renderRobotPath((Graphics2D) g, path, dLightPurple, 0.5f);
                    renderSplines(g, path, dCyan);
                    renderPoints(g, path, dCyan, 1);
                    renderArrows(g, manager, 1, dDarkPurple, dLightPurple, dCyan);
                }
            }
        }
    }

    private void renderArrows(Graphics g, NodeManager nodeM, int ovalScale, Color color1, Color color2, Color color3) {
        for (int i = 0; i < nodeM.size(); i++) {
            Node node = nodeM.get(i);
            tx.setToIdentity();
            tx.translate(node.x, node.y);
            if(nodeM.reversed)
                tx.rotate(Math.toRadians(-node.heading));
            else
                tx.rotate(Math.toRadians(-node.heading+180));
            tx.scale (main.scale, main.scale);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setTransform(tx);

            g2.setColor(color1);
            g2.fillOval(-ovalScale,-ovalScale, 2*ovalScale, 2*ovalScale);
            switch (node.getType()){
                case SPLINE:
                    g2.setColor(color2);
                    break;
                case MARKER:
                    g2.setColor(color3);
                    break;
            }
            g2.fill(poly);
            g2.dispose();
        }
    }

    public JPopupMenu getMenu(){
        return menu;
    }

    private NodeManager getCurrentManager(){
        return main.getCurrentManager();
    }

    public Path getPath(){
        return path;
    }

    public void resetPath(){
        path = null;
    }

    private void escape (KeyEvent e){
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
            codeField.setVisible(false);
            codeField.setFocusable(false);
            fX.setVisible(false);
            fY.setVisible(false);
            fX.setFocusable(false);
            fY.setFocusable(false);
            setFocusable(true);
            grabFocus();
            repaint();
        }
    }

}