import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

class Main extends JFrame {


    static final double SCALE = 8;
    public static NodeManager nodeM = new NodeManager(new ArrayList<>());
    final double clickSize = 0.3;
    private DrawPanel panel;
    private boolean edit = false;
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

    public static double getSCALE(){
        return SCALE;
    }

    private void initComponents() {
        panel = new DrawPanel(nodeM);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setBackground(new java.awt.Color(255, 255, 255));
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
            Node mouse = new Node(e.getPoint());

//            if(e.getButton()==3) mouse.setType(Marker.Type.MARKER);

            double closest = 99999;
            boolean mid = false;
            int index = -1;

            for (int i = 0; i < nodeM.size(); i++) {

                //find closest mid
                if(i<nodeM.size()-1){

                    Node midMark = nodeM.get(i).mid(nodeM.get(i+1));
                    double midDist = mouse.distance(midMark);
                    if(midDist < (clickSize*SCALE) && midDist < closest){
                        closest = midDist;
                        index = i+1;
                        mid = true;
                    }
                }
                Node close = nodeM.get(i);
                double distance = mouse.distance(close);
                //find closest that isn't a mid
                if(distance < (clickSize*SCALE) && distance < closest){
                    closest = distance;
                    index = i;
                    mouse.heading = close.heading;
                    mid = false;
                }
            }

            if(index != -1){
                if(index >0){
                    Node n1 = nodeM.get(index-1);
                    Node n2 = nodeM.get(index);
                    mouse.heading = n1.headingTo(n2);
                }

                if (SwingUtilities.isRightMouseButton(e) && !mid){
                    panel.getMenu().show(panel,e.getX(),e.getY());
                    nodeM.editIndex = index;
                    panel.repaint();
                    return;
                } else if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1){
                    nodeM.editIndex = index;
                    edit = true;
                    //if the point clicked was a mid point, gen a new point
                    if(mid) nodeM.add(index,mouse);
                    else nodeM.set(index, mouse);
                }
            } else if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1){
                int size = nodeM.size();
                if(size > 0){
                    Node n1 = nodeM.get(size-1);
                    mouse.heading = n1.headingTo(mouse);
                }
                nodeM.add(mouse);
            }
        }
        panel.repaint();
    }

    private void mReleased(MouseEvent e){
        if(SwingUtilities.isLeftMouseButton(e)){
            edit = false;
            nodeM.editIndex = -1;
        }
    }

    private void mDragged(MouseEvent e) {
        Node mouse = new Node(e.getPoint());
        if (SwingUtilities.isRightMouseButton(e)) return;
        if(edit){
            int index = nodeM.editIndex;
            Node mark = nodeM.get(index);
            if(index > 0){
                mark.heading = nodeM.get(index-1).headingTo(mouse);
            }
            if(e.isAltDown()){
                mark.heading = (Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y)));
                nodeM.set(nodeM.editIndex, mark);
            } else{
                nodeM.set(nodeM.editIndex,mark.setLocation(mouse));
            }
        } else {
            Node mark = nodeM.get(nodeM.size()-1);
            mark.heading = (Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y)));
            nodeM.set(nodeM.size()-1, mark);
        }
        panel.repaint();
    }
}

