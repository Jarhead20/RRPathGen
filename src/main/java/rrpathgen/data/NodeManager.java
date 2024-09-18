package rrpathgen.data;

import rrpathgen.util.SizedStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class NodeManager {

    public SizedStack<Node> undo;
    // SizedStack isn't needed for redo since it cannot ever be larger than undo
    public Stack<Node> redo;
    public int editIndex = -1;
    private final ArrayList<Node> nodes;
    public String name;

    public NodeManager(ArrayList<Node> nodes, int id){
        this(nodes, "trajectory" + id);
    }

    NodeManager(ArrayList<Node> nodes, String name){
        this.nodes = nodes;
        this.name = name;
        undo = new SizedStack<>(50);
        redo = new Stack<>();
    }

    public Node get(int index){
        Node n = nodes.get(index);
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
        return nodes.get(nodes.size()-1);
    }
    public void clear(){
        nodes.clear();
    }

    public void remove(int n){
        nodes.remove(n);
    }

    public List<Marker> getMarkers(){
        return nodes.stream()
                .filter(node -> node instanceof Marker)
                .map(node -> (Marker) node)
                .collect(Collectors.toList());
    }
    public List<Node> getNodes(){
        return nodes.stream()
                .filter(node -> !(node instanceof Marker))
                .collect(Collectors.toList());
    }

}
