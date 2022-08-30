import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

class Main extends JFrame {
    public ArrayList ps = new ArrayList<Marker>();
    public ArrayList heading = new ArrayList<Double>();
    private JButton button = new JButton("Finish");
    private boolean edit = false;
    private int editIndex = -1;
    final double SCALE = 8;
    final int clickSize = 3;
    public Main() {
        initComponents();
    }

    private void initComponents() {
        jPanel2 = new Panel2();

        jPanel2.add(button);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (button.isEnabled()) {
                    for (int i = 0; i < ps.size(); i++) {
                        Marker marker = (Marker)ps.get(i);
                        double x = (1.0/SCALE*marker.x)-72;
                        double y = (1.0/SCALE*marker.y)-72;
                        switch (marker.getType()){
                            case SPLINE:
                                System.out.println(".splineTo(new Vector2d(" + x + "," + -y + "),Math.toRadians(" + ((double)heading.get(i)+90) + "))");
                                break;
                            case MARKER:
                                System.out.println("marker");
                                break;
                            default:
                                System.out.println("what");
                                break;
                        }
                    }
                }
            }
        });
        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        //event handler stuff
        jPanel2.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mPressed(e);
            }
            public void mouseReleased(MouseEvent e) {
                mReleased(e);
            }
        });
        jPanel2.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                mDragged(e);
            }
        });
        this.setContentPane(jPanel2);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pack();
    }

    private void mPressed(MouseEvent e) {
    //TODO: clean up this
        if(!edit){
            Marker mp = new Marker(e.getPoint());
            if(e.getButton()==3) mp.setType(Marker.Type.MARKER);
            double closest = 99999;
            boolean mid = false;
            int index = -1;

            for (int i = 0; i < ps.size(); i++) {

                //find closest mid
                if(i<ps.size()-1){

                    Marker midMark = ((Marker)ps.get(i)).mid((Marker)ps.get(i+1));
                    double midDist = mp.distance(midMark);
                    if(midDist < (clickSize*SCALE) && midDist < closest){
                        closest = midDist;
                        index = i+1;
                        mid = true;
                    }
                }

                double distance = mp.distance((Marker)ps.get(i));
                //find closest that isn't a mid
                if(distance < (clickSize*SCALE) && distance < closest){
                    closest = distance;
                    index = i;
                    mid = false;
                }
            }

            if(index != -1){
                editIndex = index;
                edit = true; //enable edit mode
                //if the point clicked was a mid point, gen a new point
                if(mid){
                    ps.add(index,mp);
                    heading.add(index,0.0);
                } else {
                    ps.set(index, mp);
                }
            } else {
                ps.add(mp);
                heading.add(Math.atan2(0, 0));
            }
        }
        jPanel2.repaint();
    }

    private void mReleased(MouseEvent e){
        edit = false;
        editIndex = -1;
    }

    private void mDragged(MouseEvent e) {
        if(edit){
            if(e.isAltDown()){
                Marker p = (Marker) ps.get(editIndex);
                double angle = (Math.toDegrees(Math.atan2(p.x - e.getX(), p.y - e.getY())));
                heading.set(editIndex, angle);
            } else{
                Marker mark = (Marker) ps.get(editIndex);
                ps.set(editIndex,mark.setLocation(e.getPoint()));
            }
        } else {
            Marker p = (Marker) ps.get(ps.size()-1);
            double angle = (Math.toDegrees(Math.atan2(p.x - e.getX(), p.y - e.getY())));
            heading.set(heading.size()-1, angle);
        }
        jPanel2.repaint();
    }

    public static void main(String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }
    private JPanel jPanel2;
    public class Panel2 extends JPanel {
        AffineTransform tx = new AffineTransform();
        int xPoly[] = {-5,0,5};
        int yPoly[] = {-5,0,-5};
        Polygon poly = new Polygon(xPoly, yPoly, xPoly.length);
        Panel2() {
            setPreferredSize(new Dimension((int) Math.floor(144*SCALE+100),(int) Math.floor(144*SCALE+100)));
        }
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(new ImageIcon(Main.class.getResource("/field-2021-adi-dark.png")).getImage(), 0,0,(int) Math.floor(144*SCALE), (int) Math.floor(144*SCALE), null);

            for (int i = 0; i < ps.size(); i++) {
                Marker p1 = (Marker) ps.get(i);
                if(i < ps.size()-1){
                    Marker p2 = (Marker) ps.get(i+1);
                    g.setColor(new Color(255,255,255));
                    g.drawLine(p1.x,p1.y,p2.x,p2.y);
                    Marker mid = p1.mid(p2);
                    g.setColor(new Color(0,255,0));
                    g.fillOval((int) Math.floor(mid.x-(SCALE*2)), (int) Math.floor(mid.y-(SCALE*2)), (int)Math.floor(0.5*SCALE*SCALE), (int)Math.floor(0.5*SCALE*SCALE));

                }

                double angle = 180 - (double) heading.get(i);
                tx.setToIdentity();
                tx.translate(p1.x, p1.y);
                tx.rotate(Math.toRadians(angle));
                tx.scale(SCALE,SCALE);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setTransform(tx);

                g2.setColor(new Color(0,0,255));
                g2.fillOval((int) Math.floor(-0.25*SCALE),(int) Math.floor(-0.25*SCALE),(int) Math.floor(0.5*SCALE),(int) Math.floor(0.5*SCALE));

                switch (p1.getType()){
                    case MARKER:
                        g2.setColor(new Color(255,0,255));
                        break;
                    case SPLINE:
                        g2.setColor(new Color(255,255,255));
                        break;
                    default:
                        g2.setColor(new Color(255,255,255));
                        break;
                }


                g2.fill(poly);

                g2.dispose();
            }
        }
    }
}

