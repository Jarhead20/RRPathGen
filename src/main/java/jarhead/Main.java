package jarhead;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.util.*;

class Main extends JFrame {


    public double scale = 1;// = Toolkit.getDefaultToolkit().getScreenSize().height > 1080 ? 8 : 6; //set scale to 6 for 1080p and 8 for 1440p
    private NodeManager currentManager = new NodeManager(new ArrayList<>(), 0);
    private LinkedList<NodeManager> managers = new LinkedList<>();

    public DrawPanel drawPanel;
    public InfoPanel infoPanel;
    public ButtonPanel buttonPanel;
    public String importPath;
    private String configPath;

    public int currentM = 0;
    public int currentN = -1;
    public double robotWidth;
    public double robotLength;
    public double resolution;
    public Properties prop;
    public Main() {
        FlatDarculaLaf.setup();
        initComponents();
        loadConfig();
        reloadConfig();
    }

    public static void main(String[] args) throws IOException {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    public void reloadConfig() {
        try{
            double oldScale = scale;
            scale = ((double)drawPanel.getHeight())/144.0; //set scale to 6 for 1080p and 8 for 1440p
            robotLength = Double.parseDouble(prop.getProperty("ROBOT_LENGTH"));
            robotWidth = Double.parseDouble(prop.getProperty("ROBOT_WIDTH"));
            resolution = Double.parseDouble(prop.getProperty("RESOLUTION"));
            importPath = prop.getProperty("IMPORT/EXPORT");

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
            configPath = path;
            File file = new File(path);
            if(!file.exists()) {
                file.getParentFile().mkdir();
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write(
                        "ROBOT_WIDTH=18\n" +
                        "ROBOT_LENGTH=18\n" +
                        "RESOLUTION=5\n" +
                        "IMPORT/EXPORT=");
                writer.close();
            }
            prop = new Properties();
            prop.load(new FileInputStream(file));

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void initComponents() {
        this.setTitle("RRPathGen");
        drawPanel = new DrawPanel(managers,this);
        buttonPanel = new ButtonPanel(managers,this);
        infoPanel = new InfoPanel(this);
        this.getContentPane().setBackground(Color.darkGray.darker());
        GridBagLayout layout = new GridBagLayout();
        this.getContentPane().setLayout(layout);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        c.gridx = 3;
        c.gridy = 0;
        this.getContentPane().add(infoPanel, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 3;
        this.getContentPane().add(buttonPanel, c);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        this.getContentPane().add(drawPanel, c);

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
                for (int i = 0; i < getCurrentManager().size(); i++) {
                    Node n = getCurrentManager().get(i);
                    n.y *= -1;
                    getCurrentManager().set(i, n);
                }
                currentN = -1;
                r = node;
                if(record)
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

    public void saveConfig() {
        try {
            FileOutputStream stream = new FileOutputStream(configPath);
            prop.store(stream, "V1.2");
            stream.close();
        }  catch (IOException e) {
            e.printStackTrace();
        }

    }

    public double toInches(double in){
        return (1.0/scale * in)-72;
    }

    public void scale(NodeManager manager, double ns, double os){
        for (int j = 0; j < manager.size(); j++) {
            Node n = manager.get(j);
            System.out.println(n.x + " 1: " + ns + " " + os);
            n.x = (n.x/os)*ns;
            n.y = (n.y/os)*ns;
            System.out.println(n.x);
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

