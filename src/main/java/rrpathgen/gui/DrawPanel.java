package rrpathgen.gui;

import com.acmerobotics.roadrunner.geometry.Pose2d;

import rrpathgen.Main;
import rrpathgen.data.Marker;
import rrpathgen.data.Node;
import rrpathgen.data.NodeManager;
import rrpathgen.data.ProgramProperties;
import rrpathgen.trajectory.OldRRTrajectory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static rrpathgen.Main.*;


public class DrawPanel extends JPanel {
    private final LinkedList<NodeManager> managers;
    private final ProgramProperties robot;
//    private TrajectorySequence trajectory;
    private rrpathgen.trajectory.Trajectory traj;
    private final Main Main;
    private Node preEdit;
    private boolean edit = false;
    final double clickSize = 2;

    private BufferedImage preRenderedSplines;
    AffineTransform tx = new AffineTransform();
    int[] xPoly = {0, -2, 0, 2};
    int[] yPoly = {0, -4, -3, -4};
    Polygon poly = new Polygon(xPoly, yPoly, xPoly.length);

    public void update(){
        switch (robot.lib){
            case RROLD:
                traj = new OldRRTrajectory();
                break;
            case RRNEW:
                traj = new rrpathgen.trajectory.NewRRTrajectory();
                break;
        }
        Main.getManagers().forEach(nodeManager -> {
            nodeManager.clear();
        });
        Main.currentM = 0;
        Main.currentN = -1;
        traj.resetPath();
        preRenderedSplines = null;
//        renderBackgroundSplines();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Insets in = Main.getInsets();
        int width = Main.getWidth()-(Main.infoPanel.getWidth() + in.left + in.right + Main.exportPanel.getWidth());
        int height = (Main.getHeight()-(Main.buttonPanel.getHeight()+in.top + in.bottom));
        int min = Math.min(width, height);
        Main.scale = min/144.0;
        return new Dimension(min, min);
    }

    @Override
    public Dimension getMinimumSize(){
        Dimension d = getPreferredSize();
        return new Dimension(d.height, d.height);
    }

    @Override
    public Dimension getMaximumSize(){
        Dimension d = getPreferredSize();
        return new Dimension(d.height, d.height);
    }


