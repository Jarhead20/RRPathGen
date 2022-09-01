import java.util.ArrayList;

public class NodeManager {

    public int editIndex = -1;
    private final ArrayList nodes;

    NodeManager(ArrayList<Node> nodes){
        this.nodes = nodes;
    }
    public Node get(int index){
        return (Node) nodes.get(index);
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
    public void clear(){
        nodes.clear();
    }
    public void remove(Node n){
        nodes.remove(n);
    }
    public void remove(int n){
        nodes.remove(n);
    }

}
