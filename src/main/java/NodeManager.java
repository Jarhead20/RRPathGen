import java.util.ArrayList;

public class NodeManager {

    public int editIndex = -1;
    private final ArrayList nodes;

    NodeManager(ArrayList<Node> nodes){
        this.nodes = nodes;
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


}