    public DrawPanel(LinkedList<NodeManager> managers, Main Main, ProgramProperties props) {
        super();
        this.robot = props;
        this.managers = managers;
        this.Main = Main;

        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mPressed(e);
            }
            public void mouseReleased(MouseEvent e) {
                mReleased(e);
            }
        });
        this.setVisible(true);

        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                mDragged(e);
            }
        });
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {keyInput(e);}
            @Override
            public void keyPressed(KeyEvent e) { }
        });
        this.setFocusable(true);
        update();
    }

    private void renderSplines(Graphics g, Color color) {
        traj.renderSplines(g, robot.resolution, Main.scale, color);
    }

    private void renderRobotPath(Graphics2D g, Color color, float transparency) {
        if (this.getWidth() != this.getHeight()) System.out.println("w != h");
        BufferedImage image;
        if (this.getWidth() > 0)
            image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        else
            image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setColor(color);
        traj.renderRobot(g2, Main.scale, 1, new Pose2d(0, 0, 0), robot);
        Composite comp = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        g.drawImage(image, 0, 0, null);
        g.setComposite(comp);
        g2.dispose();
    }

    private void renderPoints (Graphics g, Color color, int ovalScale){
        traj.renderPoints(g, Main.scale, ovalScale, color);
    }

    private void renderMarkers(Graphics g, Color color, int ovalScale){
        traj.renderMarkers(g, Main.scale, ovalScale, color);
    }


    double oldScale = 0;

    @Override
    public void paintComponent (Graphics g){
        super.paintComponent(g);
        long time = System.currentTimeMillis();
        long trajGen = 0;
        long render = 0;
        Main.infoPanel.changePanel((Main.currentN == -1 && Main.currentMarker != -1));

        if (preRenderedSplines == null) renderBackgroundSplines();

        Main.scale = ((double) this.getWidth() - this.getInsets().left - this.getInsets().right) / 144.0;
        if (oldScale != Main.scale)
            Main.getManagers().forEach(nodeManager -> {
                Main.scale(nodeManager, Main.scale, oldScale);
                Main.scale(nodeManager.undo, Main.scale, oldScale);
                Main.scale(nodeManager.redo, Main.scale, oldScale);
            });
        g.drawImage(new ImageIcon(Objects.requireNonNull(Main.class.getResource("/field-2024-into-the-deep-juice-dark.jpg"))).getImage(), 0, 0, this.getWidth(), this.getHeight(), null);
        if (preRenderedSplines == null || preRenderedSplines.getWidth() != this.getWidth())
            renderBackgroundSplines();
        g.drawImage(preRenderedSplines, 0, 0, null);
        oldScale = Main.scale;
        if (getCurrentManager().size() > 0) {
            Node node = getCurrentManager().getNodes().get(0);
            long trajGenStart = System.currentTimeMillis();
            traj.generateTrajectory(getCurrentManager(), node, robot);
            trajGen = System.currentTimeMillis() - trajGenStart;
            long renderStart = System.currentTimeMillis();
            if(traj.isReady()) {
                renderRobotPath((Graphics2D) g, lightPurple, 0.5f);
                renderSplines(g, cyan);
                renderPoints(g, cyan, 1);
                renderMarkers(g, Color.RED, 1);
            }
            renderArrows(g, getCurrentManager(), 1, darkPurple, lightPurple, cyan);
            render = System.currentTimeMillis() - renderStart;
        }

        double overall = (System.currentTimeMillis() - time);

        if(!Main.debug) return;
        g.drawString("trajGen (ms): " + trajGen, 10, 30);
        g.drawString("render (ms): " + render, 10, 70);
        g.drawString("node count: " + getCurrentManager().size(), 10, 50);
        g.drawString("overall (ms): " + overall, 10, 10);
    }

    public void renderBackgroundSplines(){
        if(this.getWidth() > 0)
            preRenderedSplines = new BufferedImage((this.getWidth()), this.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

        else preRenderedSplines = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);

        Graphics g = preRenderedSplines.getGraphics();
        for (NodeManager manager : managers){
            if(manager.equals(getCurrentManager())) continue;
            if(manager.size() <= 0) continue;
            Node node = manager.getNodes().get(0);
            traj.generateTrajectory(manager, node, robot);
            if(traj.isReady()) {
                renderRobotPath((Graphics2D) g, dLightPurple, 0.5f);
                renderSplines(g, cyan);
                renderPoints(g, cyan, 1);
            }
            renderArrows(g, manager, 1, dDarkPurple, dLightPurple, dCyan);
        }
        g.dispose();
    }

    private void renderArrows(Graphics g, NodeManager nodeM, int ovalScale, Color color1, Color color2, Color color3) {
        Graphics2D g2d = (Graphics2D) g.create();
        BufferedImage bufferedImage = new BufferedImage(preRenderedSplines.getWidth(), preRenderedSplines.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = bufferedImage.createGraphics();
        traj.renderArrows(g2, nodeM, poly, ovalScale, color1, color2, color3);
        g2d.drawImage(bufferedImage, 0,0,null);
    }



    private NodeManager getCurrentManager(){
        return Main.getCurrentManager();
    }

    public rrpathgen.trajectory.Trajectory getTrajectory(){
        return traj;
    }

    private void mPressed(MouseEvent e) {
        //TODO: clean up this
        this.grabFocus();
        if(edit) return;

        Node mouse = new Node(e.getPoint());
        //marker
        if (SwingUtilities.isRightMouseButton(e)) {
            if(traj.getValidMarkerTypes().length < 1) return;
            double closestPose = Double.MAX_VALUE;
            Marker closestMarker = new Marker(-1);
            closestMarker.distanceToMouse = Double.MAX_VALUE;
            closestMarker.index = -1;
            double total = 0;

            List<Marker> markers = getCurrentManager().getMarkers();
            for (int i = 0; i < traj.size(); i++) {
                // find closest
                for (int j = 0; j < markers.size(); j++) {
                    Pose2d pose = traj.get(i, markers.get(j).displacement-total);
                    double x = pose.getX() * Main.scale;
                    double y = pose.getY() * Main.scale;

                    double dist = mouse.distance(new Node(x, y));
                    if (dist >= closestMarker.distanceToMouse) continue;
                    closestMarker.distanceToMouse = dist;
                    closestMarker.index = j;
                }

                for (double j = 0; j < traj.duration(i); j += robot.resolution/10) {
                    Pose2d pose = traj.get(i, j);
                    double x = pose.getX() * Main.scale;
                    double y = pose.getY() * Main.scale;

                    double dist = mouse.distance(new Node(x, y));
                    if (dist >= closestPose) continue;
                    closestMarker.displacement = j + total;
                    closestPose = dist;
                }
                total += traj.duration(i);
            }
            if(closestMarker.distanceToMouse < (clickSize * Main.scale)) {
                getCurrentManager().editIndex = closestMarker.index;
            } else {
                Marker marker = new Marker(closestMarker.displacement);
                getCurrentManager().add(0, marker);
                getCurrentManager().editIndex = 0;
            }
            Main.currentN = -1;
            Main.currentMarker = closestMarker.index;
            Main.infoPanel.markerPanel.updateText();
            edit = true;
        } else { //regular node
            Node closest = new Node();
            //find closest midpoint
            int counter = 0; //i don't like this but its the easiest way

            List<Pose2d> midPoints = traj.midPoints();

            for (Pose2d pose : midPoints) {

                double px = (pose.getX()*Main.scale) - mouse.x;
                double py = (pose.getY()*Main.scale) - mouse.y;
                double midDist = Math.sqrt(px * px + py * py);
                counter++;

                if (midDist >= closest.distanceToMouse) continue;
                closest.distanceToMouse = midDist;
                closest.index = counter;
                closest.isMidpoint = true;
            }


            for (int i = 0; i < getCurrentManager().size(); i++) {
                Node close = getCurrentManager().get(i);
                double distance = mouse.distance(close);
                //find closest that isn't a midpoint
                if(distance >= closest.distanceToMouse) continue;
                closest.distanceToMouse = distance;
                closest.index = i;
                mouse.splineHeading = close.splineHeading;
                mouse.robotHeading = close.robotHeading;
                mouse.reversed = close.reversed;
                closest.isMidpoint = false;
            }

            if (closest.distanceToMouse >= (clickSize * Main.scale))
                closest.index = -1;

            snap(mouse, e);
            if(closest.index != -1){
                getCurrentManager().editIndex = closest.index;
                edit = true;
                //if the point clicked was a midpoint, gen a new point
                if (closest.isMidpoint) {
                    preEdit = (new Node(closest.index));
                    preEdit.state = Node.State.ADD;
                    getCurrentManager().redo.clear();
                    Main.currentN = getCurrentManager().size();
                    Main.currentMarker = -1;
//                    check the markers in the manager and if they are before the point to add
                    int newIndex = closest.index;
                    for (int i = 0; i < getCurrentManager().size(); i++) {
                        if(getCurrentManager().get(i) instanceof Marker) {
                            newIndex++;
                            getCurrentManager().editIndex++;
                        }
                    }

                    //TODO: make it face towards the tangential heading
                    mouse.splineHeading = mouse.headingTo(getCurrentManager().get(newIndex));
                    mouse.robotHeading = mouse.splineHeading;




                    getCurrentManager().add(newIndex, mouse);
                } else { //editing existing node
                    Node n2 = getCurrentManager().get(closest.index);
                    mouse.x = n2.x;
                    mouse.y = n2.y;
                    mouse.setType(n2.getType());
                    Node prev = getCurrentManager().get(closest.index);
                    preEdit = prev.copy(); //storing the existing data for undo
                    preEdit.state = Node.State.DRAG;
                    getCurrentManager().redo.clear();
                    Main.currentN = closest.index;
                    Main.currentMarker = -1;
                    Main.infoPanel.editPanel.updateText();
                    getCurrentManager().set(closest.index, mouse);
                }
            } else {
                int size = getCurrentManager().size();
                if(size > 0){
                    Node n1 = getCurrentManager().last();
                    mouse.splineHeading = n1.headingTo(mouse);
                    mouse.robotHeading = mouse.splineHeading;
                }
                preEdit = mouse.copy();
                preEdit.index = getCurrentManager().size();
                preEdit.state = Node.State.ADD;
                getCurrentManager().redo.clear();
                Main.currentN = getCurrentManager().size();
                Main.currentMarker = -1;
                getCurrentManager().add(mouse);
            }
        }
        Main.infoPanel.editPanel.updateText();
        repaint();
    }

    private void mReleased(MouseEvent e){
        if(SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isRightMouseButton(e)){
            edit = false;
        }
        if(SwingUtilities.isLeftMouseButton(e)){
            getCurrentManager().undo.add(preEdit);
            getCurrentManager().editIndex = -1;
        }
        Main.infoPanel.editPanel.updateText();

    }

    private void mDragged(MouseEvent e) {
        Node mouse = new Node(e.getPoint());

        if(edit){
            if (SwingUtilities.isRightMouseButton(e)) {
                int index = getCurrentManager().editIndex;
                double min = 99999;
                double displacement = -1;
                double total = 0;
                for (int i = 0; i < traj.size(); i++) {

                    for (double j = 0; j < traj.duration(i); j += robot.resolution/10) {
                        Pose2d pose = traj.get(i, j);
                        double x = pose.getX() * Main.scale;
                        double y = pose.getY() * Main.scale;

                        double dist = mouse.distance(new Node(x, y));
                        if (dist >= min) continue;
                        displacement = j+total;
                        min = dist;
                    }
                    total += traj.duration(i);
                }
                ((Marker) getCurrentManager().get(index)).displacement = displacement;
                Main.currentN = -1;
                Main.currentMarker = index;
                Main.infoPanel.markerPanel.updateText();
            } else {
                int index = getCurrentManager().editIndex;
                Node mark = getCurrentManager().get(index);

                double heading = (Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y)));
                if (e.isControlDown()) heading = Math.floor((heading + 22.5) / 45) * 45;
                if(e.isAltDown()) {
                    if(e.isShiftDown()) mark.robotHeading = heading;
                    else mark.splineHeading = heading;
                }
                else mark.setLocation(snap(mouse, e));
                Main.currentN = index;
                Main.currentMarker = -1;
            }

        } else {
            Node mark = getCurrentManager().last();
            mark.index = getCurrentManager().size()-1;

            double heading = (Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y)));
            if (e.isControlDown()) heading = Math.floor((heading + 22.5) / 45) * 45;
            mark.splineHeading = heading;
            mark.robotHeading = heading;

            Main.currentN = getCurrentManager().size()-1;
            Main.currentMarker = -1;
            getCurrentManager().set(getCurrentManager().size()-1, snap(mark,e));
            Main.infoPanel.editPanel.updateText();
        }
        Main.infoPanel.editPanel.updateText();
        repaint();
    }

    private void keyInput(KeyEvent e){
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if(Main.currentM <= 0) break;
                Main.currentM--;
                Main.currentN = -1;
                traj.resetPath();
                break;
            case KeyEvent.VK_RIGHT:
                if(Main.currentM+1 < managers.size()){
                    Main.currentM++;
                    Main.currentN = -1;
                    traj.resetPath();
                } else if(getCurrentManager().size() > 0){
                    NodeManager manager = new NodeManager(new ArrayList<>(), managers.size());
                    managers.add(manager);
                    traj.resetPath();
                    Main.currentN = -1;
                    Main.currentM++;
                }
                break;
            case KeyEvent.VK_R:
                if(Main.currentN != -1){
                    getCurrentManager().get(Main.currentN).reversed ^= true;
                }
                break;
            case KeyEvent.VK_Z:
                if(e.isControlDown()) Main.undo(true);
                break;
            case KeyEvent.VK_Y:
                if(e.isControlDown()) Main.redo();
                break;
            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_BACK_SPACE:
                if (Main.currentN < 0) break;
                Node n = getCurrentManager().get(Main.currentN);
                n.index = Main.currentN;
                n.state = Node.State.DELETE;
                getCurrentManager().undo.add(n);
                getCurrentManager().remove(Main.currentN);
                Main.currentN--;
        }
        Main.infoPanel.editPanel.updateText();
        renderBackgroundSplines();
        repaint();
    }

    private Node snap(Node node, MouseEvent e){
        if(e.isControlDown()) {
            node.x = Main.scale*(Math.round(node.x/Main.scale));
            node.y = Main.scale*(Math.round(node.y/Main.scale));
        }
        return node;
    }
}