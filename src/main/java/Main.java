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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Main extends JFrame {
    public ArrayList ps = new ArrayList<Marker>();
    private static final Pattern p = Pattern.compile("[+-]?(\\d*\\.)?\\d+");
    private JButton finishButton = new JButton("Finish");
    private JButton importButton = new JButton("Import");
    private JButton flipButton = new JButton("Flip");
    private boolean edit = false;
    private int editIndex = -1;
    static final double SCALE = 8;
    final double clickSize = 0.3;
    public Main() {
        initComponents();
    }

    private void initComponents() {
        jPanel2 = new Panel2();

        jPanel2.add(finishButton);
        jPanel2.add(importButton);
        jPanel2.add(flipButton);
        finishButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < ps.size(); i++) {
                    Marker marker = (Marker)ps.get(i);
                    switch (marker.getType()){
                        case SPLINE:
                            System.out.println(".splineTo(new Vector2d(" + marker.x + "," + -marker.y + "),Math.toRadians(" + (marker.heading+90) + "))");
                            break;
                        case MARKER:
                            System.out.println(".splineTo(new Vector2d(" + marker.x + "," + -marker.y + "),Math.toRadians(" + (marker.heading+90) + "))");
                            System.out.println(".addDisplacementMarker(() -> {})");
                            break;
                        default:
                            System.out.println("what");
                            break;
                    }
                }
            }
        });
        flipButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < ps.size(); i++) {
                    Marker marker = (Marker)ps.get(i);
                    marker.y *= -1;
                    ps.set(i, marker);
                }
                jPanel2.repaint();
            }
        });
        importButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    File file = new File(Main.class.getResource("/import.txt").toURI());
                    Scanner reader = new Scanner(file);
                    while (reader.hasNextLine()) {
                        String line = reader.nextLine();
                        if(line.contains(".splineTo(new Vector2d(")){
                            Matcher m = p.matcher(line);
                            Marker marker = new Marker();
                            String[] data = new String[4];
                            for (int i = 0; m.find(); i++) {
                                data[i]=m.group(0);
                            }
                            marker.x = Double.parseDouble(data[1]);
                            marker.y = -Double.parseDouble(data[2]);
                            marker.heading = Double.parseDouble(data[3])-90;

                            ps.add(marker);
                        } else if(line.contains(".addDisplacementMarker(")){
                            ((Marker)ps.get(ps.size()-1)).setType(Marker.Type.MARKER);
                        }
                    }
                } catch (URISyntaxException | FileNotFoundException uriSyntaxException) {
                    uriSyntaxException.printStackTrace();
                }
                jPanel2.repaint();
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
            Marker mouse = new Marker(e.getPoint());
            if(e.getButton()==3) mouse.setType(Marker.Type.MARKER);
            double closest = 99999;
            boolean mid = false;
            int index = -1;

            for (int i = 0; i < ps.size(); i++) {

                //find closest mid
                if(i<ps.size()-1){

                    Marker midMark = ((Marker)ps.get(i)).mid((Marker)ps.get(i+1));
                    double midDist = mouse.distance(midMark);
                    if(midDist < (clickSize*SCALE) && midDist < closest){
                        closest = midDist;
                        index = i+1;
                        mid = true;
                    }
                }

                double distance = mouse.distance((Marker)ps.get(i));
                //find closest that isn't a mid
                if(distance < (clickSize*SCALE) && distance < closest){
                    closest = distance;
                    index = i;
                    mid = false;
                }
            }
            mouse.heading = 0.0;
            if(index != -1){
                editIndex = index;
                edit = true; //enable edit mode
                //if the point clicked was a mid point, gen a new point
                if(mid){
                    ps.add(index,mouse);
                } else {
                    ps.set(index, mouse);
                }
            } else {
                ps.add(mouse);
            }
        }
        jPanel2.repaint();
    }

    private void mReleased(MouseEvent e){
        edit = false;
        editIndex = -1;
    }

    private void mDragged(MouseEvent e) {
        Marker mouse = new Marker(e.getPoint());
        if(edit){
            Marker mark = (Marker) ps.get(editIndex);

            if(e.isAltDown()){
                mark.heading = (Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y)));
                ps.set(editIndex, mark);
            } else{
                ps.set(editIndex,mark.setLocation(mouse));
            }
        } else {
            Marker mark = (Marker) ps.get(ps.size()-1);
            mark.heading = (Math.toDegrees(Math.atan2(mark.x - mouse.x, mark.y - mouse.y)));
            ps.set(ps.size()-1, mark);
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
                    g.drawLine((int) (SCALE*(p1.x+72)),(int) (SCALE*(p1.y+72)),(int) (SCALE*(p2.x+72)),(int) (SCALE*(p2.y+72)));
                    Marker mid = p1.mid(p2);
                    g.setColor(new Color(0,255,0));
                    g.fillOval((int) Math.floor((SCALE*(mid.x+72))-(0.25*SCALE*SCALE)), (int) Math.floor((SCALE*(mid.y+72))-(0.25*SCALE*SCALE)), (int)Math.floor(0.5*SCALE*SCALE), (int)Math.floor(0.5*SCALE*SCALE));

                }

                double angle = 180 - ((Marker) ps.get(i)).heading;
                tx.setToIdentity();
                tx.translate((SCALE*(p1.x+72)), (SCALE*(p1.y+72)));
                tx.rotate(Math.toRadians(angle));
                tx.scale(SCALE,SCALE);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setTransform(tx);

                g2.setColor(new Color(0,0,255));
                g2.fillOval((int) Math.floor(-0.25*SCALE),(int) Math.floor(-0.25*SCALE),(int) Math.floor(0.5*SCALE),(int) Math.floor(0.5*SCALE));

                switch (p1.getType()){
                    case MARKER:
                        g2.setColor(Color.ORANGE);
                        break;
                    case SPLINE:
                        g2.setColor(Color.white);
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
    public static double getSCALE(){
        return SCALE;
    }
}

