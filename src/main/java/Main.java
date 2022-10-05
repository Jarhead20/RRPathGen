import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.path.Path;
import com.acmerobotics.roadrunner.path.PathSegment;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

class Main extends JFrame {


    public double scale = Toolkit.getDefaultToolkit().getScreenSize().height > 1080 ? 8 : 6; //set scale to 6 for 1080p and 8 for 1440p
    public NodeManager nodeM = new NodeManager(new ArrayList<>());
    public NodeManager undo = new NodeManager(new ArrayList<>());
    public NodeManager redo = new NodeManager(new ArrayList<>());

    final double clickSize = 2;
    final int snapSize = 20;
    private DrawPanel panel;
    private boolean edit = false;
    private Node preEdit;
    public Main() {
        initComponents();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }


    private void initComponents() {
        panel = new DrawPanel(nodeM, undo, redo,this);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setBackground(new java.awt.Color(0, 0, 0));
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        //event handler stuff
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mPressed(e);
            }
            public void mouseReleased(MouseEvent e) {
                mReleased(e);
            }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                mDragged(e);
            }
        });
        this.setContentPane(panel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pack();
    }

    private void mPressed(MouseEvent e) {
    //TODO: clean up this
        if(!edit){
            Node mouse = new Node(e.getPoint(), scale);

            double closest = 99999;
            boolean mid = false;
            int index = -1;
            Path path = panel.getPath();
            //find closest mid
            if(path != null){
                List<PathSegment> segments = path.getSegments();
                for(int i = 0; i < segments.size(); i++) {
                    Pose2d pose = segments.get(i).get(segments.get(i).length() / 2);
                    double px = pose.getX() - mouse.x;
                    double py = pose.getY() - mouse.y;

                    double midDist = Math.sqrt(px * px + py * py);
                    if (midDist < (clickSize * scale) && midDist < closest) {
                        closest = midDist;
                        index = i+1;
                        mid = true;
                    }
                }
            }

            for (int i = 0; i < nodeM.size(); i++) {
                Node close = nodeM.get(i);
                double distance = mouse.distance(close);
                //find closest that isn't a mid
                if(distance < (clickSize* scale) && distance < closest){
                    closest = distance;
                    index = i;
                    mouse.heading = close.heading;
                    mid = false;
                }
            }

            mouse = snap(mouse, e);

            if(index != -1){
                if(index >0){
                    Node n1 = nodeM.get(index-1);
                    Node n2 = nodeM.get(index);
                    mouse.heading = n1.headingTo(n2);
                    mouse.setType(n2.getType());
                }

                if (SwingUtilities.isRightMouseButton(e) && !mid){ //opens right click context menu
                    panel.getMenu().show(panel,e.getX(),e.getY());
                    nodeM.editIndex = index;
                    panel.repaint();
                    return;
                } else if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1){
                    nodeM.editIndex = index;
                    edit = true;
                    //if the point clicked was a mid point, gen a new point
                    if(mid) {
                        preEdit = (new Node(index));
                        preEdit.state = 2;
                        redo.clear();

                        nodeM.add(index,mouse);
                    }
                    else { //editing existing node
                        Node prev = nodeM.get(index);
                        preEdit = new Node(prev.x,prev.y, prev.heading, index); //storing the existing data for undo
                        preEdit.state = 4;
                        redo.clear();
                        nodeM.set(index, mouse);
                    }
                }
            } else if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1){
                int size = nodeM.size();
                if(size > 0){
                    Node n1 = nodeM.last();
                    mouse.heading = n1.headingTo(mouse);
                }
                preEdit = (new Node(mouse.x, mouse.y, mouse.heading, nodeM.size()));
                preEdit.state = 2;
                redo.clear();
                nodeM.add(mouse);
            }
        }
        panel.repaint();
    }

    private void mReleased(MouseEvent e){
        if(SwingUtilities.isLeftMouseButton(e)){
            undo.add(preEdit);
            edit = false;
            nodeM.editIndex = -1;
        }
    }

    private void mDragged(MouseEvent e) {
        Node mouse = new Node(e.getPoint(), scale);
        if (SwingUtilities.isRightMouseButton(e)) return;
        if(edit){
            int index = nodeM.editIndex;
            Node mark = nodeM.get(index);
            if(index > 0) mark.heading = nodeM.get(index-1).headingTo(mouse);
            if(e.isAltDown()) mark.heading = (Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y)));
            else mark.setLocation(snap(mouse, e));
        } else {
            Node mark = nodeM.last();
            mark.index = nodeM.size()-1;
            mark.heading = (Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y)));

            nodeM.set(nodeM.size()-1, snap(mark,e));
        }
        panel.repaint();
    }
    public void undo(){
        if(undo.size()<1) return;
        Node node = undo.last();
        Node r;
        Node temp;
        switch (node.state){
            case 1: //undo delete
                nodeM.add(node.index, node);
                r = node;
                redo.add(r);
                break;
            case 2: //undo add new node
                temp = nodeM.get(node.index);
                r = new Node(temp.x, temp.y, temp.heading, temp.index);
                r.state = 2;
                redo.add(r);
                nodeM.remove(node.index);
                break;
            case 3: //undo flip
                for (int i = 0; i < nodeM.size(); i++) {
                    Node n = nodeM.get(i);
                    n.y *= -1;
                    nodeM.set(i, n);
                }
                r = node;
                redo.add(r);
                break;
            case 4:  //undo drag
                if(node.index == -1){
                    node.index = nodeM.size()-1;
                }
                temp = nodeM.get(node.index);
                r = new Node(temp.x, temp.y, temp.heading, temp.index);
                r.state = 4;
                nodeM.set(node.index, node);
                redo.add(r);
                break;
        }


        undo.removeLast();
    }
    public void redo(){
        if(redo.size()<1) return;

        Node node = redo.last();
        Node u;
        Node temp;
        switch (node.state){
            case 1: //redo delete
                temp = nodeM.get(node.index);
                u = new Node(temp.x, temp.y, temp.heading, temp.index);
                u.state = 1;
                undo.add(u);
                nodeM.remove(node.index);
                break;
            case 2: //redo add new node
                nodeM.add(node.index, node);
                u = node;
                undo.add(u);
                break;
            case 3: //redo flip
                for (int i = 0; i < nodeM.size(); i++) {
                    Node n = nodeM.get(i);
                    n.y *= -1;
                    nodeM.set(i, n);
                }
                u = node;
                undo.add(u);
                break;
            case 4:  //redo drag
                if(node.index == -1){
                    node.index = nodeM.size()-1;
                }
                temp = nodeM.get(node.index);
                u = new Node(temp.x, temp.y, temp.heading, temp.index);
                u.state = 4;
                nodeM.set(node.index, node);
                undo.add(u);
        }

        redo.removeLast();
    }

    private Node snap(Node node, MouseEvent e){
        if(e.isControlDown()) {
            node.x = scale*(Math.round(node.x/scale));
            node.y = scale*(Math.round(node.y/scale));
        }
        return node;
    }

    public double toInches(double in){
        return (1.0/scale * in)-72;
    }

}

