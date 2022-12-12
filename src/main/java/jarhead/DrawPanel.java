package jarhead;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.path.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;



public class DrawPanel extends JPanel {

    private LinkedList<NodeManager> managers;

    private Path path;
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
        renderBackgroundSplines();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Insets in = main.getInsets();
        int width = main.getWidth()-(main.infoPanel.getWidth() + in.left + in.right + main.exportPanel.getWidth());
        int height = (main.getHeight()-(main.buttonPanel.getHeight()+in.top + in.bottom));
        int min = Math.min(width, height);
        main.scale = min/144;
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


    DrawPanel(LinkedList<NodeManager> managers, Main main) {
        super();
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
        //TODO: make this faster :(
        if(this.getWidth() != this.getHeight()) System.out.println("w != h");
        BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setColor(color);
        double rX = main.robotLength*main.scale;
        double rY = main.robotWidth*main.scale;
        for (double i = 0; i < path.length(); i+=main.resolution) {
            Pose2d pose1 = path.get(i-main.resolution);
            int x1 = (int) pose1.getX();
            int y1 = (int) pose1.getY();

            outLine.setToIdentity();
            outLine.translate(x1, y1);
            outLine.rotate(pose1.getHeading());

            g2.setColor(color);
            g2.setTransform(outLine);
            g2.fillRoundRect((int) Math.floor(-rX/2),(int) Math.floor(-rY/2),(int) Math.floor(rX),(int) Math.floor(rY),(int) main.scale*2, (int)main.scale*2);
        }
        if(path.length() > 0){
            Pose2d end = path.end();
            outLine.setToIdentity();
            outLine.translate(end.getX(), end.getY());
            outLine.rotate(end.getHeading());
            g2.setTransform(outLine);
            g2.fillRoundRect((int) Math.floor(-rX/2),(int) Math.floor(-rY/2),(int) Math.floor(rX),(int) Math.floor(rY), (int) main.scale*2, (int)main.scale*2);
        }

        Composite comp = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        g.drawImage(image, 0,0,null);
        g.setComposite(comp);
        g2.dispose();
    }

    private void renderPoints(Graphics g, Path path, Color c1, int ovalscale){
        path.getSegments().forEach(pathSegment -> {
            Pose2d mid = pathSegment.get(pathSegment.length()/2);
            g.setColor(c1);
            g.fillOval((int) Math.floor(mid.getX() - (ovalscale*main.scale)), (int) Math.floor(mid.getY() - (ovalscale*main.scale)), (int) Math.floor(2*ovalscale*main.scale), (int) Math.floor(2*ovalscale*main.scale));
        });
    }


    Color cyan = new Color(104, 167, 157);
    Color darkPurple = new Color(124, 78, 158);
    Color lightPurple = new Color(147, 88, 172);
    Color dLightPurple = lightPurple.darker();
    Color dCyan = cyan.darker();
    Color dDarkPurple = darkPurple.darker();

    double oldScale = 0;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(new ImageIcon(Main.class.getResource("/field-2022-kai-dark.png")).getImage(), 0, 0,this.getWidth(), this.getHeight(), null);
        if(preRenderedSplines == null) renderBackgroundSplines();
        g.drawImage(preRenderedSplines, 0,0,null);

        main.scale = ((double)this.getWidth()-this.getInsets().left - this.getInsets().right)/144.0;
        if(oldScale != main.scale)
            main.getManagers().forEach(nodeManager -> {
                main.scale(nodeManager, main.scale, oldScale);
                main.scale(nodeManager.undo, main.scale, oldScale);
                main.scale(nodeManager.redo, main.scale, oldScale);
            });

        oldScale = main.scale;
        if(getCurrentManager().size() > 0) {

            Node node = getCurrentManager().get(0);
            PathBuilder pb = new PathBuilder(new Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading-90)), Math.toRadians(-node.splineHeading-90));
            for (int i = 1; i < getCurrentManager().size(); i++) {
                node = getCurrentManager().get(i);
                try{
                    switch (node.getType()){
                        case splineTo:
                            pb.splineTo(new Vector2d(node.x, node.y), Math.toRadians(-node.splineHeading-90));
                            break;
                        case displacementMarker:
                            pb.splineTo(new Vector2d(node.x, node.y), Math.toRadians(-node.splineHeading-90));
                            break;
                        case splineToSplineHeading:
                            pb.splineToSplineHeading(new Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading-90)), Math.toRadians(-node.splineHeading-90));
                            break;
                        case splineToLinearHeading:
                            pb.splineToLinearHeading(new Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading-90)), Math.toRadians(-node.splineHeading-90));
                            break;
                        case splineToConstantHeading:
                            pb.splineToConstantHeading(new Vector2d(node.x, node.y), Math.toRadians(-node.splineHeading-90));
                            break;
                    }
                } catch (Exception e) {
                    main.undo(false);
                    i--;
                    e.printStackTrace();
                }
            }
            path = pb.build();

            renderRobotPath((Graphics2D) g, path, lightPurple, 0.5f);
            renderSplines(g, path, cyan);
            renderPoints(g, path, cyan, 1);
            renderArrows(g, getCurrentManager(), 1, darkPurple, lightPurple, cyan);
        }
    }

    public void renderBackgroundSplines(){
        if(this.getWidth() > 0)
            preRenderedSplines = new BufferedImage((this.getWidth()), this.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

        else preRenderedSplines = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
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
                        final double prevHeading = Math.toRadians(-prevNode.splineHeading - 90);
                        final double heading = Math.toRadians(-node.splineHeading - 90);
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
                default:
                    throw new IllegalStateException("Unexpected value: " + node.getType());
            }
            g2.fill(poly);
        }
        g2d.drawImage(bufferedImage, 0,0,null);
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

    private void mPressed(MouseEvent e) {
        //TODO: clean up this
        this.grabFocus();
        if(!edit){
            Node mouse = new Node(e.getPoint());

            double closest = 99999;
            boolean mid = false;
            int index = -1;
            Path path = getPath();
            //find closest mid
            if(path != null){
                List<PathSegment> segments = path.getSegments();
                for(int i = 0; i < segments.size(); i++) {
                    Pose2d pose = segments.get(i).get(segments.get(i).length() / 2);
                    double px = pose.getX() - mouse.x;
                    double py = pose.getY() - mouse.y;

                    double midDist = Math.sqrt(px * px + py * py);
                    if (midDist < (clickSize * main.scale) && midDist < closest) {
                        closest = midDist;
                        index = i+1;
                        mid = true;
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
        }
        if(e.getKeyCode() == KeyEvent.VK_R) {
            getCurrentManager().reversed = !getCurrentManager().reversed;
            getCurrentManager().get(0).splineHeading += 180;
        }
        if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z){
            main.undo();
        }

        if(e.getKeyCode() == KeyEvent.VK_DELETE){
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