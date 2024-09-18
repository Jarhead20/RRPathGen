package rrpathgen.data;

public class Node {
    public double x;
    public double y;
    public double splineHeading;
    public double robotHeading;
    public int index = -1;
    public boolean isMidpoint = false;
    public double distanceToMouse = 99999;

    public enum State {
        DELETE,
        ADD,
        DRAG,
        FLIP
    }
    public State state;
    public boolean reversed = false;

    private Type type = Type.splineTo;

    public enum Type {
        splineTo,
        splineToSplineHeading,
        splineToLinearHeading,
        splineToConstantHeading,
        lineTo,
        lineToSplineHeading,
        lineToLinearHeading,
        lineToConstantHeading,
        addTemporalMarker,
    }
    public Node(){

    }

    public Node(java.awt.Point p){
        this.x = p.x;
        this.y = p.y;

    }
    public Node(double x, double y){
        this.x = x;
        this.y = y;
    }

    public Node(int index){
        this.index = index;
    }

    public Node(double x, double y, double splineHeading, int index){
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
    public void setLocation(Node p){
        this.x = p.x;
        this.y = p.y;
    }

    public Node copy(){
        Node node = new Node(this.x, this.y, this.splineHeading, this.index);
        node.state = this.state;
        node.type = this.type;
        node.robotHeading = this.robotHeading;
        node.reversed = this.reversed;
        return node;
    }

    public Node shrink(double scale){
        Node node = copy();
        node.x /= scale;
        node.y /= scale;
        return node;
    }

    public double headingTo(Node n){
        return (Math.toDegrees(Math.atan2(this.x - n.x, this.y - n.y)));
    }

    @Override
    public String toString(){

        return "{x: " + x + " y: " + y + " robotHeading: " + robotHeading + " spline heading: " + splineHeading + " type: " + type + "}";
    }
}
