package jarhead;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.util.*;

class Main extends JFrame {

    public static boolean debug = false;

    private ProgramProperties properties;
    public double scale = 1;// = Toolkit.getDefaultToolkit().getScreenSize().height > 1080 ? 8 : 6; //set scale to 6 for 1080p and 8 for 1440p
    private NodeManager currentManager = new NodeManager(new ArrayList<>(), 0);
    private LinkedList<NodeManager> managers = new LinkedList<>();

    public DrawPanel drawPanel;
    public InfoPanel infoPanel;
    public ButtonPanel buttonPanel;
    public ExportPanel exportPanel;


    public int currentM = 0;
    public int currentN = -1;
    public int currentMarker = -1;
    public Main() {
        FlatDarculaLaf.setup();
        loadConfig();
        initComponents();

        reloadConfig();
    }

    public static void main(String[] args) throws IOException {
        if(args.length > 0 && args[0].matches("debug")) debug = true;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    public void reloadConfig() {
        try{
            drawPanel.getPreferredSize();
            scale = ((double)drawPanel.getHeight())/144.0; //set scale to 6 for 1080p and 8 for 1440p
            properties.reload();


            infoPanel.settingsPanel.update();
            drawPanel.update();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        try{
            String os = System.getProperty("os.name").toLowerCase();
            String path;
            if(os.contains("win"))
                path = System.getenv("AppData") + "/RRPathGen/config.properties";
            else if(os.contains("mac") || os.contains("darwin"))
                path = System.getProperty("user.home") + "/Library/Application Support/RRPathGen/config.properties";
            else
                path = System.getProperty("user.home") + "/.RRPathGen/config.properties";
            properties = new ProgramProperties(new File(path));


        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void initComponents() {
        this.setTitle("RRPathGen");
        this.setSize(800,800);
        exportPanel = new ExportPanel(this);
        drawPanel = new DrawPanel(managers,this, properties);
        buttonPanel = new ButtonPanel(managers,this, properties);
        infoPanel = new InfoPanel(this, properties);
        this.getContentPane().setBackground(Color.darkGray.darker());
        GridBagLayout layout = new GridBagLayout();
        this.getContentPane().setLayout(layout);

        { //this is just so i can hide it
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;
            c.gridwidth = 1;
            c.gridheight = 4;
            this.getContentPane().add(exportPanel, c);
            c.fill = GridBagConstraints.VERTICAL;
            c.gridx = 3;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 4;
            c.weightx = 0.1;
            c.weighty = 0.1;
            this.getContentPane().add(infoPanel, c);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1;
            c.gridy = 3;
            c.gridwidth = 2;
            c.gridheight = 1;
            this.getContentPane().add(buttonPanel, c);
            c.fill = GridBagConstraints.NONE;
            c.gridx = 1;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;
            c.gridwidth = 2;
            c.gridheight = 2;
            c.anchor = GridBagConstraints.CENTER;
            this.getContentPane().add(drawPanel, c);
        }

        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        managers.add(currentManager);
        this.setState(JFrame.MAXIMIZED_BOTH);
        this.pack();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setBackground(Color.pink.darker());
        this.update(this.getGraphics());

        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                reloadConfig();
            }
        });
    }

    public void flip() {
        for (int i = 0; i < getCurrentManager().size(); i++) {
            Node node = getCurrentManager().get(i);
            node.y = 144*scale-node.y;
            node.splineHeading = 180-node.splineHeading;
            node.robotHeading = 180-node.robotHeading;
            getCurrentManager().set(i, node);
        }
    }

    public void undo(){
        undo(false);
    }

    public void undo(boolean record){
        if(getCurrentManager().undo.size()<1) return;
        Node node = getCurrentManager().undo.last();
        Node r;
        Node temp;
        switch (node.state){
            case 1: //undo delete
                getCurrentManager().add(node.index, node);
                r = node;
                currentN = node.index;
                if(record)
                    getCurrentManager().redo.add(r);
                break;
            case 2: //undo add new node
                temp = getCurrentManager().get(node.index);
                r = temp.copy();
                r.state = 2;
                currentN = node.index-1;
                if(record)
                    getCurrentManager().redo.add(r);
                getCurrentManager().remove(node.index);
                break;
            case 3: //undo flip
                flip();
                drawPanel.repaint();

                if(record) {
                    Node recordOfFlip = new Node();
                    recordOfFlip.state = 3;
                    getCurrentManager().redo.add(recordOfFlip);
                }
                break;
            case 4:  //undo drag
                if(node.index == -1){
                    node.index = getCurrentManager().size()-1;
                }
                temp = getCurrentManager().get(node.index);
                r = temp.copy();
                r.state = 4;
                getCurrentManager().set(node.index, node);
                if(record)
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
        //TODO: fix undo and redo
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
                flip();
                drawPanel.repaint();

                Node recordOfFlip = new Node();
                recordOfFlip.state = 3;
                getCurrentManager().undo.add(recordOfFlip);
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
                break;
        }

        getCurrentManager().redo.removeLast();
    }

    public void saveConfig() {
        properties.save();
    }

    public double toInches(double in){
        return (1.0/scale * in)-72;
    }

    public void scale(NodeManager manager, double ns, double os){
        for (int j = 0; j < manager.size(); j++) {
            Node n = manager.get(j);
            n.x = (n.x/os)*ns;
            n.y = (n.y/os)*ns;
        }
    }

    public NodeManager getCurrentManager() {
        currentManager = managers.get(currentM);
        return currentManager;
    }

    public LinkedList<NodeManager> getManagers() {
        return managers;
    }
}

