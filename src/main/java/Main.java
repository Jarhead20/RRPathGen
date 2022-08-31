import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Scanner;

class Main extends JFrame {
    public ArrayList ps = new ArrayList<Marker>();
    public ArrayList heading = new ArrayList<Double>();
    private JButton button = new JButton("Finish");
    private JButton importButton = new JButton("Import");
    private boolean edit = false;
    private int editIndex = -1;
    final double SCALE = 4;
    final int clickSize = 3;
    public Main() {
        initComponents();
    }

    private void initComponents() {
        jPanel2 = new Panel2();

        jPanel2.add(button);
        jPanel2.add(importButton);
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
                if(importButton.isEnabled()){
                    System.out.println("import");
                }
            }
        });
        importButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(importButton.isEnabled()){
                    try {
                        File file = new File(Main.class.getResource("/import.txt").toURI());
                        Scanner reader = new Scanner(file);
                        while (reader.hasNextLine()) {
                            String data = reader.nextLine();
                            String[] datas = data.split("/()/"); //TODO: import stuff

                        }
                    } catch (URISyntaxException | FileNotFoundException uriSyntaxException) {
                        uriSyntaxException.printStackTrace();
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
                    g.fillOval((int) Math.floor(mid.x-(0.25*SCALE*SCALE)), (int) Math.floor(mid.y-(0.25*SCALE*SCALE)), (int)Math.floor(0.5*SCALE*SCALE), (int)Math.floor(0.5*SCALE*SCALE));

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

