package jarhead;

import com.formdev.flatlaf.FlatDarculaLaf;
import jarhead.InfoPanel.InfoPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.util.*;

public class Main extends JFrame {

    public static boolean debug = false;

    private ProgramProperties properties;
    public double scale = 1;// = Toolkit.getDefaultToolkit().getScreenSize().height > 1080 ? 8 : 6; //set scale to 6 for 1080p and 8 for 1440p
    private static NodeManager currentManager = new NodeManager(new ArrayList<>(), 0);
    private static LinkedList<NodeManager> managers = new LinkedList<>();

    public DrawPanel drawPanel;
    public InfoPanel infoPanel;
    public ButtonPanel buttonPanel;
    public ExportPanel exportPanel;


    public static int currentM = 0;
    public static int currentN = -1;
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

    public static void undo() {
        undo(false);
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
    public void redo(){
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
    public void scale(Stack<Node> manager, double ns, double os){
        for (Node n : manager) {
            n.x = (n.x/os)*ns;
            n.y = (n.y/os)*ns;
        }
    }

    public static NodeManager getCurrentManager() {
        currentManager = managers.get(currentM);
        return currentManager;
    }

    public LinkedList<NodeManager> getManagers() {
        return managers;
    }
}

