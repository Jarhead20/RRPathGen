import java.awt.geom.Line2D;
import java.util.ArrayList;

public class Node {
    public double x;
    public double y;
    public double heading;
    public int index = -1;

    private Type type = Type.SPLINE;

    public enum Type {
        SPLINE,
        MARKER

    }
    Node(){

    }

    Node(java.awt.Point p){
        this.x = (1.0/Main.getSCALE()*p.x)-72;
        this.y = (1.0/Main.getSCALE()*p.y)-72;

    }
    Node(double x, double y){
        this.x = x;
        this.y = y;
    }

    Node(int index){
        this.index = index;
    }

    Node(java.awt.Point p, int index){
        this.x = (1.0/Main.getSCALE()*p.x)-72;
        this.y = (1.0/Main.getSCALE()*p.y)-72;
        this.index = index;
    }
    Node(double x, double y, int index){
        this.x = x;
        this.y = y;
        this.index = index;
    }

    Node(double x, double y,double heading, int index){
        this.x = x;
        this.y = y;
        this.heading = heading;
        this.index = index;
    }

    Node(java.awt.Point p, Type t){
        this.x = p.x;
        this.y = p.y;
        this.type = t;
    }
    Node(double x, double y, Type t){
        this.x = x;
        this.y = y;
        this.type = t;
    }
    public double distance(Node pt) {
        double px = pt.x - this.x;
        double py = pt.y - this.y;
        return Math.sqrt(px * px + py * py);
    }

    public Node mid(Node pt){
        return new Node((this.x+pt.x)/2,(this.y + pt.y)/2);
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

    public double headingTo(Node n){
        return (Math.toDegrees(Math.atan2(this.x - n.x, this.y - n.y)));
    }



}
