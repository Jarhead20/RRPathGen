package jarhead;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.path.Path;
import com.acmerobotics.roadrunner.path.PathSegment;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.constraints.MecanumVelocityConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.ProfileAccelerationConstraint;
import jarhead.trajectorysequence.TrajectorySequence;
import jarhead.trajectorysequence.TrajectorySequenceBuilder;
import jarhead.trajectorysequence.sequencesegment.SequenceSegment;
import jarhead.trajectorysequence.sequencesegment.TrajectorySegment;
import jarhead.trajectorysequence.sequencesegment.TurnSegment;
import jarhead.trajectorysequence.sequencesegment.WaitSegment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class DrawPanel extends JPanel {
    private final LinkedList<NodeManager> managers;
    private final ProgramProperties robot;
    private TrajectorySequence trajectory;
    private final Main main;
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

    private void renderRobotPath(Graphics2D g, TrajectorySequence trajectory, Color color, float transparency) {
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
        if (trajectory.get(0).getDuration() > 0)
            prevHeading = trajectory.start().getHeading();
        double res;


        for (int i = 0; i < trajectory.size(); i++) {
            SequenceSegment segment = trajectory.get(i);
            if(segment == null) continue;
            if (segment instanceof TrajectorySegment) {

                Path path = ((TrajectorySegment) segment).getTrajectory().getPath();
                for (double j = 0; j < path.length();) {
                    Pose2d pose1 = path.get(j);
                    double temp = Math.min((2 * Math.PI) - Math.abs(pose1.getHeading() - prevHeading), Math.abs(pose1.getHeading() - prevHeading));
                    int x1 = (int) (pose1.getX()*main.scale);
                    int y1 = (int) (pose1.getY()*main.scale);

                    outLine.setToIdentity();
                    outLine.translate(x1, y1);
                    outLine.rotate(pose1.getHeading());

                    g2.setColor(color);
                    g2.setTransform(outLine);
                    g2.fillRoundRect((int) Math.floor(-rX / 2), (int) Math.floor(-rY / 2), (int) Math.floor(rX), (int) Math.floor(rY), (int) main.scale * 2, (int) main.scale * 2);

                    res = robot.resolution / ((robot.resolution) + temp); //* (1-(Math.abs(pose1.getHeading() - prevHeading)));
                    j += res;
                    prevHeading = pose1.getHeading();
                }
                if (path.length() > 0) {
                    Pose2d end = path.end();
                    outLine.setToIdentity();
                    outLine.translate(end.getX()*main.scale, end.getY()*main.scale);
                    outLine.rotate(end.getHeading());
                    g2.setTransform(outLine);
                    g2.fillRoundRect((int) Math.floor(-rX / 2), (int) Math.floor(-rY / 2), (int) Math.floor(rX), (int) Math.floor(rY), (int) main.scale * 2, (int) main.scale * 2);
                }


            } else if (segment instanceof TurnSegment || segment instanceof WaitSegment) {
                //
                Pose2d pose1 = segment.getStartPose();
                Pose2d end = segment.getEndPose();
                int x1 = (int) pose1.getX();
                int y1 = (int) pose1.getY();

                double h1 = Math.min(end.getHeading(), pose1.getHeading());
                double h2 = Math.max(end.getHeading(), pose1.getHeading());
                for (double j = h1; j < h2; j+= (robot.resolution/10)) {
                    outLine.setToIdentity();
                    outLine.translate(x1, y1);
                    outLine.rotate(j);
                    g.setColor(Color.red);
                    g2.setTransform(outLine);
                    g2.fillRoundRect((int) Math.floor(-rX / 2), (int) Math.floor(-rY / 2), (int) Math.floor(rX), (int) Math.floor(rY), (int) main.scale * 2, (int) main.scale * 2);
                }
            }
        }
        Composite comp = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        g.drawImage(image, 0, 0, null);
        g.setComposite(comp);
        g2.dispose();
    }

    private void renderPoints (Graphics g, TrajectorySequence trajectory, Color c1, int ovalScale){
        for (int i = 0; i < trajectory.size(); i++) {
            SequenceSegment segment = trajectory.get(i);
            if (segment == null) continue;
            g.setColor(c1);
            g = segment.renderPoints(g, main.scale, ovalScale);
        }
    }


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
        long render = 0;
        main.infoPanel.changePanel((main.currentN == -1 && main.currentMarker != -1));

        if (preRenderedSplines == null) renderBackgroundSplines();

        main.scale = ((double) this.getWidth() - this.getInsets().left - this.getInsets().right) / 144.0;
        if (oldScale != main.scale)
            main.getManagers().forEach(nodeManager -> {
                main.scale(nodeManager, main.scale, oldScale);
                main.scale(nodeManager.undo, main.scale, oldScale);
                main.scale(nodeManager.redo, main.scale, oldScale);
            });
        g.drawImage(new ImageIcon(Objects.requireNonNull(Main.class.getResource("/field-2022-kai-dark.png"))).getImage(), 0, 0, this.getWidth(), this.getHeight(), null);
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
                renderRobotPath((Graphics2D) g, trajectory, lightPurple, 0.5f);
                renderSplines(g, trajectory, cyan);
                renderPoints(g, trajectory, cyan, 1);
            }
            renderArrows(g, getCurrentManager(), 1, darkPurple, lightPurple, cyan);
            render = System.currentTimeMillis() - renderStart;
        }

        double overall = (System.currentTimeMillis() - time);
        if(Main.debug){
            g.drawString("trajGen (ms): " + trajGen, 10, 30);
            g.drawString("render (ms): " + render, 10, 70);
            g.drawString("node count: " + getCurrentManager().size(), 10, 50);
            g.drawString("overall (ms): " + overall, 10, 10);
        }
    }

    private TrajectorySequence generateTrajectory(NodeManager manager, Node exclude){
        Node node = exclude.shrink(main.scale);
        TrajectorySequenceBuilder builder = new TrajectorySequenceBuilder(new Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading - 90)), Math.toRadians(-node.splineHeading - 90), new MecanumVelocityConstraint(robot.maxVelo, robot.trackWidth), new ProfileAccelerationConstraint(robot.maxAccel), robot.maxAngVelo, robot.maxAngAccel);
        builder.setReversed(exclude.reversed);
        for (int i = 0; i < manager.size(); i++) {
            if(exclude.equals(manager.get(i))) continue; //stops empty path segment error

            node = manager.get(i).shrink(main.scale);

            try{

                switch (node.getType()){
                    case splineTo:
                        builder.splineTo(new Vector2d(node.x, node.y), Math.toRadians(-node.splineHeading-90));
                        break;
                    case splineToSplineHeading:
                        builder.splineToSplineHeading(new Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading-90)), Math.toRadians(-node.splineHeading-90));
                        break;
                    case splineToLinearHeading:
                        builder.splineToLinearHeading(new Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading-90)), Math.toRadians(-node.splineHeading-90));
                        break;
                    case splineToConstantHeading:
                        builder.splineToConstantHeading(new Vector2d(node.x, node.y), Math.toRadians(-node.splineHeading-90));
                        break;
                    case lineTo:
                        builder.lineTo(new Vector2d(node.x, node.y));
                        break;
                    case lineToSplineHeading:
                        builder.lineToSplineHeading(new Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading-90)));
                        break;
                    case lineToLinearHeading:
                        builder.lineToLinearHeading(new Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading-90)));
                        break;
                    case lineToConstantHeading:
                        builder.lineToConstantHeading(new Vector2d(node.x, node.y));
                        break;
                    case addTemporalMarker:
                        Marker marker = (Marker) manager.get(i);
                        builder.UNSTABLE_addTemporalMarkerOffset(marker.displacement, () -> {});
                }
                builder.setReversed(node.reversed);
            } catch (Exception e) {
                main.undo(false);
                i--;
                e.printStackTrace();
            }
        }
        if(manager.size() > 1)
            return builder.build();
        return null;
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

    public TrajectorySequence getTrajectory(){
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
            double min = 99999;
            double displacement = -1;
            double closestMarker = min;
            int index = -1;
            double total = 0;
            List<Marker> markers = getCurrentManager().getMarkers();
            for (int i = 0; i < trajectory.size(); i++) {
                SequenceSegment segment = trajectory.get(i);
                if (segment == null) continue;
                if (!(segment instanceof TrajectorySegment)) continue;

                Trajectory traj = ((TrajectorySegment) segment).getTrajectory();
                for (int j = 0; j < markers.size(); j++) {
                    Pose2d pose = traj.get(markers.get(j).displacement-total);
                    double dist = mouse.distance(new Node(pose.getX()*main.scale, pose.getY()*main.scale));
                    if (dist >= closestMarker) continue;
                    closestMarker = dist;
                    index = j;
                }

                for (double j = 0; j < traj.duration(); j += robot.resolution/10) {
                    Pose2d pose = traj.get(j);
                    double x = pose.getX() * main.scale;
                    double y = pose.getY() * main.scale;

                    double dist = mouse.distance(new Node(x, y));
                    if (dist >= min) continue;
                    displacement = j + total;
                    min = dist;
                }
                total += traj.duration();
            }
            if(closestMarker < (clickSize * main.scale)) {
                getCurrentManager().editIndex = index;
            } else {
                Marker marker = new Marker(displacement);
                getCurrentManager().add(0, marker);
                getCurrentManager().editIndex = 0;
            }
            main.currentN = -1;
            main.currentMarker = index;
            main.infoPanel.markerPanel.updateText();
            edit = true;
        } else { //regular node
            Node closest = new Node();
            
            TrajectorySequence trajectory = getTrajectory();
            //find closest mid
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
                        closest.mid = true;
                    }
                }
            }

            for (int i = 0; i < getCurrentManager().size(); i++) {
                Node close = getCurrentManager().get(i);
                double distance = mouse.distance(close);
                //find closest that isn't a mid
                if(distance >= closest.distanceToMouse) continue;
                closest.distanceToMouse = distance;
                closest.index = i;
                mouse.splineHeading = close.splineHeading;
                mouse.robotHeading = close.robotHeading;
                mouse.reversed = close.reversed;
                closest.mid = false;
            }

            if (closest.distanceToMouse >= (clickSize * main.scale))
                closest.index = -1;

            snap(mouse, e);
            if(closest.index != -1){
                if(e.getClickCount() == 1) {
                    getCurrentManager().editIndex = closest.index;
                    edit = true;
                    //if the point clicked was a mid point, gen a new point
                    if (closest.mid) {
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
                }
            } else if(e.getClickCount() == 1){
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
                for (int i = 0; i < trajectory.size(); i++) {
                    SequenceSegment segment = trajectory.get(i);
                    if (segment == null) continue;
                    if (!(segment instanceof TrajectorySegment)) continue;

                    Trajectory path = ((TrajectorySegment) segment).getTrajectory();
                    for (double j = 0; j < path.duration(); j += robot.resolution/10) {
                        Pose2d pose = path.get(j);
                        double x = pose.getX() * main.scale;
                        double y = pose.getY() * main.scale;

                        double dist = mouse.distance(new Node(x, y));
                        if (dist >= min) continue;
                        displacement = j+total;
                        min = dist;
                    }
                    total += path.duration();
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