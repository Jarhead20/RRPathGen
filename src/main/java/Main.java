import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

class Main extends JFrame {
    public ArrayList ps1 = new ArrayList<Double>();
    public ArrayList ps2 = new ArrayList<Double>();
    public ArrayList heading = new ArrayList<Double>();
    private JButton button = new JButton("Finish");
    public Main() {
        initComponents();
    }

    private void initComponents() {
        jPanel2 = new Panel2();

        jPanel2.add(button, BorderLayout.EAST);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (button.isEnabled()) {
                    for (int i = 0; i < ps1.size(); i++) {
                        double x = (0.25*(Integer)ps1.get(i))-72;
                        double y = (0.25*(Integer)ps2.get(i))-72;
                        System.out.println(".splineTo(new Vector2d(" + x + "," + -y + "),Math.toRadians(" + ((double)heading.get(i)+90) + "))");
                    }
                }
            }
        });
        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        jPanel2.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mPressed(e);
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
        int x = e.getX();
        int y = e.getY();

        ps1.add(x);
        ps2.add(y);
        heading.add(Math.atan2(0, 0));
        jPanel2.repaint();
    }

    private void mDragged(MouseEvent evt) {
        double angle = (Math.toDegrees(Math.atan2((int)ps1.get(ps1.size()-1) - evt.getX(), (int)ps2.get(ps2.size()-1) - evt.getY())));
        heading.set(heading.size()-1, angle);
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
            setPreferredSize(new Dimension(700,700));
        }
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(new ImageIcon(Main.class.getResource("/field-2021-adi-dark.png")).getImage(), 0,0,144*4, 144*4, null);
            g.setColor(new Color(255,255,255));
            for (int i = 0; i < ps1.size(); i++) {
                int px1 = (int) ps1.get(i);
                int py1 = (int) ps2.get(i);
                if(i < ps1.size()-1){
                    int px2 = (int) ps1.get(i+1);
                    int py2 = (int) ps2.get(i+1);
                    g.drawLine(px1,py1,px2,py2);
                }

                double angle = 180 - (double) heading.get(i);
                tx.setToIdentity();
                tx.translate(px1, py1);
                tx.rotate(Math.toRadians(angle));
                tx.scale(4,4);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setTransform(tx);
                g2.fill(poly);
                g2.dispose();
            }
        }
    }
}