package jarhead;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.path.Path;
import com.acmerobotics.roadrunner.path.PathSegment;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.List;

class Main extends JFrame {


    public double scale = Toolkit.getDefaultToolkit().getScreenSize().height > 1080 ? 8 : 6; //set scale to 6 for 1080p and 8 for 1440p
    private NodeManager currentManager = new NodeManager(new ArrayList<>(), 0);
    private LinkedList<NodeManager> managers = new LinkedList<>();
    final double clickSize = 2;
    private DrawPanel panel;
    private boolean edit = false;
    private Node preEdit;
    public int currentM = 0;
    public double robotWidth;
    public double robotLength;
    public double resolution;
    public Main() {
        loadConfig();
        initComponents();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    private void loadConfig() {
        try{
            System.out.println(Main.class.getResource("/config.properties").getPath());
            InputStream stream = Main.class.getResourceAsStream("/config.properties");
            Properties prop = new Properties();
            prop.load(stream);
            stream.close();
            if(prop.getProperty("SCALE").matches("0")) scale = Toolkit.getDefaultToolkit().getScreenSize().height > 1080 ? 8 : 6; //set scale to 6 for 1080p and 8 for 1440p
            else scale = Double.parseDouble(prop.getProperty("SCALE"));
            robotLength = Double.parseDouble(prop.getProperty("ROBOT_LENGTH"));
            robotWidth = Double.parseDouble(prop.getProperty("ROBOT_WIDTH"));
            resolution = Double.parseDouble(prop.getProperty("RESOLUTION"));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initComponents() {
        managers.add(currentManager);
        panel = new DrawPanel(managers,this);

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
        panel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {keyInput(e);}
            @Override
            public void keyPressed(KeyEvent e) { }
        });
        panel.setFocusable(true);
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

            for (int i = 0; i < getCurrentManager().size(); i++) {
                Node close = getCurrentManager().get(i);
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
                    Node n1 = getCurrentManager().get(index-1);
                    Node n2 = getCurrentManager().get(index);
                    mouse.heading = n1.headingTo(n2);
                    mouse.setType(n2.getType());
                }

                if (SwingUtilities.isRightMouseButton(e) && !mid){ //opens right click context menu
                    panel.getMenu().show(panel,e.getX(),e.getY());
                    getCurrentManager().editIndex = index;
                    panel.repaint();
                    return;
                } else if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1){
                    getCurrentManager().editIndex = index;
                    edit = true;
                    //if the point clicked was a mid point, gen a new point
                    if(mid) {
                        preEdit = (new Node(index));
                        preEdit.state = 2;
                        getCurrentManager().redo.clear();

                        getCurrentManager().add(index,mouse);
                    }
                    else { //editing existing node
                        Node prev = getCurrentManager().get(index);
                        preEdit = new Node(prev.x,prev.y, prev.heading, index); //storing the existing data for undo
                        preEdit.state = 4;
                        getCurrentManager().redo.clear();
                        getCurrentManager().set(index, mouse);
                    }
                }
            } else if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1){
                int size = getCurrentManager().size();
                if(size > 0){
                    Node n1 = getCurrentManager().last();
                    mouse.heading = n1.headingTo(mouse);
                }
                preEdit = (new Node(mouse.x, mouse.y, mouse.heading, getCurrentManager().size()));
                preEdit.state = 2;
                getCurrentManager().redo.clear();
                getCurrentManager().add(mouse);
            }
        }
        panel.repaint();
    }

    private void mReleased(MouseEvent e){
        if(SwingUtilities.isLeftMouseButton(e)){
            getCurrentManager().undo.add(preEdit);
            edit = false;
            getCurrentManager().editIndex = -1;
        }
    }

    private void mDragged(MouseEvent e) {
        Node mouse = new Node(e.getPoint(), scale);
        if (SwingUtilities.isRightMouseButton(e)) return;
        if(edit){
            int index = getCurrentManager().editIndex;
            Node mark = getCurrentManager().get(index);
            if(index > 0) mark.heading = getCurrentManager().get(index-1).headingTo(mouse);
            if(e.isAltDown()) mark.heading = (Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y)));
            else mark.setLocation(snap(mouse, e));
        } else {
            Node mark = getCurrentManager().last();
            mark.index = getCurrentManager().size()-1;
            mark.heading = (Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y)));

            getCurrentManager().set(getCurrentManager().size()-1, snap(mark,e));
        }
        panel.repaint();
    }
    public void undo(){
        if(getCurrentManager().undo.size()<1) return;
        Node node = getCurrentManager().undo.last();
        Node r;
        Node temp;
        switch (node.state){
            case 1: //undo delete
                getCurrentManager().add(node.index, node);
                r = node;
                getCurrentManager().redo.add(r);
                break;
            case 2: //undo add new node
                temp = getCurrentManager().get(node.index);
                r = temp.copy();
                r.state = 2;
                getCurrentManager().redo.add(r);
                getCurrentManager().remove(node.index);
                break;
            case 3: //undo flip
                for (int i = 0; i < getCurrentManager().size(); i++) {
                    Node n = getCurrentManager().get(i);
                    n.y *= -1;
                    getCurrentManager().set(i, n);
                }
                r = node;
                getCurrentManager().redo.add(r);
                break;
            case 4:  //undo drag
                if(node.index == -1){
                    node.index = getCurrentManager().size()-1;
                }
                temp = getCurrentManager().get(node.index);
                r = temp.copy();
                r.state = 4;
                getCurrentManager().set(node.index, node);
                getCurrentManager().redo.add(r);
                break;
        }


        getCurrentManager().undo.removeLast();
    }
    public void redo(){
        if(getCurrentManager().redo.size()<1) return;

        Node node = getCurrentManager().redo.last();
        Node u;
        Node temp;
        switch (node.state){
            case 1: //redo delete
                temp = getCurrentManager().get(node.index);
                u = temp.copy();
                u.state = 1;
                getCurrentManager().undo.add(u);
                getCurrentManager().remove(node.index);
                break;
            case 2: //redo add new node
                getCurrentManager().add(node.index, node);
                u = node;
                getCurrentManager().undo.add(u);
                break;
            case 3: //redo flip
                for (int i = 0; i < getCurrentManager().size(); i++) {
                    Node n = getCurrentManager().get(i);
                    n.y *= -1;
                    getCurrentManager().set(i, n);
                }
                u = node;
                getCurrentManager().undo.add(u);
                break;
            case 4:  //redo drag
                if(node.index == -1){
                    node.index = getCurrentManager().size()-1;
                }
                temp = getCurrentManager().get(node.index);
                u = temp.copy();
                u.state = 4;
                getCurrentManager().set(node.index, node);
                getCurrentManager().undo.add(u);
        }

        getCurrentManager().redo.removeLast();
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

    private void keyInput(KeyEvent e){
        if(e.getKeyCode() == KeyEvent.VK_LEFT)
            if(currentM > 0){
                currentM--;
                panel.resetPath();
            }

        if(e.getKeyCode() == KeyEvent.VK_RIGHT){
            if(currentM+1 < managers.size()){
                currentM++;
                panel.resetPath();
            } else if(getCurrentManager().size() > 0){
                NodeManager manager = new NodeManager(new ArrayList<>(), managers.size());
                managers.add(manager);
                panel.resetPath();
                currentM++;
            }
        }
        if(e.getKeyCode() == KeyEvent.VK_R) {
            getCurrentManager().reversed = !getCurrentManager().reversed;
            getCurrentManager().get(0).heading += 180;
        }


        panel.renderBackgroundSplines();
        panel.repaint();
    }

    public NodeManager getCurrentManager() {
        currentManager = managers.get(currentM);
        return currentManager;
    }
}