/*
.splineTo(new Vector2d(-23.5,-63.25),Math.toRadians(-2.6630007660671566))
.splineTo(new Vector2d(-12.0,-62.5),Math.toRadians(43.83086067209258))
.splineTo(new Vector2d(-2.5,-50.0),Math.toRadians(20.282559088916614))
.splineTo(new Vector2d(13.75,-48.0),Math.toRadians(-41.30861401354872))
.splineTo(new Vector2d(21.25,-58.75),Math.toRadians(-60.64224645720873))
.splineTo(new Vector2d(28.5,-68.5),Math.toRadians(3.6522227803063316))
.splineTo(new Vector2d(42.75,-67.25),Math.toRadians(61.144338780283476))
.splineTo(new Vector2d(49.5,-50.5),Math.toRadians(90.89517371021107))
.splineTo(new Vector2d(48.5,-29.0),Math.toRadians(86.9335144988741))
.splineTo(new Vector2d(49.5,-13.75),Math.toRadians(12.225122675735747))
.splineTo(new Vector2d(66.0,-10.5),Math.toRadians(66.44773632710535))
.splineTo(new Vector2d(68.25,5.75),Math.toRadians(98.61564818416412))
.splineTo(new Vector2d(64.25,25.75),Math.toRadians(159.27444113443946))
.splineTo(new Vector2d(52.25,28.5),Math.toRadians(239.93141717813754))
.splineTo(new Vector2d(46.75,17.5),Math.toRadians(-86.78451600825181))
.splineTo(new Vector2d(48.0,-8.5),Math.toRadians(-87.79740183823421))
.splineTo(new Vector2d(48.0,26.75),Math.toRadians(86.9335144988741))
.splineTo(new Vector2d(47.0,45.0),Math.toRadians(104.93141717813755))
.splineTo(new Vector2d(40.75,63.75),Math.toRadians(170.53767779197437))
.splineTo(new Vector2d(30.75,62.5),Math.toRadians(178.53119928561418))
.splineTo(new Vector2d(15.75,60.75),Math.toRadians(176.24827092947402))
.splineTo(new Vector2d(-0.5,61.75),Math.toRadians(164.74488129694222))
.splineTo(new Vector2d(-11.75,63.5),Math.toRadians(192.52880770915152))
.splineTo(new Vector2d(-20.75,60.75),Math.toRadians(200.5560452195835))
.splineTo(new Vector2d(-29.25,55.0),Math.toRadians(242.52556837372288))
.splineTo(new Vector2d(-33.0,46.75),Math.toRadians(251.565051177078))
.splineTo(new Vector2d(-37.75,33.5),Math.toRadians(208.6104596659652))
.splineTo(new Vector2d(-47.0,32.75),Math.toRadians(90.0))
.splineTo(new Vector2d(-47.75,29.75),Math.toRadians(217.56859202882748))
.splineTo(new Vector2d(-52.25,24.0),Math.toRadians(-13.392497753751115))
.splineTo(new Vector2d(-37.25,19.75),Math.toRadians(9.462322208025611))
.splineTo(new Vector2d(-27.0,21.5),Math.toRadians(45.0))
.splineTo(new Vector2d(-24.25,28.25),Math.toRadians(147.99461679191648))
.splineTo(new Vector2d(-27.0,32.75),Math.toRadians(177.13759477388825))
.splineTo(new Vector2d(-32.0,33.0),Math.toRadians(184.08561677997488))
.splineTo(new Vector2d(-51.0,29.5),Math.toRadians(206.56505117707798))
.splineTo(new Vector2d(-50.25,20.75),Math.toRadians(-69.44395478041653))
.splineTo(new Vector2d(-44.5,18.25),Math.toRadians(-19.179008025810717))
.splineTo(new Vector2d(-37.5,16.0),Math.toRadians(261.86989764584405))
.splineTo(new Vector2d(-38.75,6.25),Math.toRadians(268.3153156821037))
.splineTo(new Vector2d(-39.0,-4.5),Math.toRadians(270.0))
.splineTo(new Vector2d(-39.5,-19.75),Math.toRadians(270.0))
.splineTo(new Vector2d(-38.75,-35.75),Math.toRadians(-86.98721249581666))
.splineTo(new Vector2d(-37.5,-51.0),Math.toRadians(-65.3764352138364))
.splineTo(new Vector2d(-33.5,-59.75),Math.toRadians(-28.61045966596521))
.splineTo(new Vector2d(-27.75,-63.0),Math.toRadians(-14.036243467926482))
*/