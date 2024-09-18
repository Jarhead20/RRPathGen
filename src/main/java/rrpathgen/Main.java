package rrpathgen;

import com.formdev.flatlaf.FlatDarculaLaf;
import rrpathgen.data.Node;
import rrpathgen.data.NodeManager;
import rrpathgen.data.ProgramProperties;
import rrpathgen.gui.ButtonPanel;
import rrpathgen.gui.DrawPanel;
import rrpathgen.gui.ExportPanel;
import rrpathgen.gui.infoPanel.InfoPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.util.*;

public class Main extends JFrame {

    public static boolean debug = false;

    private static ProgramProperties properties;
    public static double scale = 1;// = Toolkit.getDefaultToolkit().getScreenSize().height > 1080 ? 8 : 6; //set scale to 6 for 1080p and 8 for 1440p
    private static NodeManager currentManager = new NodeManager(new ArrayList<>(), 0);
    private static LinkedList<NodeManager> managers = new LinkedList<>();

    public static DrawPanel drawPanel;
    public static InfoPanel infoPanel;
    public static ButtonPanel buttonPanel;
    public static ExportPanel exportPanel;

    public static Color cyan = new Color(104, 167, 157);
    public static Color darkPurple = new Color(124, 78, 158);
    public static Color lightPurple = new Color(147, 88, 172);
    public static Color dLightPurple = lightPurple.darker();
    public static Color dCyan = cyan.darker();
    public static Color dDarkPurple = darkPurple.darker();


    public static int currentM = 0;
    public static int currentN = -1;
    public static int currentMarker = -1;
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

    public static void reloadConfig() {
        try{
            drawPanel.getPreferredSize();
            scale = ((double)drawPanel.getHeight())/144.0; //set scale to 6 for 1080p and 8 for 1440p
            properties.reload();


            infoPanel.settingsPanel.update();
            drawPanel.update();
            infoPanel.markerPanel.upateNodeTypes();
            infoPanel.editPanel.updateNodeTypes();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void loadConfig() {
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

    public static void flip() {
        for (int i = 0; i < getCurrentManager().size(); i++) {
            Node node = getCurrentManager().get(i);
            node.y = 144*scale-node.y;
            node.splineHeading = 180-node.splineHeading;
            node.robotHeading = 180-node.robotHeading;
            getCurrentManager().set(i, node);
        }
    }

    public static void undo(boolean record){
        if(getCurrentManager().undo.size()<1) return;
        Node node = getCurrentManager().undo.peek();
        switch (node.state) {
            case ADD:
                node = getCurrentManager().get(node.index);
                node.state = Node.State.ADD;
                getCurrentManager().remove(node.index);
                currentN = node.index-1;
                break;
            case DELETE:
                getCurrentManager().add(node.index, node);
                currentN = node.index;
                break;
            case DRAG:
                if(node.index == -1) {
                    node.index = getCurrentManager().size() - 1;
                }
                Node temp = getCurrentManager().get(node.index);
                temp.state = Node.State.DRAG;
                getCurrentManager().set(node.index, node);
                node = temp;
                break;
            case FLIP:
                flip();
                break;
        }
        if (record) getCurrentManager().redo.add(node);
        getCurrentManager().undo.pop();
    }
    public static void redo(){
        if(getCurrentManager().redo.size()<1) return;
        Node node = getCurrentManager().redo.peek();

        //TODO: fix undo and redo
        switch (node.state){
            case ADD:
                getCurrentManager().add(node.index, node);
                currentN = node.index;
                break;
            case DELETE:
                node = getCurrentManager().get(node.index);
                node.state = Node.State.DELETE;
                getCurrentManager().remove(node.index);
                break;
            case DRAG:
                if(node.index == -1){
                    node.index = getCurrentManager().size()-1;
                }
                Node temp = getCurrentManager().get(node.index);
                temp.state = Node.State.DRAG;
                getCurrentManager().set(node.index, node);
                node = temp;
                break;
            case FLIP:
                flip();
                break;
        }
        getCurrentManager().undo.add(node);
        getCurrentManager().redo.pop();
    }

    public static void saveConfig() {
        properties.save();
    }

    public static double toInches(double in){
        return (1.0/scale * in)-72;
    }

    public static void scale(NodeManager manager, double ns, double os){
        for (int j = 0; j < manager.size(); j++) {
            Node n = manager.get(j);
            n.x = (n.x/os)*ns;
            n.y = (n.y/os)*ns;
        }
    }
    public static void scale(Stack<Node> manager, double ns, double os){
        for (Node n : manager) {
            n.x = (n.x/os)*ns;
            n.y = (n.y/os)*ns;
        }
    }

    public static NodeManager getCurrentManager() {
        currentManager = managers.get(currentM);
        return currentManager;
    }

    public static LinkedList<NodeManager> getManagers() {
        return managers;
    }
}

