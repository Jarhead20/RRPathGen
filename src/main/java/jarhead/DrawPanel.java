package jarhead;

import com.acmerobotics.roadrunner.*;
import com.acmerobotics.roadrunner.Action;
import jarhead.roadrunner.*;
import jarhead.roadrunner.ActionEvent;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.lang.Math;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class DrawPanel extends JPanel {
    private final LinkedList<NodeManager> managers;
    private final ProgramProperties robot;
    private TrajectorySequence trajectory;
    private final Main main;


    private LinkedList<NodeManager> managers;
    private ProgramProperties robot;
    private Action trajectory;
    DriveShim driveShim = new DriveShim(DriveTrainType.MECANUM, new Constraints(60,60,60,60,15), new Pose2d(0,0,0));
    Render render = new Render();
    private Main main;
    private Node preEdit;
    private boolean edit = false;
    final double clickSize = 2;

    private BufferedImage preRenderedSplines;
    AffineTransform tx = new AffineTransform();
    AffineTransform outLine = new AffineTransform();
    int[] xPoly = {0, -2, 0, 2};
    int[] yPoly = {0, -4, -3, -4};
    Polygon poly = new Polygon(xPoly, yPoly, xPoly.length);

    public void update(){
        resetPath();
        preRenderedSplines = null;
//        renderBackgroundSplines();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Insets in = main.getInsets();
        int width = main.getWidth()-(main.infoPanel.getWidth() + in.left + in.right + main.exportPanel.getWidth());
        int height = (main.getHeight()-(main.buttonPanel.getHeight()+in.top + in.bottom));
        int min = Math.min(width, height);
        main.scale = min/144.0;
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


    DrawPanel(LinkedList<NodeManager> managers, Main main, ProgramProperties props) {
        super();
        this.robot = props;
        this.managers = managers;
        this.main = main;
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
    }

    private void renderSplines(Graphics g, TrajectorySequence trajectory, Color color) {
        for (int i = 0; i < trajectory.size(); i++) {
            SequenceSegment segment = trajectory.get(i);
            if(segment == null) continue;

            g.setColor(color);
            g = segment.renderSplines(g, robot.resolution, main.scale);
        }
    }

    private void renderRobotPath(Graphics2D g, Action action2, Color color, float transparency) {
        if (this.getWidth() != this.getHeight()) System.out.println("w != h");
        BufferedImage image;
        if (this.getWidth() > 0)
            image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        else
            image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setColor(color);
        double rX = robot.robotLength * main.scale;
        double rY = robot.robotWidth * main.scale;
        double prevHeading = 0;
        SequentialAction action1 = (SequentialAction) action2;
        for (Action action : action1.getInitialActions()) {
            if (action != null) {
                if (action instanceof TrajectoryAction) {
                    TimeTrajectory trajectory = ((TrajectoryAction) action).getT();
//                    CompositePosePath paths = (Path) trajectory.path;

                    if (trajectory.duration > 0)
                        prevHeading = trajectory.get(0).heading.real.value();
                    double res;
                    for (double j = 0; j < trajectory.duration;) {

                        Pose2dDual<Time> pose1 = trajectory.get(j);
                        double temp = Math.min((2 * Math.PI) - Math.abs(pose1.heading.real.value() - prevHeading), Math.abs(pose1.heading.real.value() - prevHeading));
                        int x1 = (int) (pose1.position.x.value()*main.scale);
                        int y1 = (int) (pose1.position.y.value()*main.scale);

                        res = robot.resolution / ((robot.resolution) + temp); //* (1-(Math.abs(pose1.getHeading() - prevHeading)));
                        j += res/10;
                        prevHeading = pose1.heading.real.value();

                        outLine.setToIdentity();
                        outLine.translate(x1, y1);
                        outLine.rotate(pose1.heading.real.value());

                        g2.setColor(color);
                        g2.setTransform(outLine);
                        g2.fillRoundRect((int) Math.floor(-rX / 2), (int) Math.floor(-rY / 2), (int) Math.floor(rX), (int) Math.floor(rY), (int) main.scale * 2, (int) main.scale * 2);
                    }
                    if (trajectory.duration > 0) {
                        Pose2dDual<Time> end = trajectory.get(trajectory.duration);
                        outLine.setToIdentity();
                        outLine.translate(end.position.x.value()*main.scale, end.position.y.value()*main.scale);
                        outLine.rotate(end.heading.real.value());
                        g2.setTransform(outLine);
                        g2.fillRoundRect((int) Math.floor(-rX / 2), (int) Math.floor(-rY / 2), (int) Math.floor(rX), (int) Math.floor(rY), (int) main.scale * 2, (int) main.scale * 2);
                    }

                }
            }
        }
        Composite comp = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        g.drawImage(image, 0, 0, null);
        g.setComposite(comp);
        g2.dispose();




//
//        for (int i = 0; i < trajectory.size(); i++) {
//            SequenceSegment segment = trajectory.get(i);
//            if(segment != null){
//                if (segment instanceof TrajectorySegment) {
//
//                    Path path = ((TrajectorySegment) segment).getTrajectory().getPath();
//                    for (double j = 0; j < path.length();) {
//                        Pose2d pose1 = path.get(j);
//                        double temp = Math.min((2 * Math.PI) - Math.abs(pose1.getHeading() - prevHeading), Math.abs(pose1.getHeading() - prevHeading));
//                        int x1 = (int) (pose1.getX()*main.scale);
//                        int y1 = (int) (pose1.getY()*main.scale);
//
//                        res = robot.resolution / ((robot.resolution) + temp); //* (1-(Math.abs(pose1.getHeading() - prevHeading)));
//                        j += res;
//                        prevHeading = pose1.getHeading();
//
//                        outLine.setToIdentity();
//                        outLine.translate(x1, y1);
//                        outLine.rotate(pose1.getHeading());
//
//                        g2.setColor(color);
//                        g2.setTransform(outLine);
//                        g2.fillRoundRect((int) Math.floor(-rX / 2), (int) Math.floor(-rY / 2), (int) Math.floor(rX), (int) Math.floor(rY), (int) main.scale * 2, (int) main.scale * 2);
//                    }
//                    if (path.length() > 0) {
//                        Pose2d end = path.end();
//                        outLine.setToIdentity();
//                        outLine.translate(end.getX()*main.scale, end.getY()*main.scale);
//                        outLine.rotate(end.getHeading());
//                        g2.setTransform(outLine);
//                        g2.fillRoundRect((int) Math.floor(-rX / 2), (int) Math.floor(-rY / 2), (int) Math.floor(rX), (int) Math.floor(rY), (int) main.scale * 2, (int) main.scale * 2);
//                    }
//
//
//                } else if (segment instanceof TurnSegment || segment instanceof WaitSegment) {
//                    Pose2d pose1 = segment.getStartPose();
//                    Pose2d end = segment.getEndPose();
//                    int x1 = (int) pose1.getX();
//                    int y1 = (int) pose1.getY();
//                    TurnSegment segment1 = (TurnSegment) segment;
//
////                    double rotation = pose1.getHeading() - end.getHeading();
////                    double rotation = segment1.getTotalRotation();
//                    double h1 = Math.min(end.getHeading(), pose1.getHeading());
//                    double h2 = Math.max(end.getHeading(), pose1.getHeading());
//                    for (double j = h1; j < h2; j+= (robot.resolution/10)) {
//                        outLine.setToIdentity();
//                        outLine.translate(x1, y1);
//                        outLine.rotate(j);
//                        g.setColor(Color.red);
//                        g2.setTransform(outLine);
//                        g2.fillRoundRect((int) Math.floor(-rX / 2), (int) Math.floor(-rY / 2), (int) Math.floor(rX), (int) Math.floor(rY), (int) main.scale * 2, (int) main.scale * 2);
//                    }
//
////                    g.fillOval((int)startPose.getX() - (rX/2), (int)startPose.getY() - rY, rX, rY);
//                }
//            }
//        }
            Composite comp = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
            g.drawImage(image, 0, 0, null);
            g.setComposite(comp);
            g2.dispose();
//        }
    }

    private void renderPoints (Graphics g, TrajectorySequence trajectory, Color c1, int ovalScale){
        for (int i = 0; i < trajectory.size(); i++) {
            SequenceSegment segment = trajectory.get(i);
            if (segment == null) continue;
            g.setColor(c1);
            g = segment.renderPoints(g, main.scale, ovalScale);
        }
    }
//    private void renderPoints (Graphics g, Action action, Color c1, int ovalscale){
//
//        if (action != null) {
//            if (action instanceof DriveShim.TrajectoryAction) {
//                TimeTrajectory trajectory = ((DriveShim.TrajectoryAction) action).getT();
//                CompositePosePath paths = (CompositePosePath) trajectory.path;
//                for (PosePath path : paths.paths) {
//                    Pose2dDual<Arclength> mid = path.get(path.length() / 2, 0);
//
//                    double x = mid.trans.x.value()*main.scale;
//                    double y = mid.trans.y.value()*main.scale;
//                    g.setColor(c1);
//                    g.fillOval((int) Math.floor(x - (ovalscale * main.scale)), (int) Math.floor(y - (ovalscale * main.scale)), (int) Math.floor(2 * ovalscale * main.scale), (int) Math.floor(2 * ovalscale * main.scale));
//                }
//
//            } else if (action instanceof DriveShim.TurnAction || action instanceof SleepAction) {
////                Pose2d startPose = segment.getStartPose();
////                Pose2d endPose = segment.getEndPose();
////                g.drawLine((int) startPose.getX(), (int) startPose.getY(), (int) endPose.getX(), (int) endPose.getY());
//            }
//        }
////        g.setColor(Color.red);
////        g.fillOval((int) Math.floor(mx - (ovalscale * main.scale)), (int) Math.floor(my - (ovalscale * main.scale)), (int) Math.floor(2 * ovalscale * main.scale), (int) Math.floor(2 * ovalscale * main.scale));
//    }


    Color cyan = new Color(104, 167, 157);
    Color darkPurple = new Color(124, 78, 158);
    Color lightPurple = new Color(147, 88, 172);
    Color dLightPurple = lightPurple.darker();
    Color dCyan = cyan.darker();
    Color dDarkPurple = darkPurple.darker();

    double oldScale = 0;

    @Override
    public void paintComponent (Graphics g){
        super.paintComponent(g);
        long time = System.currentTimeMillis();
        long trajGen = 0;
        long renderTime = 0;
        main.infoPanel.changePanel((main.currentN == -1 && main.currentMarker != -1));

        if (preRenderedSplines == null) renderBackgroundSplines();

        main.scale = ((double) this.getWidth() - this.getInsets().left - this.getInsets().right) / 144.0;
        if (oldScale != main.scale)
            main.getManagers().forEach(nodeManager -> {
                main.scale(nodeManager, main.scale, oldScale);
                main.scale(nodeManager.undo, main.scale, oldScale);
                main.scale(nodeManager.redo, main.scale, oldScale);
            });
        g.drawImage(new ImageIcon(Objects.requireNonNull(Main.class.getResource("/field-2023-centerstage-juice-dark-rotated.png"))).getImage(), 0, 0, this.getWidth(), this.getHeight(), null);
        if (preRenderedSplines == null || preRenderedSplines.getWidth() != this.getWidth())
            renderBackgroundSplines();
        g.drawImage(preRenderedSplines, 0, 0, null);
        oldScale = main.scale;
        if (getCurrentManager().size() > 0) {
            Node node = getCurrentManager().getNodes().get(0);
            long trajGenStart = System.currentTimeMillis();
            trajectory = generateTrajectory(getCurrentManager(), node);
            trajGen = System.currentTimeMillis() - trajGenStart;
            long renderStart = System.currentTimeMillis();
            if(trajectory != null) {
//                renderRobotPath((Graphics2D) g, trajectory, lightPurple, 0.5f);
                render.renderSplines((Graphics2D) g, ActionEntityKt.actionTimeline(trajectory).getSecond(), cyan, 5, main.scale);
//                renderPoints(g, trajectory, cyan, 1);
            }
            renderTime = System.currentTimeMillis() - renderStart;
            //renderArrows(g, getCurrentManager(), 1, darkPurple, lightPurple, cyan);
        }

        double overall = (System.currentTimeMillis() - time);

        if(!Main.debug) return;
        g.drawString("trajGen (ms): " + trajGen, 10, 30);
        g.drawString("render (ms): " + renderTime, 10, 70);
        g.drawString("render (ms): " + render, 10, 70);g.drawString("node count: " + getCurrentManager().size(), 10, 50);
        g.drawString("overall (ms): " + overall, 10, 10);
    }

    private Action generateTrajectory(NodeManager manager, Node exlude){
        if(manager.size() < 2) return null;
        Node n1 = manager.get(1).shrink(main.scale);
//        TrajectoryActionBuilder builder = build.videoBuilder(new Pose2d(node.x,node.y,Math.toRadians(-node.robotHeading-90)));

        return render.generateTrajectory(driveShim, exlude, main.scale, manager);
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
            TrajectorySequence trajectory = generateTrajectory(manager, node);
            if(trajectory != null) {
                renderRobotPath((Graphics2D) g, trajectory, dLightPurple, 0.5f);
                renderSplines(g, trajectory, cyan);
                renderPoints(g, trajectory, cyan, 1);
            }
            renderArrows(g, manager, 1, dDarkPurple, dLightPurple, dCyan);
        }
        g.dispose();
    }

    private void renderArrows(Graphics g, NodeManager nodeM, int ovalScale, Color color1, Color color2, Color color3) {
        Graphics2D g2d = (Graphics2D) g.create();
        BufferedImage bufferedImage = new BufferedImage(preRenderedSplines.getWidth(), preRenderedSplines.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = bufferedImage.createGraphics();
        List<Node> nodes = nodeM.getNodes();
        for (Node node : nodes) {
            tx.setToIdentity();
            tx.translate(node.x, node.y);
            if (!node.reversed)
                tx.rotate(Math.toRadians(-node.robotHeading + 180));
            else
                tx.rotate(Math.toRadians(-node.robotHeading));
            tx.scale(main.scale, main.scale);

            g2.setTransform(tx);

            g2.setColor(color1);
            g2.fillOval(-ovalScale, -ovalScale, 2 * ovalScale, 2 * ovalScale);
            switch (node.getType()) {
                case splineTo:
                    g2.setColor(color2);
                    break;
                case splineToSplineHeading:
                    g2.setColor(color2.brighter());
                    break;
                case splineToLinearHeading:
                    g2.setColor(Color.magenta);
                    break;
                case splineToConstantHeading:
                    g2.setColor(color3.brighter());
                    break;
                default:
                    g2.setColor(color3.brighter());
//                    throw new IllegalStateException("Unexpected value: " + node.getType());
                    break;
            }
            g2.fill(poly);
        }
        g2d.drawImage(bufferedImage, 0,0,null);
    }



    private NodeManager getCurrentManager(){
        return main.getCurrentManager();
    }

    public Action getTrajectory(){
        return trajectory;
    }

    public void resetPath(){
        trajectory = null;
    }

    private void mPressed(MouseEvent e) {
        //TODO: clean up this
        this.grabFocus();
        if(edit) return;

        Node mouse = new Node(e.getPoint());
        //marker
        if (SwingUtilities.isRightMouseButton(e)) {
            double closestPose = 99999;
            Marker closestMarker = new Marker(-1);
            closestMarker.distanceToMouse = 99999;
            closestMarker.index = -1;
            double total = 0;

            List<Marker> markers = getCurrentManager().getMarkers();
            for (int i = 0; i < trajectory.size(); i++) {
                SequenceSegment segment = trajectory.get(i);
                if (segment == null) continue;
                if (!(segment instanceof TrajectorySegment)) continue;

                Trajectory traj = ((TrajectorySegment) segment).getTrajectory();
                // find closest
                for (int j = 0; j < markers.size(); j++) {
                    Pose2d pose = traj.get(markers.get(j).displacement-total);
                    double x = pose.getX() * main.scale;
                    double y = pose.getY() * main.scale;

                    double dist = mouse.distance(new Node(x, y));
                    if (dist >= closestMarker.distanceToMouse) continue;
                    closestMarker.distanceToMouse = dist;
                    closestMarker.index = j;
                }

                for (double j = 0; j < traj.duration(); j += robot.resolution/10) {
                    Pose2d pose = traj.get(j);
                    double x = pose.getX() * main.scale;
                    double y = pose.getY() * main.scale;

                    double dist = mouse.distance(new Node(x, y));
                    if (dist >= closestPose) continue;
                    closestMarker.displacement = j + total;
                    closestPose = dist;
                }
                total += traj.duration();
            }
            if(closestMarker.distanceToMouse < (clickSize * main.scale)) {
                getCurrentManager().editIndex = closestMarker.index;
            } else {
                Marker marker = new Marker(closestMarker.displacement);
                getCurrentManager().add(0, marker);
                getCurrentManager().editIndex = 0;
            }
            main.currentN = -1;
            main.currentMarker = closestMarker.index;
            main.infoPanel.markerPanel.updateText();
            edit = true;
        } else { //regular node
            Node closest = new Node();

            TrajectorySequence trajectory = getTrajectory();
            //find closest midpoint
            int counter = 0; //i don't like this but its the easiest way
            if(trajectory != null){
                for (int i = 0; i < trajectory.size(); i++) {
                    SequenceSegment segment = trajectory.get(i);
                    if(segment == null) continue;
                    if (!(segment instanceof TrajectorySegment)) continue;

                    Path path = ((TrajectorySegment) segment).getTrajectory().getPath();
                    List<PathSegment> segments = path.getSegments();

                    for (PathSegment segment2 : segments) {
                        Pose2d pose = segment2.get(segment2.length() / 2.0);

                        double px = (pose.getX()*main.scale) - mouse.x;
                        double py = (pose.getY()*main.scale) - mouse.y;
                        double midDist = Math.sqrt(px * px + py * py);
                        counter++;

                        if (midDist >= closest.distanceToMouse) continue;
                        closest.distanceToMouse = midDist;
                        closest.index = counter;
                        closest.isMidpoint = true;
                    }
                }
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

            if (closest.distanceToMouse >= (clickSize * main.scale))
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
                    main.currentN = getCurrentManager().size();
                    main.currentMarker = -1;
                    //TODO: make it face towards the tangential heading
                    mouse.splineHeading = mouse.headingTo(getCurrentManager().get(closest.index));
                    mouse.robotHeading = mouse.splineHeading;
                    getCurrentManager().add(closest.index, mouse);
                } else { //editing existing node
                    Node n2 = getCurrentManager().get(closest.index);
                    mouse.x = n2.x;
                    mouse.y = n2.y;
                    mouse.setType(n2.getType());
                    Node prev = getCurrentManager().get(closest.index);
                    preEdit = prev.copy(); //storing the existing data for undo
                    preEdit.state = Node.State.DRAG;
                    getCurrentManager().redo.clear();
                    main.currentN = closest.index;
                    main.currentMarker = -1;
                    main.infoPanel.editPanel.updateText();
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
                main.currentN = getCurrentManager().size();
                main.currentMarker = -1;
                getCurrentManager().add(mouse);
            }
        }
        main.infoPanel.editPanel.updateText();
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
        main.infoPanel.editPanel.updateText();

    }

    private void mDragged(MouseEvent e) {
        Node mouse = new Node(e.getPoint());

        if(edit){
            if (SwingUtilities.isRightMouseButton(e)) {
                int index = getCurrentManager().editIndex;
                double min = 99999;
                double displacement = -1;
                double total = 0;
                Action action = getTrajectory();
                if (action != null) {
                    if (action instanceof TrajectoryAction) {
                        TimeTrajectory trajectory = ((TrajectoryAction) action).getT();
                        CompositePosePath paths = (CompositePosePath) trajectory.path;
                        for (PosePath path : paths.paths)

                            for (double j = 0; j < path.length(); j += robot.resolution/10) {
                                Pose2dDual<Arclength> pose = path.get(j, 0);
                                double x = pose.position.x.value() * main.scale;
                                double y = pose.position.y.value() * main.scale;

                                double dist = mouse.distance(new Node(x, y));
                                if (dist < min) {
                                    displacement = j+total;
                                    min = dist;
                                }
                            }
                            total += paths.length;
                    }
                }
                ((Marker) getCurrentManager().get(index)).displacement = displacement;
                main.currentN = -1;
                main.currentMarker = index;
                main.infoPanel.markerPanel.updateText();
            } else {
                int index = getCurrentManager().editIndex;
                Node mark = getCurrentManager().get(index);
                if(e.isAltDown()) {
                    if(e.isShiftDown()) mark.robotHeading = (Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y)));
                    else mark.splineHeading = (Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y)));
                }
                else mark.setLocation(snap(mouse, e));
                main.currentN = index;
                main.currentMarker = -1;
            }

        } else {
            Node mark = getCurrentManager().last();
            mark.index = getCurrentManager().size()-1;

            mark.splineHeading = Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y));
            mark.robotHeading = mark.splineHeading;

            main.currentN = getCurrentManager().size()-1;
            main.currentMarker = -1;
            getCurrentManager().set(getCurrentManager().size()-1, snap(mark,e));
            main.infoPanel.editPanel.updateText();
        }
        main.infoPanel.editPanel.updateText();
        repaint();
    }

    private void keyInput(KeyEvent e){
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if(main.currentM <= 0) break;
                main.currentM--;
                main.currentN = -1;
                resetPath();
                break;
            case KeyEvent.VK_RIGHT:
                if(main.currentM+1 < managers.size()){
                    main.currentM++;
                    main.currentN = -1;
                    resetPath();
                } else if(getCurrentManager().size() > 0){
                    NodeManager manager = new NodeManager(new ArrayList<>(), managers.size());
                    managers.add(manager);
                    resetPath();
                    main.currentN = -1;
                    main.currentM++;
                }
                break;
            case KeyEvent.VK_R:
                if(main.currentN != -1){
                    getCurrentManager().get(main.currentN).reversed ^= true;
                }
                break;
            case KeyEvent.VK_Z:
                if(e.isControlDown()) main.undo(true);
                break;
            case KeyEvent.VK_Y:
                if(e.isControlDown()) main.redo();
                break;
            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_BACK_SPACE:
                if (main.currentN < 0) break;
                Node n = getCurrentManager().get(main.currentN);
                n.index = main.currentN;
                n.state = Node.State.DELETE;
                getCurrentManager().undo.add(n);
                getCurrentManager().remove(main.currentN);
                main.currentN--;
                break;
            }
        main.infoPanel.editPanel.updateText();
        renderBackgroundSplines();
        repaint();
    }

    private Node snap(Node node, MouseEvent e){
        if(e.isControlDown()) {
            node.x = main.scale*(Math.round(node.x/main.scale));
            node.y = main.scale*(Math.round(node.y/main.scale));
        }
        return node;
    }
}
