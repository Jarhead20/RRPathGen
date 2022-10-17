package jarhead;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.*;

class Main extends JFrame {


    public double scale;// = Toolkit.getDefaultToolkit().getScreenSize().height > 1080 ? 8 : 6; //set scale to 6 for 1080p and 8 for 1440p
    private NodeManager currentManager = new NodeManager(new ArrayList<>(), 0);
    private LinkedList<NodeManager> managers = new LinkedList<>();

    public DrawPanel drawPanel;
    public InfoPanel infoPanel;


    public int currentM = 0;
    public double robotWidth;
    public double robotLength;
    public double resolution;
    public Properties prop;
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

    public void loadConfig() {
        try{
            if(prop == null){
                prop = new Properties();
                InputStream stream = Main.class.getResourceAsStream("/config.properties");
                prop.load(stream);
                stream.close();
            }

            if(prop.getProperty("SCALE").matches("0")) {
                scale = Toolkit.getDefaultToolkit().getScreenSize().height > 1080 ? 8 : 6; //set scale to 6 for 1080p and 8 for 1440p
            }
            else scale = Double.parseDouble(prop.getProperty("SCALE"));
            robotLength = Double.parseDouble(prop.getProperty("ROBOT_LENGTH"));
            robotWidth = Double.parseDouble(prop.getProperty("ROBOT_WIDTH"));
            resolution = Double.parseDouble(prop.getProperty("RESOLUTION"));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void initComponents() {
        managers.add(currentManager);
        drawPanel = new DrawPanel(managers,this);
        infoPanel = new InfoPanel(this);

        this.getContentPane().setBackground(Color.darkGray.darker());
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(infoPanel, BorderLayout.EAST);
        this.getContentPane().add(drawPanel, BorderLayout.WEST);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.pack();
        this.setVisible(true);
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

    public double toInches(double in){
        return (1.0/scale * in)-72;
    }

    public NodeManager getCurrentManager() {
        currentManager = managers.get(currentM);
        return currentManager;
    }

    public LinkedList<NodeManager> getManagers() {
        return managers;
    }
}

