package jarhead;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class NodeManager {

    public SizedStack<Node> undo;
    // SizedStack isn't needed for redo since it cannot ever be larger than undo
    public Stack<Node> redo;
    public int editIndex = -1;
    private ArrayList nodes;
    public String name;
    private int id;

    NodeManager(ArrayList<Node> nodes, int id){
        this(nodes, id, "untitled" + id);
    }

    NodeManager(ArrayList<Node> nodes, int id, String name){
        this.nodes = nodes;
        this.id = id;
        this.name = name;
        undo = new SizedStack<>(50);
        redo = new Stack<>();
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

    public List<Marker> getMarkers(){
        return (List<Marker>) nodes.stream().filter(node -> node instanceof Marker).collect(Collectors.toList());
    }
    public List<Node> getNodes(){
        return (List<Node>) nodes.stream().filter(node -> !(node instanceof Marker)).collect(Collectors.toList());
    }

}
