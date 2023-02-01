package jarhead;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class NodeManager {

    public NodeManager undo;
    public NodeManager redo;
    public int editIndex = -1;
    private ArrayList nodes;
    public String name;
    public boolean reversed = false;
    private int id;

    NodeManager(ArrayList<Node> nodes, int id){
        this(nodes, id, "untitled" + id);
    }

    NodeManager(ArrayList<Node> nodes, int id, String name){
        this.nodes = nodes;
        this.id = id;
        this.name = name;
        if(id != -1){
            undo = new NodeManager(new ArrayList<>(), -1);
            redo = new NodeManager(new ArrayList<>(), -1);
        }
    }

    public Node get(int index){
        Node n = (Node) nodes.get(index);
        n.index = index;
        return n;
    }
    public void set(int index, Node n){
        nodes.set(index, n);
    }
    public void add(int index, Node n){
        nodes.add(index, n);
    }
    public void add(Node n){
        nodes.add(n);
    }
    public int size(){
        return nodes.size();
    }
    public Node last(){
        return (Node) nodes.get(nodes.size()-1);
    }
    public void clear(){
        nodes.clear();
    }
    public void remove(Node n){
        nodes.remove(n);
    }
    public void remove(int n){
        nodes.remove(n);
    }
    public void removeLast(){
        remove(size()-1);
    }
    public List<Marker> getMarkers(){
        return (List<Marker>) nodes.stream().filter(node -> node instanceof Marker).collect(Collectors.toList());
    }
    public List<Node> getNodes(){
        return (List<Node>) nodes.stream().filter(node -> !(node instanceof Marker)).collect(Collectors.toList());
    }

}
