package jarhead;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.path.*;
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



public class DrawPanel extends JPanel {

    boolean debug = true;

    private LinkedList<NodeManager> managers;
    private ProgramProperties robot;
    private TrajectorySequence trajectory;
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
            if(segment != null){
                if (segment instanceof TrajectorySegment) {
                    Path path = ((TrajectorySegment) segment).getTrajectory().getPath();
                    g.setColor(color);
                    for (double j = 0; j < path.length(); j+= robot.resolution) {
                        Pose2d pose1 = path.get(j-robot.resolution);
                        Pose2d pose2 = path.get(j);
                        int x1 = (int) (pose1.getX()*main.scale);
                        int y1 = (int) (pose1.getY()*main.scale);
                        int x2 = (int) (pose2.getX()*main.scale);
                        int y2 = (int) (pose2.getY()*main.scale);
                        g.drawLine(x1,y1,x2,y2);
                    }

                } else if (segment instanceof TurnSegment || segment instanceof WaitSegment) {
                    Pose2d startPose = segment.getStartPose();
                    Pose2d endPose = segment.getEndPose();
                    g.drawLine((int)startPose.getX(), (int)startPose.getY(), (int)endPose.getX(), (int)endPose.getY());
                }
            }
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
            if(segment != null){
                if (segment instanceof TrajectorySegment) {

                    Path path = ((TrajectorySegment) segment).getTrajectory().getPath();
                    for (double j = 0; j < path.length();) {
                        Pose2d pose1 = path.get(j);
                        double temp = Math.min((2 * Math.PI) - Math.abs(pose1.getHeading() - prevHeading), Math.abs(pose1.getHeading() - prevHeading));
                        int x1 = (int) (pose1.getX()*main.scale);
                        int y1 = (int) (pose1.getY()*main.scale);

                        res = robot.resolution / ((robot.resolution) + temp); //* (1-(Math.abs(pose1.getHeading() - prevHeading)));
                        j += res;
                        prevHeading = pose1.getHeading();

                        outLine.setToIdentity();
                        outLine.translate(x1, y1);
                        outLine.rotate(pose1.getHeading());

                        g2.setColor(color);
                        g2.setTransform(outLine);
                        g2.fillRoundRect((int) Math.floor(-rX / 2), (int) Math.floor(-rY / 2), (int) Math.floor(rX), (int) Math.floor(rY), (int) main.scale * 2, (int) main.scale * 2);
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
                    Pose2d pose1 = segment.getStartPose();
                    Pose2d end = segment.getEndPose();
                    int x1 = (int) pose1.getX();
                    int y1 = (int) pose1.getY();
                    TurnSegment segment1 = (TurnSegment) segment;

//                    double rotation = pose1.getHeading() - end.getHeading();
//                    double rotation = segment1.getTotalRotation();
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

//                    g.fillOval((int)startPose.getX() - (rX/2), (int)startPose.getY() - rY, rX, rY);
                }
            }
        }


//        for (double i = 0; i < this.trajectory.duration(); ) {
//            Pose2d pose1 = this.trajectory.start();
//            int x1 = (int) pose1.getX();
//            int y1 = (int) pose1.getY();
//            double temp = Math.min((2 * Math.PI) - Math.abs(pose1.getHeading() - prevHeading), Math.abs(pose1.getHeading() - prevHeading));
//
//
//            for (int i = 0; i < trajectory.size(); i++) {
//                SequenceSegment segment = trajectory.get(i);
//                if (segment != null) {
//                    if (segment instanceof TrajectorySegment) {
//
//                        Path path = ((TrajectorySegment) segment).getTrajectory().getPath();
//
//                        for (double j = 0; j < path.length(); j += robot.resolution) {
//                            Pose2d pose1 = path.get(j - robot.resolution);
//                            int x1 = (int) pose1.getX();
//                            int y1 = (int) pose1.getY();
//
//                            res = robot.resolution / ((robot.resolution / 10) + temp); //* (1-(Math.abs(pose1.getHeading() - prevHeading)));
//                            i += res;
//                            prevHeading = pose1.getHeading();
//
//                            outLine.setToIdentity();
//                            outLine.translate(x1, y1);
//                            outLine.rotate(pose1.getHeading());
//
//                            g2.setColor(color);
//                            g2.setTransform(outLine);
//                            g2.fillRoundRect((int) Math.floor(-rX / 2), (int) Math.floor(-rY / 2), (int) Math.floor(rX), (int) Math.floor(rY), (int) main.scale * 2, (int) main.scale * 2);
//                        }
//                        if (path.length() > 0) {
//                            Pose2d end = path.end();
//                            outLine.setToIdentity();
//                            outLine.translate(end.getX(), end.getY());
//                            outLine.rotate(end.getHeading());
//                            g2.setTransform(outLine);
//                            g2.fillRoundRect((int) Math.floor(-rX / 2), (int) Math.floor(-rY / 2), (int) Math.floor(rX), (int) Math.floor(rY), (int) main.scale * 2, (int) main.scale * 2);
//                        }
//
//                    } else if (segment instanceof TurnSegment || segment instanceof WaitSegment) {
//                        Pose2d startPose = segment.getStartPose();
//                        Pose2d endPose = segment.getEndPose();
//                        g.drawLine((int) startPose.getX(), (int) startPose.getY(), (int) endPose.getX(), (int) endPose.getY());
//                    }
//                }
//            }
            Composite comp = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
            g.drawImage(image, 0, 0, null);
            g.setComposite(comp);
            g2.dispose();
//        }
    }

    private void renderPoints (Graphics g, TrajectorySequence trajectory, Color c1,int ovalscale){

        for (int i = 0; i < trajectory.size(); i++) {
            SequenceSegment segment = trajectory.get(i);
            if (segment != null) {
                if (segment instanceof TrajectorySegment) {
                    Path path = ((TrajectorySegment) segment).getTrajectory().getPath();

                    path.getSegments().forEach(pathSegment -> {

                        Pose2d mid = pathSegment.get(pathSegment.length() / 2);
                        double x = mid.getX()*main.scale;
                        double y = mid.getY()*main.scale;
                        g.setColor(c1);
                        g.fillOval((int) Math.floor(x - (ovalscale * main.scale)), (int) Math.floor(y - (ovalscale * main.scale)), (int) Math.floor(2 * ovalscale * main.scale), (int) Math.floor(2 * ovalscale * main.scale));
                    });

                } else if (segment instanceof TurnSegment || segment instanceof WaitSegment) {
                    Pose2d startPose = segment.getStartPose();
                    Pose2d endPose = segment.getEndPose();
                    g.drawLine((int) startPose.getX(), (int) startPose.getY(), (int) endPose.getX(), (int) endPose.getY());
                }
            }
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
        if (preRenderedSplines == null) renderBackgroundSplines();
//        g.drawImage(preRenderedSplines, 0,0,null);

        main.scale = ((double) this.getWidth() - this.getInsets().left - this.getInsets().right) / 144.0;
        if (oldScale != main.scale)
            main.getManagers().forEach(nodeManager -> {
                main.scale(nodeManager, main.scale, oldScale);
                main.scale(nodeManager.undo, main.scale, oldScale);
                main.scale(nodeManager.redo, main.scale, oldScale);
            });
        g.drawImage(new ImageIcon(Main.class.getResource("/field-2022-kai-dark.png")).getImage(), 0, 0, this.getWidth(), this.getHeight(), null);
        if (preRenderedSplines == null || preRenderedSplines.getWidth() != this.getWidth())
            renderBackgroundSplines();
        g.drawImage(preRenderedSplines, 0, 0, null);
        oldScale = main.scale;
        if (getCurrentManager().size() > 0) {
            Node node = getCurrentManager().get(0).shrink(main.scale);
            trajGen = System.currentTimeMillis();
            TrajectorySequenceBuilder builder = new TrajectorySequenceBuilder(new Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading - 90)), Math.toRadians(-node.splineHeading - 90), new MecanumVelocityConstraint(60.0, 10), new ProfileAccelerationConstraint(60), 60, 60);
            trajectory = generatePath(getCurrentManager(), builder);
            

            if(trajectory != null) {
                renderRobotPath((Graphics2D) g, trajectory, lightPurple, 0.5f);
                renderSplines(g, trajectory, cyan);
                renderPoints(g, trajectory, cyan, 1);
            }
            renderArrows(g, getCurrentManager(), 1, darkPurple, lightPurple, cyan);
        }
        double overal = (System.currentTimeMillis() - time);
        if(debug){
            g.drawString("trajGen (ms): " + (System.currentTimeMillis() - trajGen), 10, 30);
            g.drawString("node count: " + getCurrentManager().size(), 10, 50);
            g.drawString("overal (ms): " + overal, 10, 10);
        }
    }

    private TrajectorySequence generatePath(NodeManager manager, TrajectorySequenceBuilder builder){
        for (int i = 1; i < manager.size(); i++) {
            Node node = manager.get(i).shrink(main.scale);
            try{
                switch (node.getType()){
                    case splineTo:
                        builder.splineTo(new Vector2d(node.x, node.y), Math.toRadians(-node.splineHeading-90));
                        break;
                    case displacementMarker:
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
                }
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
            if(!manager.equals(getCurrentManager())){
                if(manager.size() > 0) {
                    Node node = manager.get(0).shrink(main.scale);
                    TrajectorySequenceBuilder builder = new TrajectorySequenceBuilder(new Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading - 90)), Math.toRadians(-node.splineHeading - 90), new MecanumVelocityConstraint(60.0, 10), new ProfileAccelerationConstraint(60), 60, 60);
                    TrajectorySequence trajectory = generatePath(manager, builder);
                    if(trajectory != null) {
                        renderRobotPath((Graphics2D) g, trajectory, dLightPurple, 0.5f);
                        renderSplines(g, trajectory, cyan);
                        renderPoints(g, trajectory, cyan, 1);
                    }
                    renderArrows(g, manager, 1, dDarkPurple, dLightPurple, dCyan);
                }
            }
        }
        g.dispose();
    }

    private void renderArrows(Graphics g, NodeManager nodeM, int ovalscale, Color color1, Color color2, Color color3) {
        Graphics2D g2d = (Graphics2D) g.create();
        BufferedImage bufferedImage = new BufferedImage(preRenderedSplines.getWidth(), preRenderedSplines.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = bufferedImage.createGraphics();
        for (int i = 0; i < nodeM.size(); i++) {
            Node node = nodeM.get(i);
            tx.setToIdentity();
            tx.translate(node.x, node.y);
            if(nodeM.reversed)
                tx.rotate(Math.toRadians(-node.robotHeading));
            else
                tx.rotate(Math.toRadians(-node.robotHeading +180));
            tx.scale(main.scale, main.scale);

            g2.setTransform(tx);

            g2.setColor(color1);
            g2.fillOval(-ovalscale,-ovalscale, 2*ovalscale, 2*ovalscale);
            switch (node.getType()){
                case splineTo:
                    g2.setColor(color2);
                    break;
                case displacementMarker:
                    g2.setColor(color3);
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
                case lineTo:
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

    public TrajectorySequence getTrajectory(){
        return trajectory;
    }

    public void resetPath(){
        trajectory = null;
    }

    private void mPressed(MouseEvent e) {
        //TODO: clean up this
        this.grabFocus();
        if(!edit){
            Node mouse = new Node(e.getPoint());

            double closest = 99999;
            boolean mid = false;
            int index = -1;
            TrajectorySequence trajectory = getTrajectory();
            double tangentialHeading = 0;
            //find closest mid
            int counter = 0; //i don't like this but its the easiest way
            if(trajectory != null){
                for (int i = 0; i < trajectory.size(); i++) {
                    SequenceSegment segment = trajectory.get(i);
                    if(segment != null){
                        if (segment instanceof TrajectorySegment) {
                            Path path = ((TrajectorySegment) segment).getTrajectory().getPath();
                            List<PathSegment> segments = path.getSegments();
                            for (int j = 0; j < segments.size(); j++) {
                                Pose2d pose = segments.get(j).get(segments.get(j).length() / 2.0);
                                double px = (pose.getX()*main.scale) - mouse.x;
                                double py = (pose.getY()*main.scale) - mouse.y;
                                counter++;
                                double midDist = Math.sqrt(px * px + py * py);
                                if (midDist < (clickSize * main.scale) && midDist < closest) {
                                    closest = midDist;
                                    index = counter;

                                    tangentialHeading = pose.getHeading();
                                    mid = true;
                                }
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < getCurrentManager().size(); i++) {
                Node close = getCurrentManager().get(i);
                double distance = mouse.distance(close);
                //find closest that isn't a mid
                if(distance < (clickSize* main.scale) && distance < closest){
                    closest = distance;
                    index = i;
                    mouse.splineHeading = close.splineHeading;
                    mouse.robotHeading = close.robotHeading;
                    mid = false;
                }
            }

            mouse = snap(mouse, e);
            if(index != -1){

                if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1){
                    getCurrentManager().editIndex = index;
                    edit = true;
                    //if the point clicked was a mid point, gen a new point
                    if(mid) {
                        preEdit = (new Node(index));
                        preEdit.state = 2;
                        getCurrentManager().redo.clear();
                        main.currentN = getCurrentManager().size();
                        //TODO: make it face towards the tangential heading
                        mouse.splineHeading = mouse.headingTo(getCurrentManager().get(index));
                        mouse.robotHeading = mouse.splineHeading;
                        getCurrentManager().add(index,mouse);
                    }
                    else { //editing existing node
                        Node n2 = getCurrentManager().get(index);
                        mouse.x = n2.x;
                        mouse.y = n2.y;
                        mouse.setType(n2.getType());
                        mouse.code = n2.code;
                        Node prev = getCurrentManager().get(index);
                        preEdit = prev.copy(); //storing the existing data for undo
                        preEdit.state = 4;
                        getCurrentManager().redo.clear();
                        main.currentN = index;
                        main.infoPanel.editPanel.update();
                        getCurrentManager().set(index, mouse);
                    }
                }
            } else if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1){
                int size = getCurrentManager().size();
                if(size > 0){
                    Node n1 = getCurrentManager().last();
                    mouse.splineHeading = n1.headingTo(mouse);
                    mouse.robotHeading = mouse.splineHeading;
                }
                preEdit = mouse.copy();
                preEdit.index = getCurrentManager().size();
                preEdit.state = 2;
                getCurrentManager().redo.clear();
                main.currentN = getCurrentManager().size();

                getCurrentManager().add(mouse);
            }
        }
        main.infoPanel.editPanel.update();
        repaint();
    }

    private void mReleased(MouseEvent e){
        if(SwingUtilities.isLeftMouseButton(e)){
            getCurrentManager().undo.add(preEdit);
            edit = false;
            getCurrentManager().editIndex = -1;
        }
        main.infoPanel.editPanel.update();
    }

    private void mDragged(MouseEvent e) {
        Node mouse = new Node(e.getPoint());
        if (SwingUtilities.isRightMouseButton(e)) return;
        if(edit){
            int index = getCurrentManager().editIndex;
            Node mark = getCurrentManager().get(index);
//            if(index > 0) mark.heading = getCurrentManager().get(index-1).headingTo(mouse);
            if(e.isAltDown()) {
                if(e.isShiftDown()) mark.robotHeading = (Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y)));
                else mark.splineHeading = (Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y)));
            }
            else mark.setLocation(snap(mouse, e));
            main.currentN = index;
            main.infoPanel.editPanel.update();
        } else {
            Node mark = getCurrentManager().last();
            mark.index = getCurrentManager().size()-1;
            mark.splineHeading = (Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y)));
            mark.robotHeading = (Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y)));
            main.currentN = getCurrentManager().size()-1;
            getCurrentManager().set(getCurrentManager().size()-1, snap(mark,e));
            main.infoPanel.editPanel.update();
        }
        main.infoPanel.editPanel.update();
        repaint();
    }

    private void keyInput(KeyEvent e){
        if(e.getKeyCode() == KeyEvent.VK_LEFT)
            if(main.currentM > 0){
                main.currentM--;
                main.currentN = -1;
                resetPath();
            }
        if(e.getKeyCode() == KeyEvent.VK_RIGHT){
            System.out.println(main.currentM + " " + managers.size() + " " + getCurrentManager().size());
            if(main.currentM+1 < managers.size()){
                main.currentM++;
                main.currentN = -1;
                resetPath();
            } else if(getCurrentManager().size() > 0){
                System.out.println("yea");
                NodeManager manager = new NodeManager(new ArrayList<>(), managers.size());
                managers.add(manager);
                resetPath();
                main.currentN = -1;
                main.currentM++;
                System.out.println(main.currentM + " " + managers.size() + " " + getCurrentManager().size());
            }
        }
        if(e.getKeyCode() == KeyEvent.VK_R) {
            getCurrentManager().reversed = !getCurrentManager().reversed;
            getCurrentManager().get(0).splineHeading += 180;
        }
        if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z){
            main.undo();
        }

//        if(e.getKeyCode() == KeyEvent.VK_J) preRenderedSplines = null;

        if(e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
            if(main.currentN >= 0){
                Node n = getCurrentManager().get(main.currentN);
                n.index = main.currentN;
                n.state = 1;
                getCurrentManager().undo.add(n);
                getCurrentManager().remove(main.currentN);
                main.currentN--;
            }
        }
        main.infoPanel.editPanel.update();
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