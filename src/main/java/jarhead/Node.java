package jarhead;

public class Node {
    public double x;
    public double y;
    public double splineHeading;
    public double robotHeading;
    public int index = -1;
    public int state = 1;
    public String code = "";

    private Type type = Type.splineTo;

    public enum Type {
        splineTo,
        displacementMarker,
        splineToSplineHeading,
        splineToLinearHeading,
        splineToConstantHeading
    }
    Node(){

    }

    Node(java.awt.Point p){
        this.x = p.x;
        this.y = p.y;

    }
    Node(double x, double y){
        this.x = x;
        this.y = y;
    }

    Node(int index){
        this.index = index;
    }

    Node(double x, double y, double splineHeading, int index){
        this.x = x;
        this.y = y;
        this.splineHeading = splineHeading;
        this.index = index;
    }

    public double distance(Node pt) {
        double px = pt.x - this.x;
        double py = pt.y - this.y;
        return Math.sqrt(px * px + py * py);
    }

    public void setType(Type t){
        this.type = t;
    }
    public Type getType(){
        return this.type;
    }
    public Node setLocation(Node p){
        this.x = p.x;
        this.y = p.y;
        return this;
    }

    public Node copy(){
        Node node = new Node(this.x, this.y, this.splineHeading, this.index);
        node.code = this.code;
        node.state = this.state;
        node.type = this.type;
        return node;
    }

    public double headingTo(Node n){
        return (Math.toDegrees(Math.atan2(this.x - n.x, this.y - n.y)));
    }



}
